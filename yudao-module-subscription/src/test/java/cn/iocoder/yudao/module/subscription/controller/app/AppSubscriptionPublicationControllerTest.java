package cn.iocoder.yudao.module.subscription.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.subscription.controller.app.vo.AppSubscriptionPublicationRespVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityService;
import cn.iocoder.yudao.module.trade.api.order.TradeSubscriptionOrderApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppSubscriptionPublicationControllerTest {

    private static final Long STUDENT_ID = 10L;
    private static final Long OFFER_ID = 20L;
    private static final Long PRODUCT_SPU_ID = 30L;
    private static final Long OFFER_SKU_ID = 40L;
    private static final Long OFFER_SKU_ID_2 = 41L;
    private static final Long PRODUCT_SKU_ID = 50L;
    private static final Long PRODUCT_SKU_ID_2 = 51L;

    @Mock
    private SubscriptionVisibilityService visibilityService;
    @Mock
    private TradeSubscriptionOrderApi tradeSubscriptionOrderApi;
    @InjectMocks
    private AppSubscriptionPublicationController controller;

    @Test
    void list_shouldFillPurchaseAvailabilityByOrderedQuantity() {
        when(visibilityService.calculate(isNull(), eq(STUDENT_ID), isNull())).thenReturn(visibility());
        when(tradeSubscriptionOrderApi.getEffectiveSubscriptionOrderItemQuantityMap(eq(STUDENT_ID), anySet()))
                .thenReturn(Map.of(OFFER_SKU_ID, 0, OFFER_SKU_ID_2, 1));

        CommonResult<AppSubscriptionPublicationRespVO> result = controller.list(STUDENT_ID, Set.of(PRODUCT_SPU_ID));

        AppSubscriptionPublicationRespVO.Offer offer = result.getData().getOffers().get(0);
        assertTrue(offer.getPurchasable());
        assertNull(offer.getPurchaseUnavailableReasonDesc());

        AppSubscriptionPublicationRespVO.OfferSku availableSku = offer.getFinalSkus().get(0);
        assertTrue(availableSku.getPurchasable());
        assertEquals(1, availableSku.getMaxQuantityPerStudent());
        assertEquals(0, availableSku.getOrderedQuantity());
        assertEquals(1, availableSku.getRemainingQuantity());
        assertNull(availableSku.getPurchaseUnavailableReasonDesc());

        AppSubscriptionPublicationRespVO.OfferSku exhaustedSku = offer.getFinalSkus().get(1);
        assertFalse(exhaustedSku.getPurchasable());
        assertEquals(1, exhaustedSku.getMaxQuantityPerStudent());
        assertEquals(1, exhaustedSku.getOrderedQuantity());
        assertEquals(0, exhaustedSku.getRemainingQuantity());
        assertEquals("已达该刊物限购数量", exhaustedSku.getPurchaseUnavailableReasonDesc());
    }

    private SubscriptionVisibilityResultBO visibility() {
        SubscriptionVisibilityResultBO visibility = new SubscriptionVisibilityResultBO();
        SubscriptionVisibilityResultBO.VisibleOffer visibleOffer = new SubscriptionVisibilityResultBO.VisibleOffer();
        visibleOffer.setOffer(offer());
        visibleOffer.setPublication(publication());
        visibleOffer.setSkus(List.of(
                visibleSku(OFFER_SKU_ID, PRODUCT_SKU_ID),
                visibleSku(OFFER_SKU_ID_2, PRODUCT_SKU_ID_2)));
        visibility.setVisibleOffers(List.of(visibleOffer));
        return visibility;
    }

    private SubscriptionWindowOfferDO offer() {
        return SubscriptionWindowOfferDO.builder()
                .id(OFFER_ID)
                .productSpuId(PRODUCT_SPU_ID)
                .build();
    }

    private ProductPublicationRespDTO publication() {
        ProductPublicationRespDTO publication = new ProductPublicationRespDTO();
        publication.setId(PRODUCT_SPU_ID);
        publication.setName("测试刊物");
        return publication;
    }

    private SubscriptionVisibilityResultBO.VisibleOfferSku visibleSku(Long offerSkuId, Long productSkuId) {
        SubscriptionVisibilityResultBO.VisibleOfferSku visibleSku = new SubscriptionVisibilityResultBO.VisibleOfferSku();
        visibleSku.setOfferSku(SubscriptionWindowOfferSkuDO.builder()
                .id(offerSkuId)
                .offerId(OFFER_ID)
                .productSkuId(productSkuId)
                .maxQuantityPerStudent(1)
                .build());
        ProductPublicationRespDTO.PublicationSkuDTO productSku = new ProductPublicationRespDTO.PublicationSkuDTO();
        productSku.setId(productSkuId);
        productSku.setName("SKU " + productSkuId);
        visibleSku.setProductSku(productSku);
        return visibleSku;
    }

}
