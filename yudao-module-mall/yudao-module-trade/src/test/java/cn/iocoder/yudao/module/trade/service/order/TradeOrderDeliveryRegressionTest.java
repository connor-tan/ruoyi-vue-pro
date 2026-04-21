package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.edu.api.station.EduStationApi;
import cn.iocoder.yudao.module.edu.api.station.dto.EduSchoolStationRespDTO;
import cn.iocoder.yudao.module.member.api.address.MemberAddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.MemberAddressRespDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.product.api.comment.ProductCommentApi;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.enums.publication.ProductDomainTypeEnum;
import cn.iocoder.yudao.module.subscription.api.order.SubscriptionOrderEligibilityApi;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderStationDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.redis.no.TradeNoRedisDAO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.cart.CartService;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryPickUpStoreService;
import cn.iocoder.yudao.module.trade.service.message.TradeMessageService;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import cn.iocoder.yudao.module.trade.service.price.TradePriceService;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import cn.iocoder.yudao.module.trade.service.price.calculator.TradePriceCalculatorHelper;
import cn.iocoder.yudao.module.promotion.api.combination.CombinationRecordApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_RECEIVE_FAIL_SPLIT_DELIVERY_REQUIRED;

@Import(TradeOrderUpdateServiceImpl.class)
class TradeOrderDeliveryRegressionTest extends BaseDbUnitTest {

    @Resource
    private TradeOrderUpdateServiceImpl tradeOrderUpdateService;

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Resource
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;

    @MockitoBean
    private TradeNoRedisDAO tradeNoRedisDAO;
    @MockitoBean
    private List<TradeOrderHandler> tradeOrderHandlers;
    @MockitoBean
    private CartService cartService;
    @MockitoBean
    private TradePriceService tradePriceService;
    @MockitoBean
    private DeliveryExpressService deliveryExpressService;
    @MockitoBean
    private TradeMessageService tradeMessageService;
    @MockitoBean
    private DeliveryPickUpStoreService pickUpStoreService;
    @MockitoBean
    private PayOrderApi payOrderApi;
    @MockitoBean
    private MemberAddressApi addressApi;
    @MockitoBean
    private ProductCommentApi productCommentApi;
    @MockitoBean
    private ProductSkuApi productSkuApi;
    @MockitoBean
    private ProductSpuApi productSpuApi;
    @MockitoBean
    private SubscriptionOrderEligibilityApi subscriptionOrderEligibilityApi;
    @MockitoBean
    private EduStationApi eduStationApi;
    @MockitoBean
    private SocialClientApi socialClientApi;
    @MockitoBean
    private PayRefundApi payRefundApi;
    @MockitoBean
    private CombinationRecordApi combinationRecordApi;
    @MockitoBean
    private TradeOrderProperties tradeOrderProperties;
    @MockitoBean
    private NotifyMessageSendApi notifyMessageSendApi;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("terminal", TerminalEnum.APP.getTerminal());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(tradeNoRedisDAO.generate(anyString())).thenReturn("ORDER-NO-1");
        when(tradeOrderProperties.getPayAppKey()).thenReturn("mall");
        when(tradeOrderProperties.getPayExpireTime()).thenReturn(Duration.ofDays(1));
        when(cartService.getCartList(any(), any())).thenReturn(Collections.emptyList());
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void createOrder_shouldCreateDeliveriesAndBindOrderItems() {
        mockMixedOrderBase();
        when(addressApi.getAddress(eq(1L), eq(100L))).thenReturn(buildAddress());
        when(payOrderApi.createOrder(any())).thenReturn(9000L);
        when(tradePriceService.calculateOrderPrice(any())).thenAnswer(invocation ->
                buildMixedCalculateResp(invocation.getArgument(0)));

        AppTradeOrderCreateReqVO reqVO = new AppTradeOrderCreateReqVO();
        reqVO.setAddressId(1L);
        reqVO.setPointStatus(false);
        reqVO.setDeliveryType(DeliveryTypeEnum.EXPRESS.getType());
        reqVO.setItems(List.of(normalItem(), publicationItem()));

        TradeOrderDO order = tradeOrderUpdateService.createOrder(100L, reqVO);

        TradeOrderDO dbOrder = tradeOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(DeliveryTypeEnum.MIXED.getType(), dbOrder.getDeliveryType());
        assertEquals(9000L, dbOrder.getPayOrderId());
        assertEquals("收件人", dbOrder.getReceiverName());

        List<TradeOrderDeliveryDO> deliveries = tradeOrderDeliveryMapper.selectListByOrderId(order.getId());
        assertEquals(2, deliveries.size());
        TradeOrderDeliveryDO expressDelivery = deliveries.stream()
                .filter(item -> DeliveryTypeEnum.EXPRESS.getType().equals(item.getDeliveryType()))
                .findFirst().orElseThrow();
        TradeOrderDeliveryDO stationDelivery = deliveries.stream()
                .filter(item -> DeliveryTypeEnum.STATION.getType().equals(item.getDeliveryType()))
                .findFirst().orElseThrow();
        assertEquals("未来小学", stationDelivery.getSchoolNameSnapshot());
        assertEquals("站点 A", stationDelivery.getStationNameSnapshot());

        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(order.getId());
        assertEquals(2, orderItems.size());
        TradeOrderItemDO normalOrderItem = orderItems.stream()
                .filter(item -> item.getSubscriptionWindowSkuId() == null)
                .findFirst().orElseThrow();
        TradeOrderItemDO publicationOrderItem = orderItems.stream()
                .filter(item -> item.getSubscriptionWindowSkuId() != null)
                .findFirst().orElseThrow();
        assertEquals(expressDelivery.getId(), normalOrderItem.getDeliveryId());
        assertEquals(stationDelivery.getId(), publicationOrderItem.getDeliveryId());
        assertEquals("学生 1", publicationOrderItem.getSubscriptionStudentNameSnapshot());
        assertEquals("未来小学", publicationOrderItem.getSubscriptionSchoolNameSnapshot());
        assertEquals(4L, publicationOrderItem.getSubscriptionClassId());
        assertEquals("2026级三年级1班", publicationOrderItem.getSubscriptionClassNameSnapshot());
        assertEquals("三年级", publicationOrderItem.getSubscriptionGradeNameSnapshot());
    }

