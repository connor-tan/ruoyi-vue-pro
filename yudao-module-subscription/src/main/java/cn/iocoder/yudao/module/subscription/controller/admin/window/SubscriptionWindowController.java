package cn.iocoder.yudao.module.subscription.controller.admin.window;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowSaveReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowUpdateStatusReqVO;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
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

@Tag(name = "管理后台 - 订刊窗口")
@RestController
@RequestMapping("/subscription/window")
@Validated
public class SubscriptionWindowController {

    @Resource
    private SubscriptionWindowService windowService;

    @PostMapping("/create")
    @Operation(summary = "创建订刊窗口")
    @PreAuthorize("@ss.hasPermission('subscription:window:create')")
    public CommonResult<Long> create(@Valid @RequestBody SubscriptionWindowSaveReqVO reqVO) {
        return success(windowService.createWindow(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新订刊窗口")
    @PreAuthorize("@ss.hasPermission('subscription:window:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SubscriptionWindowSaveReqVO reqVO) {
        windowService.updateWindow(reqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新订刊窗口状态")
    @PreAuthorize("@ss.hasPermission('subscription:window:update')")
    public CommonResult<Boolean> updateStatus(@Valid @RequestBody SubscriptionWindowUpdateStatusReqVO reqVO) {
        windowService.updateWindowStatus(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除订刊窗口")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('subscription:window:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        windowService.deleteWindow(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得订刊窗口")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('subscription:window:query')")
    public CommonResult<SubscriptionWindowRespVO> get(@RequestParam("id") Long id) {
        return success(windowService.getWindowResp(id));
    }

    @GetMapping("/page")
    @Operation(summary = "订刊窗口分页")
    @PreAuthorize("@ss.hasPermission('subscription:window:query')")
    public CommonResult<PageResult<SubscriptionWindowRespVO>> page(@Valid SubscriptionWindowPageReqVO reqVO) {
        return success(windowService.getWindowPageResp(reqVO));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "订刊窗口简表")
    @PreAuthorize("@ss.hasPermission('subscription:window:query')")
    public CommonResult<List<SubscriptionWindowRespVO>> simpleList(@RequestParam(value = "status", required = false) Integer status) {
        return success(windowService.getWindowSimpleList(status));
    }

    @GetMapping("/current")
    @Operation(summary = "获得当前开放订刊窗口")
    @PreAuthorize("@ss.hasPermission('subscription:window:query')")
    public CommonResult<SubscriptionWindowRespVO> current() {
        return success(windowService.getCurrentOpenWindowResp());
    }

}
