package cn.iocoder.yudao.module.subscription.controller.admin.preview;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.subscription.controller.admin.preview.vo.SubscriptionRulePreviewExecuteReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.preview.vo.SubscriptionRulePreviewRespVO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 订刊规则预览")
@RestController
@RequestMapping("/subscription/preview")
@Validated
public class SubscriptionRulePreviewController {

    @Resource
    private SubscriptionVisibilityService subscriptionVisibilityService;

    @PostMapping("/execute")
    @Operation(summary = "预览某个学生在某个窗口下的订刊结果")
    @PreAuthorize("@ss.hasPermission('subscription:preview:query')")
    public CommonResult<SubscriptionRulePreviewRespVO> execute(@Valid @RequestBody SubscriptionRulePreviewExecuteReqVO reqVO) {
        return success(subscriptionVisibilityService.preview(reqVO.getStudentId(), reqVO.getWindowId()));
    }
}
