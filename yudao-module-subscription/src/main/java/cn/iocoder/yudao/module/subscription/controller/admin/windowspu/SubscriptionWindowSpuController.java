package cn.iocoder.yudao.module.subscription.controller.admin.windowspu;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuAvailablePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuAvailableRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuBatchCreateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuBatchCreateRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuSaveReqVO;
import cn.iocoder.yudao.module.subscription.service.windowspu.SubscriptionWindowSpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 窗口刊物")
@RestController
@RequestMapping("/subscription/window-spu")
@Validated
public class SubscriptionWindowSpuController {

    @Resource
    private SubscriptionWindowSpuService subscriptionWindowSpuService;

    @GetMapping("/page")
    @Operation(summary = "获得窗口刊物分页")
    @PreAuthorize("@ss.hasPermission('subscription:window-spu:query')")
    public CommonResult<PageResult<SubscriptionWindowSpuRespVO>> getWindowSpuPage(@Valid SubscriptionWindowSpuPageReqVO reqVO) {
        return success(subscriptionWindowSpuService.getWindowSpuPage(reqVO));
    }

    @GetMapping("/available-page")
    @Operation(summary = "获得按年级可加入的窗口刊物分页")
    @PreAuthorize("@ss.hasPermission('subscription:window-spu:query')")
    public CommonResult<PageResult<SubscriptionWindowSpuAvailableRespVO>> getAvailablePage(
            @Valid SubscriptionWindowSpuAvailablePageReqVO reqVO) {
        return success(subscriptionWindowSpuService.getAvailablePage(reqVO));
    }

    @PostMapping("/batch-create")
    @Operation(summary = "按年级批量新增窗口刊物")
    @PreAuthorize("@ss.hasPermission('subscription:window-spu:create')")
    public CommonResult<SubscriptionWindowSpuBatchCreateRespVO> batchCreate(
            @Valid @RequestBody SubscriptionWindowSpuBatchCreateReqVO reqVO) {
        return success(subscriptionWindowSpuService.batchCreate(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新窗口刊物")
    @PreAuthorize("@ss.hasPermission('subscription:window-spu:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SubscriptionWindowSpuSaveReqVO reqVO) {
        subscriptionWindowSpuService.updateWindowSpu(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "移除窗口刊物")
    @PreAuthorize("@ss.hasPermission('subscription:window-spu:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") @Parameter(required = true) Long id) {
        subscriptionWindowSpuService.deleteWindowSpu(id);
        return success(true);
    }
}
