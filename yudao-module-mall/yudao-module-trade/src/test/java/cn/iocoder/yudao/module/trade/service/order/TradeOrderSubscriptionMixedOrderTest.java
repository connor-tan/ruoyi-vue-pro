package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.member.api.address.MemberAddressApi;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.enums.publication.ProductDomainTypeEnum;
import cn.iocoder.yudao.module.subscription.api.order.SubscriptionOrderEligibilityApi;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementReqVO;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.service.cart.CartService;
import cn.iocoder.yudao.module.trade.service.price.TradePriceService;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import cn.iocoder.yudao.module.trade.service.price.calculator.TradePriceCalculatorHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_PUBLICATION_SUBSCRIPTION_CONTEXT_REQUIRED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_SUBSCRIPTION_LIMIT_EXCEEDED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeOrderSubscriptionMixedOrderTest {

    private TradeOrderUpdateServiceImpl service;
    private CartService cartService;
    private TradePriceService tradePriceService;
    private ProductSkuApi productSkuApi;
    private ProductSpuApi productSpuApi;
    private SubscriptionOrderEligibilityApi subscriptionOrderEligibilityApi;
    private TradeOrderItemMapper tradeOrderItemMapper;
    private MemberAddressApi addressApi;

    @BeforeEach
    void setUp() {
        service = new TradeOrderUpdateServiceImpl();
        cartService = mock(CartService.class);
        tradePriceService = mock(TradePriceService.class);
        productSkuApi = mock(ProductSkuApi.class, CALLS_REAL_METHODS);
        productSpuApi = mock(ProductSpuApi.class, CALLS_REAL_METHODS);
        subscriptionOrderEligibilityApi = mock(SubscriptionOrderEligibilityApi.class);
        tradeOrderItemMapper = mock(TradeOrderItemMapper.class);
        addressApi = mock(MemberAddressApi.class);
        ReflectionTestUtils.setField(service, "cartService", cartService);
        ReflectionTestUtils.setField(service, "tradePriceService", tradePriceService);
        ReflectionTestUtils.setField(service, "productSkuApi", productSkuApi);
        ReflectionTestUtils.setField(service, "productSpuApi", productSpuApi);
        ReflectionTestUtils.setField(service, "subscriptionOrderEligibilityApi", subscriptionOrderEligibilityApi);
        ReflectionTestUtils.setField(service, "tradeOrderItemMapper", tradeOrderItemMapper);
        ReflectionTestUtils.setField(service, "addressApi", addressApi);
    }

    @Test
    void settlementShouldAllowMixedNormalAndSubscriptionItemsWithItemSnapshot() {
        mockProductDomainData();
        when(cartService.getCartList(eq(100L), any())).thenReturn(Collections.emptyList());
        when(subscriptionOrderEligibilityApi.validateOrderItems(any())).thenReturn(List.of(eligibility(1, 2)));
        when(tradeOrderItemMapper.selectSubscriptionBoughtCount(1L, 40L,
                TradeOrderStatusEnum.CANCELED.getStatus())).thenReturn(0);
        when(tradePriceService.calculateOrderPrice(any())).thenAnswer(invocation ->
                TradePriceCalculatorHelper.buildCalculateResp(invocation.getArgument(0),
                        List.of(normalSpu(), publicationSpu()), List.of(normalSku(), publicationSku())));
        AppTradeOrderSettlementReqVO reqVO = settlementReq(
                normalItem(),
                publicationItem(1, 1L, 40L));

        service.settlementOrder(100L, reqVO);

        verify(subscriptionOrderEligibilityApi).validateOrderItems(argThat(req ->
                req.getUserId().equals(100L)
                        && req.getItems().size() == 1
                        && req.getItems().get(0).getRequestIndex().equals(1)
                        && req.getItems().get(0).getStudentId().equals(1L)
                        && req.getItems().get(0).getWindowSkuId().equals(40L)
                        && req.getItems().get(0).getSkuId().equals(200L)));
        ArgumentCaptor<TradePriceCalculateReqBO> captor = ArgumentCaptor.forClass(TradePriceCalculateReqBO.class);
        verify(tradePriceService).calculateOrderPrice(captor.capture());
        TradePriceCalculateReqBO calculateReqBO = captor.getValue();
        assertEquals(2, calculateReqBO.getItems().size());
        assertNull(calculateReqBO.getItems().get(0).getSubscriptionStudentId());
        TradePriceCalculateReqBO.Item subscriptionItem = calculateReqBO.getItems().get(1);
        assertEquals(1L, subscriptionItem.getSubscriptionStudentId());
        assertEquals(2L, subscriptionItem.getSubscriptionSchoolId());
        assertEquals(3L, subscriptionItem.getSubscriptionGradeCatalogId());
        assertEquals(10L, subscriptionItem.getSubscriptionWindowId());
        assertEquals("春季订刊", subscriptionItem.getSubscriptionWindowNameSnapshot());
        assertEquals(2026, subscriptionItem.getSubscriptionTargetYearStart());
        assertEquals(2027, subscriptionItem.getSubscriptionTargetYearEnd());
        assertEquals("SPRING", subscriptionItem.getSubscriptionTargetPeriod());
        assertEquals(20L, subscriptionItem.getSubscriptionWindowSpuId());
        assertEquals(40L, subscriptionItem.getSubscriptionWindowSkuId());
        assertEquals("INCLUDE_RULE_MATCH", subscriptionItem.getSubscriptionVisibilityReason());
        assertEquals(300L, subscriptionItem.getSubscriptionMatchedRuleId());
        assertEquals(true, subscriptionItem.getSubscriptionGradeApplicabilityOverride());
    }

    @Test
    void settlementShouldRejectPublicationSkuWithoutSubscriptionContext() {
        mockProductDomainData();
        when(cartService.getCartList(eq(100L), any())).thenReturn(Collections.emptyList());
        AppTradeOrderSettlementReqVO reqVO = settlementReq(publicationItem(0, null, null));

        ServiceException exception = assertThrows(ServiceException.class, () -> service.settlementOrder(100L, reqVO));

        assertEquals(ORDER_PUBLICATION_SUBSCRIPTION_CONTEXT_REQUIRED.getCode(), exception.getCode());
        verify(subscriptionOrderEligibilityApi, never()).validateOrderItems(any());
        verify(tradePriceService, never()).calculateOrderPrice(any());
    }

    @Test
    void settlementShouldRejectWhenSubscriptionLimitExceededByExistingOrders() {
        mockProductDomainData();
        when(cartService.getCartList(eq(100L), any())).thenReturn(Collections.emptyList());
        when(subscriptionOrderEligibilityApi.validateOrderItems(any())).thenReturn(List.of(eligibility(0, 2)));
        when(tradeOrderItemMapper.selectSubscriptionBoughtCount(1L, 40L,
                TradeOrderStatusEnum.CANCELED.getStatus())).thenReturn(1);
        AppTradeOrderSettlementReqVO reqVO = settlementReq(publicationItem(0, 1L, 40L).setCount(2));

        ServiceException exception = assertThrows(ServiceException.class, () -> service.settlementOrder(100L, reqVO));

        assertEquals(ORDER_SUBSCRIPTION_LIMIT_EXCEEDED.getCode(), exception.getCode());
        verify(tradePriceService, never()).calculateOrderPrice(any());
    }

    @Test
    void convertOrderItemShouldPersistSubscriptionSnapshotFields() {
        TradePriceCalculateRespBO.OrderItem orderItem = new TradePriceCalculateRespBO.OrderItem()
                .setSpuId(22L)
                .setSpuName("刊物商品")
                .setSkuId(200L)
                .setCount(1)
                .setPrice(200)
                .setDiscountPrice(0)
                .setPayPrice(200)
                .setSubscriptionStudentId(1L)
                .setSubscriptionSchoolId(2L)
                .setSubscriptionGradeCatalogId(3L)
                .setSubscriptionWindowId(10L)
                .setSubscriptionWindowNameSnapshot("春季订刊")
                .setSubscriptionTargetYearStart(2026)
                .setSubscriptionTargetYearEnd(2027)
                .setSubscriptionTargetPeriod("SPRING")
                .setSubscriptionWindowSpuId(20L)
                .setSubscriptionWindowSkuId(40L)
                .setSubscriptionVisibilityReason("INCLUDE_RULE_MATCH")
                .setSubscriptionMatchedRuleId(300L)
                .setSubscriptionGradeApplicabilityOverride(true);
        TradePriceCalculateRespBO calculateRespBO = new TradePriceCalculateRespBO().setItems(List.of(orderItem));
        TradeOrderDO orderDO = new TradeOrderDO().setId(900L).setUserId(100L);

        List<TradeOrderItemDO> orderItems = TradeOrderConvert.INSTANCE.convertList(orderDO, calculateRespBO);

        assertEquals(1, orderItems.size());
        TradeOrderItemDO result = orderItems.get(0);
        assertEquals(900L, result.getOrderId());
        assertEquals(100L, result.getUserId());
        assertEquals(1L, result.getSubscriptionStudentId());
        assertEquals(2L, result.getSubscriptionSchoolId());
        assertEquals(3L, result.getSubscriptionGradeCatalogId());
        assertEquals(10L, result.getSubscriptionWindowId());
        assertEquals("春季订刊", result.getSubscriptionWindowNameSnapshot());
        assertEquals(2026, result.getSubscriptionTargetYearStart());
        assertEquals(2027, result.getSubscriptionTargetYearEnd());
        assertEquals("SPRING", result.getSubscriptionTargetPeriod());
        assertEquals(20L, result.getSubscriptionWindowSpuId());
        assertEquals(40L, result.getSubscriptionWindowSkuId());
        assertEquals("INCLUDE_RULE_MATCH", result.getSubscriptionVisibilityReason());
        assertEquals(300L, result.getSubscriptionMatchedRuleId());
        assertEquals(true, result.getSubscriptionGradeApplicabilityOverride());
    }

    private void mockProductDomainData() {
        when(productSkuApi.getSkuList(anyCollection())).thenReturn(List.of(normalSku(), publicationSku()));
        when(productSpuApi.getSpuList(anyCollection())).thenReturn(List.of(normalSpu(), publicationSpu()));
    }

    private AppTradeOrderSettlementReqVO settlementReq(AppTradeOrderSettlementReqVO.Item... items) {
        return new AppTradeOrderSettlementReqVO()
                .setItems(List.of(items))
                .setPointStatus(false)
                .setDeliveryType(DeliveryTypeEnum.EXPRESS.getType());
    }

    private AppTradeOrderSettlementReqVO.Item normalItem() {
        return new AppTradeOrderSettlementReqVO.Item()
                .setSkuId(100L)
                .setCount(1);
    }

    private AppTradeOrderSettlementReqVO.Item publicationItem(Integer count, Long studentId, Long windowSkuId) {
        return new AppTradeOrderSettlementReqVO.Item()
                .setSkuId(200L)
                .setCount(count)
                .setStudentId(studentId)
                .setWindowSkuId(windowSkuId);
    }

    private SubscriptionOrderEligibilityRespDTO eligibility(Integer requestIndex, Integer maxQuantityPerStudent) {
        return new SubscriptionOrderEligibilityRespDTO()
                .setRequestIndex(requestIndex)
                .setStudentId(1L)
                .setSchoolId(2L)
                .setGradeCatalogId(3L)
                .setWindowId(10L)
                .setWindowNameSnapshot("春季订刊")
                .setTargetYearStart(2026)
                .setTargetYearEnd(2027)
                .setTargetPeriod("SPRING")
                .setWindowSpuId(20L)
                .setWindowSkuId(40L)
                .setVisibilityReason("INCLUDE_RULE_MATCH")
                .setMatchedRuleId(300L)
                .setGradeApplicabilityOverride(true)
                .setMaxQuantityPerStudent(maxQuantityPerStudent);
    }

    private ProductSkuRespDTO normalSku() {
        return new ProductSkuRespDTO()
                .setId(100L)
                .setSpuId(11L)
                .setPrice(100)
                .setStock(100);
    }

    private ProductSkuRespDTO publicationSku() {
        return new ProductSkuRespDTO()
                .setId(200L)
                .setSpuId(22L)
                .setPrice(200)
                .setStock(100);
    }

    private ProductSpuRespDTO normalSpu() {
        return new ProductSpuRespDTO()
                .setId(11L)
                .setName("普通商品")
                .setDomainType(ProductDomainTypeEnum.NORMAL.getCode())
                .setGiveIntegral(0);
    }

    private ProductSpuRespDTO publicationSpu() {
        return new ProductSpuRespDTO()
                .setId(22L)
                .setName("刊物商品")
                .setDomainType(ProductDomainTypeEnum.PUBLICATION.getCode())
                .setGiveIntegral(0);
    }

}
