package cn.iocoder.yudao.module.trade.controller.app.order;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.*;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemCommentCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemRespVO;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderPublicationIssueDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.aftersale.AfterSaleService;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderCheckoutService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderCommentService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderLifecycleService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderPaymentService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderPublicationIssueService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderReceiveService;
import cn.iocoder.yudao.module.trade.service.price.TradePriceService;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 交易订单")
@RestController
@RequestMapping("/trade/order")
@Validated
@Slf4j
public class AppTradeOrderController {

    @Resource
    private TradeOrderCheckoutService tradeOrderCheckoutService;
    @Resource
    private TradeOrderPaymentService tradeOrderPaymentService;
    @Resource
    private TradeOrderReceiveService tradeOrderReceiveService;
    @Resource
    private TradeOrderLifecycleService tradeOrderLifecycleService;
    @Resource
    private TradeOrderCommentService tradeOrderCommentService;
    @Resource
    private TradeOrderQueryService tradeOrderQueryService;
    @Resource
    private TradeOrderPublicationIssueService publicationIssueService;
    @Resource
    private DeliveryExpressService deliveryExpressService;
    @Resource
    private AfterSaleService afterSaleService;
    @Resource
    private TradePriceService priceService;

    @Resource
    private TradeOrderProperties tradeOrderProperties;

    @GetMapping("/settlement")
    @Operation(summary = "获得订单结算信息")
    public CommonResult<AppTradeOrderSettlementRespVO> settlementOrder(@Valid AppTradeOrderSettlementReqVO settlementReqVO) {
        return success(tradeOrderCheckoutService.settlementOrder(getLoginUserId(), settlementReqVO));
    }

    @GetMapping("/settlement-product")
    @Operation(summary = "获得商品结算信息", description = "用于商品列表、商品详情，获得参与活动后的价格信息")
    @Parameter(name = "spuIds", description = "商品 SPU 编号数组")
    @PermitAll
    public CommonResult<List<AppTradeProductSettlementRespVO>> settlementProduct(@RequestParam("spuIds") List<Long> spuIds) {
        return success(priceService.calculateProductPrice(getLoginUserId(), spuIds));
    }

    @PostMapping("/create")
    @Operation(summary = "创建订单")
    public CommonResult<AppTradeOrderCreateRespVO> createOrder(@Valid @RequestBody AppTradeOrderCreateReqVO createReqVO) {
        TradeOrderDO order = tradeOrderCheckoutService.createOrder(getLoginUserId(), createReqVO);
        return success(new AppTradeOrderCreateRespVO().setId(order.getId()).setPayOrderId(order.getPayOrderId()));
    }

