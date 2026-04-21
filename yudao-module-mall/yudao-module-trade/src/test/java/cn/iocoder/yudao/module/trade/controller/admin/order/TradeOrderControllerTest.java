package cn.iocoder.yudao.module.trade.controller.admin.order;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderStationDeliveryReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderLogService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderUpdateService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeOrderControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TradeOrderController controller;

    @Mock
    private TradeOrderUpdateService tradeOrderUpdateService;
    @Mock
    private TradeOrderQueryService tradeOrderQueryService;
    @Mock
    private TradeOrderLogService tradeOrderLogService;
    @Mock
    private MemberUserApi memberUserApi;

    @Test
    void getOrderDetail_shouldIncludeDeliveriesAndItemSnapshots() {
        TradeOrderDO order = new TradeOrderDO().setId(1L).setUserId(100L).setDeliveryType(DeliveryTypeEnum.MIXED.getType());
        TradeOrderItemDO item = new TradeOrderItemDO().setId(11L).setOrderId(1L).setDeliveryId(101L)
                .setSpuId(201L).setSpuName("刊物商品").setSkuId(301L).setCount(1).setPrice(100).setPayPrice(100)
                .setAfterSaleStatus(0)
                .setSubscriptionStudentId(1L).setSubscriptionStudentNameSnapshot("学生 1")
                .setSubscriptionSchoolId(2L).setSubscriptionSchoolNameSnapshot("未来小学")
                .setSubscriptionGradeCatalogId(3L).setSubscriptionGradeNameSnapshot("三年级");
        TradeOrderDeliveryDO expressDelivery = new TradeOrderDeliveryDO().setId(100L).setOrderId(1L)
                .setDeliveryType(DeliveryTypeEnum.EXPRESS.getType()).setStatus(20)
                .setReceiverName("收件人").setReceiverMobile("13800000000").setReceiverAreaId(110101)
                .setReceiverDetailAddress("测试地址");
        TradeOrderDeliveryDO stationDelivery = new TradeOrderDeliveryDO().setId(101L).setOrderId(1L)
                .setDeliveryType(DeliveryTypeEnum.STATION.getType()).setStatus(20)
                .setSchoolId(2L).setSchoolNameSnapshot("未来小学")
                .setStationId(9L).setStationNameSnapshot("站点 A")
                .setStationAddressSnapshot("示例路 1 号").setContactName("李老师").setContactMobile("13900000000");
        MemberUserRespDTO user = new MemberUserRespDTO();
        user.setId(100L);
        user.setNickname("家长");

        when(tradeOrderQueryService.getOrder(1L)).thenReturn(order);
        when(tradeOrderQueryService.getOrderItemListByOrderId(1L)).thenReturn(List.of(item));
        when(tradeOrderQueryService.getOrderDeliveryListByOrderId(1L)).thenReturn(List.of(expressDelivery, stationDelivery));
        when(tradeOrderLogService.getOrderLogListByOrderId(1L)).thenReturn(List.of());
        when(memberUserApi.getUser(100L)).thenReturn(user);

        CommonResult<TradeOrderDetailRespVO> result = controller.getOrderDetail(1L);

        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getItems());
        assertEquals(1, result.getData().getItems().size());
        assertEquals(101L, result.getData().getItems().get(0).getDeliveryId());
        assertEquals("学生 1", result.getData().getItems().get(0).getSubscriptionStudentNameSnapshot());
        assertNotNull(result.getData().getDeliveries());
        assertEquals(2, result.getData().getDeliveries().size());
        assertTrue(result.getData().getDeliveries().stream().anyMatch(delivery ->
                DeliveryTypeEnum.STATION.getType().equals(delivery.getDeliveryType())
                        && "站点 A".equals(delivery.getStationNameSnapshot())));
        assertTrue(result.getData().getDeliveries().stream().anyMatch(delivery ->
                DeliveryTypeEnum.EXPRESS.getType().equals(delivery.getDeliveryType())
                        && "收件人".equals(delivery.getReceiverName())));
    }

    @Test
    void deliveryEndpoints_shouldDelegateToUpdateService() {
        TradeOrderDeliveryReqVO deliveryReqVO = new TradeOrderDeliveryReqVO()
                .setDeliveryId(100L).setLogisticsId(1L).setLogisticsNo("SF123");
        TradeOrderStationDeliveryReqVO stationReqVO = new TradeOrderStationDeliveryReqVO().setDeliveryId(101L);

        CommonResult<Boolean> deliveryResult = controller.deliveryOrder(deliveryReqVO);
        CommonResult<Boolean> stationResult = controller.stationDeliveryOrder(stationReqVO);

        assertEquals(0, deliveryResult.getCode());
        assertTrue(deliveryResult.getData());
        assertEquals(0, stationResult.getCode());
        assertTrue(stationResult.getData());
        verify(tradeOrderUpdateService).deliveryOrder(deliveryReqVO);
        verify(tradeOrderUpdateService).stationDeliveryOrder(stationReqVO);
    }
}
