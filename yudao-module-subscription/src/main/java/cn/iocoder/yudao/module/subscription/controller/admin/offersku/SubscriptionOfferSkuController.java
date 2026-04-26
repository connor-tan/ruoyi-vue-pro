package cn.iocoder.yudao.module.subscription.controller.admin.offersku;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo.SubscriptionOfferSkuBatchUpdateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo.SubscriptionOfferSkuRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo.SubscriptionOfferSkuSaveReqVO;
import cn.iocoder.yudao.module.subscription.service.offersku.SubscriptionOfferSkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 订刊窗口 SKU")
@RestController
@RequestMapping("/subscription/offer-sku")
@Validated
public class SubscriptionOfferSkuController {

    @Resource
    private SubscriptionOfferSkuService offerSkuService;

    @GetMapping("/list")
    @Operation(summary = "获得窗口刊物 SKU 列表")
    @Parameter(name = "offerId", required = true)
    @PreAuthorize("@ss.hasPermission('subscription:offer:query')")
    public CommonResult<List<SubscriptionOfferSkuRespVO>> list(@RequestParam("offerId") Long offerId) {
        return success(offerSkuService.getOfferSkuList(offerId));
    }

    @PostMapping("/sync")
    @Operation(summary = "同步窗口刊物 SKU")
    @Parameter(name = "offerId", required = true)
    @PreAuthorize("@ss.hasPermission('subscription:offer:update')")
    public CommonResult<Integer> sync(@RequestParam("offerId") Long offerId) {
        return success(offerSkuService.syncMatchedOfferSkus(offerId));
    }

    @PostMapping("/create")
    @Operation(summary = "添加窗口刊物 SKU")
    @PreAuthorize("@ss.hasPermission('subscription:offer:update')")
    public CommonResult<Long> create(@Valid @RequestBody SubscriptionOfferSkuSaveReqVO reqVO) {
        return success(offerSkuService.saveOfferSku(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新窗口刊物 SKU")
    @PreAuthorize("@ss.hasPermission('subscription:offer:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SubscriptionOfferSkuSaveReqVO reqVO) {
        offerSkuService.saveOfferSku(reqVO);
        return success(true);
    }

    @PutMapping("/batch-update")
    @Operation(summary = "批量更新窗口刊物 SKU")
    @PreAuthorize("@ss.hasPermission('subscription:offer:update')")
    public CommonResult<Boolean> batchUpdate(@Valid @RequestBody SubscriptionOfferSkuBatchUpdateReqVO reqVO) {
        offerSkuService.batchUpdate(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除窗口刊物 SKU")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('subscription:offer:update')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        offerSkuService.deleteOfferSku(id);
        return success(true);
    }

}
