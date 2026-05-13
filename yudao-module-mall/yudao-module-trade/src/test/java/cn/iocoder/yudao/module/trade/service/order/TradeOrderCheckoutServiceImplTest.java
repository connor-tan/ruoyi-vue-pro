package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.member.api.address.MemberAddressApi;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.subscription.api.order.SubscriptionOrderEligibilityApi;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderDeliveryRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.CartDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.redis.no.TradeNoRedisDAO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.cart.CartService;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderDeliveryGroupSupport;
import cn.iocoder.yudao.module.trade.service.price.TradePriceService;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeOrderCheckoutServiceImplTest {

    private static final long USER_ID = 1L;
    private static final long STUDENT_ID = 10L;
    private static final long OFFER_SKU_ID = 20L;
    private static final long PRODUCT_SKU_ID = 30L;
    private static final long PRODUCT_SPU_ID = 40L;
    private static final long NORMAL_SKU_ID = 31L;
    private static final long NORMAL_SPU_ID = 41L;
    private static final long PICK_UP_STORE_ID = 1000L;
    private static final int PRODUCT_PRICE = 1500;
    private static final int EXPRESS_DELIVERY_PRICE = 500;

    @Mock
    private TradeOrderMapper tradeOrderMapper;
    @Mock
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Mock
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Mock
    private TradeNoRedisDAO tradeNoRedisDAO;
    @Mock
    private CartService cartService;
    @Mock
    private ProductSkuApi productSkuApi;
    @Mock
    private ProductSpuApi productSpuApi;
    @Mock
    private SubscriptionOrderEligibilityApi subscriptionOrderEligibilityApi;
    @Mock
    private TradePriceService tradePriceService;
    @Mock
    private MemberAddressApi addressApi;
    @Mock
    private cn.iocoder.yudao.module.pay.api.order.PayOrderApi payOrderApi;
    @Mock
    private TradeOrderProperties tradeOrderProperties;
    @Mock
    private TradeOrderHandler tradeOrderHandler;
    @Spy
    private TradeOrderDeliveryGroupSupport deliveryGroupSupport = new TradeOrderDeliveryGroupSupport();
    @Mock
    private TradeOrderPublicationIssueService publicationIssueService;
    @InjectMocks
    private TradeOrderCheckoutServiceImpl tradeOrderCheckoutService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tradeOrderCheckoutService, "tradeOrderHandlers", List.of(tradeOrderHandler));
    }

    @Test
    void settlementOrder_shouldAggregateSameStudentOfferSkuBeforeEligibilityCheck() {
        mockNoDefaultAddress();
        mockProducts(Map.of(PRODUCT_SKU_ID, productSku(PRODUCT_SKU_ID, PRODUCT_SPU_ID)),
                Map.of(PRODUCT_SPU_ID, publicationSpu(List.of(DeliveryTypeEnum.EXPRESS.getType()))));
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

    @Test
    void settlementOrder_shouldAllowPublicationStationWhenSkuSupportsExpressAndStation() {
        mockNoDefaultAddress();
        mockProducts(Map.of(PRODUCT_SKU_ID, productSku(PRODUCT_SKU_ID, PRODUCT_SPU_ID)),
                Map.of(PRODUCT_SPU_ID, publicationSpu(List.of(
                        DeliveryTypeEnum.EXPRESS.getType(), DeliveryTypeEnum.STATION.getType()))));
        when(subscriptionOrderEligibilityApi.validateOrder(any())).thenReturn(eligibility());
        mockCalculateOrderPrice();

        AppTradeOrderSettlementReqVO reqVO = new AppTradeOrderSettlementReqVO();
        reqVO.setPointStatus(false);
        reqVO.setItems(List.of(publicationItem(DeliveryTypeEnum.STATION.getType())));
        AppTradeOrderSettlementRespVO respVO = tradeOrderCheckoutService.settlementOrder(USER_ID, reqVO);

        assertEquals(1, respVO.getDeliveries().size());
        assertEquals(DeliveryTypeEnum.STATION.getType(), respVO.getDeliveries().get(0).getDeliveryType());
        assertEquals(300L, respVO.getDeliveries().get(0).getStationId());
        assertEquals(0, respVO.getPrice().getDeliveryPrice());
    }

    @Test
    void settlementOrder_shouldAllowStationPublicationAndPickUpNormalMixed() {
        mockNoDefaultAddress();
        mockProducts(Map.of(
                        PRODUCT_SKU_ID, productSku(PRODUCT_SKU_ID, PRODUCT_SPU_ID),
                        NORMAL_SKU_ID, productSku(NORMAL_SKU_ID, NORMAL_SPU_ID)),
                Map.of(
                        PRODUCT_SPU_ID, publicationSpu(List.of(DeliveryTypeEnum.STATION.getType())),
                        NORMAL_SPU_ID, normalSpu(List.of(DeliveryTypeEnum.PICK_UP.getType()))));
        when(subscriptionOrderEligibilityApi.validateOrder(any())).thenReturn(eligibility());
        mockCalculateOrderPrice();

        AppTradeOrderSettlementReqVO reqVO = new AppTradeOrderSettlementReqVO();
        reqVO.setPointStatus(false);
        reqVO.setPickUpStoreId(PICK_UP_STORE_ID);
        reqVO.setReceiverName("自提人");
        reqVO.setReceiverMobile("13900001111");
        reqVO.setItems(List.of(
                publicationItem(DeliveryTypeEnum.STATION.getType()),
                normalItem(DeliveryTypeEnum.PICK_UP.getType())));
        AppTradeOrderSettlementRespVO respVO = tradeOrderCheckoutService.settlementOrder(USER_ID, reqVO);

        assertEquals(2, respVO.getDeliveries().size());
        assertEquals(Set.of(DeliveryTypeEnum.STATION.getType(), DeliveryTypeEnum.PICK_UP.getType()),
                Set.copyOf(respVO.getDeliveries().stream()
                        .map(AppTradeOrderDeliveryRespVO::getDeliveryType).toList()));
        AppTradeOrderDeliveryRespVO pickUpDelivery = respVO.getDeliveries().stream()
                .filter(delivery -> DeliveryTypeEnum.PICK_UP.getType().equals(delivery.getDeliveryType()))
                .findFirst().orElseThrow();
        assertEquals(PICK_UP_STORE_ID, pickUpDelivery.getPickUpStoreId());
        assertEquals("自提人", pickUpDelivery.getReceiverName());
        assertEquals("13900001111", pickUpDelivery.getReceiverMobile());
        assertEquals(0, respVO.getPrice().getDeliveryPrice());
    }

    @Test
    void createOrder_shouldPersistPickUpFactsOnDeliveryGroupForMixedOrder() {
        mockNoDefaultAddress();
        mockProducts(Map.of(
                        PRODUCT_SKU_ID, productSku(PRODUCT_SKU_ID, PRODUCT_SPU_ID),
                        NORMAL_SKU_ID, productSku(NORMAL_SKU_ID, NORMAL_SPU_ID)),
                Map.of(
                        PRODUCT_SPU_ID, publicationSpu(List.of(DeliveryTypeEnum.STATION.getType())),
                        NORMAL_SPU_ID, normalSpu(List.of(DeliveryTypeEnum.PICK_UP.getType()))));
        when(subscriptionOrderEligibilityApi.validateOrder(any())).thenReturn(eligibility());
        mockCalculateOrderPrice();
        when(tradeNoRedisDAO.generate(anyString())).thenReturn("NO202605070001");
        when(tradeOrderMapper.insert(any(TradeOrderDO.class))).thenAnswer(invocation -> {
            TradeOrderDO order = invocation.getArgument(0);
            order.setId(1L);
            return 1;
        });
        when(tradeOrderDeliveryMapper.insert(any(TradeOrderDeliveryDO.class))).thenAnswer(invocation -> {
            TradeOrderDeliveryDO delivery = invocation.getArgument(0);
            delivery.setId(100L + delivery.getDeliveryType());
            return 1;
        });
        when(tradeOrderProperties.getPayAppKey()).thenReturn("mall");
        when(tradeOrderProperties.getPayExpireTime()).thenReturn(Duration.ofMinutes(30));
        when(payOrderApi.createOrder(any())).thenReturn(99L);

        AppTradeOrderCreateReqVO reqVO = new AppTradeOrderCreateReqVO();
        reqVO.setPointStatus(false);
        reqVO.setPickUpStoreId(PICK_UP_STORE_ID);
        reqVO.setReceiverName("自提人");
        reqVO.setReceiverMobile("13900001111");
        reqVO.setItems(List.of(
                publicationItem(DeliveryTypeEnum.STATION.getType()),
                normalItem(DeliveryTypeEnum.PICK_UP.getType())));
        TradeOrderDO order = tradeOrderCheckoutService.createOrder(USER_ID, reqVO);

        assertEquals(DeliveryTypeEnum.MIXED.getType(), order.getDeliveryType());
        verify(tradeOrderDeliveryMapper, times(2)).insert(any(TradeOrderDeliveryDO.class));
        ArgumentCaptor<TradeOrderDeliveryDO> deliveryCaptor = ArgumentCaptor.forClass(TradeOrderDeliveryDO.class);
        verify(tradeOrderDeliveryMapper, times(2)).insert(deliveryCaptor.capture());
        TradeOrderDeliveryDO pickUpDelivery = deliveryCaptor.getAllValues().stream()
                .filter(delivery -> DeliveryTypeEnum.PICK_UP.getType().equals(delivery.getDeliveryType()))
                .findFirst().orElseThrow();
        assertEquals(PICK_UP_STORE_ID, pickUpDelivery.getPickUpStoreId());
        assertEquals("自提人", pickUpDelivery.getReceiverName());
        assertEquals("13900001111", pickUpDelivery.getReceiverMobile());
        assertTrue(pickUpDelivery.getPickUpVerifyCode().matches("\\d{8}"));
        assertEquals(TradeOrderStatusEnum.UNPAID.getStatus(), pickUpDelivery.getStatus());
    }

    @Test
    void settlementOrder_shouldRejectUnsupportedItemDeliveryType() {
        mockNoDefaultAddress();
        mockProducts(Map.of(PRODUCT_SKU_ID, productSku(PRODUCT_SKU_ID, PRODUCT_SPU_ID)),
                Map.of(PRODUCT_SPU_ID, publicationSpu(List.of(DeliveryTypeEnum.EXPRESS.getType()))));

        AppTradeOrderSettlementReqVO reqVO = new AppTradeOrderSettlementReqVO();
        reqVO.setPointStatus(false);
        reqVO.setItems(List.of(publicationItem(DeliveryTypeEnum.STATION.getType())));

        assertThrows(ServiceException.class,
                () -> tradeOrderCheckoutService.settlementOrder(USER_ID, reqVO));
        verify(subscriptionOrderEligibilityApi, never()).validateOrder(any());
    }

    @Test
    void settlementOrder_shouldCalculateExpressDeliveryOnlyForExpressGroup() {
        mockNoDefaultAddress();
        mockProducts(Map.of(
                        PRODUCT_SKU_ID, productSku(PRODUCT_SKU_ID, PRODUCT_SPU_ID),
                        NORMAL_SKU_ID, productSku(NORMAL_SKU_ID, NORMAL_SPU_ID)),
                Map.of(
                        PRODUCT_SPU_ID, publicationSpu(List.of(DeliveryTypeEnum.STATION.getType())),
                        NORMAL_SPU_ID, normalSpu(List.of(DeliveryTypeEnum.EXPRESS.getType()))));
        when(subscriptionOrderEligibilityApi.validateOrder(any())).thenReturn(eligibility());
        mockCalculateOrderPrice();

        AppTradeOrderSettlementReqVO reqVO = new AppTradeOrderSettlementReqVO();
        reqVO.setPointStatus(false);
        reqVO.setItems(List.of(
                publicationItem(DeliveryTypeEnum.STATION.getType()),
                normalItem(DeliveryTypeEnum.EXPRESS.getType())));
        AppTradeOrderSettlementRespVO respVO = tradeOrderCheckoutService.settlementOrder(USER_ID, reqVO);

        assertEquals(2, respVO.getDeliveries().size());
        assertEquals(EXPRESS_DELIVERY_PRICE, respVO.getPrice().getDeliveryPrice());
        assertTrue(respVO.getDeliveries().stream()
                .filter(delivery -> DeliveryTypeEnum.STATION.getType().equals(delivery.getDeliveryType()))
                .allMatch(delivery -> delivery.getDeliveryPrice() == 0));
    }

    private AppTradeOrderSettlementReqVO settlementReq() {
        AppTradeOrderSettlementReqVO reqVO = new AppTradeOrderSettlementReqVO();
        reqVO.setPointStatus(false);
        reqVO.setItems(List.of(publicationItem(DeliveryTypeEnum.EXPRESS.getType()),
                publicationItem(DeliveryTypeEnum.EXPRESS.getType())));
        return reqVO;
    }

    private AppTradeOrderSettlementReqVO.Item publicationItem(Integer deliveryType) {
        AppTradeOrderSettlementReqVO.Item item = new AppTradeOrderSettlementReqVO.Item();
        item.setSkuId(PRODUCT_SKU_ID);
        item.setCount(1);
        item.setStudentId(STUDENT_ID);
        item.setOfferSkuId(OFFER_SKU_ID);
        item.setDeliveryType(deliveryType);
        return item;
    }

    private AppTradeOrderSettlementReqVO.Item normalItem(Integer deliveryType) {
        AppTradeOrderSettlementReqVO.Item item = new AppTradeOrderSettlementReqVO.Item();
        item.setSkuId(NORMAL_SKU_ID);
        item.setCount(1);
        item.setDeliveryType(deliveryType);
        return item;
    }

    private ProductSkuRespDTO productSku(Long skuId, Long spuId) {
        ProductSkuRespDTO sku = new ProductSkuRespDTO();
        sku.setId(skuId);
        sku.setSpuId(spuId);
        sku.setPrice(PRODUCT_PRICE);
        sku.setStock(100);
        return sku;
    }

    private ProductSpuRespDTO publicationSpu(List<Integer> deliveryTypes) {
        return spu(PRODUCT_SPU_ID, BizSceneEnum.PUBLICATION.getCode(), deliveryTypes);
    }

    private ProductSpuRespDTO normalSpu(List<Integer> deliveryTypes) {
        return spu(NORMAL_SPU_ID, BizSceneEnum.NORMAL.getCode(), deliveryTypes);
    }

    private ProductSpuRespDTO spu(Long spuId, String bizScene, List<Integer> deliveryTypes) {
        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setId(spuId);
        spu.setName("测试商品" + spuId);
        spu.setBizScene(bizScene);
        spu.setStatus(ProductSpuStatusEnum.ENABLE.getStatus());
        spu.setDeliveryTypes(deliveryTypes);
        spu.setDeliveryTemplateId(deliveryTypes.contains(DeliveryTypeEnum.EXPRESS.getType()) ? 1L : null);
        return spu;
    }

    private SubscriptionOrderEligibilityRespDTO eligibility() {
        SubscriptionOrderEligibilityRespDTO respDTO = new SubscriptionOrderEligibilityRespDTO();
        respDTO.setStudentId(STUDENT_ID);
        respDTO.setStudentNameSnapshot("测试学生");
        respDTO.setSchoolId(200L);
        respDTO.setSchoolNameSnapshot("测试学校");
        respDTO.setClassId(201L);
        respDTO.setClassNameSnapshot("一年级1班");
        respDTO.setGradeCatalogId(202L);
        respDTO.setGradeNameSnapshot("一年级");
        respDTO.setStationId(300L);
        respDTO.setStationNameSnapshot("测试站点");
        respDTO.setStationAddressSnapshot("测试地址");
        respDTO.setContactName("张老师");
        respDTO.setContactMobile("13900000000");
        respDTO.setOfferSkuId(OFFER_SKU_ID);
        respDTO.setOfferId(100L);
        respDTO.setWindowId(200L);
        return respDTO;
    }

    private void mockNoDefaultAddress() {
        when(addressApi.getDefaultAddress(USER_ID)).thenReturn(null);
        when(cartService.getCartList(eq(USER_ID), anySet())).thenReturn(Collections.emptyList());
    }

    private void mockProducts(Map<Long, ProductSkuRespDTO> skuMap, Map<Long, ProductSpuRespDTO> spuMap) {
        when(productSkuApi.getSkuMap(anySet())).thenReturn(skuMap);
        when(productSpuApi.getSpuMap(anySet())).thenReturn(spuMap);
    }

    private void mockCalculateOrderPrice() {
        when(tradePriceService.calculateOrderPrice(any())).thenAnswer(invocation -> {
            TradePriceCalculateReqBO reqBO = invocation.getArgument(0);
            return buildCalculateResp(reqBO);
        });
    }

    private TradePriceCalculateRespBO buildCalculateResp(TradePriceCalculateReqBO reqBO) {
        List<TradePriceCalculateRespBO.OrderItem> items = reqBO.getItems().stream()
                .map(this::buildCalculateItem)
                .toList();
        TradePriceCalculateRespBO respBO = new TradePriceCalculateRespBO()
                .setType(0)
                .setItems(items)
                .setPromotions(Collections.emptyList())
                .setCoupons(Collections.emptyList())
                .setTotalPoint(0)
                .setUsePoint(0)
                .setGivePoint(0)
                .setFreeDelivery(false)
                .setGiveCouponTemplateCounts(Collections.emptyMap());
        int totalPrice = items.stream().mapToInt(item -> item.getPrice() * item.getCount()).sum();
        int deliveryPrice = items.stream().mapToInt(TradePriceCalculateRespBO.OrderItem::getDeliveryPrice).sum();
        respBO.setPrice(new TradePriceCalculateRespBO.Price()
                .setTotalPrice(totalPrice)
                .setDiscountPrice(0)
                .setDeliveryPrice(deliveryPrice)
                .setCouponPrice(0)
                .setPointPrice(0)
                .setVipPrice(0)
                .setPayPrice(totalPrice + deliveryPrice));
        return respBO;
    }

    private TradePriceCalculateRespBO.OrderItem buildCalculateItem(TradePriceCalculateReqBO.Item item) {
        boolean publication = PRODUCT_SKU_ID == item.getSkuId();
        Integer deliveryPrice = DeliveryTypeEnum.EXPRESS.getType().equals(item.getDeliveryType())
                ? EXPRESS_DELIVERY_PRICE : 0;
        return new TradePriceCalculateRespBO.OrderItem()
                .setSkuId(item.getSkuId())
                .setSpuId(publication ? PRODUCT_SPU_ID : NORMAL_SPU_ID)
                .setCount(item.getCount())
                .setCartId(item.getCartId())
                .setSelected(Boolean.TRUE)
                .setPrice(PRODUCT_PRICE)
                .setDiscountPrice(0)
                .setCouponPrice(0)
                .setPointPrice(0)
                .setVipPrice(0)
                .setDeliveryPrice(deliveryPrice)
                .setPayPrice(PRODUCT_PRICE * item.getCount() + deliveryPrice)
                .setSpuName(publication ? "测试刊物" : "测试普通商品")
                .setBizScene(publication ? BizSceneEnum.PUBLICATION.getCode() : BizSceneEnum.NORMAL.getCode())
                .setDeliveryTypes(publication
                        ? List.of(DeliveryTypeEnum.EXPRESS.getType(), DeliveryTypeEnum.STATION.getType())
                        : List.of(DeliveryTypeEnum.EXPRESS.getType(), DeliveryTypeEnum.PICK_UP.getType()))
                .setDeliveryTemplateId(DeliveryTypeEnum.EXPRESS.getType().equals(item.getDeliveryType()) ? 1L : null)
                .setResolvedDeliveryType(item.getDeliveryType())
                .setSubscriptionStudentId(item.getSubscriptionStudentId())
                .setSubscriptionStudentNameSnapshot(item.getSubscriptionStudentNameSnapshot())
                .setSubscriptionSchoolId(item.getSubscriptionSchoolId())
                .setSubscriptionSchoolNameSnapshot(item.getSubscriptionSchoolNameSnapshot())
                .setSubscriptionClassId(item.getSubscriptionClassId())
                .setSubscriptionClassNameSnapshot(item.getSubscriptionClassNameSnapshot())
                .setSubscriptionGradeCatalogId(item.getSubscriptionGradeCatalogId())
                .setSubscriptionGradeNameSnapshot(item.getSubscriptionGradeNameSnapshot())
                .setSubscriptionStationId(item.getSubscriptionStationId())
                .setSubscriptionStationNameSnapshot(item.getSubscriptionStationNameSnapshot())
                .setSubscriptionStationAddressSnapshot(item.getSubscriptionStationAddressSnapshot())
                .setSubscriptionContactName(item.getSubscriptionContactName())
                .setSubscriptionContactMobile(item.getSubscriptionContactMobile())
                .setSubscriptionWindowId(item.getSubscriptionWindowId())
                .setSubscriptionOfferId(item.getSubscriptionOfferId())
                .setSubscriptionOfferSkuId(item.getSubscriptionOfferSkuId());
    }

}
