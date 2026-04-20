package cn.iocoder.yudao.module.subscription.controller.admin.windowsku;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.subscription.controller.admin.windowsku.vo.SubscriptionWindowSkuBatchUpdateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowsku.vo.SubscriptionWindowSkuRespVO;
import cn.iocoder.yudao.module.subscription.service.windowsku.SubscriptionWindowSkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 窗口刊物 SKU")
@RestController
@RequestMapping("/subscription/window-sku")
@Validated
public class SubscriptionWindowSkuController {

    @Resource
    private SubscriptionWindowSkuService subscriptionWindowSkuService;

    @GetMapping("/list-by-window-spu")
    @Operation(summary = "获得窗口刊物下的 SKU 配置")
    @PreAuthorize("@ss.hasPermission('subscription:window-sku:query')")
    public CommonResult<List<SubscriptionWindowSkuRespVO>> getListByWindowSpu(
            @RequestParam("windowSpuId") @Parameter(required = true) Long windowSpuId) {
        return success(subscriptionWindowSkuService.getWindowSkuListByWindowSpuId(windowSpuId));
    }

    @PutMapping("/batch-update")
    @Operation(summary = "批量更新窗口刊物 SKU 配置")
    @PreAuthorize("@ss.hasPermission('subscription:window-sku:update')")
    public CommonResult<Boolean> batchUpdate(@Valid @RequestBody SubscriptionWindowSkuBatchUpdateReqVO reqVO) {
        subscriptionWindowSkuService.batchUpdateWindowSku(reqVO);
        return success(true);
    }
}
