package cn.iocoder.yudao.module.trade.service.delivery;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidatePageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.TradePublicationDeliveryBatchDO;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.TradePublicationDeliveryBatchItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.TradePublicationDeliveryBatchMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderPublicationIssueMapper;
import cn.iocoder.yudao.module.trade.dal.redis.no.TradeNoRedisDAO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.service.delivery.bo.TradePublicationDeliveryCandidateItemBO;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderPublicationIssueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;

import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_EXPRESS_BATCH_TOO_LARGE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_EXPRESS_LOGISTICS_REQUIRED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_ISSUE_DELIVERY_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradePublicationDeliveryBatchServiceImplTest {

    @Mock
    private TradeOrderPublicationIssueMapper publicationIssueMapper;
    @Mock
    private TradePublicationDeliveryBatchMapper publicationDeliveryBatchMapper;
    @Mock
    private TradePublicationDeliveryBatchItemMapper publicationDeliveryBatchItemMapper;
    @Mock
    private TradeNoRedisDAO tradeNoRedisDAO;
    @Mock
    private DeliveryExpressService deliveryExpressService;
    @Mock
    private TradeOrderPublicationIssueService publicationIssueService;
    @InjectMocks
    private TradePublicationDeliveryBatchServiceImpl service;

    @Test
    void createAndDeliver_shouldCreateWarehouseIssueBatch() {
        mockCandidateItems(List.of(candidateItem(9001L, DeliveryTypeEnum.SCHOOL.getType()),
                candidateItem(9002L, DeliveryTypeEnum.SCHOOL.getType())));
        mockBatchInsert();
        when(tradeNoRedisDAO.generate(TradeNoRedisDAO.PUBLICATION_DELIVERY_BATCH_NO_PREFIX)).thenReturn("pd100");
        when(publicationIssueMapper.updateDeliveredByIds(any(),
                eq(PublicationDeliveryStatusEnum.UNDELIVERED.getStatus()), eq(200L), any(LocalDateTime.class)))
                .thenReturn(2);

        Long batchId = service.createAndDeliver(createReqVO(DeliveryTypeEnum.SCHOOL.getType()), 9L);

        assertEquals(200L, batchId);
        verify(publicationIssueService).afterIssueDelivered(eq(Set.of(9001L, 9002L)), any(LocalDateTime.class));
    }

    @Test
    void createAndDeliver_shouldCreateExpressIssueBatch() {
        mockCandidateItems(List.of(candidateItem(9001L, DeliveryTypeEnum.EXPRESS.getType())));
        mockBatchInsert();
        when(tradeNoRedisDAO.generate(TradeNoRedisDAO.PUBLICATION_DELIVERY_BATCH_NO_PREFIX)).thenReturn("pd100");
        when(publicationIssueMapper.updateDeliveredById(eq(9001L),
                eq(PublicationDeliveryStatusEnum.UNDELIVERED.getStatus()), eq(200L), any(LocalDateTime.class),
                eq(1L), eq("SF100"))).thenReturn(1);

        Long batchId = service.createAndDeliver(createExpressReqVO(), 9L);

        assertEquals(200L, batchId);
        verify(deliveryExpressService).validateDeliveryExpress(1L);
        verify(publicationIssueService).afterIssueDelivered(eq(Set.of(9001L)), any(LocalDateTime.class));
    }

    @Test
    void createAndDeliver_shouldRejectWhenNoCandidateItems() {
        mockCandidateItems(List.of());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.createAndDeliver(createReqVO(DeliveryTypeEnum.SCHOOL.getType()), 9L));

        assertEquals(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND.getCode(), ex.getCode());
        verify(publicationDeliveryBatchMapper, never()).insert(any(TradePublicationDeliveryBatchDO.class));
    }

    @Test
    void createAndDeliver_shouldRejectDuplicateOrderIssue() {
        mockCandidateItems(List.of(candidateItem(9001L, DeliveryTypeEnum.SCHOOL.getType())));
        mockBatchInsert();
        when(tradeNoRedisDAO.generate(TradeNoRedisDAO.PUBLICATION_DELIVERY_BATCH_NO_PREFIX)).thenReturn("pd100");
        doThrow(new DuplicateKeyException("duplicate")).when(publicationDeliveryBatchItemMapper).insertBatch(any());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.createAndDeliver(createReqVO(DeliveryTypeEnum.SCHOOL.getType()), 9L));

        assertEquals(PUBLICATION_ISSUE_DELIVERY_DUPLICATE.getCode(), ex.getCode());
        verify(publicationIssueMapper, never()).updateDeliveredByIds(any(), anyInt(), any(), any());
    }

    @Test
    void createAndDeliver_shouldRejectWhenIssueUpdateCountMismatch() {
        mockCandidateItems(List.of(candidateItem(9001L, DeliveryTypeEnum.SCHOOL.getType()),
                candidateItem(9002L, DeliveryTypeEnum.SCHOOL.getType())));
        mockBatchInsert();
        when(tradeNoRedisDAO.generate(TradeNoRedisDAO.PUBLICATION_DELIVERY_BATCH_NO_PREFIX)).thenReturn("pd100");
        when(publicationIssueMapper.updateDeliveredByIds(any(),
                eq(PublicationDeliveryStatusEnum.UNDELIVERED.getStatus()), eq(200L), any(LocalDateTime.class)))
                .thenReturn(1);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.createAndDeliver(createReqVO(DeliveryTypeEnum.SCHOOL.getType()), 9L));

        assertEquals(PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL.getCode(), ex.getCode());
    }

    @Test
    void createAndDeliver_shouldRejectExpressWhenLogisticsMissing() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.createAndDeliver(createReqVO(DeliveryTypeEnum.EXPRESS.getType()), 9L));

        assertEquals(PUBLICATION_EXPRESS_LOGISTICS_REQUIRED.getCode(), ex.getCode());
    }

    @Test
    void getCandidateItemList_shouldRejectExpressBatchOverLimit() {
        List<TradePublicationDeliveryCandidateItemBO> items = LongStream.rangeClosed(1, 501)
                .mapToObj(id -> candidateItem(id, DeliveryTypeEnum.EXPRESS.getType()))
                .toList();
        when(publicationIssueMapper.selectPublicationDeliveryCandidateItemList(any(),
                eq(TradeOrderStatusEnum.UNDELIVERED.getStatus()),
                eq(PublicationDeliveryStatusEnum.UNDELIVERED.getStatus()), eq(501))).thenReturn(items);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.getCandidateItemList(new TradePublicationDeliveryCandidatePageReqVO()
                        .setDeliveryType(DeliveryTypeEnum.EXPRESS.getType())));

        assertEquals(PUBLICATION_EXPRESS_BATCH_TOO_LARGE.getCode(), ex.getCode());
    }

    private void mockCandidateItems(List<TradePublicationDeliveryCandidateItemBO> items) {
        when(publicationIssueMapper.selectPublicationDeliveryCandidateItemList(any(),
                eq(TradeOrderStatusEnum.UNDELIVERED.getStatus()),
                eq(PublicationDeliveryStatusEnum.UNDELIVERED.getStatus()), any())).thenReturn(items);
    }

    private void mockBatchInsert() {
        doAnswer(invocation -> {
            TradePublicationDeliveryBatchDO batch = invocation.getArgument(0);
            batch.setId(200L);
            return 1;
        }).when(publicationDeliveryBatchMapper).insert(any(TradePublicationDeliveryBatchDO.class));
    }

    private TradePublicationDeliveryBatchCreateReqVO createReqVO(Integer deliveryType) {
        return new TradePublicationDeliveryBatchCreateReqVO()
                .setDeliveryType(deliveryType)
                .setSchoolId(1L)
                .setWarehouseId(DeliveryTypeEnum.SCHOOL.getType().equals(deliveryType) ? 2L : null)
                .setWindowId(3L)
                .setOfferId(4L)
                .setOfferSkuId(5L)
                .setSkuId(6L)
                .setIssueNo(1);
    }

    private TradePublicationDeliveryBatchCreateReqVO createExpressReqVO() {
        return createReqVO(DeliveryTypeEnum.EXPRESS.getType())
                .setExpressItems(List.of(new TradePublicationDeliveryBatchCreateReqVO.ExpressItem()
                        .setOrderIssueId(9001L)
                        .setLogisticsId(1L)
                        .setLogisticsNo("SF100")));
    }

    private TradePublicationDeliveryCandidateItemBO candidateItem(Long orderIssueId, Integer deliveryType) {
        return new TradePublicationDeliveryCandidateItemBO()
                .setOrderIssueId(orderIssueId)
                .setOrderId(1L)
                .setOrderNo("NO1")
                .setOrderItemId(orderIssueId + 1000)
                .setDeliveryId(11L)
                .setUserId(8L)
                .setDeliveryType(deliveryType)
                .setCount(1)
                .setSchoolId(1L)
                .setSchoolNameSnapshot("实验小学")
                .setWarehouseId(DeliveryTypeEnum.SCHOOL.getType().equals(deliveryType) ? 2L : null)
                .setWarehouseNameSnapshot("城北站")
                .setWindowId(3L)
                .setWindowNameSnapshot("2026 春季订刊")
                .setOfferId(4L)
                .setOfferSkuId(5L)
                .setSkuId(6L)
                .setProductNameSnapshot("测试刊物")
                .setIssueNo(1)
                .setIssueName("第1期")
                .setStudentId(orderIssueId)
                .setStudentNameSnapshot("学生" + orderIssueId)
                .setClassId(7L)
                .setClassNameSnapshot("一年级1班");
    }

}
