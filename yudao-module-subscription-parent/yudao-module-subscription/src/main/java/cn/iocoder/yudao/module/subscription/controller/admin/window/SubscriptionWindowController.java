package cn.iocoder.yudao.module.subscription.controller.admin.window;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowEnablePrecheckRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowSaveReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowUpdateStatusReqVO;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 订刊窗口")
@RestController
@RequestMapping("/subscription/window")
@Validated
public class SubscriptionWindowController {

    @Resource
    private SubscriptionWindowService subscriptionWindowService;

    @PostMapping("/create")
    @Operation(summary = "创建订刊窗口")
    @PreAuthorize("@ss.hasPermission('subscription:window:create')")
    public CommonResult<Long> createWindow(@Valid @RequestBody SubscriptionWindowSaveReqVO createReqVO) {
        return success(subscriptionWindowService.createWindow(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新订刊窗口")
    @PreAuthorize("@ss.hasPermission('subscription:window:update')")
    public CommonResult<Boolean> updateWindow(@Valid @RequestBody SubscriptionWindowSaveReqVO updateReqVO) {
        subscriptionWindowService.updateWindow(updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得订刊窗口")
    @PreAuthorize("@ss.hasPermission('subscription:window:query')")
    public CommonResult<SubscriptionWindowRespVO> getWindow(@RequestParam("id") @Parameter(required = true) Long id) {
        return success(subscriptionWindowService.getWindow(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得订刊窗口分页")
    @PreAuthorize("@ss.hasPermission('subscription:window:query')")
    public CommonResult<PageResult<SubscriptionWindowRespVO>> getWindowPage(@Valid SubscriptionWindowPageReqVO pageReqVO) {
        return success(subscriptionWindowService.getWindowPage(pageReqVO));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得订刊窗口精简列表")
    @PreAuthorize("@ss.hasPermission('subscription:window:query')")
    public CommonResult<List<SubscriptionWindowSimpleRespVO>> getWindowSimpleList() {
        return success(subscriptionWindowService.getWindowSimpleList());
    }

    @GetMapping("/precheck-enable")
    @Operation(summary = "启用订刊窗口前检查")
    @PreAuthorize("@ss.hasPermission('subscription:window:update')")
    public CommonResult<SubscriptionWindowEnablePrecheckRespVO> precheckEnableWindow(
            @RequestParam("id") @Parameter(required = true) Long id) {
        return success(subscriptionWindowService.precheckEnableWindow(id));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新订刊窗口状态")
    @PreAuthorize("@ss.hasPermission('subscription:window:update')")
    public CommonResult<Boolean> updateWindowStatus(@Valid @RequestBody SubscriptionWindowUpdateStatusReqVO reqVO) {
        subscriptionWindowService.updateWindowStatus(reqVO);
        return success(true);
    }
}
