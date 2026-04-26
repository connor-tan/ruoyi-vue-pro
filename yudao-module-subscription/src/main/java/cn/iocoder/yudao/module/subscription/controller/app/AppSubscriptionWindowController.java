package cn.iocoder.yudao.module.subscription.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.subscription.controller.SubscriptionVisibilityVOAssembler;
import cn.iocoder.yudao.module.subscription.controller.app.vo.AppSubscriptionWindowRespVO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 订刊窗口")
@RestController
@RequestMapping("/subscription/app/window")
@Validated
public class AppSubscriptionWindowController {

    @Resource
    private SubscriptionVisibilityService visibilityService;

    @GetMapping("/current")
    @Operation(summary = "获得当前订刊窗口状态")
    @Parameter(name = "studentId", required = true)
    public CommonResult<AppSubscriptionWindowRespVO> current(@RequestParam("studentId") Long studentId) {
        SubscriptionVisibilityResultBO result = visibilityService.calculate(getLoginUserId(), studentId, null);
        AppSubscriptionWindowRespVO respVO = new AppSubscriptionWindowRespVO();
        respVO.setWindow(SubscriptionVisibilityVOAssembler.buildAppWindow(result.getWindow()));
        respVO.setStudent(SubscriptionVisibilityVOAssembler.buildAppStudent(result.getStudent()));
        respVO.setBlockedReason(result.getBlockedReason());
        respVO.setBlockedReasonDesc(result.getBlockedReasonDesc());
        return success(respVO);
    }

}
