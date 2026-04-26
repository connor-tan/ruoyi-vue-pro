package cn.iocoder.yudao.module.subscription.controller.admin.preview;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.subscription.controller.SubscriptionVisibilityVOAssembler;
import cn.iocoder.yudao.module.subscription.controller.admin.preview.vo.SubscriptionRulePreviewRespVO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 订刊规则预览")
@RestController
@RequestMapping("/subscription/preview")
@Validated
public class SubscriptionRulePreviewController {

    @Resource
    private SubscriptionVisibilityService visibilityService;

    @GetMapping("/student")
    @Operation(summary = "按学生预览订刊可见性")
    @Parameter(name = "studentId", required = true)
    @PreAuthorize("@ss.hasPermission('subscription:preview:query')")
    public CommonResult<SubscriptionRulePreviewRespVO> previewStudent(
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "windowId", required = false) Long windowId) {
        SubscriptionVisibilityResultBO result = visibilityService.calculate(null, studentId, windowId);
        SubscriptionRulePreviewRespVO respVO = new SubscriptionRulePreviewRespVO();
        respVO.setWindow(SubscriptionVisibilityVOAssembler.buildWindow(result.getWindow()));
        respVO.setStudent(SubscriptionVisibilityVOAssembler.buildStudent(result.getStudent()));
        respVO.setBlockedReason(result.getBlockedReason());
        respVO.setBlockedReasonDesc(result.getBlockedReasonDesc());
        respVO.setDecisions(SubscriptionVisibilityVOAssembler.buildDecisions(result.getDecisions()));
        return success(respVO);
    }

}
