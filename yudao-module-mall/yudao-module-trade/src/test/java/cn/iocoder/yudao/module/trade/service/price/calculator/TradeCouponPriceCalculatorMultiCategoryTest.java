package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.product.api.category.ProductCategoryApi;
import cn.iocoder.yudao.module.promotion.api.coupon.CouponApi;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TradeCouponPriceCalculatorMultiCategoryTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TradeCouponPriceCalculator tradeCouponPriceCalculator;

    @Mock
    private CouponApi couponApi;
    @Mock
    private ProductCategoryApi productCategoryApi;

    @Test
    void calculate_shouldMatchParentCategoryCouponByOrderItemLeafCategory() {
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setUserId(1L)
                .setCouponId(10L);
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO()
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setItems(List.of(new TradePriceCalculateRespBO.OrderItem()
                        .setSpuId(20L)
                        .setSkuId(30L)
                        .setCount(1)
                        .setSelected(true)
                        .setPrice(1000)
                        .setCategoryIds(List.of(101L))));
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);
        CouponRespDTO coupon = new CouponRespDTO()
                .setId(10L)
                .setName("父分类券")
                .setStatus(CouponStatusEnum.UNUSED.getStatus())
                .setUsePrice(0)
                .setValidStartTime(LocalDateTime.now().minusDays(1))
                .setValidEndTime(LocalDateTime.now().plusDays(1))
                .setProductScope(PromotionProductScopeEnum.CATEGORY.getScope())
                .setProductScopeValues(List.of(100L))
                .setDiscountType(PromotionDiscountTypeEnum.PRICE.getType())
                .setDiscountPrice(100);
        when(couponApi.getCouponListByUserId(1L, CouponStatusEnum.UNUSED.getStatus()))
                .thenReturn(new ArrayList<>(List.of(coupon)));
        when(productCategoryApi.getSelfAndAncestorCategoryIds(List.of(101L)))
                .thenReturn(Set.of(101L, 100L));

        tradeCouponPriceCalculator.calculate(param, result);

        assertEquals(10L, result.getCouponId());
        assertEquals(100, result.getPrice().getCouponPrice());
        assertEquals(900, result.getPrice().getPayPrice());
        assertEquals(100, result.getItems().get(0).getCouponPrice());
        assertEquals(900, result.getItems().get(0).getPayPrice());
        verify(productCategoryApi, times(2)).getSelfAndAncestorCategoryIds(List.of(101L));
    }

}
