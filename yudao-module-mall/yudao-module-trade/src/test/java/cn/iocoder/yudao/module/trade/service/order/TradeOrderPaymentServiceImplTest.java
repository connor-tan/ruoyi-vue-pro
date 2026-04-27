package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
class TradeOrderPaymentServiceImplTest {

    @Mock
    private TradeOrderMapper tradeOrderMapper;
    @Mock
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Mock
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Mock
    private PayOrderApi payOrderApi;
    @Mock
    private PayRefundApi payRefundApi;
    @Mock
    private TradeOrderProperties tradeOrderProperties;
    @Mock
    private TradeOrderLifecycleService tradeOrderLifecycleService;
    @InjectMocks
    private TradeOrderPaymentServiceImpl tradeOrderPaymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tradeOrderPaymentService, "tradeOrderHandlers", Collections.emptyList());
    }

    @Test
    void updateOrderPaid_shouldUpdateOrderAndUnpaidDeliveries() {
        TradeOrderDO order = order(TradeOrderStatusEnum.UNPAID.getStatus(), false);
        PayOrderRespDTO payOrder = payOrder(order.getId(), order.getPayPrice());
        TradeOrderDeliveryDO delivery = new TradeOrderDeliveryDO().setId(11L)
                .setStatus(TradeOrderStatusEnum.UNPAID.getStatus());
        when(tradeOrderMapper.selectById(order.getId())).thenReturn(order);
        when(payOrderApi.getOrder(order.getPayOrderId())).thenReturn(payOrder);
        when(tradeOrderMapper.updateByIdAndStatus(eq(order.getId()), eq(order.getStatus()), any())).thenReturn(1);
        when(tradeOrderDeliveryMapper.selectListByOrderId(order.getId())).thenReturn(List.of(delivery));
        when(tradeOrderItemMapper.selectListByOrderId(order.getId())).thenReturn(Collections.emptyList());

        tradeOrderPaymentService.updateOrderPaid(order.getId(), order.getPayOrderId());

        ArgumentCaptor<TradeOrderDO> orderCaptor = ArgumentCaptor.forClass(TradeOrderDO.class);
        verify(tradeOrderMapper).updateByIdAndStatus(eq(order.getId()), eq(TradeOrderStatusEnum.UNPAID.getStatus()),
                orderCaptor.capture());
        assertEquals(TradeOrderStatusEnum.UNDELIVERED.getStatus(), orderCaptor.getValue().getStatus());

        ArgumentCaptor<TradeOrderDeliveryDO> deliveryCaptor = ArgumentCaptor.forClass(TradeOrderDeliveryDO.class);
        verify(tradeOrderDeliveryMapper).updateById(deliveryCaptor.capture());
        assertEquals(TradeOrderStatusEnum.UNDELIVERED.getStatus(), deliveryCaptor.getValue().getStatus());
    }

    @Test
    void updateOrderPaid_shouldReturnWhenSamePayOrderAlreadyPaid() {
        TradeOrderDO order = order(TradeOrderStatusEnum.UNDELIVERED.getStatus(), true);
        when(tradeOrderMapper.selectById(order.getId())).thenReturn(order);

        tradeOrderPaymentService.updateOrderPaid(order.getId(), order.getPayOrderId());

        verify(payOrderApi, never()).getOrder(order.getPayOrderId());
        verify(tradeOrderMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    @Test
    void updateOrderPaid_shouldRejectWhenPayPriceMismatch() {
        TradeOrderDO order = order(TradeOrderStatusEnum.UNPAID.getStatus(), false);
        when(tradeOrderMapper.selectById(order.getId())).thenReturn(order);
        when(payOrderApi.getOrder(order.getPayOrderId())).thenReturn(payOrder(order.getId(), order.getPayPrice() + 1));

        assertThrows(ServiceException.class,
                () -> tradeOrderPaymentService.updateOrderPaid(order.getId(), order.getPayOrderId()));
    }

    private TradeOrderDO order(Integer status, Boolean payStatus) {
        return new TradeOrderDO().setId(1L).setUserId(2L).setPayOrderId(100L).setPayPrice(500)
                .setStatus(status).setPayStatus(payStatus);
    }

    private PayOrderRespDTO payOrder(Long orderId, Integer price) {
        PayOrderRespDTO payOrder = new PayOrderRespDTO();
        payOrder.setId(100L);
        payOrder.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
        payOrder.setMerchantOrderId(String.valueOf(orderId));
        payOrder.setPrice(price);
        payOrder.setChannelCode("wx_pub");
        return payOrder;
    }

}