    @Test
    void updateOrderPaid_shouldPromoteChildDeliveriesToUndelivered() {
        TradeOrderDO order = buildBaseOrder(TradeOrderStatusEnum.UNPAID.getStatus(),
                DeliveryTypeEnum.MIXED.getType(), 200, false, 10L);
        tradeOrderMapper.insert(order);
        tradeOrderDeliveryMapper.insert(buildDelivery(order.getId(), DeliveryTypeEnum.EXPRESS.getType(),
                TradeOrderStatusEnum.UNPAID.getStatus()));
        tradeOrderDeliveryMapper.insert(buildDelivery(order.getId(), DeliveryTypeEnum.STATION.getType(),
                TradeOrderStatusEnum.UNPAID.getStatus()));
        when(payOrderApi.getOrder(eq(10L))).thenReturn(new PayOrderRespDTO()
                .setId(10L).setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                .setMerchantOrderId(order.getId().toString()).setChannelCode("wx_lite").setPrice(200));

        tradeOrderUpdateService.updateOrderPaid(order.getId(), 10L);

        TradeOrderDO dbOrder = tradeOrderMapper.selectById(order.getId());
        assertEquals(TradeOrderStatusEnum.UNDELIVERED.getStatus(), dbOrder.getStatus());
        assertTrue(dbOrder.getPayStatus());
        List<TradeOrderDeliveryDO> deliveries = tradeOrderDeliveryMapper.selectListByOrderId(order.getId());
        assertTrue(CollUtil.isNotEmpty(deliveries));
        assertTrue(deliveries.stream().allMatch(item ->
                TradeOrderStatusEnum.UNDELIVERED.getStatus().equals(item.getStatus())));
    }

