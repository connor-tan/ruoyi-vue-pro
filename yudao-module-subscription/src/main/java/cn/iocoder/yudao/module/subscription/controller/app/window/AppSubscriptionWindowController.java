package cn.iocoder.yudao.module.subscription.controller.app.window;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.subscription.controller.app.window.vo.AppSubscriptionCurrentWindowRespVO;
import cn.iocoder.yudao.module.subscription.service.app.SubscriptionAppQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 当前订刊窗口")
@RestController
@RequestMapping("/subscription/window")
@Validated
public class AppSubscriptionWindowController {

    @Resource
    private SubscriptionAppQueryService subscriptionAppQueryService;

    @GetMapping("/current")
    @Operation(summary = "获得当前开放中的订刊窗口")
    @PermitAll
    public CommonResult<AppSubscriptionCurrentWindowRespVO> getCurrentWindow() {
        return success(subscriptionAppQueryService.getCurrentWindow());
    }
}
