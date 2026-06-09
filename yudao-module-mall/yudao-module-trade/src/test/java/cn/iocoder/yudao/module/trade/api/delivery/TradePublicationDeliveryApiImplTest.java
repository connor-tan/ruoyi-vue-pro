package cn.iocoder.yudao.module.trade.api.delivery;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateItemRespDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidatePageReqDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryConfirmReqDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCreateReqDTO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderPublicationIssueMapper;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderPublicationIssueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;

import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_EXPRESS_BATCH_TOO_LARGE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_EXPRESS_LOGISTICS_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradePublicationDeliveryApiImplTest {

    @Mock
    private TradeOrderPublicationIssueMapper publicationIssueMapper;
    @Mock
    private DeliveryExpressService deliveryExpressService;
    @Mock
    private TradeOrderPublicationIssueService publicationIssueService;
    @InjectMocks
    private TradePublicationDeliveryApiImpl api;

    @Test
    void getDeliverableItemList_shouldValidateExpressLogisticsAndItems() {
        when(publicationIssueMapper.selectPublicationDeliveryCandidateItemList(any(),
                eq(TradeOrderStatusEnum.UNDELIVERED.getStatus()),
                eq(PublicationDeliveryStatusEnum.UNDELIVERED.getStatus()), eq(501)))
                .thenReturn(List.of(candidateItem(9001L, DeliveryTypeEnum.EXPRESS.getType())));

        List<TradePublicationDeliveryCandidateItemRespDTO> items = api.getDeliverableItemList(expressCreateReq());

        assertEquals(1, items.size());
        verify(deliveryExpressService).validateDeliveryExpress(1L);
    }

    @Test
    void getDeliverableItemList_shouldRejectExpressWhenLogisticsMissing() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> api.getDeliverableItemList(createReq(DeliveryTypeEnum.EXPRESS.getType())));

        assertEquals(PUBLICATION_EXPRESS_LOGISTICS_REQUIRED.getCode(), ex.getCode());
    }

    @Test
    void getDeliverableItemList_shouldRejectExpressWhenWarehouseMissing() {
        TradePublicationDeliveryCreateReqDTO reqDTO = expressCreateReq().setWarehouseId(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> api.getDeliverableItemList(reqDTO));

        assertEquals(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void getCandidateItemList_shouldRejectExpressBatchOverLimit() {
        List<TradePublicationDeliveryCandidateItemRespDTO> items = LongStream.rangeClosed(1, 501)
                .mapToObj(id -> candidateItem(id, DeliveryTypeEnum.EXPRESS.getType()))
                .toList();
        when(publicationIssueMapper.selectPublicationDeliveryCandidateItemList(any(),
                eq(TradeOrderStatusEnum.UNDELIVERED.getStatus()),
                eq(PublicationDeliveryStatusEnum.UNDELIVERED.getStatus()), eq(501))).thenReturn(items);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> api.getCandidateItemList(new TradePublicationDeliveryCandidatePageReqDTO()
                        .setDeliveryType(DeliveryTypeEnum.EXPRESS.getType())));

        assertEquals(PUBLICATION_EXPRESS_BATCH_TOO_LARGE.getCode(), ex.getCode());
    }

    @Test
    void confirmDelivered_shouldUpdateSchoolIssuesAndRefreshOrderStatus() {
        LocalDateTime deliveryTime = LocalDateTime.now();
        when(publicationIssueMapper.updateDeliveredByIds(eq(Set.of(9001L, 9002L)),
                eq(PublicationDeliveryStatusEnum.UNDELIVERED.getStatus()), eq(200L), eq(deliveryTime)))
                .thenReturn(2);

        api.confirmDelivered(new TradePublicationDeliveryConfirmReqDTO()
                .setDeliveryBatchId(200L)
                .setDeliveryType(DeliveryTypeEnum.SCHOOL.getType())
                .setDeliveryTime(deliveryTime)
                .setItems(List.of(confirmItem(9001L), confirmItem(9002L))));

        verify(publicationIssueService).afterIssueDelivered(eq(Set.of(9001L, 9002L)), eq(deliveryTime));
    }

    @Test
    void confirmDelivered_shouldRejectWhenUpdateCountMismatch() {
        LocalDateTime deliveryTime = LocalDateTime.now();
        when(publicationIssueMapper.updateDeliveredByIds(any(),
                eq(PublicationDeliveryStatusEnum.UNDELIVERED.getStatus()), eq(200L), eq(deliveryTime)))
                .thenReturn(1);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> api.confirmDelivered(new TradePublicationDeliveryConfirmReqDTO()
                        .setDeliveryBatchId(200L)
                        .setDeliveryType(DeliveryTypeEnum.SCHOOL.getType())
                        .setDeliveryTime(deliveryTime)
                        .setItems(List.of(confirmItem(9001L), confirmItem(9002L)))));

        assertEquals(PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL.getCode(), ex.getCode());
    }

    private TradePublicationDeliveryCreateReqDTO createReq(Integer deliveryType) {
        return new TradePublicationDeliveryCreateReqDTO()
                .setDeliveryType(deliveryType)
                .setSchoolId(1L)
                .setStationId(9L)
                .setWarehouseId(2L)
                .setWindowId(3L)
                .setOfferId(4L)
                .setOfferSkuId(5L)
                .setSkuId(6L)
                .setIssueNo(1);
    }

    private TradePublicationDeliveryCreateReqDTO expressCreateReq() {
        return createReq(DeliveryTypeEnum.EXPRESS.getType())
                .setExpressItems(List.of(new TradePublicationDeliveryCreateReqDTO.ExpressItem()
                        .setOrderIssueId(9001L)
                        .setLogisticsId(1L)
                        .setLogisticsNo("SF100")));
    }

    private TradePublicationDeliveryConfirmReqDTO.Item confirmItem(Long orderIssueId) {
        return new TradePublicationDeliveryConfirmReqDTO.Item().setOrderIssueId(orderIssueId);
    }

    private TradePublicationDeliveryCandidateItemRespDTO candidateItem(Long orderIssueId, Integer deliveryType) {
        return new TradePublicationDeliveryCandidateItemRespDTO()
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
                .setStationId(9L)
                .setStationNameSnapshot("城北站点")
                .setWarehouseId(2L)
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
