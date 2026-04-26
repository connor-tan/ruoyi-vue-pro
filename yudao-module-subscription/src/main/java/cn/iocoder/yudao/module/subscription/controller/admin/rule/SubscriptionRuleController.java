package cn.iocoder.yudao.module.subscription.controller.admin.rule;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.rule.vo.SubscriptionRulePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.rule.vo.SubscriptionRuleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.rule.vo.SubscriptionRuleSaveReqVO;
import cn.iocoder.yudao.module.subscription.service.rule.SubscriptionRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 订刊特殊规则")
@RestController
@RequestMapping("/subscription/rule")
@Validated
public class SubscriptionRuleController {

    @Resource
    private SubscriptionRuleService ruleService;

    @PostMapping("/create")
    @Operation(summary = "创建订刊特殊规则")
    @PreAuthorize("@ss.hasPermission('subscription:rule:create')")
    public CommonResult<Long> create(@Valid @RequestBody SubscriptionRuleSaveReqVO reqVO) {
        return success(ruleService.createRule(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新订刊特殊规则")
    @PreAuthorize("@ss.hasPermission('subscription:rule:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SubscriptionRuleSaveReqVO reqVO) {
        ruleService.updateRule(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除订刊特殊规则")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('subscription:rule:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        ruleService.deleteRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得订刊特殊规则")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('subscription:rule:query')")
    public CommonResult<SubscriptionRuleRespVO> get(@RequestParam("id") Long id) {
        return success(ruleService.getRuleResp(id));
    }

    @GetMapping("/page")
    @Operation(summary = "订刊特殊规则分页")
    @PreAuthorize("@ss.hasPermission('subscription:rule:query')")
    public CommonResult<PageResult<SubscriptionRuleRespVO>> page(@Valid SubscriptionRulePageReqVO reqVO) {
        return success(ruleService.getRulePage(reqVO));
    }

}
