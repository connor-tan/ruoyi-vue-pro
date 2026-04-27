package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeOrderLifecycleServiceImplTest {

    @Mock
    private TradeOrderMapper tradeOrderMapper;
    @Mock
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Mock
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Mock
    private PayOrderApi payOrderApi;
    @Mock
    private TradeOrderProperties tradeOrderProperties;
    @Mock
    private TradeOrderHandler tradeOrderHandler;
    @Mock
    private TradeOrderLifecycleService self;
    @InjectMocks
    private TradeOrderLifecycleServiceImpl tradeOrderLifecycleService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tradeOrderLifecycleService, "tradeOrderHandlers", List.of(tradeOrderHandler));
        ReflectionTestUtils.setField(tradeOrderLifecycleService, "self", self);
    }

    @Test
    void cancelOrderByMember_shouldCancelOrderAndUnpaidDeliveries() {
        TradeOrderDO order = order(TradeOrderStatusEnum.UNPAID.getStatus());
        TradeOrderDeliveryDO delivery = delivery(TradeOrderStatusEnum.UNPAID.getStatus());
        when(tradeOrderMapper.selectOrderByIdAndUserId(order.getId(), order.getUserId())).thenReturn(order);
        when(payOrderApi.getOrder(order.getPayOrderId())).thenReturn(null);
        when(tradeOrderMapper.updateByIdAndStatus(eq(order.getId()), eq(order.getStatus()), any())).thenReturn(1);
        when(tradeOrderDeliveryMapper.selectListByOrderId(order.getId())).thenReturn(List.of(delivery));
        when(tradeOrderItemMapper.selectListByOrderId(order.getId())).thenReturn(Collections.emptyList());

        tradeOrderLifecycleService.cancelOrderByMember(order.getUserId(), order.getId());

        ArgumentCaptor<TradeOrderDO> orderCaptor = ArgumentCaptor.forClass(TradeOrderDO.class);
        verify(tradeOrderMapper).updateByIdAndStatus(eq(order.getId()),
                eq(TradeOrderStatusEnum.UNPAID.getStatus()), orderCaptor.capture());
        assertEquals(TradeOrderStatusEnum.CANCELED.getStatus(), orderCaptor.getValue().getStatus());

        ArgumentCaptor<TradeOrderDeliveryDO> deliveryCaptor = ArgumentCaptor.forClass(TradeOrderDeliveryDO.class);
        verify(tradeOrderDeliveryMapper).updateById(deliveryCaptor.capture());
        assertEquals(TradeOrderStatusEnum.CANCELED.getStatus(), deliveryCaptor.getValue().getStatus());
    }

    @Test
    void cancelOrderBySystem_shouldDelegateExpiredOrdersToProxy() {
        TradeOrderDO order = order(TradeOrderStatusEnum.UNPAID.getStatus());
        when(tradeOrderProperties.getPayExpireTime()).thenReturn(Duration.ofMinutes(30));
        when(tradeOrderMapper.selectListByStatusAndCreateTimeLt(eq(TradeOrderStatusEnum.UNPAID.getStatus()), any()))
                .thenReturn(List.of(order));

        int count = tradeOrderLifecycleService.cancelOrderBySystem();

        assertEquals(1, count);
        verify(self).cancelOrderBySystem(order);
    }

    @Test
    void cancelOrderByMember_shouldRejectWhenPayOrderAlreadySuccess() {
        TradeOrderDO order = order(TradeOrderStatusEnum.UNPAID.getStatus());
        when(tradeOrderMapper.selectOrderByIdAndUserId(order.getId(), order.getUserId())).thenReturn(order);
        when(payOrderApi.getOrder(order.getPayOrderId())).thenReturn(payOrderSuccess());

        assertThrows(ServiceException.class,
                () -> tradeOrderLifecycleService.cancelOrderByMember(order.getUserId(), order.getId()));

        verify(tradeOrderMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    private TradeOrderDO order(Integer status) {
        return new TradeOrderDO().setId(1L).setUserId(2L).setPayOrderId(100L).setStatus(status);
    }

    private TradeOrderDeliveryDO delivery(Integer status) {
        return new TradeOrderDeliveryDO().setId(11L).setOrderId(1L).setStatus(status);
    }

    private PayOrderRespDTO payOrderSuccess() {
        PayOrderRespDTO payOrder = new PayOrderRespDTO();
        payOrder.setId(100L);
        payOrder.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
        return payOrder;
    }

}
