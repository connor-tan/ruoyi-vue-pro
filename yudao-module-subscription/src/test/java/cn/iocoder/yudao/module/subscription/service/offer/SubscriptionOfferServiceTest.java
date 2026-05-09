package cn.iocoder.yudao.module.subscription.service.offer;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.api.gradecatalog.EduGradeCatalogApi;
import cn.iocoder.yudao.module.edu.api.gradecatalog.dto.EduGradeCatalogRespDTO;
import cn.iocoder.yudao.module.product.api.category.ProductCategoryApi;
import cn.iocoder.yudao.module.product.api.publication.ProductPublicationApi;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.SubscriptionOfferAvailablePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.SubscriptionOfferAvailableRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.SubscriptionOfferBatchCreateByQueryReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.SubscriptionOfferBatchCreateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.SubscriptionOfferBatchCreateRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.SubscriptionOfferSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferGradeRelMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferSkuMapper;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_ANCHOR_IMMUTABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionOfferServiceTest {

    private static final long WINDOW_ID = 10L;
    private static final long OFFER_ID = 20L;
    private static final long PRODUCT_SPU_ID = 30L;
    private static final long PRODUCT_SKU_ID = 40L;
    private static final String TARGET_PERIOD = "FULL_YEAR";

    @Mock
    private SubscriptionWindowOfferMapper offerMapper;
    @Mock
    private SubscriptionWindowOfferSkuMapper offerSkuMapper;
    @Mock
    private SubscriptionWindowOfferGradeRelMapper offerGradeRelMapper;
    @Mock
    private SubscriptionWindowService windowService;
    @Mock
    private ProductPublicationApi productPublicationApi;
    @Mock
    private ProductCategoryApi productCategoryApi;
    @Mock
    private EduGradeCatalogApi gradeCatalogApi;
    @InjectMocks
    private SubscriptionOfferServiceImpl offerService;

    @Test
    void batchCreateOffer_shouldSkipWhenNoMatchedEnabledSku() {
        SubscriptionOfferBatchCreateReqVO reqVO = new SubscriptionOfferBatchCreateReqVO();
        reqVO.setWindowId(WINDOW_ID);
        reqVO.setProductSpuIds(List.of(PRODUCT_SPU_ID));
        ProductPublicationRespDTO publication = publication("SECOND_TERM", CommonStatusEnum.ENABLE.getStatus());
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(window());
        when(productPublicationApi.getPublicationList(reqVO.getProductSpuIds())).thenReturn(List.of(publication));

        SubscriptionOfferBatchCreateRespVO result = offerService.batchCreateOffer(reqVO);

        assertEquals(0, result.getCreatedOfferCount());
        assertEquals(1, result.getSkippedCount());
        assertEquals(PRODUCT_SPU_ID, result.getSkippedItems().get(0).getProductSpuId());
        verify(offerMapper, never()).insert(any(SubscriptionWindowOfferDO.class));
        verify(offerSkuMapper, never()).insertBatch(anyList());
    }

    @Test
    void batchCreateOffer_shouldInitializeMatchedOfferSku() {
        SubscriptionOfferBatchCreateReqVO reqVO = new SubscriptionOfferBatchCreateReqVO();
        reqVO.setWindowId(WINDOW_ID);
        reqVO.setProductSpuIds(List.of(PRODUCT_SPU_ID));
        ProductPublicationRespDTO publication = publication(TARGET_PERIOD, CommonStatusEnum.ENABLE.getStatus());
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(window());
        when(productPublicationApi.getPublicationList(reqVO.getProductSpuIds())).thenReturn(List.of(publication));
        doAnswer(invocation -> {
            SubscriptionWindowOfferDO offer = invocation.getArgument(0);
            offer.setId(OFFER_ID);
            return 1;
        }).when(offerMapper).insert(any(SubscriptionWindowOfferDO.class));
        ArgumentCaptor<List<SubscriptionWindowOfferSkuDO>> captor = ArgumentCaptor.forClass(List.class);

        SubscriptionOfferBatchCreateRespVO result = offerService.batchCreateOffer(reqVO);

        assertEquals(List.of(OFFER_ID), result.getCreatedOfferIds());
        assertEquals(1, result.getCreatedOfferCount());
        assertEquals(1, result.getCreatedOfferSkuCount());
        verify(offerSkuMapper).insertBatch(captor.capture());
        assertEquals(OFFER_ID, captor.getValue().get(0).getOfferId());
        assertEquals(PRODUCT_SKU_ID, captor.getValue().get(0).getProductSkuId());
    }

    @Test
    void updateOffer_shouldRejectAnchorMutation() {
        when(offerMapper.selectById(OFFER_ID)).thenReturn(offer());
        SubscriptionOfferSaveReqVO reqVO = new SubscriptionOfferSaveReqVO();
        reqVO.setId(OFFER_ID);
        reqVO.setWindowId(WINDOW_ID + 1);

        assertServiceException(() -> offerService.updateOffer(reqVO), OFFER_ANCHOR_IMMUTABLE);

        verify(offerMapper, never()).updateById(any(SubscriptionWindowOfferDO.class));
    }

    @Test
    void updateOffer_shouldOnlyUpdateEditableFields() {
        when(offerMapper.selectById(OFFER_ID)).thenReturn(offer());
        SubscriptionOfferSaveReqVO reqVO = new SubscriptionOfferSaveReqVO();
        reqVO.setId(OFFER_ID);
        reqVO.setRecommendFlag(true);
        reqVO.setSort(9);
        reqVO.setStatus(CommonStatusEnum.DISABLE.getStatus());
        reqVO.setRemark("运营备注");
        ArgumentCaptor<SubscriptionWindowOfferDO> captor = ArgumentCaptor.forClass(SubscriptionWindowOfferDO.class);

        offerService.updateOffer(reqVO);

        verify(offerMapper).updateById(captor.capture());
        SubscriptionWindowOfferDO updateObj = captor.getValue();
        assertEquals(OFFER_ID, updateObj.getId());
        assertNull(updateObj.getWindowId());
        assertNull(updateObj.getProductSpuId());
        assertEquals(true, updateObj.getRecommendFlag());
        assertEquals(9, updateObj.getSort());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), updateObj.getStatus());
    }

    @Test
    void getAvailablePage_shouldReturnCanAddPublicationWithMatchedGrade() {
        SubscriptionOfferAvailablePageReqVO reqVO = new SubscriptionOfferAvailablePageReqVO();
        reqVO.setWindowId(WINDOW_ID);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setGradeCatalogId(1L);
        SubscriptionOfferAvailableRespVO available = new SubscriptionOfferAvailableRespVO();
        available.setProductSpuId(PRODUCT_SPU_ID);
        available.setProductName("测试刊物");
        available.setCandidateStatus("CAN_ADD");
        available.setMatchedGradeCatalogIdText("1");
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(window());
        when(offerMapper.selectAvailableCandidates(eq(reqVO), eq(TARGET_PERIOD), eq(0), eq(10)))
                .thenReturn(List.of(available));
        when(offerMapper.selectAvailableCandidateCount(eq(reqVO), eq(TARGET_PERIOD))).thenReturn(1L);
        EduGradeCatalogRespDTO grade = new EduGradeCatalogRespDTO();
        grade.setId(1L);
        grade.setGradeName("一年级");
        when(gradeCatalogApi.getGradeCatalogMap(any())).thenReturn(Map.of(1L, grade));

        PageResult<SubscriptionOfferAvailableRespVO> pageResult = offerService.getAvailablePage(reqVO);

        assertEquals(1, pageResult.getTotal());
        SubscriptionOfferAvailableRespVO candidate = pageResult.getList().get(0);
        assertEquals(PRODUCT_SPU_ID, candidate.getProductSpuId());
        assertEquals("CAN_ADD", candidate.getCandidateStatus());
        assertEquals(List.of(1L), candidate.getMatchedGradeCatalogIds());
        assertEquals(List.of("一年级"), candidate.getMatchedGradeNames());
        verify(productCategoryApi, never()).getSelfAndDescendantCategoryIds(any());
    }

    @Test
    void getAvailablePage_shouldExpandParentCategoryForCandidateListAndCount() {
        SubscriptionOfferAvailablePageReqVO reqVO = new SubscriptionOfferAvailablePageReqVO();
        reqVO.setWindowId(WINDOW_ID);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setCategoryIds(List.of(100L));
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(window());
        when(productCategoryApi.getSelfAndDescendantCategoryIds(List.of(100L)))
                .thenReturn(new LinkedHashSet<>(List.of(100L, 101L)));
        when(offerMapper.selectAvailableCandidates(any(SubscriptionOfferAvailablePageReqVO.class),
                eq(TARGET_PERIOD), eq(0), eq(10))).thenReturn(List.of());
        when(offerMapper.selectAvailableCandidateCount(any(SubscriptionOfferAvailablePageReqVO.class),
                eq(TARGET_PERIOD))).thenReturn(0L);
        ArgumentCaptor<SubscriptionOfferAvailablePageReqVO> queryCaptor =
                ArgumentCaptor.forClass(SubscriptionOfferAvailablePageReqVO.class);

        PageResult<SubscriptionOfferAvailableRespVO> pageResult = offerService.getAvailablePage(reqVO);

        assertEquals(0, pageResult.getTotal());
        verify(offerMapper).selectAvailableCandidates(queryCaptor.capture(), eq(TARGET_PERIOD), eq(0), eq(10));
        verify(offerMapper).selectAvailableCandidateCount(queryCaptor.capture(), eq(TARGET_PERIOD));
        assertEquals(List.of(100L, 101L), queryCaptor.getAllValues().get(0).getCategoryIds());
        assertEquals(List.of(100L, 101L), queryCaptor.getAllValues().get(1).getCategoryIds());
    }

    @Test
    void batchCreateByQuery_shouldExpandParentCategoryForFullCandidateQuery() {
        SubscriptionOfferAvailablePageReqVO query = new SubscriptionOfferAvailablePageReqVO();
        query.setWindowId(WINDOW_ID);
        query.setPageNo(1);
        query.setPageSize(1);
        query.setCategoryIds(List.of(100L));
        SubscriptionOfferBatchCreateByQueryReqVO reqVO = new SubscriptionOfferBatchCreateByQueryReqVO();
        reqVO.setWindowId(WINDOW_ID);
        reqVO.setQuery(query);
        SubscriptionOfferAvailableRespVO available = new SubscriptionOfferAvailableRespVO();
        available.setProductSpuId(PRODUCT_SPU_ID);
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(window());
        when(productCategoryApi.getSelfAndDescendantCategoryIds(List.of(100L)))
                .thenReturn(new LinkedHashSet<>(List.of(100L, 101L)));
        when(offerMapper.selectAvailableCandidates(any(SubscriptionOfferAvailablePageReqVO.class),
                eq(TARGET_PERIOD), isNull(), isNull())).thenReturn(List.of(available));
        when(productPublicationApi.getPublicationList(List.of(PRODUCT_SPU_ID))).thenReturn(List.of());
        ArgumentCaptor<SubscriptionOfferAvailablePageReqVO> queryCaptor =
                ArgumentCaptor.forClass(SubscriptionOfferAvailablePageReqVO.class);

        SubscriptionOfferBatchCreateRespVO result = offerService.batchCreateByQuery(reqVO);

        assertEquals(0, result.getCreatedOfferCount());
        assertEquals(1, result.getSkippedCount());
        verify(offerMapper).selectAvailableCandidates(queryCaptor.capture(), eq(TARGET_PERIOD), isNull(), isNull());
        SubscriptionOfferAvailablePageReqVO mapperQuery = queryCaptor.getValue();
        assertEquals(WINDOW_ID, mapperQuery.getWindowId());
        assertEquals("CAN_ADD", mapperQuery.getCandidateStatus());
        assertEquals(List.of(100L, 101L), mapperQuery.getCategoryIds());
    }

    private SubscriptionWindowDO window() {
        return SubscriptionWindowDO.builder()
                .id(WINDOW_ID)
                .targetPeriod(TARGET_PERIOD)
                .build();
    }

    private SubscriptionWindowOfferDO offer() {
        return SubscriptionWindowOfferDO.builder()
                .id(OFFER_ID)
                .windowId(WINDOW_ID)
                .productSpuId(PRODUCT_SPU_ID)
                .recommendFlag(false)
                .sort(0)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build();
    }

    private ProductPublicationRespDTO publication(String targetPeriod, Integer skuStatus) {
        ProductPublicationRespDTO publication = new ProductPublicationRespDTO();
        publication.setId(PRODUCT_SPU_ID);
        publication.setName("测试刊物");
        publication.setBizScene(BizSceneEnum.PUBLICATION.getCode());
        publication.setStatus(ProductSpuStatusEnum.ENABLE.getStatus());
        ProductPublicationRespDTO.PublicationSkuExtDTO skuExt = new ProductPublicationRespDTO.PublicationSkuExtDTO();
        skuExt.setTargetPeriod(targetPeriod);
        ProductPublicationRespDTO.PublicationSkuDTO sku = new ProductPublicationRespDTO.PublicationSkuDTO();
        sku.setId(PRODUCT_SKU_ID);
        sku.setStatus(skuStatus);
        sku.setPublicationExt(skuExt);
        sku.setApplicableGradeCatalogIds(List.of(1L));
        publication.setSkus(List.of(sku));
        return publication;
    }

}
