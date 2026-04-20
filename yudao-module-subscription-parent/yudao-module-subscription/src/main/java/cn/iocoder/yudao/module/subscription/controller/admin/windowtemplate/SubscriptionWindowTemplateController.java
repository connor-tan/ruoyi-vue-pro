package cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplatePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplateRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplateSaveReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplateSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplateUpdateStatusReqVO;
import cn.iocoder.yudao.module.subscription.service.windowtemplate.SubscriptionWindowTemplateService;
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

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 订刊规则模板")
@RestController
@RequestMapping("/subscription/window-template")
@Validated
public class SubscriptionWindowTemplateController {

    @Resource
    private SubscriptionWindowTemplateService subscriptionWindowTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建订刊规则模板")
    @PreAuthorize("@ss.hasPermission('subscription:window-template:create')")
    public CommonResult<Long> createWindowTemplate(@Valid @RequestBody SubscriptionWindowTemplateSaveReqVO createReqVO) {
        return success(subscriptionWindowTemplateService.createWindowTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新订刊规则模板")
    @PreAuthorize("@ss.hasPermission('subscription:window-template:update')")
    public CommonResult<Boolean> updateWindowTemplate(@Valid @RequestBody SubscriptionWindowTemplateSaveReqVO updateReqVO) {
        subscriptionWindowTemplateService.updateWindowTemplate(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新订刊规则模板状态")
    @PreAuthorize("@ss.hasPermission('subscription:window-template:update')")
    public CommonResult<Boolean> updateWindowTemplateStatus(@Valid @RequestBody SubscriptionWindowTemplateUpdateStatusReqVO reqVO) {
        subscriptionWindowTemplateService.updateWindowTemplateStatus(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除订刊规则模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('subscription:window-template:delete')")
    public CommonResult<Boolean> deleteWindowTemplate(@RequestParam("id") Long id) {
        subscriptionWindowTemplateService.deleteWindowTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得订刊规则模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('subscription:window-template:query')")
    public CommonResult<SubscriptionWindowTemplateRespVO> getWindowTemplate(@RequestParam("id") Long id) {
        return success(subscriptionWindowTemplateService.getWindowTemplate(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得订刊规则模板分页")
    @PreAuthorize("@ss.hasPermission('subscription:window-template:query')")
    public CommonResult<PageResult<SubscriptionWindowTemplateRespVO>> getWindowTemplatePage(@Valid SubscriptionWindowTemplatePageReqVO pageReqVO) {
        return success(subscriptionWindowTemplateService.getWindowTemplatePage(pageReqVO));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用中的订刊规则模板精简列表")
    @PreAuthorize("@ss.hasAnyPermissions('subscription:window:query', 'subscription:window:create', 'subscription:window:update', 'subscription:window-template:query')")
    public CommonResult<List<SubscriptionWindowTemplateSimpleRespVO>> getWindowTemplateSimpleList() {
        return success(subscriptionWindowTemplateService.getWindowTemplateSimpleList());
    }
}
