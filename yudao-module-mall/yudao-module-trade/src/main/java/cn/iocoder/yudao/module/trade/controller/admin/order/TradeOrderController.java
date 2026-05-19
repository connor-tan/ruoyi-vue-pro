package cn.iocoder.yudao.module.trade.controller.admin.order;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.member.api.address.dto.MemberAddressRespDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementRespVO;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderLogDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderPublicationIssueDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderAdminAdjustService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderCheckoutService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderFulfillmentService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderLogService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderManualService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderPublicationIssueService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderReceiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 交易订单")
@RestController
@RequestMapping("/trade/order")
@Validated
@Slf4j
public class TradeOrderController {

    @Resource
    private TradeOrderFulfillmentService tradeOrderFulfillmentService;
    @Resource
    private TradeOrderAdminAdjustService tradeOrderAdminAdjustService;
    @Resource
    private TradeOrderReceiveService tradeOrderReceiveService;
    @Resource
    private TradeOrderQueryService tradeOrderQueryService;
    @Resource
    private TradeOrderLogService tradeOrderLogService;
    @Resource
    private TradeOrderPublicationIssueService publicationIssueService;
    @Resource
    private TradeOrderManualService tradeOrderManualService;
    @Resource
    private TradeOrderCheckoutService tradeOrderCheckoutService;

    @Resource
    private MemberUserApi memberUserApi;

