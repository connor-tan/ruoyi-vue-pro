package cn.iocoder.yudao.module.subscription.service.order;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentSubscriptionContextRespDTO;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.PublicationIssueModeEnum;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import cn.iocoder.yudao.module.subscription.service.offer.SubscriptionOfferService;
import cn.iocoder.yudao.module.subscription.service.offersku.SubscriptionOfferSkuService;
import cn.iocoder.yudao.module.subscription.service.offerskuissue.SubscriptionOfferSkuIssueService;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityService;
import cn.iocoder.yudao.module.trade.api.order.TradeSubscriptionOrderApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.ORDER_MAX_QUANTITY_EXCEEDED;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.ORDER_OFFER_SKU_NOT_AVAILABLE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionOrderEligibilityServiceImplTest {

    private static final Long USER_ID = 10L;
    private static final Long STUDENT_ID = 20L;
    private static final Long WINDOW_ID = 30L;
    private static final Long OFFER_ID = 40L;
    private static final Long OFFER_SKU_ID = 50L;
    private static final Long SKU_ID = 60L;

    @Mock
    private SubscriptionVisibilityService visibilityService;
    @Mock
    private SubscriptionOfferService offerService;
    @Mock
    private SubscriptionOfferSkuService offerSkuService;
    @Mock
    private SubscriptionOfferSkuIssueService offerSkuIssueService;
    @Mock
    private TradeSubscriptionOrderApi tradeSubscriptionOrderApi;
    @InjectMocks
    private SubscriptionOrderEligibilityServiceImpl eligibilityService;

    @Test
    void validateOrder_shouldKeepWarehousePrincipalInternalOnly() {
        SubscriptionWindowOfferSkuDO offerSku = offerSku();
        SubscriptionWindowOfferDO offer = offer();
        SubscriptionVisibilityResultBO visibility = visibility();
        when(offerSkuService.validateOfferSkuExists(OFFER_SKU_ID)).thenReturn(offerSku);
        when(offerService.validateOfferExists(OFFER_ID)).thenReturn(offer);
        when(visibilityService.calculate(eq(USER_ID), eq(STUDENT_ID), eq(WINDOW_ID))).thenReturn(visibility);
        when(tradeSubscriptionOrderApi.getEffectiveSubscriptionOrderItemQuantity(STUDENT_ID, OFFER_SKU_ID))
                .thenReturn(0);

        SubscriptionOrderEligibilityRespDTO result = eligibilityService.validateOrder(req());

        assertEquals(300L, result.getWarehouseId());
        assertEquals("中心仓", result.getWarehouseNameSnapshot());
        assertEquals("仓库负责人", result.getWarehousePrincipalSnapshot());
        assertNull(result.getContactName());
        assertNull(result.getContactMobile());
    }

    @Test
    void validateOrder_shouldRejectWhenMaxQuantityExceeded() {
        SubscriptionWindowOfferSkuDO offerSku = offerSku();
        SubscriptionWindowOfferDO offer = offer();
        SubscriptionVisibilityResultBO visibility = visibility();
        when(offerSkuService.validateOfferSkuExists(OFFER_SKU_ID)).thenReturn(offerSku);
        when(offerService.validateOfferExists(OFFER_ID)).thenReturn(offer);
        when(visibilityService.calculate(eq(USER_ID), eq(STUDENT_ID), eq(WINDOW_ID))).thenReturn(visibility);
        when(tradeSubscriptionOrderApi.getEffectiveSubscriptionOrderItemQuantity(STUDENT_ID, OFFER_SKU_ID))
                .thenReturn(1);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> eligibilityService.validateOrder(req()));

        assertEquals(ORDER_MAX_QUANTITY_EXCEEDED.getCode(), ex.getCode());
    }

    @Test
    void validateOrder_shouldLockAnchorWhenRequested() {
        SubscriptionWindowOfferSkuDO offerSku = offerSku();
        SubscriptionWindowOfferDO offer = offer();
        SubscriptionVisibilityResultBO visibility = visibility();
        SubscriptionOrderEligibilityReqDTO reqDTO = req();
        reqDTO.setLockAnchor(true);
        when(offerSkuService.validateOfferSkuExists(OFFER_SKU_ID)).thenReturn(offerSku);
        when(offerService.validateOfferExistsForUpdate(OFFER_ID)).thenReturn(offer);
        when(offerSkuService.validateOfferSkuExistsForUpdate(OFFER_SKU_ID)).thenReturn(offerSku);
        when(visibilityService.calculate(eq(USER_ID), eq(STUDENT_ID), eq(WINDOW_ID))).thenReturn(visibility);
        when(tradeSubscriptionOrderApi.getEffectiveSubscriptionOrderItemQuantity(STUDENT_ID, OFFER_SKU_ID))
                .thenReturn(0);

        SubscriptionOrderEligibilityRespDTO result = eligibilityService.validateOrder(reqDTO);

        assertEquals(OFFER_SKU_ID, result.getOfferSkuId());
        InOrder inOrder = inOrder(offerSkuService, offerService);
        inOrder.verify(offerSkuService).validateOfferSkuExists(OFFER_SKU_ID);
        inOrder.verify(offerService).validateOfferExistsForUpdate(OFFER_ID);
        inOrder.verify(offerSkuService).validateOfferSkuExistsForUpdate(OFFER_SKU_ID);
        verify(offerService, never()).validateOfferExists(OFFER_ID);
    }

    @Test
    void validateOrder_shouldRejectWhenOfferSkuDisabled() {
        SubscriptionWindowOfferSkuDO offerSku = offerSku();
        offerSku.setStatus(CommonStatusEnum.DISABLE.getStatus());
        when(offerSkuService.validateOfferSkuExists(OFFER_SKU_ID)).thenReturn(offerSku);
        when(offerService.validateOfferExists(OFFER_ID)).thenReturn(offer());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> eligibilityService.validateOrder(req()));

        assertEquals(ORDER_OFFER_SKU_NOT_AVAILABLE.getCode(), ex.getCode());
        verify(visibilityService, never()).calculate(any(), any(), any());
    }

    private SubscriptionOrderEligibilityReqDTO req() {
        SubscriptionOrderEligibilityReqDTO reqDTO = new SubscriptionOrderEligibilityReqDTO();
        reqDTO.setUserId(USER_ID);
        reqDTO.setStudentId(STUDENT_ID);
        reqDTO.setOfferSkuId(OFFER_SKU_ID);
        reqDTO.setSkuId(SKU_ID);
        reqDTO.setCount(1);
        return reqDTO;
    }

    private SubscriptionWindowOfferSkuDO offerSku() {
        return SubscriptionWindowOfferSkuDO.builder()
                .id(OFFER_SKU_ID)
                .offerId(OFFER_ID)
                .productSkuId(SKU_ID)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .maxQuantityPerStudent(1)
                .build();
    }

    private SubscriptionWindowOfferDO offer() {
        return SubscriptionWindowOfferDO.builder()
                .id(OFFER_ID)
                .windowId(WINDOW_ID)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build();
    }

    private SubscriptionVisibilityResultBO visibility() {
        SubscriptionVisibilityResultBO visibility = new SubscriptionVisibilityResultBO();
        visibility.setStudent(student());
        visibility.setWindow(window());

        SubscriptionVisibilityResultBO.VisibleOfferSku visibleSku = new SubscriptionVisibilityResultBO.VisibleOfferSku();
        visibleSku.setOfferSku(offerSku());
        visibleSku.setProductSku(productSku());
        visibleSku.setReason("BASE_MATCH");

        SubscriptionVisibilityResultBO.VisibleOffer visibleOffer = new SubscriptionVisibilityResultBO.VisibleOffer();
        visibleOffer.setOffer(offer());
        visibleOffer.setPublication(publication());
        visibleOffer.setSkus(List.of(visibleSku));
        visibility.setVisibleOffers(List.of(visibleOffer));
        return visibility;
    }

    private EduStudentSubscriptionContextRespDTO student() {
        EduStudentSubscriptionContextRespDTO student = new EduStudentSubscriptionContextRespDTO();
        student.setStudentId(STUDENT_ID);
        student.setStudentName("张三");
        student.setSchoolId(100L);
        student.setSchoolName("第一小学");
        student.setSchoolAddress("学校地址");
        student.setWarehouseId(300L);
        student.setWarehouseName("中心仓");
        student.setWarehouseAddress("仓库地址");
        student.setWarehousePrincipal("仓库负责人");
        return student;
    }

    private SubscriptionWindowDO window() {
        return SubscriptionWindowDO.builder()
                .id(WINDOW_ID)
                .name("2026 春季")
                .targetYearStart(2026)
                .targetYearEnd(2027)
                .build();
    }

    private ProductPublicationRespDTO publication() {
        ProductPublicationRespDTO publication = new ProductPublicationRespDTO();
        ProductPublicationRespDTO.PublicationSpuExtDTO ext = new ProductPublicationRespDTO.PublicationSpuExtDTO();
        ext.setIssueMode(PublicationIssueModeEnum.SINGLE.getCode());
        publication.setPublicationExt(ext);
        return publication;
    }

    private ProductPublicationRespDTO.PublicationSkuDTO productSku() {
        ProductPublicationRespDTO.PublicationSkuDTO sku = new ProductPublicationRespDTO.PublicationSkuDTO();
        sku.setId(SKU_ID);
        return sku;
    }

}
