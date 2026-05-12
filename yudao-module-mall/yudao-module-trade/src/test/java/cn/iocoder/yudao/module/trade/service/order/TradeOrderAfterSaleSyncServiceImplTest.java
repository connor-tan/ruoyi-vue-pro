package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeOrderAfterSaleSyncServiceImplTest {

    @Mock
    private TradeOrderMapper tradeOrderMapper;
    @Mock
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Mock
    private TradeOrderLifecycleService tradeOrderLifecycleService;
    @Mock
    private TradeOrderPublicationIssueService publicationIssueService;
    @Mock
    private TradeOrderHandler tradeOrderHandler;
    @InjectMocks
    private TradeOrderAfterSaleSyncServiceImpl afterSaleSyncService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(afterSaleSyncService, "tradeOrderHandlers", List.of(tradeOrderHandler));
    }

    @Test
    void updateOrderItemWhenAfterSaleSuccess_shouldSkipIssueCancelForPublicationPartialRefund() {
        TradeOrderItemDO orderItem = publicationOrderItem(1000);
        mockSuccessContext(orderItem);

        afterSaleSyncService.updateOrderItemWhenAfterSaleSuccess(orderItem.getId(), 500);

        verify(publicationIssueService, never()).cancelUnfinishedByOrderItemId(any());
        verify(tradeOrderHandler).afterCancelOrderItem(any(), eq(orderItem));
        verify(tradeOrderLifecycleService).cancelOrderByAfterSale(any(), eq(500));
        ArgumentCaptor<TradeOrderDO> captor = ArgumentCaptor.forClass(TradeOrderDO.class);
        verify(tradeOrderMapper).updateById(captor.capture());
        assertEquals(500, captor.getValue().getRefundPrice());
        assertEquals(TradeOrderRefundStatusEnum.ALL.getStatus(), captor.getValue().getRefundStatus());
    }

    @Test
    void updateOrderItemWhenAfterSaleSuccess_shouldCancelIssueForPublicationFullRefund() {
        TradeOrderItemDO orderItem = publicationOrderItem(1000);
        mockSuccessContext(orderItem);

        afterSaleSyncService.updateOrderItemWhenAfterSaleSuccess(orderItem.getId(), 1000);

        verify(publicationIssueService).cancelUnfinishedByOrderItemId(orderItem.getId());
        verify(tradeOrderLifecycleService).cancelOrderByAfterSale(any(), eq(1000));
    }

    private void mockSuccessContext(TradeOrderItemDO orderItem) {
        when(tradeOrderItemMapper.updateAfterSaleStatus(orderItem.getId(),
                TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus(),
                TradeOrderItemAfterSaleStatusEnum.SUCCESS.getStatus(), null)).thenReturn(1);
        when(tradeOrderItemMapper.selectById(orderItem.getId())).thenReturn(orderItem);
        when(tradeOrderMapper.selectById(orderItem.getOrderId())).thenReturn(order());
        when(tradeOrderItemMapper.selectListByOrderId(orderItem.getOrderId()))
                .thenReturn(List.of(orderItem.setAfterSaleStatus(TradeOrderItemAfterSaleStatusEnum.SUCCESS.getStatus())));
    }

    private TradeOrderItemDO publicationOrderItem(Integer payPrice) {
        return new TradeOrderItemDO()
                .setId(1001L)
                .setOrderId(1L)
                .setSubscriptionOfferSkuId(5L)
                .setPayPrice(payPrice)
                .setUsePoint(0)
                .setAfterSaleStatus(TradeOrderItemAfterSaleStatusEnum.SUCCESS.getStatus());
    }

    private TradeOrderDO order() {
        return new TradeOrderDO()
                .setId(1L)
                .setRefundPrice(0)
                .setRefundPoint(0);
    }

}
