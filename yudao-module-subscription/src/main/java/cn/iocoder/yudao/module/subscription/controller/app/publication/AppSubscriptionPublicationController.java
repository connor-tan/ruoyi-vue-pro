package cn.iocoder.yudao.module.subscription.controller.app.publication;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.app.publication.vo.AppSubscriptionPublicationPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.app.publication.vo.AppSubscriptionPublicationRespVO;
import cn.iocoder.yudao.module.subscription.service.app.SubscriptionAppQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 订刊刊物")
@RestController
@RequestMapping("/subscription/app/publication")
@Validated
public class AppSubscriptionPublicationController {

    @Resource
    private SubscriptionAppQueryService subscriptionAppQueryService;

    @GetMapping("/page")
    @Operation(summary = "获得当前孩子可见刊物分页")
    public CommonResult<PageResult<AppSubscriptionPublicationRespVO>> getPublicationPage(
            @Valid AppSubscriptionPublicationPageReqVO pageReqVO) {
        return success(subscriptionAppQueryService.getPublicationPage(getLoginUserId(), pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得当前孩子可见刊物详情")
    public CommonResult<AppSubscriptionPublicationRespVO> getPublication(
            @RequestParam("studentId") @Parameter(required = true) Long studentId,
            @RequestParam("productSpuId") @Parameter(required = true) Long productSpuId) {
        return success(subscriptionAppQueryService.getPublication(getLoginUserId(), studentId, productSpuId));
    }
}
