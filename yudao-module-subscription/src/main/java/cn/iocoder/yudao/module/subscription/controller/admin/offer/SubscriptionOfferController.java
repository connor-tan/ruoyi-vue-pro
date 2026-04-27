package cn.iocoder.yudao.module.subscription.controller.admin.offer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.*;
import cn.iocoder.yudao.module.subscription.service.offer.SubscriptionOfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 订刊窗口刊物")
@RestController
@RequestMapping("/subscription/offer")
@Validated
public class SubscriptionOfferController {

    @Resource
    private SubscriptionOfferService offerService;

    @PostMapping("/batch-create")
    @Operation(summary = "批量添加窗口刊物")
    @PreAuthorize("@ss.hasPermission('subscription:offer:create')")
    public CommonResult<SubscriptionOfferBatchCreateRespVO> batchCreate(
            @Valid @RequestBody SubscriptionOfferBatchCreateReqVO reqVO) {
        return success(offerService.batchCreateOffer(reqVO));
    }

    @PostMapping("/batch-create-by-query")
    @Operation(summary = "按筛选条件批量添加窗口刊物")
    @PreAuthorize("@ss.hasPermission('subscription:offer:create')")
    public CommonResult<SubscriptionOfferBatchCreateRespVO> batchCreateByQuery(
            @Valid @RequestBody SubscriptionOfferBatchCreateByQueryReqVO reqVO) {
        return success(offerService.batchCreateByQuery(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新窗口刊物")
    @PreAuthorize("@ss.hasPermission('subscription:offer:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SubscriptionOfferSaveReqVO reqVO) {
        offerService.updateOffer(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除窗口刊物")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('subscription:offer:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        offerService.deleteOffer(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得窗口刊物")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('subscription:offer:query')")
    public CommonResult<SubscriptionOfferRespVO> get(@RequestParam("id") Long id) {
        return success(offerService.getOfferResp(id));
    }

    @GetMapping("/page")
    @Operation(summary = "窗口刊物分页")
    @PreAuthorize("@ss.hasPermission('subscription:offer:query')")
    public CommonResult<PageResult<SubscriptionOfferRespVO>> page(@Valid SubscriptionOfferPageReqVO reqVO) {
        return success(offerService.getOfferPage(reqVO));
    }

    @GetMapping("/available-page")
    @Operation(summary = "窗口刊物候选分页")
    @PreAuthorize("@ss.hasPermission('subscription:offer:query')")
    public CommonResult<PageResult<SubscriptionOfferAvailableRespVO>> availablePage(
            @Valid SubscriptionOfferAvailablePageReqVO reqVO) {
        return success(offerService.getAvailablePage(reqVO));
    }

}
