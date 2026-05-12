package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderPublicationIssueDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderPublicationIssueMapper;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryStatusEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationFulfillmentStatusEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationReceiveStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderStatusAggregateSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeOrderPublicationIssueServiceImplTest {

    @Mock
    private TradeOrderPublicationIssueMapper publicationIssueMapper;
    @Mock
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Mock
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Mock
    private TradeOrderMapper tradeOrderMapper;
    @Mock
    private TradeOrderStatusAggregateSupport statusAggregateSupport;
    @Mock
    private TradeOrderProperties tradeOrderProperties;
    @InjectMocks
    private TradeOrderPublicationIssueServiceImpl publicationIssueService;

    @Test
    void cancelUnfinishedByOrderItemId_shouldCompleteUndeliveredDeliveryWhenCanceledIssuesAreOnlyBlocker() {
        Long orderItemId = 1001L;
        TradeOrderPublicationIssueDO beforeIssue = issue(false,
                PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(),
                PublicationReceiveStatusEnum.UNRECEIVED.getStatus());
        TradeOrderPublicationIssueDO afterIssue = issue(true,
                PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(),
                PublicationReceiveStatusEnum.UNRECEIVED.getStatus());
        mockCancelContext(orderItemId, beforeIssue, afterIssue, TradeOrderStatusEnum.UNDELIVERED.getStatus());
        when(publicationIssueMapper.selectNotDeliveredCountByDeliveryId(beforeIssue.getDeliveryId(),
                PublicationDeliveryStatusEnum.DELIVERED.getStatus())).thenReturn(0L);
        when(tradeOrderDeliveryMapper.updateByIdAndStatus(eq(beforeIssue.getDeliveryId()),
                eq(TradeOrderStatusEnum.UNDELIVERED.getStatus()), any(TradeOrderDeliveryDO.class))).thenReturn(1);
        when(publicationIssueMapper.selectNotReceivedCountByDeliveryId(beforeIssue.getDeliveryId(),
                PublicationReceiveStatusEnum.RECEIVED.getStatus())).thenReturn(0L);
        when(tradeOrderDeliveryMapper.updateByIdAndStatus(eq(beforeIssue.getDeliveryId()),
                eq(TradeOrderStatusEnum.DELIVERED.getStatus()), any(TradeOrderDeliveryDO.class))).thenReturn(1);

        publicationIssueService.cancelUnfinishedByOrderItemId(orderItemId);

        assertCanceledStatsUpdated(orderItemId);
        ArgumentCaptor<TradeOrderDeliveryDO> deliveryCaptor = ArgumentCaptor.forClass(TradeOrderDeliveryDO.class);
        verify(tradeOrderDeliveryMapper).updateByIdAndStatus(eq(beforeIssue.getDeliveryId()),
                eq(TradeOrderStatusEnum.UNDELIVERED.getStatus()), deliveryCaptor.capture());
        assertEquals(TradeOrderStatusEnum.DELIVERED.getStatus(), deliveryCaptor.getValue().getStatus());
        assertNotNull(deliveryCaptor.getValue().getDeliveryTime());
        verify(tradeOrderDeliveryMapper).updateByIdAndStatus(eq(beforeIssue.getDeliveryId()),
                eq(TradeOrderStatusEnum.DELIVERED.getStatus()), deliveryCaptor.capture());
        assertEquals(TradeOrderStatusEnum.COMPLETED.getStatus(), deliveryCaptor.getValue().getStatus());
        assertNotNull(deliveryCaptor.getValue().getReceiveTime());
        verify(statusAggregateSupport).refreshOrderStatusByDeliveries(order());
    }

    @Test
    void cancelUnfinishedByOrderItemId_shouldCompleteDeliveredDeliveryWhenCanceledIssuesAreOnlyReceiveBlocker() {
        Long orderItemId = 1001L;
        TradeOrderPublicationIssueDO beforeIssue = issue(false,
                PublicationDeliveryStatusEnum.DELIVERED.getStatus(),
                PublicationReceiveStatusEnum.UNRECEIVED.getStatus());
        TradeOrderPublicationIssueDO afterIssue = issue(true,
                PublicationDeliveryStatusEnum.DELIVERED.getStatus(),
                PublicationReceiveStatusEnum.UNRECEIVED.getStatus());
        mockCancelContext(orderItemId, beforeIssue, afterIssue, TradeOrderStatusEnum.DELIVERED.getStatus());
        when(publicationIssueMapper.selectNotReceivedCountByDeliveryId(beforeIssue.getDeliveryId(),
                PublicationReceiveStatusEnum.RECEIVED.getStatus())).thenReturn(0L);
        when(tradeOrderDeliveryMapper.updateByIdAndStatus(eq(beforeIssue.getDeliveryId()),
                eq(TradeOrderStatusEnum.DELIVERED.getStatus()), any(TradeOrderDeliveryDO.class))).thenReturn(1);

        publicationIssueService.cancelUnfinishedByOrderItemId(orderItemId);

        assertCanceledStatsUpdated(orderItemId);
        verify(publicationIssueMapper, never()).selectNotDeliveredCountByDeliveryId(any(), any());
        verify(tradeOrderDeliveryMapper, never()).updateByIdAndStatus(eq(beforeIssue.getDeliveryId()),
                eq(TradeOrderStatusEnum.UNDELIVERED.getStatus()), any(TradeOrderDeliveryDO.class));
        ArgumentCaptor<TradeOrderDeliveryDO> deliveryCaptor = ArgumentCaptor.forClass(TradeOrderDeliveryDO.class);
        verify(tradeOrderDeliveryMapper).updateByIdAndStatus(eq(beforeIssue.getDeliveryId()),
                eq(TradeOrderStatusEnum.DELIVERED.getStatus()), deliveryCaptor.capture());
        assertEquals(TradeOrderStatusEnum.COMPLETED.getStatus(), deliveryCaptor.getValue().getStatus());
        assertNotNull(deliveryCaptor.getValue().getReceiveTime());
        verify(statusAggregateSupport).refreshOrderStatusByDeliveries(order());
    }

    private void mockCancelContext(Long orderItemId, TradeOrderPublicationIssueDO beforeIssue,
                                   TradeOrderPublicationIssueDO afterIssue, Integer deliveryStatus) {
        when(publicationIssueMapper.selectListByOrderItemIds(List.of(orderItemId)))
                .thenReturn(List.of(beforeIssue), List.of(afterIssue));
        when(tradeOrderDeliveryMapper.selectByIds(anyCollection()))
                .thenReturn(List.of(delivery(deliveryStatus)));
        when(tradeOrderMapper.selectByIds(anyCollection())).thenReturn(List.of(order()));
    }

    private void assertCanceledStatsUpdated(Long orderItemId) {
        ArgumentCaptor<TradeOrderItemDO> itemCaptor = ArgumentCaptor.forClass(TradeOrderItemDO.class);
        verify(tradeOrderItemMapper).updateById(itemCaptor.capture());
        TradeOrderItemDO update = itemCaptor.getValue();
        assertEquals(orderItemId, update.getId());
        assertEquals(1, update.getPublicationIssueTotalCount());
        assertEquals(0, update.getPublicationIssueDeliveredCount());
        assertEquals(0, update.getPublicationIssueReceivedCount());
        assertEquals(PublicationFulfillmentStatusEnum.CANCELED.getStatus(), update.getPublicationFulfillmentStatus());
        assertEquals(PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(), update.getPublicationDeliveryStatus());
    }

    private TradeOrderPublicationIssueDO issue(Boolean canceled, Integer deliveryStatus, Integer receiveStatus) {
        return new TradeOrderPublicationIssueDO()
                .setId(9001L)
                .setOrderId(1L)
                .setOrderItemId(1001L)
                .setDeliveryId(11L)
                .setDeliveryStatus(deliveryStatus)
                .setReceiveStatus(receiveStatus)
                .setCanceled(canceled);
    }

    private TradeOrderDeliveryDO delivery(Integer status) {
        return new TradeOrderDeliveryDO()
                .setId(11L)
                .setOrderId(1L)
                .setStatus(status);
    }

    private TradeOrderDO order() {
        return new TradeOrderDO().setId(1L);
    }

}
