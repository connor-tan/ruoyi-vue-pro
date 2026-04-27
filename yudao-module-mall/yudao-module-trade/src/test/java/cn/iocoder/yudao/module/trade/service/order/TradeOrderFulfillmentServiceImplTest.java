package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderStationDeliveryReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import cn.iocoder.yudao.module.trade.service.message.TradeMessageService;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderDeliveryAccessSupport;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderStatusAggregateSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeOrderFulfillmentServiceImplTest {

    @Mock
    private TradeOrderMapper tradeOrderMapper;
    @Mock
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Mock
    private DeliveryExpressService deliveryExpressService;
    @Mock
    private TradeMessageService tradeMessageService;
    @Mock
    private SocialClientApi socialClientApi;
    @Mock
    private TradeOrderDeliveryAccessSupport deliveryAccessSupport;
    @Mock
    private TradeOrderStatusAggregateSupport statusAggregateSupport;
    @Mock
    private TradeOrderFulfillmentService self;
    @InjectMocks
    private TradeOrderFulfillmentServiceImpl tradeOrderFulfillmentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tradeOrderFulfillmentService, "tradeOrderHandlers", Collections.emptyList());
        ReflectionTestUtils.setField(tradeOrderFulfillmentService, "self", self);
    }

    @Test
    void deliveryOrder_shouldDeliveryExpressGroupAndRefreshOrderStatus() {
        TradeOrderDeliveryDO delivery = delivery(DeliveryTypeEnum.EXPRESS.getType());
        TradeOrderDO order = order();
        TradeOrderDeliveryReqVO reqVO = new TradeOrderDeliveryReqVO().setDeliveryId(delivery.getId())
                .setLogisticsId(100L).setLogisticsNo("SF100");
        when(deliveryAccessSupport.validateDeliveryExists(delivery.getId())).thenReturn(delivery);
        when(tradeOrderMapper.selectById(order.getId())).thenReturn(order);
        when(deliveryExpressService.validateDeliveryExpress(100L)).thenReturn(new DeliveryExpressDO().setName("顺丰"));
        when(tradeOrderDeliveryMapper.updateByIdAndStatus(eq(delivery.getId()), eq(delivery.getStatus()), any()))
                .thenReturn(1);
        when(statusAggregateSupport.refreshOrderStatusByDeliveries(order))
                .thenReturn(new TradeOrderDO().setId(order.getId()).setUserId(order.getUserId())
                        .setStatus(TradeOrderStatusEnum.DELIVERED.getStatus()));

        tradeOrderFulfillmentService.deliveryOrder(reqVO);

        ArgumentCaptor<TradeOrderDeliveryDO> captor = ArgumentCaptor.forClass(TradeOrderDeliveryDO.class);
        verify(tradeOrderDeliveryMapper).updateByIdAndStatus(eq(delivery.getId()),
                eq(TradeOrderStatusEnum.UNDELIVERED.getStatus()), captor.capture());
        assertEquals(TradeOrderStatusEnum.DELIVERED.getStatus(), captor.getValue().getStatus());
        verify(statusAggregateSupport).refreshOrderStatusByDeliveries(order);
        verify(self).sendDeliveryOrderMessage(any(), eq(reqVO));
    }

    @Test
    void stationDeliveryOrder_shouldDeliveryStationGroup() {
        TradeOrderDeliveryDO delivery = delivery(DeliveryTypeEnum.STATION.getType());
        TradeOrderDO order = order();
        TradeOrderStationDeliveryReqVO reqVO = new TradeOrderStationDeliveryReqVO().setDeliveryId(delivery.getId());
        when(deliveryAccessSupport.validateDeliveryExists(delivery.getId())).thenReturn(delivery);
        when(tradeOrderMapper.selectById(order.getId())).thenReturn(order);
        when(tradeOrderDeliveryMapper.updateByIdAndStatus(eq(delivery.getId()), eq(delivery.getStatus()), any()))
                .thenReturn(1);

        tradeOrderFulfillmentService.stationDeliveryOrder(reqVO);

        verify(statusAggregateSupport).refreshOrderStatusByDeliveries(order);
    }

    @Test
    void stationDeliveryOrder_shouldRejectWrongDeliveryType() {
        TradeOrderDeliveryDO delivery = delivery(DeliveryTypeEnum.EXPRESS.getType());
        when(deliveryAccessSupport.validateDeliveryExists(delivery.getId())).thenReturn(delivery);

        assertThrows(ServiceException.class,
                () -> tradeOrderFulfillmentService.stationDeliveryOrder(
                        new TradeOrderStationDeliveryReqVO().setDeliveryId(delivery.getId())));
    }

    private TradeOrderDO order() {
        return new TradeOrderDO().setId(1L).setUserId(2L).setStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus())
                .setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus());
    }

    private TradeOrderDeliveryDO delivery(Integer deliveryType) {
        return new TradeOrderDeliveryDO().setId(11L).setOrderId(1L).setDeliveryType(deliveryType)
                .setStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus());
    }

}
