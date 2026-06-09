package cn.iocoder.yudao.module.repo.service.publicationdelivery;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchCreateReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationdelivery.RepoPublicationDeliveryBatchDO;
import cn.iocoder.yudao.module.repo.dal.mysql.publicationdelivery.RepoPublicationDeliveryBatchItemMapper;
import cn.iocoder.yudao.module.repo.dal.mysql.publicationdelivery.RepoPublicationDeliveryBatchMapper;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.RepoPublicationReceiptService;
import cn.iocoder.yudao.module.trade.api.delivery.TradePublicationDeliveryApi;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateItemRespDTO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_RECEIPT_WAREHOUSE_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepoPublicationDeliveryBatchServiceImplTest {

    @Mock
    private TradePublicationDeliveryApi tradePublicationDeliveryApi;
    @Mock
    private RepoPublicationReceiptService publicationReceiptService;
    @Mock
    private RepoPublicationDeliveryBatchMapper publicationDeliveryBatchMapper;
    @Mock
    private RepoPublicationDeliveryBatchItemMapper publicationDeliveryBatchItemMapper;
    @InjectMocks
    private RepoPublicationDeliveryBatchServiceImpl service;

    @Test
    void createAndDeliver_shouldRejectMissingWarehouseBeforeTradeQuery() {
        RepoPublicationDeliveryBatchCreateReqVO reqVO = createReq().setWarehouseId(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createAndDeliver(reqVO, 900L));

        assertEquals(PUBLICATION_RECEIPT_WAREHOUSE_REQUIRED.getCode(), ex.getCode());
        verifyNoInteractions(tradePublicationDeliveryApi, publicationReceiptService,
                publicationDeliveryBatchMapper, publicationDeliveryBatchItemMapper);
    }

    @Test
    void createAndDeliver_shouldRejectDeliverableItemsOutsideRequestScope() {
        when(tradePublicationDeliveryApi.getDeliverableItemList(any()))
                .thenReturn(List.of(candidateItem().setWarehouseId(21L)));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createAndDeliver(createReq(), 900L));

        assertEquals(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND.getCode(), ex.getCode());
        verify(publicationDeliveryBatchMapper, never()).insert(any(RepoPublicationDeliveryBatchDO.class));
        verifyNoInteractions(publicationReceiptService, publicationDeliveryBatchItemMapper);
    }

    private RepoPublicationDeliveryBatchCreateReqVO createReq() {
        return new RepoPublicationDeliveryBatchCreateReqVO()
                .setDeliveryType(DeliveryTypeEnum.EXPRESS.getType())
                .setSchoolId(10L)
                .setStationId(11L)
                .setWarehouseId(20L)
                .setWindowId(30L)
                .setOfferId(40L)
                .setOfferSkuId(50L)
                .setSkuId(60L)
                .setIssueId(70L)
                .setIssueNo(1)
                .setExpressItems(List.of(new RepoPublicationDeliveryBatchCreateReqVO.ExpressItem()
                        .setOrderIssueId(1000L)
                        .setLogisticsId(1L)
                        .setLogisticsNo("SF100")));
    }

    private TradePublicationDeliveryCandidateItemRespDTO candidateItem() {
        return new TradePublicationDeliveryCandidateItemRespDTO()
                .setOrderIssueId(1000L)
                .setOrderId(100L)
                .setOrderNo("NO100")
                .setOrderItemId(200L)
                .setDeliveryId(300L)
                .setUserId(400L)
                .setDeliveryType(DeliveryTypeEnum.EXPRESS.getType())
                .setCount(1)
                .setSchoolId(10L)
                .setSchoolNameSnapshot("实验小学")
                .setStationId(11L)
                .setStationNameSnapshot("城北站点")
                .setWarehouseId(20L)
                .setWarehouseNameSnapshot("中心仓")
                .setWindowId(30L)
                .setWindowNameSnapshot("2026 春季订刊")
                .setOfferId(40L)
                .setOfferSkuId(50L)
                .setSkuId(60L)
                .setProductNameSnapshot("测试刊物")
                .setStudentId(500L)
                .setStudentNameSnapshot("学生A")
                .setClassId(600L)
                .setClassNameSnapshot("一年级1班")
                .setIssueId(70L)
                .setIssueNo(1)
                .setIssueName("第1期");
    }

}
