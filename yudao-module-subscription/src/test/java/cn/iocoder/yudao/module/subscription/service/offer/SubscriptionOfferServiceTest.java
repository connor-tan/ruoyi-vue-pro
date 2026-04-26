package cn.iocoder.yudao.module.subscription.service.offer;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.api.gradecatalog.EduGradeCatalogApi;
import cn.iocoder.yudao.module.product.api.publication.ProductPublicationApi;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.SubscriptionOfferBatchCreateReqVO;
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

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_ANCHOR_IMMUTABLE;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_NO_MATCHED_SKU;
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
    private EduGradeCatalogApi gradeCatalogApi;
    @InjectMocks
    private SubscriptionOfferService offerService;

    @Test
    void batchCreateOffer_shouldRejectWhenNoMatchedEnabledSku() {
        SubscriptionOfferBatchCreateReqVO reqVO = new SubscriptionOfferBatchCreateReqVO();
        reqVO.setWindowId(WINDOW_ID);
        reqVO.setProductSpuIds(List.of(PRODUCT_SPU_ID));
        ProductPublicationRespDTO publication = publication("SECOND_TERM", CommonStatusEnum.ENABLE.getStatus());
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(window());
        when(productPublicationApi.getPublicationList(reqVO.getProductSpuIds())).thenReturn(List.of(publication));

        assertServiceException(() -> offerService.batchCreateOffer(reqVO), OFFER_NO_MATCHED_SKU);

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

        List<Long> ids = offerService.batchCreateOffer(reqVO);

        assertEquals(List.of(OFFER_ID), ids);
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
        publication.setBizScene(BizSceneEnum.PUBLICATION.getCode());
        ProductPublicationRespDTO.PublicationSkuExtDTO skuExt = new ProductPublicationRespDTO.PublicationSkuExtDTO();
        skuExt.setTargetPeriod(targetPeriod);
        ProductPublicationRespDTO.PublicationSkuDTO sku = new ProductPublicationRespDTO.PublicationSkuDTO();
        sku.setId(PRODUCT_SKU_ID);
        sku.setStatus(skuStatus);
        sku.setPublicationExt(skuExt);
        publication.setSkus(List.of(sku));
        return publication;
    }

}