    @PostMapping("/update-paid")
    @Operation(summary = "更新订单为已支付") // 由 pay-module 支付服务，进行回调，可见 PayNotifyJob
    @PermitAll
    public CommonResult<Boolean> updateOrderPaid(@RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        tradeOrderPaymentService.updateOrderPaid(Long.valueOf(notifyReqDTO.getMerchantOrderId()),
                notifyReqDTO.getPayOrderId());
        return success(true);
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得交易订单")
    @Parameters({
            @Parameter(name = "id", description = "交易订单编号"),
            @Parameter(name = "sync", description = "是否同步支付状态", example = "true")
    })
    public CommonResult<AppTradeOrderDetailRespVO> getOrderDetail(@RequestParam("id") Long id,
                                                                  @RequestParam(value = "sync", required = false) Boolean sync) {
        // 1.1 查询订单
        TradeOrderDO order = tradeOrderQueryService.getOrder(getLoginUserId(), id);
        if (order == null) {
            return success(null);
        }
        // 1.2 sync 仅在等待支付
        if (Boolean.TRUE.equals(sync)
                && TradeOrderStatusEnum.isUnpaid(order.getStatus()) && !order.getPayStatus()) {
            tradeOrderPaymentService.syncOrderPayStatusQuietly(order.getId(), order.getPayOrderId());
            // 重新查询，因为同步后，可能会有变化
            order = tradeOrderQueryService.getOrder(id);
        }

        // 2.1 查询订单项
        List<TradeOrderItemDO> orderItems = tradeOrderQueryService.getOrderItemListByOrderId(order.getId());
        List<TradeOrderDeliveryDO> deliveries = tradeOrderQueryService.getOrderDeliveryListByOrderId(order.getId());
        // 2.2 查询物流公司
        DeliveryExpressDO express = order.getLogisticsId() != null && order.getLogisticsId() > 0 ?
                deliveryExpressService.getDeliveryExpress(order.getLogisticsId()) : null;
        // 2.3 最终组合
        AppTradeOrderDetailRespVO respVO = TradeOrderConvert.INSTANCE.convert02(order, orderItems, deliveries,
                tradeOrderProperties, express);
        fillPublicationIssues(respVO.getItems(), publicationIssueService.getIssueListByOrderId(order.getId()));
        return success(respVO);
    }

    @GetMapping("/get-express-track-list")
    @Operation(summary = "获得交易订单的物流轨迹")
    @Parameter(name = "id", description = "交易订单编号")
    public CommonResult<List<AppOrderExpressTrackRespDTO>> getOrderExpressTrackList(@RequestParam("id") Long id) {
        return success(TradeOrderConvert.INSTANCE.convertList02(
                tradeOrderQueryService.getExpressTrackList(id, getLoginUserId())));
    }

    @GetMapping("/get-delivery-express-track-list")
    @Operation(summary = "获得订单配送组的物流轨迹")
    @Parameter(name = "deliveryId", description = "配送组编号")
    public CommonResult<List<AppOrderExpressTrackRespDTO>> getDeliveryExpressTrackList(
            @RequestParam("deliveryId") Long deliveryId) {
        return success(TradeOrderConvert.INSTANCE.convertList02(
                tradeOrderQueryService.getDeliveryExpressTrackList(deliveryId, getLoginUserId())));
    }

    @GetMapping("/get-publication-issue-express-track-list")
    @Operation(summary = "获得刊物订单期次的物流轨迹")
    @Parameter(name = "orderIssueId", description = "订单刊物期次编号")
    public CommonResult<List<AppOrderExpressTrackRespDTO>> getPublicationIssueExpressTrackList(
            @RequestParam("orderIssueId") Long orderIssueId) {
        return success(TradeOrderConvert.INSTANCE.convertList02(
                tradeOrderQueryService.getPublicationIssueExpressTrackList(orderIssueId, getLoginUserId())));
    }

    @GetMapping("/page")
    @Operation(summary = "获得交易订单分页")
    public CommonResult<PageResult<AppTradeOrderPageItemRespVO>> getOrderPage(AppTradeOrderPageReqVO reqVO) {
        // 查询订单
        PageResult<TradeOrderDO> pageResult = tradeOrderQueryService.getOrderPage(getLoginUserId(), reqVO);
        // 查询订单项
        List<TradeOrderItemDO> orderItems = tradeOrderQueryService.getOrderItemListByOrderId(
                convertSet(pageResult.getList(), TradeOrderDO::getId));
        // 查询配送组：App 列表需要据此隐藏整单确认收货，改走配送组确认
        List<TradeOrderDeliveryDO> deliveries = tradeOrderQueryService.getOrderDeliveryListByOrderId(
                convertSet(pageResult.getList(), TradeOrderDO::getId));
        // 最终组合
        PageResult<AppTradeOrderPageItemRespVO> respPage = TradeOrderConvert.INSTANCE.convertPage02(pageResult,
                orderItems, deliveries);
        Map<Long, List<TradeOrderPublicationIssueDO>> issueMap = convertMultiMap(
                publicationIssueService.getIssueListByOrderIds(convertSet(pageResult.getList(), TradeOrderDO::getId)),
                TradeOrderPublicationIssueDO::getOrderId);
        respPage.getList().forEach(order -> fillPublicationIssues(order.getItems(),
                issueMap.get(order.getId())));
        return success(respPage);
    }

    @GetMapping("/get-count")
    @Operation(summary = "获得交易订单数量")
    public CommonResult<Map<String, Long>> getOrderCount() {
        Map<String, Long> orderCount = Maps.newLinkedHashMapWithExpectedSize(5);
        // 全部
        orderCount.put("allCount", tradeOrderQueryService.getOrderCount(getLoginUserId(), null, null));
        // 待付款（未支付）
        orderCount.put("unpaidCount", tradeOrderQueryService.getOrderCount(getLoginUserId(),
                TradeOrderStatusEnum.UNPAID.getStatus(), null));
        // 待发货
        orderCount.put("undeliveredCount", tradeOrderQueryService.getOrderCount(getLoginUserId(),
                TradeOrderStatusEnum.UNDELIVERED.getStatus(), null));
        // 待收货
        orderCount.put("deliveredCount", tradeOrderQueryService.getOrderCount(getLoginUserId(),
                TradeOrderStatusEnum.DELIVERED.getStatus(), null));
        // 待评价
        orderCount.put("uncommentedCount", tradeOrderQueryService.getOrderCount(getLoginUserId(),
                TradeOrderStatusEnum.COMPLETED.getStatus(), false));
        // 售后数量
        orderCount.put("afterSaleCount", afterSaleService.getApplyingAfterSaleCount(getLoginUserId()));
        return success(orderCount);
    }

    @PutMapping("/receive")
    @Operation(summary = "确认交易订单收货")
    @Parameter(name = "id", description = "交易订单编号")
    public CommonResult<Boolean> receiveOrder(@RequestParam("id") Long id) {
        tradeOrderReceiveService.receiveOrderByMember(getLoginUserId(), id);
        return success(true);
    }

    @PutMapping("/receive-delivery")
    @Operation(summary = "确认订单配送组收货")
    @Parameter(name = "deliveryId", description = "配送组编号")
    public CommonResult<Boolean> receiveDelivery(@RequestParam("deliveryId") Long deliveryId) {
        tradeOrderReceiveService.receiveDeliveryByMember(getLoginUserId(), deliveryId);
        return success(true);
    }

    @PutMapping("/receive-publication-issue")
    @Operation(summary = "确认刊物期次收货")
    @Parameter(name = "orderIssueId", description = "订单刊物期次编号")
    public CommonResult<Boolean> receivePublicationIssue(@RequestParam("orderIssueId") Long orderIssueId) {
        publicationIssueService.receiveIssueByMember(getLoginUserId(), orderIssueId);
        return success(true);
    }

    @DeleteMapping("/cancel")
    @Operation(summary = "取消交易订单")
    @Parameter(name = "id", description = "交易订单编号")
    public CommonResult<Boolean> cancelOrder(@RequestParam("id") Long id) {
        tradeOrderLifecycleService.cancelOrderByMember(getLoginUserId(), id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除交易订单")
    @Parameter(name = "id", description = "交易订单编号")
    public CommonResult<Boolean> deleteOrder(@RequestParam("id") Long id) {
        tradeOrderLifecycleService.deleteOrder(getLoginUserId(), id);
        return success(true);
    }

    // ========== 订单项 ==========

    @GetMapping("/item/get")
    @Operation(summary = "获得交易订单项")
    @Parameter(name = "id", description = "交易订单项编号")
    public CommonResult<AppTradeOrderItemRespVO> getOrderItem(@RequestParam("id") Long id) {
        TradeOrderItemDO item = tradeOrderQueryService.getOrderItem(getLoginUserId(), id);
        AppTradeOrderItemRespVO respVO = TradeOrderConvert.INSTANCE.convert03(item);
        if (item != null) {
            fillPublicationIssues(List.of(respVO), publicationIssueService.getIssueListByOrderId(item.getOrderId()));
        }
        return success(respVO);
    }

    @PostMapping("/item/create-comment")
    @Idempotent
    @Operation(summary = "创建交易订单项的评价")
    public CommonResult<Long> createOrderItemComment(@RequestBody AppTradeOrderItemCommentCreateReqVO createReqVO) {
        return success(tradeOrderCommentService.createOrderItemCommentByMember(getLoginUserId(), createReqVO));
    }

    private void fillPublicationIssues(List<AppTradeOrderItemRespVO> items, List<TradeOrderPublicationIssueDO> issues) {
        if (items == null || issues == null) {
            return;
        }
        Map<Long, List<TradeOrderPublicationIssueDO>> issueMap = convertMultiMap(issues,
                TradeOrderPublicationIssueDO::getOrderItemId);
        items.forEach(item -> item.setPublicationIssues(
                TradeOrderConvert.INSTANCE.convertPublicationIssues02(issueMap.get(item.getId()))));
    }

}
