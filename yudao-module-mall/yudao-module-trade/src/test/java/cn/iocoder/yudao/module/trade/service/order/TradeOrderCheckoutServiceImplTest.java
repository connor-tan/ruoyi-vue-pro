package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.member.api.address.MemberAddressApi;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.subscription.api.order.SubscriptionOrderEligibilityApi;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.CartDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.service.cart.CartService;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderDeliveryGroupSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeOrderCheckoutServiceImplTest {

    private static final long USER_ID = 1L;
    private static final long STUDENT_ID = 10L;
    private static final long OFFER_SKU_ID = 20L;
    private static final long PRODUCT_SKU_ID = 30L;
    private static final long PRODUCT_SPU_ID = 40L;

    @Mock
    private CartService cartService;
    @Mock
    private ProductSkuApi productSkuApi;
    @Mock
    private ProductSpuApi productSpuApi;
    @Mock
    private SubscriptionOrderEligibilityApi subscriptionOrderEligibilityApi;
    @Mock
    private MemberAddressApi addressApi;
    @Spy
    private TradeOrderDeliveryGroupSupport deliveryGroupSupport = new TradeOrderDeliveryGroupSupport();
    @InjectMocks
    private TradeOrderCheckoutServiceImpl tradeOrderCheckoutService;

    @Test
    void settlementOrder_shouldAggregateSameStudentOfferSkuBeforeEligibilityCheck() {
        when(addressApi.getDefaultAddress(USER_ID)).thenReturn(null);
        when(cartService.getCartList(eq(USER_ID), anySet())).thenReturn(Collections.emptyList());
        when(productSkuApi.getSkuMap(eq(Set.of(PRODUCT_SKU_ID)))).thenReturn(Map.of(PRODUCT_SKU_ID, productSku()));
        when(productSpuApi.getSpuMap(eq(Set.of(PRODUCT_SPU_ID)))).thenReturn(Map.of(PRODUCT_SPU_ID, publicationSpu()));
        when(subscriptionOrderEligibilityApi.validateOrder(any())).thenAnswer(invocation -> {
            SubscriptionOrderEligibilityReqDTO reqDTO = invocation.getArgument(0);
            if (reqDTO.getCount() > 1) {
                throw new IllegalStateException("limit exceeded");
            }
            return eligibility();
        });

        assertThrows(IllegalStateException.class,
                () -> tradeOrderCheckoutService.settlementOrder(USER_ID, settlementReq()));

        ArgumentCaptor<SubscriptionOrderEligibilityReqDTO> captor =
                ArgumentCaptor.forClass(SubscriptionOrderEligibilityReqDTO.class);
        verify(subscriptionOrderEligibilityApi, times(2)).validateOrder(captor.capture());
        assertEquals(List.of(1, 2), captor.getAllValues().stream()
                .map(SubscriptionOrderEligibilityReqDTO::getCount).toList());
    }

    private AppTradeOrderSettlementReqVO settlementReq() {
        AppTradeOrderSettlementReqVO reqVO = new AppTradeOrderSettlementReqVO();
        reqVO.setPointStatus(false);
        reqVO.setItems(List.of(publicationItem(), publicationItem()));
        return reqVO;
    }

    private AppTradeOrderSettlementReqVO.Item publicationItem() {
        AppTradeOrderSettlementReqVO.Item item = new AppTradeOrderSettlementReqVO.Item();
        item.setSkuId(PRODUCT_SKU_ID);
        item.setCount(1);
        item.setStudentId(STUDENT_ID);
        item.setOfferSkuId(OFFER_SKU_ID);
        item.setDeliveryType(DeliveryTypeEnum.EXPRESS.getType());
        return item;
    }

    private ProductSkuRespDTO productSku() {
        ProductSkuRespDTO sku = new ProductSkuRespDTO();
        sku.setId(PRODUCT_SKU_ID);
        sku.setSpuId(PRODUCT_SPU_ID);
        return sku;
    }

    private ProductSpuRespDTO publicationSpu() {
        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setId(PRODUCT_SPU_ID);
        spu.setBizScene(BizSceneEnum.PUBLICATION.getCode());
        return spu;
    }

    private SubscriptionOrderEligibilityRespDTO eligibility() {
        SubscriptionOrderEligibilityRespDTO respDTO = new SubscriptionOrderEligibilityRespDTO();
        respDTO.setStudentId(STUDENT_ID);
        respDTO.setOfferSkuId(OFFER_SKU_ID);
        respDTO.setOfferId(100L);
        respDTO.setWindowId(200L);
        return respDTO;
    }

}
