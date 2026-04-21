package cn.iocoder.yudao.module.trade.controller.app.order;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderDeliveryRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderPageItemRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.aftersale.AfterSaleService;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderUpdateService;
import cn.iocoder.yudao.module.trade.service.price.TradePriceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppTradeOrderControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppTradeOrderController controller;

    @Mock
    private TradeOrderUpdateService tradeOrderUpdateService;
    @Mock
    private TradeOrderQueryService tradeOrderQueryService;
    @Mock
    private DeliveryExpressService deliveryExpressService;
    @Mock
    private AfterSaleService afterSaleService;
    @Mock
    private TradePriceService priceService;
    @Mock
    private TradeOrderProperties tradeOrderProperties;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/app-api/trade/order/get-detail");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        LoginUser loginUser = new LoginUser();
        loginUser.setId(100L);
        loginUser.setUserType(UserTypeEnum.MEMBER.getValue());
        SecurityFrameworkUtils.setLoginUser(loginUser, request);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void settlement_shouldReturnDeliveryPreviewFromService() {
        AppTradeOrderSettlementReqVO reqVO = new AppTradeOrderSettlementReqVO();
        AppTradeOrderSettlementRespVO respVO = new AppTradeOrderSettlementRespVO();
        respVO.setDeliveries(List.of(new AppTradeOrderDeliveryRespVO()
                .setDeliveryType(DeliveryTypeEnum.STATION.getType())
                .setSchoolId(2L)
                .setSchoolNameSnapshot("未来小学")
                .setStationId(9L)
                .setStationNameSnapshot("站点 A")));
        when(tradeOrderUpdateService.settlementOrder(100L, reqVO)).thenReturn(respVO);

        CommonResult<AppTradeOrderSettlementRespVO> result = controller.settlementOrder(reqVO);

        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getDeliveries());
        assertEquals(1, result.getData().getDeliveries().size());
        assertEquals("站点 A", result.getData().getDeliveries().get(0).getStationNameSnapshot());
        verify(tradeOrderUpdateService).settlementOrder(100L, reqVO);
    }

    @Test
    void getOrderDetail_shouldIncludeDeliveriesAndItemDeliveryId() {
        TradeOrderDO order = new TradeOrderDO().setId(1L).setUserId(100L);
        order.setCreateTime(LocalDateTime.of(2026, 4, 21, 10, 0, 0));
        order.setStatus(20);
        order.setDeliveryType(DeliveryTypeEnum.MIXED.getType());
        order.setPayStatus(true);
        order.setPayOrderId(500L);
        order.setReceiverAreaId(110101);
        TradeOrderItemDO item = new TradeOrderItemDO().setId(11L).setOrderId(1L).setDeliveryId(101L)
                .setSpuId(201L).setSpuName("刊物商品").setSkuId(301L).setCount(1).setPrice(100).setPayPrice(100)
                .setCommentStatus(false).setAfterSaleStatus(0)
                .setSubscriptionStudentId(1L).setSubscriptionStudentNameSnapshot("学生 1");
        TradeOrderDeliveryDO stationDelivery = new TradeOrderDeliveryDO().setId(101L).setOrderId(1L)
                .setDeliveryType(DeliveryTypeEnum.STATION.getType()).setStatus(20)
                .setSchoolId(2L).setSchoolNameSnapshot("未来小学")
                .setStationId(9L).setStationNameSnapshot("站点 A");

        when(tradeOrderQueryService.getOrder(100L, 1L)).thenReturn(order);
        when(tradeOrderQueryService.getOrderItemListByOrderId(1L)).thenReturn(List.of(item));
        when(tradeOrderQueryService.getOrderDeliveryListByOrderId(1L)).thenReturn(List.of(stationDelivery));
        when(tradeOrderProperties.getPayExpireTime()).thenReturn(Duration.ofHours(2));

        CommonResult<AppTradeOrderDetailRespVO> result = controller.getOrderDetail(1L, false);

        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getItems());
        assertEquals(101L, result.getData().getItems().get(0).getDeliveryId());
        assertEquals("学生 1", result.getData().getItems().get(0).getSubscriptionStudentNameSnapshot());
        assertNotNull(result.getData().getDeliveries());
        assertEquals(1, result.getData().getDeliveries().size());
        assertEquals("站点 A", result.getData().getDeliveries().get(0).getStationNameSnapshot());
    }

    @Test
    void getOrderPage_shouldMarkOrdersWithSplitDeliveries() {
        AppTradeOrderPageReqVO reqVO = new AppTradeOrderPageReqVO();
        TradeOrderDO order = new TradeOrderDO().setId(1L).setUserId(100L).setStatus(20)
                .setDeliveryType(DeliveryTypeEnum.MIXED.getType()).setPayOrderId(500L).setPayPrice(100);
        TradeOrderItemDO item = new TradeOrderItemDO().setId(11L).setOrderId(1L)
                .setSpuId(201L).setSpuName("刊物商品").setSkuId(301L).setCount(1).setPrice(100).setPayPrice(100)
                .setCommentStatus(false).setAfterSaleStatus(0);
        TradeOrderDeliveryDO delivery = new TradeOrderDeliveryDO().setId(101L).setOrderId(1L)
                .setDeliveryType(DeliveryTypeEnum.STATION.getType()).setStatus(20);

        when(tradeOrderQueryService.getOrderPage(100L, reqVO)).thenReturn(new PageResult<>(List.of(order), 1L));
        when(tradeOrderQueryService.getOrderItemListByOrderId(java.util.Set.of(1L))).thenReturn(List.of(item));
        when(tradeOrderQueryService.getOrderDeliveryListByOrderId(java.util.Set.of(1L))).thenReturn(List.of(delivery));

        CommonResult<PageResult<AppTradeOrderPageItemRespVO>> result = controller.getOrderPage(reqVO);

        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        assertTrue(Boolean.TRUE.equals(result.getData().getList().get(0).getHasDeliveries()));
    }

    @Test
    void receiveDelivery_shouldDelegateWithCurrentLoginUser() {
        CommonResult<Boolean> result = controller.receiveDelivery(101L);

        assertEquals(0, result.getCode());
        assertTrue(result.getData());
        verify(tradeOrderUpdateService).receiveDeliveryByMember(100L, 101L);
    }
}
