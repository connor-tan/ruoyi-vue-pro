package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.promotion.api.combination.CombinationRecordApi;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryPickUpStoreService;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderDeliveryAccessSupport;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderStatusAggregateSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_RECEIVE_FAIL_DELIVERY_NOT_OWNED;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeOrderReceiveServiceImplTest {

    @Mock
    private TradeOrderMapper tradeOrderMapper;
    @Mock
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Mock
    private DeliveryPickUpStoreService pickUpStoreService;
    @Mock
    private CombinationRecordApi combinationRecordApi;
    @Mock
    private TradeOrderProperties tradeOrderProperties;
    @Mock
    private TradeOrderDeliveryAccessSupport deliveryAccessSupport;
    @Mock
    private TradeOrderStatusAggregateSupport statusAggregateSupport;
    @Mock
    private TradeOrderHandler tradeOrderHandler;
    @Mock
    private TradeOrderReceiveService self;
    @InjectMocks
    private TradeOrderReceiveServiceImpl tradeOrderReceiveService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tradeOrderReceiveService, "tradeOrderHandlers", List.of(tradeOrderHandler));
        ReflectionTestUtils.setField(tradeOrderReceiveService, "self", self);
    }

    @Test
    void receiveDeliveryByMember_shouldKeepOrderDeliveredWhenOtherDeliveryPending() {
        TradeOrderDO order = order(TradeOrderStatusEnum.DELIVERED.getStatus());
        TradeOrderDeliveryDO delivery = delivery(TradeOrderStatusEnum.DELIVERED.getStatus());
        TradeOrderDO refreshedOrder = order(TradeOrderStatusEnum.DELIVERED.getStatus());
        when(deliveryAccessSupport.validateDeliveryExists(delivery.getId())).thenReturn(delivery);
        when(deliveryAccessSupport.validateDeliveryOrderOwned(delivery, order.getUserId(),
                ORDER_RECEIVE_FAIL_DELIVERY_NOT_OWNED)).thenReturn(order);
        when(tradeOrderDeliveryMapper.updateByIdAndStatus(eq(delivery.getId()), eq(delivery.getStatus()), any()))
                .thenReturn(1);
        when(statusAggregateSupport.refreshOrderStatusByDeliveries(order)).thenReturn(refreshedOrder);

        tradeOrderReceiveService.receiveDeliveryByMember(order.getUserId(), delivery.getId());

        verify(tradeOrderDeliveryMapper).updateByIdAndStatus(eq(delivery.getId()),
                eq(TradeOrderStatusEnum.DELIVERED.getStatus()), any());
        verify(tradeOrderHandler, never()).afterReceiveOrder(any());
    }

    @Test
    void receiveDeliveryByMember_shouldCompleteOrderWhenAllDeliveriesCompleted() {
        TradeOrderDO order = order(TradeOrderStatusEnum.DELIVERED.getStatus());
        TradeOrderDeliveryDO delivery = delivery(TradeOrderStatusEnum.DELIVERED.getStatus());
        TradeOrderDO refreshedOrder = order(TradeOrderStatusEnum.COMPLETED.getStatus());
        when(deliveryAccessSupport.validateDeliveryExists(delivery.getId())).thenReturn(delivery);
        when(deliveryAccessSupport.validateDeliveryOrderOwned(delivery, order.getUserId(),
                ORDER_RECEIVE_FAIL_DELIVERY_NOT_OWNED)).thenReturn(order);
        when(tradeOrderDeliveryMapper.updateByIdAndStatus(eq(delivery.getId()), eq(delivery.getStatus()), any()))
                .thenReturn(1);
        when(statusAggregateSupport.refreshOrderStatusByDeliveries(order)).thenReturn(refreshedOrder);

        tradeOrderReceiveService.receiveDeliveryByMember(order.getUserId(), delivery.getId());

        verify(tradeOrderHandler).afterReceiveOrder(refreshedOrder);
    }

    @Test
    void receiveOrderByMember_shouldRejectSplitDeliveryOrder() {
        TradeOrderDO order = order(TradeOrderStatusEnum.DELIVERED.getStatus());
        when(tradeOrderMapper.selectByIdAndUserId(order.getId(), order.getUserId())).thenReturn(order);
        when(deliveryAccessSupport.getDeliveryListByOrderId(order.getId())).thenReturn(List.of(delivery(
                TradeOrderStatusEnum.DELIVERED.getStatus())));

        assertThrows(ServiceException.class,
                () -> tradeOrderReceiveService.receiveOrderByMember(order.getUserId(), order.getId()));
    }

    @Test
    void pickUpOrder_shouldCompletePickUpDeliveryAndKeepMixedOrderUndelivered() {
        Long verifyUserId = 9L;
        TradeOrderDO order = order(TradeOrderStatusEnum.UNDELIVERED.getStatus())
                .setType(TradeOrderTypeEnum.NORMAL.getType());
        TradeOrderDeliveryDO pickUpDelivery = delivery(TradeOrderStatusEnum.UNDELIVERED.getStatus())
                .setDeliveryType(DeliveryTypeEnum.PICK_UP.getType())
                .setPickUpStoreId(100L)
                .setPickUpVerifyCode("12345678");
        TradeOrderDO refreshedOrder = order(TradeOrderStatusEnum.UNDELIVERED.getStatus());
        DeliveryPickUpStoreDO pickUpStore = new DeliveryPickUpStoreDO().setId(100L).setVerifyUserIds(List.of(verifyUserId));
        when(pickUpStoreService.getDeliveryPickUpStore(pickUpDelivery.getPickUpStoreId())).thenReturn(pickUpStore);
        when(tradeOrderDeliveryMapper.updateByIdAndStatus(eq(pickUpDelivery.getId()), eq(pickUpDelivery.getStatus()), any()))
                .thenReturn(1);
        when(statusAggregateSupport.refreshOrderStatusByDeliveries(order)).thenReturn(refreshedOrder);

        tradeOrderReceiveService.pickUpOrder(verifyUserId, order, pickUpDelivery);

        verify(tradeOrderDeliveryMapper).updateByIdAndStatus(eq(pickUpDelivery.getId()),
                eq(TradeOrderStatusEnum.UNDELIVERED.getStatus()), any());
        verify(statusAggregateSupport).refreshOrderStatusByDeliveries(order);
        verify(tradeOrderHandler, never()).afterReceiveOrder(any());
    }

    private TradeOrderDO order(Integer status) {
        return new TradeOrderDO().setId(1L).setUserId(2L).setStatus(status);
    }

    private TradeOrderDeliveryDO delivery(Integer status) {
        return new TradeOrderDeliveryDO().setId(11L).setOrderId(1L).setStatus(status);
    }

}