    @Test
    void mixedDeliveries_shouldAggregateStatusAcrossDeliveryLifecycle() {
        TradeOrderDO order = buildBaseOrder(TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                DeliveryTypeEnum.MIXED.getType(), 300, true, 11L);
        tradeOrderMapper.insert(order);
        order.setReceiverName("收件人").setReceiverMobile("13800000000").setReceiverDetailAddress("测试地址");
        tradeOrderMapper.updateById(order);

        TradeOrderDeliveryDO expressDelivery = buildDelivery(order.getId(), DeliveryTypeEnum.EXPRESS.getType(),
                TradeOrderStatusEnum.UNDELIVERED.getStatus());
        expressDelivery.setReceiverName("收件人");
        expressDelivery.setReceiverMobile("13800000000");
        expressDelivery.setReceiverAreaId(110101);
        expressDelivery.setReceiverDetailAddress("测试地址");
        tradeOrderDeliveryMapper.insert(expressDelivery);

        TradeOrderDeliveryDO stationDelivery = buildDelivery(order.getId(), DeliveryTypeEnum.STATION.getType(),
                TradeOrderStatusEnum.UNDELIVERED.getStatus());
        stationDelivery.setSchoolId(2L);
        stationDelivery.setSchoolNameSnapshot("未来小学");
        stationDelivery.setStationId(9L);
        stationDelivery.setStationNameSnapshot("站点 A");
        stationDelivery.setStationAddressSnapshot("示例路 1 号");
        tradeOrderDeliveryMapper.insert(stationDelivery);

        when(deliveryExpressService.validateDeliveryExpress(eq(1L)))
                .thenReturn(new DeliveryExpressDO().setId(1L).setName("顺丰").setCode("SF"));

        tradeOrderUpdateService.deliveryOrder(new TradeOrderDeliveryReqVO()
                .setDeliveryId(expressDelivery.getId())
                .setLogisticsId(1L)
                .setLogisticsNo("SF123"));

        TradeOrderDO afterExpressDelivered = tradeOrderMapper.selectById(order.getId());
        assertEquals(TradeOrderStatusEnum.UNDELIVERED.getStatus(), afterExpressDelivered.getStatus());
        assertEquals(TradeOrderStatusEnum.DELIVERED.getStatus(),
                tradeOrderDeliveryMapper.selectById(expressDelivery.getId()).getStatus());

        tradeOrderUpdateService.stationDeliveryOrder(new TradeOrderStationDeliveryReqVO()
                .setDeliveryId(stationDelivery.getId()));

        TradeOrderDO afterAllDelivered = tradeOrderMapper.selectById(order.getId());
        assertEquals(TradeOrderStatusEnum.DELIVERED.getStatus(), afterAllDelivered.getStatus());
        assertEquals(TradeOrderStatusEnum.DELIVERED.getStatus(),
                tradeOrderDeliveryMapper.selectById(stationDelivery.getId()).getStatus());

        tradeOrderUpdateService.receiveDeliveryByMember(order.getUserId(), expressDelivery.getId());
        TradeOrderDO afterReceiveExpress = tradeOrderMapper.selectById(order.getId());
        assertEquals(TradeOrderStatusEnum.DELIVERED.getStatus(), afterReceiveExpress.getStatus());
        assertEquals(TradeOrderStatusEnum.COMPLETED.getStatus(),
                tradeOrderDeliveryMapper.selectById(expressDelivery.getId()).getStatus());

        tradeOrderUpdateService.receiveDeliveryByMember(order.getUserId(), stationDelivery.getId());
        TradeOrderDO completedOrder = tradeOrderMapper.selectById(order.getId());
        assertEquals(TradeOrderStatusEnum.COMPLETED.getStatus(), completedOrder.getStatus());
        assertNotNull(completedOrder.getReceiveTime());
    }

