package cn.iocoder.yudao.module.subscription.controller.admin.support;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionRuleConditionValueRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionRuleFactorRespVO;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleFactorEnum;
import cn.iocoder.yudao.module.subscription.service.rule.SubscriptionRuleConditionValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 订刊规则支撑数据")
@RestController
@RequestMapping("/subscription/support")
@Validated
public class SubscriptionSupportController {

    @Resource
    private SubscriptionRuleConditionValueService conditionValueService;

    @GetMapping("/rule-factor-list")
    @Operation(summary = "获得规则因子列表")
    @PreAuthorize("@ss.hasPermission('subscription:rule:query')")
    public CommonResult<List<SubscriptionRuleFactorRespVO>> getRuleFactorList() {
        return success(Arrays.stream(SubscriptionRuleFactorEnum.values())
                .map(item -> new SubscriptionRuleFactorRespVO(item.getCode(), item.getName()))
                .toList());
    }

    @GetMapping("/rule-effect-list")
    @Operation(summary = "获得规则作用列表")
    @PreAuthorize("@ss.hasPermission('subscription:rule:query')")
    public CommonResult<List<SubscriptionRuleFactorRespVO>> getRuleEffectList() {
        return success(Arrays.stream(SubscriptionRuleEffectTypeEnum.values())
                .map(item -> new SubscriptionRuleFactorRespVO(item.getType(), item.getName()))
                .toList());
    }

    @GetMapping("/rule-condition-values")
    @Operation(summary = "获得规则条件值列表")
    @PreAuthorize("@ss.hasPermission('subscription:rule:query')")
    public CommonResult<List<SubscriptionRuleConditionValueRespVO>> getRuleConditionValues(
            @RequestParam("factor") String factor,
            @RequestParam(value = "windowId", required = false) Long windowId,
            @RequestParam(value = "offerId", required = false) Long offerId) {
        return success(conditionValueService.getConditionValueList(factor, windowId, offerId));
    }

}
