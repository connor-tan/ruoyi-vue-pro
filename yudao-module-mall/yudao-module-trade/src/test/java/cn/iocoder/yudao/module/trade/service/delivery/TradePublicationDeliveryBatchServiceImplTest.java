package cn.iocoder.yudao.module.trade.service.delivery;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.TradePublicationDeliveryBatchDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.TradePublicationDeliveryBatchItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.TradePublicationDeliveryBatchMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.redis.no.TradeNoRedisDAO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.service.delivery.bo.TradePublicationDeliveryCandidateItemBO;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderStatusAggregateSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_DUPLICATE_ORDER_ITEM;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradePublicationDeliveryBatchServiceImplTest {

    @Mock
    private TradeOrderMapper tradeOrderMapper;
    @Mock
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Mock
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Mock
    private TradePublicationDeliveryBatchMapper publicationDeliveryBatchMapper;
    @Mock
    private TradePublicationDeliveryBatchItemMapper publicationDeliveryBatchItemMapper;
    @Mock
    private TradeOrderStatusAggregateSupport statusAggregateSupport;
    @Mock
    private TradeNoRedisDAO tradeNoRedisDAO;
    @InjectMocks
    private TradePublicationDeliveryBatchServiceImpl service;

    @Test
    void createAndDeliver_shouldCreateBatchButKeepDeliveryUndeliveredWhenGroupHasRemainingItems() {
        mockCandidateItems(List.of(candidateItem(1001L, 11L, 1L), candidateItem(1002L, 11L, 1L)));
        mockBatchInsert();
        when(tradeNoRedisDAO.generate(TradeNoRedisDAO.PUBLICATION_DELIVERY_BATCH_NO_PREFIX)).thenReturn("pd100");
        when(tradeOrderItemMapper.updatePublicationDeliveryByIds(any(), anyInt(), anyInt(), eq(200L), any()))
                .thenReturn(2);
        when(tradeOrderDeliveryMapper.selectByIds(any())).thenReturn(List.of(delivery(11L, 1L)));
        when(tradeOrderMapper.selectByIds(any())).thenReturn(List.of(order(1L)));
        when(tradeOrderItemMapper.selectUndeliveredCountByDeliveryId(11L, 20)).thenReturn(1L);

        Long batchId = service.createAndDeliver(createReqVO(), 9L);

        assertEquals(200L, batchId);
        verify(tradeOrderDeliveryMapper, never()).updateByIdAndStatus(any(), any(), any());
        verify(statusAggregateSupport, never()).refreshOrderStatusByDeliveries(any());
    }

    @Test
    void createAndDeliver_shouldDeliveryGroupWhenAllItemsDelivered() {
        mockCandidateItems(List.of(candidateItem(1001L, 11L, 1L), candidateItem(1002L, 11L, 1L)));
        mockBatchInsert();
        when(tradeNoRedisDAO.generate(TradeNoRedisDAO.PUBLICATION_DELIVERY_BATCH_NO_PREFIX)).thenReturn("pd100");
        when(tradeOrderItemMapper.updatePublicationDeliveryByIds(any(), anyInt(), anyInt(), eq(200L), any()))
                .thenReturn(2);
        when(tradeOrderDeliveryMapper.selectByIds(any())).thenReturn(List.of(delivery(11L, 1L)));
        TradeOrderDO order = order(1L);
        when(tradeOrderMapper.selectByIds(any())).thenReturn(List.of(order));
        when(tradeOrderItemMapper.selectUndeliveredCountByDeliveryId(11L, 20)).thenReturn(0L);
        when(tradeOrderDeliveryMapper.updateByIdAndStatus(eq(11L),
                eq(TradeOrderStatusEnum.UNDELIVERED.getStatus()), any())).thenReturn(1);

        Long batchId = service.createAndDeliver(createReqVO(), 9L);

        assertEquals(200L, batchId);
        ArgumentCaptor<TradeOrderDeliveryDO> captor = ArgumentCaptor.forClass(TradeOrderDeliveryDO.class);
        verify(tradeOrderDeliveryMapper).updateByIdAndStatus(eq(11L),
                eq(TradeOrderStatusEnum.UNDELIVERED.getStatus()), captor.capture());
        assertEquals(TradeOrderStatusEnum.DELIVERED.getStatus(), captor.getValue().getStatus());
        verify(statusAggregateSupport).refreshOrderStatusByDeliveries(order);
    }

    @Test
    void createAndDeliver_shouldRejectWhenNoCandidateItems() {
        mockCandidateItems(List.of());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.createAndDeliver(createReqVO(), 9L));

        assertEquals(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND.getCode(), ex.getCode());
        verify(publicationDeliveryBatchMapper, never()).insert(any(TradePublicationDeliveryBatchDO.class));
    }

    @Test
    void createAndDeliver_shouldRejectDuplicateOrderItem() {
        mockCandidateItems(List.of(candidateItem(1001L, 11L, 1L)));
        mockBatchInsert();
        when(tradeNoRedisDAO.generate(TradeNoRedisDAO.PUBLICATION_DELIVERY_BATCH_NO_PREFIX)).thenReturn("pd100");
        doThrow(new DuplicateKeyException("duplicate")).when(publicationDeliveryBatchItemMapper)
                .insertBatch(any());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.createAndDeliver(createReqVO(), 9L));

        assertEquals(PUBLICATION_DELIVERY_DUPLICATE_ORDER_ITEM.getCode(), ex.getCode());
        verify(tradeOrderItemMapper, never()).updatePublicationDeliveryByIds(any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    void createAndDeliver_shouldRejectWhenOrderItemUpdateCountMismatch() {
        mockCandidateItems(List.of(candidateItem(1001L, 11L, 1L), candidateItem(1002L, 11L, 1L)));
        mockBatchInsert();
        when(tradeNoRedisDAO.generate(TradeNoRedisDAO.PUBLICATION_DELIVERY_BATCH_NO_PREFIX)).thenReturn("pd100");
        when(tradeOrderItemMapper.updatePublicationDeliveryByIds(any(), anyInt(), anyInt(), eq(200L), any()))
                .thenReturn(1);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.createAndDeliver(createReqVO(), 9L));

        assertEquals(PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL.getCode(), ex.getCode());
        verify(tradeOrderDeliveryMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    private void mockCandidateItems(List<TradePublicationDeliveryCandidateItemBO> items) {
        when(tradeOrderItemMapper.selectPublicationDeliveryCandidateItemList(any(), anyString(), anyInt(), anyInt(),
                anyInt(), anyInt(), anyInt())).thenReturn(items);
    }

    private void mockBatchInsert() {
        doAnswer(invocation -> {
            TradePublicationDeliveryBatchDO batch = invocation.getArgument(0);
            batch.setId(200L);
            return 1;
        }).when(publicationDeliveryBatchMapper).insert(any(TradePublicationDeliveryBatchDO.class));
    }

    private TradePublicationDeliveryBatchCreateReqVO createReqVO() {
        return new TradePublicationDeliveryBatchCreateReqVO()
                .setSchoolId(1L)
                .setStationId(2L)
                .setWindowId(3L)
                .setOfferId(4L)
                .setOfferSkuId(5L)
                .setSkuId(6L);
    }

    private TradePublicationDeliveryCandidateItemBO candidateItem(Long orderItemId, Long deliveryId, Long orderId) {
        return new TradePublicationDeliveryCandidateItemBO()
                .setOrderId(orderId)
                .setOrderNo("NO" + orderId)
                .setOrderItemId(orderItemId)
                .setDeliveryId(deliveryId)
                .setUserId(8L)
                .setCount(1)
                .setSchoolId(1L)
                .setSchoolNameSnapshot("实验小学")
                .setStationId(2L)
                .setStationNameSnapshot("城北站")
                .setWindowId(3L)
                .setWindowNameSnapshot("2026 春季订刊")
                .setOfferId(4L)
                .setOfferSkuId(5L)
                .setSkuId(6L)
                .setProductNameSnapshot("测试刊物")
                .setTargetPeriod("SPRING")
                .setStudentId(orderItemId)
                .setStudentNameSnapshot("学生" + orderItemId)
                .setClassId(7L)
                .setClassNameSnapshot("一年级1班");
    }

    private TradeOrderDeliveryDO delivery(Long deliveryId, Long orderId) {
        return new TradeOrderDeliveryDO()
                .setId(deliveryId)
                .setOrderId(orderId)
                .setStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus());
    }

    private TradeOrderDO order(Long orderId) {
        return new TradeOrderDO()
                .setId(orderId)
                .setStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus());
    }

}