    @Test
    void receiveOrderByMember_shouldRejectWhenOrderHasSplitDeliveries() {
        TradeOrderDO order = buildBaseOrder(TradeOrderStatusEnum.DELIVERED.getStatus(),
                DeliveryTypeEnum.MIXED.getType(), 300, true, 12L);
        tradeOrderMapper.insert(order);

        tradeOrderDeliveryMapper.insert(buildDelivery(order.getId(), DeliveryTypeEnum.EXPRESS.getType(),
                TradeOrderStatusEnum.DELIVERED.getStatus()));
        tradeOrderDeliveryMapper.insert(buildDelivery(order.getId(), DeliveryTypeEnum.STATION.getType(),
                TradeOrderStatusEnum.DELIVERED.getStatus()));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> tradeOrderUpdateService.receiveOrderByMember(order.getUserId(), order.getId()));
        assertEquals(ORDER_RECEIVE_FAIL_SPLIT_DELIVERY_REQUIRED.getCode(), exception.getCode());
    }

    @Test
    void deliveryOrder_shouldFallbackToLegacyOrderWhenNoChildDelivery() {
        TradeOrderDO order = buildBaseOrder(TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                DeliveryTypeEnum.EXPRESS.getType(), 180, true, 12L);
        order.setReceiverName("收件人").setReceiverMobile("13800000000").setReceiverDetailAddress("测试地址");
        tradeOrderMapper.insert(order);

        when(deliveryExpressService.validateDeliveryExpress(eq(1L)))
                .thenReturn(new DeliveryExpressDO().setId(1L).setName("顺丰").setCode("SF"));

        tradeOrderUpdateService.deliveryOrder(new TradeOrderDeliveryReqVO()
                .setId(order.getId())
                .setLogisticsId(1L)
                .setLogisticsNo("SF-LEGACY-001"));

        TradeOrderDO dbOrder = tradeOrderMapper.selectById(order.getId());
        assertEquals(TradeOrderStatusEnum.DELIVERED.getStatus(), dbOrder.getStatus());
        assertEquals(1L, dbOrder.getLogisticsId());
        assertEquals("SF-LEGACY-001", dbOrder.getLogisticsNo());
        assertTrue(CollUtil.isEmpty(tradeOrderDeliveryMapper.selectListByOrderId(order.getId())));
    }

    private void mockMixedOrderBase() {
        when(productSkuApi.getSkuList(anyCollection())).thenReturn(List.of(normalSku(), publicationSku()));
        when(productSkuApi.getSkuMap(anyCollection())).thenReturn(Map.of(
                100L, normalSku(),
                200L, publicationSku()));
        when(productSpuApi.getSpuList(anyCollection())).thenReturn(List.of(normalSpu(), publicationSpu()));
        when(productSpuApi.getSpuMap(anyCollection())).thenReturn(Map.of(
                11L, normalSpu(),
                22L, publicationSpu()));
        when(subscriptionOrderEligibilityApi.validateOrderItems(any())).thenReturn(List.of(eligibility()));
        when(eduStationApi.getSchoolStationMap(anyCollection()))
                .thenReturn(Map.of(2L, schoolStation()));
    }

    private TradePriceCalculateRespBO buildMixedCalculateResp(TradePriceCalculateReqBO reqBO) {
        TradePriceCalculateRespBO respBO = TradePriceCalculatorHelper.buildCalculateResp(reqBO,
                List.of(normalSpu(), publicationSpu()), List.of(normalSku(), publicationSku()));
        respBO.getItems().get(0).setResolvedDeliveryType(DeliveryTypeEnum.EXPRESS.getType());
        respBO.getItems().get(1).setResolvedDeliveryType(DeliveryTypeEnum.STATION.getType());
        return respBO;
    }

    private AppTradeOrderSettlementReqVO.Item normalItem() {
        AppTradeOrderSettlementReqVO.Item item = new AppTradeOrderSettlementReqVO.Item();
        item.setSkuId(100L);
        item.setCount(1);
        return item;
    }

    private AppTradeOrderSettlementReqVO.Item publicationItem() {
        AppTradeOrderSettlementReqVO.Item item = new AppTradeOrderSettlementReqVO.Item();
        item.setSkuId(200L);
        item.setCount(1);
        item.setStudentId(1L);
        item.setWindowSkuId(40L);
        return item;
    }

    private ProductSkuRespDTO normalSku() {
        return new ProductSkuRespDTO().setId(100L).setSpuId(11L).setPrice(100).setStock(100);
    }

    private ProductSkuRespDTO publicationSku() {
        return new ProductSkuRespDTO().setId(200L).setSpuId(22L).setPrice(200).setStock(100);
    }

    private ProductSpuRespDTO normalSpu() {
        return new ProductSpuRespDTO().setId(11L).setName("普通商品")
                .setDomainType(ProductDomainTypeEnum.NORMAL.getCode()).setGiveIntegral(0);
    }

    private ProductSpuRespDTO publicationSpu() {
        return new ProductSpuRespDTO().setId(22L).setName("刊物商品")
                .setDomainType(ProductDomainTypeEnum.PUBLICATION.getCode()).setGiveIntegral(0);
    }

    private SubscriptionOrderEligibilityRespDTO eligibility() {
        return new SubscriptionOrderEligibilityRespDTO()
                .setRequestIndex(1)
                .setStudentId(1L)
                .setStudentName("学生 1")
                .setSchoolId(2L)
                .setSchoolName("未来小学")
                .setClassId(4L)
                .setClassName("2026级三年级1班")
                .setGradeCatalogId(3L)
                .setGradeName("三年级")
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
                .setMaxQuantityPerStudent(5);
    }

    private EduSchoolStationRespDTO schoolStation() {
        EduSchoolStationRespDTO dto = new EduSchoolStationRespDTO();
        dto.setSchoolId(2L);
        dto.setSchoolName("未来小学");
        dto.setStationId(9L);
        dto.setStationName("站点 A");
        dto.setStationAddress("示例路 1 号");
        dto.setContactName("张老师");
        dto.setContactMobile("13800000000");
        return dto;
    }

    private MemberAddressRespDTO buildAddress() {
        return new MemberAddressRespDTO()
                .setId(1L)
                .setUserId(100L)
                .setName("收件人")
                .setMobile("13800000000")
                .setAreaId(110101)
                .setDetailAddress("测试地址");
    }

    private TradeOrderDO buildBaseOrder(Integer status, Integer deliveryType, Integer payPrice,
                                        boolean payStatus, Long payOrderId) {
        TradeOrderDO order = new TradeOrderDO();
        order.setNo("ORDER-" + System.nanoTime());
        order.setType(0);
        order.setTerminal(TerminalEnum.APP.getTerminal());
        order.setUserId(100L);
        order.setUserIp("127.0.0.1");
        order.setStatus(status);
        order.setProductCount(2);
        order.setCommentStatus(false);
        order.setPayStatus(payStatus);
        order.setPayOrderId(payOrderId);
        order.setTotalPrice(payPrice);
        order.setDiscountPrice(0);
        order.setDeliveryPrice(0);
        order.setAdjustPrice(0);
        order.setPayPrice(payPrice);
        order.setDeliveryType(deliveryType);
        order.setCouponId(0L);
        order.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus());
        order.setRefundPrice(0);
        order.setCouponPrice(0);
        order.setUsePoint(0);
        order.setPointPrice(0);
        order.setGivePoint(0);
        order.setRefundPoint(0);
        order.setVipPrice(0);
        return order;
    }

    private TradeOrderDeliveryDO buildDelivery(Long orderId, Integer deliveryType, Integer status) {
        TradeOrderDeliveryDO delivery = new TradeOrderDeliveryDO();
        delivery.setOrderId(orderId);
        delivery.setDeliveryType(deliveryType);
        delivery.setStatus(status);
        delivery.setProductCount(1);
        delivery.setPayPrice(100);
        delivery.setDeliveryPrice(0);
        return delivery;
    }

}