    @GetMapping("/page")
    @Operation(summary = "获得交易订单分页")
    @PreAuthorize("@ss.hasPermission('trade:order:query')")
    public CommonResult<PageResult<TradeOrderPageItemRespVO>> getOrderPage(TradeOrderPageReqVO reqVO) {
        // 查询订单
        PageResult<TradeOrderDO> pageResult = tradeOrderQueryService.getOrderPage(reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 查询用户信息
        Set<Long> userIds = CollUtil.unionDistinct(convertList(pageResult.getList(), TradeOrderDO::getUserId,
                        Objects::nonNull),
                convertList(pageResult.getList(), TradeOrderDO::getBrokerageUserId, Objects::nonNull));
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(userIds);
        // 查询订单项
        List<TradeOrderItemDO> orderItems = tradeOrderQueryService.getOrderItemListByOrderId(
                convertSet(pageResult.getList(), TradeOrderDO::getId));
        List<TradeOrderDeliveryDO> deliveries = tradeOrderQueryService.getOrderDeliveryListByOrderId(
                convertSet(pageResult.getList(), TradeOrderDO::getId));
        // 最终组合
        PageResult<TradeOrderPageItemRespVO> respPage = TradeOrderConvert.INSTANCE.convertPage(pageResult, orderItems,
                deliveries, userMap);
        Map<Long, List<TradeOrderPublicationIssueDO>> issueMap = convertMultiMap(
                publicationIssueService.getIssueListByOrderIds(convertSet(pageResult.getList(), TradeOrderDO::getId)),
                TradeOrderPublicationIssueDO::getOrderId);
        respPage.getList().forEach(order -> fillPublicationIssues(order.getItems(), issueMap.get(order.getId())));
        return success(respPage);
    }

    @GetMapping("/summary")
    @Operation(summary = "获得交易订单统计")
    @PreAuthorize("@ss.hasPermission('trade:order:query')")
    public CommonResult<TradeOrderSummaryRespVO> getOrderSummary(TradeOrderPageReqVO reqVO) {
        return success(tradeOrderQueryService.getOrderSummary(reqVO));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得交易订单详情")
    @Parameter(name = "id", description = "订单编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('trade:order:query')")
    public CommonResult<TradeOrderDetailRespVO> getOrderDetail(@RequestParam("id") Long id) {
        // 查询订单
        TradeOrderDO order = tradeOrderQueryService.getOrder(id);
        if (order == null) {
            return success(null);
        }
        // 查询订单项
        List<TradeOrderItemDO> orderItems = tradeOrderQueryService.getOrderItemListByOrderId(id);
        List<TradeOrderDeliveryDO> deliveries = tradeOrderQueryService.getOrderDeliveryListByOrderId(id);

        // 拼接数据
        MemberUserRespDTO user = order.getUserId() == null ? null : memberUserApi.getUser(order.getUserId());
        MemberUserRespDTO brokerageUser = order.getBrokerageUserId() != null ?
                memberUserApi.getUser(order.getBrokerageUserId()) : null;
        List<TradeOrderLogDO> orderLogs = tradeOrderLogService.getOrderLogListByOrderId(id);
        TradeOrderDetailRespVO respVO = TradeOrderConvert.INSTANCE.convert(order, orderItems, deliveries, orderLogs,
                user, brokerageUser);
        fillPublicationIssues(respVO.getItems(), publicationIssueService.getIssueListByOrderId(id));
        return success(respVO);
    }

    @GetMapping("/get-express-track-list")
    @Operation(summary = "获得交易订单的物流轨迹")
    @Parameter(name = "id", description = "交易订单编号")
    @PreAuthorize("@ss.hasPermission('trade:order:query')")
    public CommonResult<List<?>> getOrderExpressTrackList(@RequestParam("id") Long id) {
        return success(TradeOrderConvert.INSTANCE.convertList02(
                tradeOrderQueryService.getExpressTrackList(id)));
    }

    @PostMapping("/manual-create")
    @Operation(summary = "手动创建后台订单")
    @PreAuthorize("@ss.hasPermission('trade:order:create')")
    public CommonResult<Long> createManualOrder(@Valid @RequestBody TradeOrderManualCreateReqVO reqVO) {
        return success(tradeOrderManualService.createManualOrder(reqVO));
    }

    @GetMapping("/manual-import-template")
    @Operation(summary = "下载后台订单导入模板")
    @PreAuthorize("@ss.hasPermission('trade:order:import')")
    public void importManualOrderTemplate(HttpServletResponse response) throws IOException {
        TradeOrderManualImportExcelVO example = new TradeOrderManualImportExcelVO()
                .setImportOrderNo("ORDER-001")
                .setSkuId(1001L)
                .setCount(1)
                .setDeliveryType(DeliveryTypeEnum.EXPRESS.getType())
                .setStudentId(2001L)
                .setOfferSkuId(3001L)
                .setManualUnitPrice(null)
                .setManualOrderPrice(null)
                .setReceiverName("张三")
                .setReceiverMobile("13800000000")
                .setReceiverAreaId(110101)
                .setReceiverDetailAddress("示例地址")
                .setPickUpStoreId(null)
                .setRemark("示例：同一导入订单号可填写多行商品；订单级字段可只填一行，但同组非空值必须一致");
        ExcelUtils.write(response, "后台订单导入模板.xls", "订单明细",
                TradeOrderManualImportExcelVO.class, List.of(example));
    }

    @PostMapping("/manual-import")
    @Operation(summary = "批量导入后台订单")
    @PreAuthorize("@ss.hasPermission('trade:order:import')")
    public CommonResult<TradeOrderManualImportRespVO> importManualOrder(@RequestParam("file") MultipartFile file)
            throws IOException {
        return success(tradeOrderManualService.importManualOrders(
                ExcelUtils.read(file, TradeOrderManualImportExcelVO.class)));
    }

    @GetMapping("/admin-online/address-list")
    @Operation(summary = "获得后台在线下单学生家长地址列表")
    @Parameter(name = "studentId", description = "学生编号", required = true)
    @PreAuthorize("@ss.hasPermission('trade:order:create')")
    public CommonResult<List<TradeOrderAdminOnlineAddressRespVO>> getAdminOnlineAddressList(
            @RequestParam("studentId") Long studentId) {
        return success(convertAdminOnlineAddressList(tradeOrderCheckoutService.getAdminOnlineAddressList(studentId)));
    }

    @PostMapping("/admin-online/settlement")
    @Operation(summary = "后台在线订刊下单结算")
    @PreAuthorize("@ss.hasPermission('trade:order:create')")
    public CommonResult<AppTradeOrderSettlementRespVO> settlementAdminOnlineOrder(
            @Valid @RequestBody TradeOrderAdminOnlineSettlementReqVO reqVO) {
        return success(tradeOrderCheckoutService.settlementAdminOnlineOrder(reqVO));
    }

    @PostMapping("/admin-online/create")
    @Operation(summary = "后台在线订刊下单创建订单")
    @PreAuthorize("@ss.hasPermission('trade:order:create')")
    public CommonResult<TradeOrderAdminOnlineCreateRespVO> createAdminOnlineOrder(
            @Valid @RequestBody TradeOrderAdminOnlineCreateReqVO reqVO) {
        TradeOrderDO order = tradeOrderCheckoutService.createAdminOnlineOrder(reqVO);
        return success(new TradeOrderAdminOnlineCreateRespVO()
                .setId(order.getId())
                .setPayOrderId(order.getPayOrderId()));
    }

    @PutMapping("/{id}/confirm-offline-pay")
    @Operation(summary = "确认后台订单线下收款")
    @Parameter(name = "id", description = "交易订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('trade:order:update')")
    public CommonResult<Long> confirmManualOrderOfflinePay(@PathVariable("id") Long id) {
        return success(tradeOrderManualService.confirmOfflinePay(id));
    }

    @PutMapping("/{id}/manual-cancel")
    @Operation(summary = "取消后台订单")
    @Parameter(name = "id", description = "交易订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('trade:order:update')")
    public CommonResult<Boolean> cancelManualOrder(@PathVariable("id") Long id) {
        tradeOrderManualService.cancelManualOrder(id);
        return success(true);
    }

    @PutMapping("/delivery")
    @Operation(summary = "订单发货")
    @PreAuthorize("@ss.hasPermission('trade:order:update')")
    public CommonResult<Boolean> deliveryOrder(@Valid @RequestBody TradeOrderDeliveryReqVO deliveryReqVO) {
        tradeOrderFulfillmentService.deliveryOrder(deliveryReqVO);
        return success(true);
    }

    @PutMapping("/update-remark")
    @Operation(summary = "订单备注")
    @PreAuthorize("@ss.hasPermission('trade:order:update')")
    public CommonResult<Boolean> updateOrderRemark(@RequestBody TradeOrderRemarkReqVO reqVO) {
        tradeOrderAdminAdjustService.updateOrderRemark(reqVO);
        return success(true);
    }

    @PutMapping("/update-price")
    @Operation(summary = "订单调价")
    @PreAuthorize("@ss.hasPermission('trade:order:update')")
    public CommonResult<Boolean> updateOrderPrice(@RequestBody TradeOrderUpdatePriceReqVO reqVO) {
        tradeOrderAdminAdjustService.updateOrderPrice(reqVO);
        return success(true);
    }

    @PutMapping("/update-address")
    @Operation(summary = "修改订单收货地址")
    @PreAuthorize("@ss.hasPermission('trade:order:update')")
    public CommonResult<Boolean> updateOrderAddress(@RequestBody TradeOrderUpdateAddressReqVO reqVO) {
        tradeOrderAdminAdjustService.updateOrderAddress(reqVO);
        return success(true);
    }

    @PutMapping("/pick-up-by-id")
    @Operation(summary = "订单核销")
    @Parameter(name = "id", description = "交易订单编号")
    @PreAuthorize("@ss.hasPermission('trade:order:pick-up')")
    public CommonResult<Boolean> pickUpOrderById(@RequestParam("id") Long id) {
        tradeOrderReceiveService.pickUpOrderByAdmin(getLoginUserId(), id);
        return success(true);
    }

    @PutMapping("/pick-up-by-verify-code")
    @Operation(summary = "订单核销")
    @Parameter(name = "pickUpVerifyCode", description = "自提核销码")
    @PreAuthorize("@ss.hasPermission('trade:order:pick-up')")
    public CommonResult<Boolean> pickUpOrderByVerifyCode(@RequestParam("pickUpVerifyCode") String pickUpVerifyCode) {
        tradeOrderReceiveService.pickUpOrderByAdmin(getLoginUserId(), pickUpVerifyCode);
        return success(true);
    }

    @GetMapping("/get-by-pick-up-verify-code")
    @Operation(summary = "查询核销码对应的订单")
    @Parameter(name = "pickUpVerifyCode", description = "自提核销码")
    @PreAuthorize("@ss.hasPermission('trade:order:query')")
    public CommonResult<TradeOrderDetailRespVO> getByPickUpVerifyCode(@RequestParam("pickUpVerifyCode") String pickUpVerifyCode) {
        TradeOrderDO tradeOrder = tradeOrderReceiveService.getByPickUpVerifyCode(pickUpVerifyCode);
        if (tradeOrder == null) {
            return success(null);
        }
        List<TradeOrderItemDO> orderItems = tradeOrderQueryService.getOrderItemListByOrderId(tradeOrder.getId());
        List<TradeOrderDeliveryDO> deliveries = tradeOrderQueryService.getOrderDeliveryListByOrderId(tradeOrder.getId());
        MemberUserRespDTO user = tradeOrder.getUserId() == null ? null : memberUserApi.getUser(tradeOrder.getUserId());
        MemberUserRespDTO brokerageUser = tradeOrder.getBrokerageUserId() != null ?
                memberUserApi.getUser(tradeOrder.getBrokerageUserId()) : null;
        List<TradeOrderLogDO> orderLogs = tradeOrderLogService.getOrderLogListByOrderId(tradeOrder.getId());
        TradeOrderDetailRespVO respVO = TradeOrderConvert.INSTANCE.convert(tradeOrder, orderItems, deliveries,
                orderLogs, user, brokerageUser);
        fillPublicationIssues(respVO.getItems(), publicationIssueService.getIssueListByOrderId(tradeOrder.getId()));
        return success(respVO);
    }

    private void fillPublicationIssues(List<? extends TradeOrderItemBaseVO> items,
                                       List<TradeOrderPublicationIssueDO> issues) {
        if (items == null || issues == null) {
            return;
        }
        Map<Long, List<TradeOrderPublicationIssueDO>> issueMap = convertMultiMap(issues,
                TradeOrderPublicationIssueDO::getOrderItemId);
        items.forEach(item -> item.setPublicationIssues(
                TradeOrderConvert.INSTANCE.convertPublicationIssues(issueMap.get(item.getId()))));
    }

    private List<TradeOrderAdminOnlineAddressRespVO> convertAdminOnlineAddressList(List<MemberAddressRespDTO> list) {
        return convertList(list, address -> new TradeOrderAdminOnlineAddressRespVO()
                .setId(address.getId())
                .setName(address.getName())
                .setMobile(address.getMobile())
                .setAreaId(address.getAreaId())
                .setAreaName(AreaUtils.format(address.getAreaId()))
                .setDetailAddress(address.getDetailAddress())
                .setDefaultStatus(address.getDefaultStatus()));
    }

}
