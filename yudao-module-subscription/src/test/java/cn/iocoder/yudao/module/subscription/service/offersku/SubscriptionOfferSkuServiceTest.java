package cn.iocoder.yudao.module.subscription.service.offersku;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.api.publication.ProductPublicationApi;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo.SubscriptionOfferSkuSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferSkuMapper;
import cn.iocoder.yudao.module.subscription.service.offer.SubscriptionOfferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_SKU_BELONG_ERROR;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_SKU_DUPLICATE;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_SKU_EFFECTIVE_REQUIRED;
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

    @Mock
    private SubscriptionWindowOfferSkuMapper offerSkuMapper;
    @Mock
    private SubscriptionOfferService offerService;
    @Mock
    private ProductPublicationApi productPublicationApi;
    @Mock
    private SubscriptionOfferSkuAvailabilityValidator offerSkuAvailabilityValidator;
    @InjectMocks
    private SubscriptionOfferSkuServiceImpl offerSkuService;

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
                publicationSku(PRODUCT_SKU_ID, CommonStatusEnum.ENABLE.getStatus()),
                publicationSku(PRODUCT_SKU_ID_2, CommonStatusEnum.ENABLE.getStatus()),
                publicationSku(52L, CommonStatusEnum.ENABLE.getStatus()),
                publicationSku(53L, CommonStatusEnum.DISABLE.getStatus())
        )));
        when(offerSkuMapper.selectListByOfferId(OFFER_ID)).thenReturn(List.of(
                SubscriptionWindowOfferSkuDO.builder()
                        .id(OFFER_SKU_ID)
                        .offerId(OFFER_ID)
                        .productSkuId(PRODUCT_SKU_ID_2)
                        .sort(3)
                        .build()
        ));

        int count = offerSkuService.syncMatchedOfferSkus(OFFER_ID);

        assertEquals(2, count);
        ArgumentCaptor<List<SubscriptionWindowOfferSkuDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(offerSkuMapper).insertBatch(captor.capture());
        SubscriptionWindowOfferSkuDO inserted = captor.getValue().get(0);
        assertEquals(2, captor.getValue().size());
        assertEquals(OFFER_ID, inserted.getOfferId());
        assertEquals(PRODUCT_SKU_ID, inserted.getProductSkuId());
        assertEquals(4, inserted.getSort());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), inserted.getStatus());
        assertEquals(1, inserted.getMaxQuantityPerStudent());
        assertEquals(52L, captor.getValue().get(1).getProductSkuId());
        assertEquals(5, captor.getValue().get(1).getSort());
    }

    @Test
    void syncMatchedOfferSkus_shouldValidateEnabledOfferWhenPublicationHasNoSku() {
        when(offerService.validateOfferExists(OFFER_ID)).thenReturn(offer(OFFER_ID));
        when(productPublicationApi.getPublication(PRODUCT_SPU_ID)).thenReturn(publication(List.of()));
        doThrow(exception(OFFER_SKU_EFFECTIVE_REQUIRED))
                .when(offerSkuAvailabilityValidator).validateEnabledOfferHasEffectiveSku(OFFER_ID);

        assertServiceException(() -> offerSkuService.syncMatchedOfferSkus(OFFER_ID), OFFER_SKU_EFFECTIVE_REQUIRED);

        verify(offerSkuMapper, never()).insertBatch(anyList());
        verify(offerSkuAvailabilityValidator).validateEnabledOfferHasEffectiveSku(OFFER_ID);
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

    private ProductPublicationRespDTO publication() {
        return publication(List.of(publicationSku(PRODUCT_SKU_ID, CommonStatusEnum.ENABLE.getStatus())));
    }

    private ProductPublicationRespDTO publication(List<ProductPublicationRespDTO.PublicationSkuDTO> skus) {
        ProductPublicationRespDTO publication = new ProductPublicationRespDTO();
        publication.setId(PRODUCT_SPU_ID);
        publication.setSkus(skus);
        return publication;
    }

    private ProductPublicationRespDTO.PublicationSkuDTO publicationSku(Long skuId, Integer status) {
        ProductPublicationRespDTO.PublicationSkuDTO sku = new ProductPublicationRespDTO.PublicationSkuDTO();
        sku.setId(skuId);
        sku.setStatus(status);
        return sku;
    }

}
