package cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule.vo.SubscriptionWindowPublicationRulePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule.vo.SubscriptionWindowPublicationRuleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule.vo.SubscriptionWindowPublicationRuleSaveReqVO;
import cn.iocoder.yudao.module.subscription.service.windowpublicationrule.SubscriptionWindowPublicationRuleService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 窗口刊物特殊规则")
@RestController
@RequestMapping("/subscription/window-publication-rule")
@Validated
public class SubscriptionWindowPublicationRuleController {

    @Resource
    private SubscriptionWindowPublicationRuleService subscriptionWindowPublicationRuleService;

    @PostMapping("/create")
    @Operation(summary = "创建窗口刊物特殊规则")
    @PreAuthorize("@ss.hasPermission('subscription:window-publication-rule:create')")
    public CommonResult<Long> createWindowPublicationRule(@Valid @RequestBody SubscriptionWindowPublicationRuleSaveReqVO createReqVO) {
        return success(subscriptionWindowPublicationRuleService.createWindowPublicationRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新窗口刊物特殊规则")
    @PreAuthorize("@ss.hasPermission('subscription:window-publication-rule:update')")
    public CommonResult<Boolean> updateWindowPublicationRule(@Valid @RequestBody SubscriptionWindowPublicationRuleSaveReqVO updateReqVO) {
        subscriptionWindowPublicationRuleService.updateWindowPublicationRule(updateReqVO);
        return success(true);
    }

    @GetMapping("/list-by-window-publication")
    @Operation(summary = "获得窗口刊物特殊规则列表")
    @PreAuthorize("@ss.hasPermission('subscription:window-publication-rule:query')")
    public CommonResult<List<SubscriptionWindowPublicationRuleRespVO>> getWindowPublicationRuleList(
            @Valid SubscriptionWindowPublicationRulePageReqVO pageReqVO) {
        return success(subscriptionWindowPublicationRuleService.getWindowPublicationRuleList(pageReqVO));
    }
}
