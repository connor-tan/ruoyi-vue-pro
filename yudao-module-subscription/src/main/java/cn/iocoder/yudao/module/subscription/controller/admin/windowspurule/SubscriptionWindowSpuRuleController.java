package cn.iocoder.yudao.module.subscription.controller.admin.windowspurule;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo.SubscriptionWindowSpuRulePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo.SubscriptionWindowSpuRuleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo.SubscriptionWindowSpuRuleSaveReqVO;
import cn.iocoder.yudao.module.subscription.service.windowspurule.SubscriptionWindowSpuRuleService;
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

@Tag(name = "管理后台 - 窗口刊物特殊规则")
@RestController
@RequestMapping("/subscription/window-spu-rule")
@Validated
public class SubscriptionWindowSpuRuleController {

    @Resource
    private SubscriptionWindowSpuRuleService subscriptionWindowSpuRuleService;

    @GetMapping("/page")
    @Operation(summary = "获得窗口刊物特殊规则分页")
    @PreAuthorize("@ss.hasPermission('subscription:window-spu-rule:query')")
    public CommonResult<PageResult<SubscriptionWindowSpuRuleRespVO>> getPage(
            @Valid SubscriptionWindowSpuRulePageReqVO reqVO) {
        return success(subscriptionWindowSpuRuleService.getWindowSpuRulePage(reqVO));
    }

    @PostMapping("/create")
    @Operation(summary = "创建窗口刊物特殊规则")
    @PreAuthorize("@ss.hasPermission('subscription:window-spu-rule:create')")
    public CommonResult<Long> create(@Valid @RequestBody SubscriptionWindowSpuRuleSaveReqVO reqVO) {
        return success(subscriptionWindowSpuRuleService.createWindowSpuRule(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新窗口刊物特殊规则")
    @PreAuthorize("@ss.hasPermission('subscription:window-spu-rule:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SubscriptionWindowSpuRuleSaveReqVO reqVO) {
        subscriptionWindowSpuRuleService.updateWindowSpuRule(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除窗口刊物特殊规则")
    @PreAuthorize("@ss.hasPermission('subscription:window-spu-rule:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") @Parameter(required = true) Long id) {
        subscriptionWindowSpuRuleService.deleteWindowSpuRule(id);
        return success(true);
    }
}
