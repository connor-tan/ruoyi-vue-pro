package cn.iocoder.yudao.module.subscription.service.offersku;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.api.publication.ProductPublicationApi;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo.SubscriptionOfferSkuSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferSkuMapper;
import cn.iocoder.yudao.module.subscription.service.offer.SubscriptionOfferService;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_SKU_BELONG_ERROR;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_SKU_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionOfferSkuServiceTest {

    private static final long WINDOW_ID = 10L;
    private static final long OFFER_ID = 20L;
    private static final long OTHER_OFFER_ID = 21L;
    private static final long OFFER_SKU_ID = 30L;
    private static final long PRODUCT_SPU_ID = 40L;
    private static final long PRODUCT_SKU_ID = 50L;
    private static final long PRODUCT_SKU_ID_2 = 51L;
    private static final String TARGET_PERIOD = "FULL_YEAR";

    @Mock
    private SubscriptionWindowOfferSkuMapper offerSkuMapper;
    @Mock
    private SubscriptionOfferService offerService;
    @Mock
    private SubscriptionWindowService windowService;
    @Mock
    private ProductPublicationApi productPublicationApi;
    @Mock
    private SubscriptionOfferSkuAvailabilityService offerSkuAvailabilityService;
    @InjectMocks
    private SubscriptionOfferSkuService offerSkuService;

    @Test
    void saveOfferSku_shouldRejectCrossOfferUpdate() {
        when(offerService.validateOfferExists(OFFER_ID)).thenReturn(offer(OFFER_ID));
        when(offerSkuMapper.selectById(OFFER_SKU_ID)).thenReturn(offerSku(OTHER_OFFER_ID));
        SubscriptionOfferSkuSaveReqVO reqVO = offerSkuReq();

        assertServiceException(() -> offerSkuService.saveOfferSku(reqVO), OFFER_SKU_BELONG_ERROR);

        verify(offerSkuMapper, never()).updateById(any(SubscriptionWindowOfferSkuDO.class));
    }

    @Test
    void saveOfferSku_shouldRejectDuplicateWhenUpdating() {
        when(offerService.validateOfferExists(OFFER_ID)).thenReturn(offer(OFFER_ID));
        when(offerSkuMapper.selectById(OFFER_SKU_ID)).thenReturn(offerSku(OFFER_ID));
        when(productPublicationApi.getPublication(PRODUCT_SPU_ID)).thenReturn(publication());
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(window());
        when(offerSkuMapper.selectByOfferIdAndProductSkuIdAndIdNot(OFFER_ID, PRODUCT_SKU_ID, OFFER_SKU_ID))
                .thenReturn(new SubscriptionWindowOfferSkuDO());
        SubscriptionOfferSkuSaveReqVO reqVO = offerSkuReq();

        assertServiceException(() -> offerSkuService.saveOfferSku(reqVO), OFFER_SKU_DUPLICATE);

        verify(offerSkuMapper, never()).updateById(any(SubscriptionWindowOfferSkuDO.class));
    }

    @Test
    void syncMatchedOfferSkus_shouldInsertMissingMatchedSkus() {
        when(offerService.validateOfferExists(OFFER_ID)).thenReturn(offer(OFFER_ID));
        when(productPublicationApi.getPublication(PRODUCT_SPU_ID)).thenReturn(publication(List.of(
                publicationSku(PRODUCT_SKU_ID, TARGET_PERIOD, CommonStatusEnum.ENABLE.getStatus()),
                publicationSku(PRODUCT_SKU_ID_2, TARGET_PERIOD, CommonStatusEnum.ENABLE.getStatus()),
                publicationSku(52L, "FIRST_TERM", CommonStatusEnum.ENABLE.getStatus()),
                publicationSku(53L, TARGET_PERIOD, CommonStatusEnum.DISABLE.getStatus())
        )));
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(window());
        when(offerSkuMapper.selectListByOfferId(OFFER_ID)).thenReturn(List.of(
                SubscriptionWindowOfferSkuDO.builder()
                        .id(OFFER_SKU_ID)
                        .offerId(OFFER_ID)
                        .productSkuId(PRODUCT_SKU_ID_2)
                        .sort(3)
                        .build()
        ));

        int count = offerSkuService.syncMatchedOfferSkus(OFFER_ID);

        assertEquals(1, count);
        ArgumentCaptor<List<SubscriptionWindowOfferSkuDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(offerSkuMapper).insertBatch(captor.capture());
        SubscriptionWindowOfferSkuDO inserted = captor.getValue().get(0);
        assertEquals(OFFER_ID, inserted.getOfferId());
        assertEquals(PRODUCT_SKU_ID, inserted.getProductSkuId());
        assertEquals(4, inserted.getSort());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), inserted.getStatus());
        assertEquals(1, inserted.getMaxQuantityPerStudent());
    }

    private SubscriptionOfferSkuSaveReqVO offerSkuReq() {
        SubscriptionOfferSkuSaveReqVO reqVO = new SubscriptionOfferSkuSaveReqVO();
        reqVO.setId(OFFER_SKU_ID);
        reqVO.setOfferId(OFFER_ID);
        reqVO.setProductSkuId(PRODUCT_SKU_ID);
        return reqVO;
    }

    private SubscriptionWindowOfferDO offer(Long offerId) {
        return SubscriptionWindowOfferDO.builder()
                .id(offerId)
                .windowId(WINDOW_ID)
                .productSpuId(PRODUCT_SPU_ID)
                .build();
    }

    private SubscriptionWindowOfferSkuDO offerSku(Long offerId) {
        return SubscriptionWindowOfferSkuDO.builder()
                .id(OFFER_SKU_ID)
                .offerId(offerId)
                .productSkuId(PRODUCT_SKU_ID)
                .build();
    }

    private SubscriptionWindowDO window() {
        return SubscriptionWindowDO.builder()
                .id(WINDOW_ID)
                .targetPeriod(TARGET_PERIOD)
                .build();
    }

    private ProductPublicationRespDTO publication() {
        return publication(List.of(publicationSku(PRODUCT_SKU_ID, TARGET_PERIOD, CommonStatusEnum.ENABLE.getStatus())));
    }

    private ProductPublicationRespDTO publication(List<ProductPublicationRespDTO.PublicationSkuDTO> skus) {
        ProductPublicationRespDTO publication = new ProductPublicationRespDTO();
        publication.setId(PRODUCT_SPU_ID);
        publication.setSkus(skus);
        return publication;
    }

    private ProductPublicationRespDTO.PublicationSkuDTO publicationSku(Long skuId, String targetPeriod, Integer status) {
        ProductPublicationRespDTO.PublicationSkuExtDTO skuExt = new ProductPublicationRespDTO.PublicationSkuExtDTO();
        skuExt.setTargetPeriod(targetPeriod);
        ProductPublicationRespDTO.PublicationSkuDTO sku = new ProductPublicationRespDTO.PublicationSkuDTO();
        sku.setId(skuId);
        sku.setStatus(status);
        sku.setPublicationExt(skuExt);
        return sku;
    }

}
