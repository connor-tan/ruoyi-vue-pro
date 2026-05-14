package cn.iocoder.yudao.module.trade.controller.app.aftersale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppAfterSaleCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppAfterSaleDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppAfterSalePageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppAfterSaleRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.AfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.service.aftersale.AfterSaleService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 交易售后")
@RestController
@RequestMapping("/trade/after-sale")
@Validated
@Slf4j
public class AppAfterSaleController {

    @Resource
    private AfterSaleService afterSaleService;
    @Resource
    private TradeOrderQueryService tradeOrderQueryService;

    @GetMapping(value = "/page")
    @Operation(summary = "获得售后分页")
    public CommonResult<PageResult<AppAfterSaleRespVO>> getAfterSalePage(AppAfterSalePageReqVO pageReqVO) {
        PageResult<AfterSaleDO> pageResult = afterSaleService.getAfterSalePage(getLoginUserId(), pageReqVO);
        PageResult<AppAfterSaleRespVO> respPage = BeanUtils.toBean(pageResult, AppAfterSaleRespVO.class);
        List<TradeOrderItemDO> orderItems = tradeOrderQueryService.getOrderItemListByOrderId(
                convertSet(pageResult.getList(), AfterSaleDO::getOrderId));
        Map<Long, TradeOrderItemDO> orderItemMap = convertMap(orderItems, TradeOrderItemDO::getId);
        respPage.getList().forEach(afterSale -> fillOrderItemSnapshot(afterSale,
                orderItemMap.get(afterSale.getOrderItemId())));
        return success(respPage);
    }

    @GetMapping(value = "/get")
    @Operation(summary = "获得售后订单")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    public CommonResult<AppAfterSaleRespVO> getAfterSale(@RequestParam("id") Long id) {
        AfterSaleDO afterSale = afterSaleService.getAfterSale(getLoginUserId(), id);
        AppAfterSaleRespVO respVO = BeanUtils.toBean(afterSale, AppAfterSaleRespVO.class);
        fillOrderItemSnapshot(respVO, afterSale == null ? null :
                tradeOrderQueryService.getOrderItem(afterSale.getOrderItemId()));
        return success(respVO);
    }

    @PostMapping(value = "/create")
    @Operation(summary = "申请售后")
    public CommonResult<Long> createAfterSale(@RequestBody AppAfterSaleCreateReqVO createReqVO) {
        return success(afterSaleService.createAfterSale(getLoginUserId(), createReqVO));
    }

    @PutMapping(value = "/delivery")
    @Operation(summary = "退回货物")
    public CommonResult<Boolean> deliveryAfterSale(@RequestBody AppAfterSaleDeliveryReqVO deliveryReqVO) {
        afterSaleService.deliveryAfterSale(getLoginUserId(), deliveryReqVO);
        return success(true);
    }

    @DeleteMapping(value = "/cancel")
    @Operation(summary = "取消售后")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    public CommonResult<Boolean> cancelAfterSale(@RequestParam("id") Long id) {
        afterSaleService.cancelAfterSale(getLoginUserId(), id);
        return success(true);
    }

    private void fillOrderItemSnapshot(AppAfterSaleRespVO afterSale, TradeOrderItemDO orderItem) {
        if (afterSale == null) {
            return;
        }
        if (afterSale.getProperties() == null) {
            afterSale.setProperties(Collections.emptyList());
        }
        if (orderItem == null) {
            return;
        }
        afterSale.setSubscriptionStudentId(orderItem.getSubscriptionStudentId());
        afterSale.setSubscriptionStudentNameSnapshot(orderItem.getSubscriptionStudentNameSnapshot());
        afterSale.setSubscriptionSchoolId(orderItem.getSubscriptionSchoolId());
        afterSale.setSubscriptionSchoolNameSnapshot(orderItem.getSubscriptionSchoolNameSnapshot());
        afterSale.setSubscriptionClassId(orderItem.getSubscriptionClassId());
        afterSale.setSubscriptionClassNameSnapshot(orderItem.getSubscriptionClassNameSnapshot());
        afterSale.setSubscriptionGradeCatalogId(orderItem.getSubscriptionGradeCatalogId());
        afterSale.setSubscriptionGradeNameSnapshot(orderItem.getSubscriptionGradeNameSnapshot());
        afterSale.setSubscriptionWindowId(orderItem.getSubscriptionWindowId());
        afterSale.setSubscriptionWindowNameSnapshot(orderItem.getSubscriptionWindowNameSnapshot());
        afterSale.setSubscriptionTargetYearStart(orderItem.getSubscriptionTargetYearStart());
        afterSale.setSubscriptionTargetYearEnd(orderItem.getSubscriptionTargetYearEnd());
        afterSale.setSubscriptionOfferId(orderItem.getSubscriptionOfferId());
        afterSale.setSubscriptionOfferSkuId(orderItem.getSubscriptionOfferSkuId());
    }

}
