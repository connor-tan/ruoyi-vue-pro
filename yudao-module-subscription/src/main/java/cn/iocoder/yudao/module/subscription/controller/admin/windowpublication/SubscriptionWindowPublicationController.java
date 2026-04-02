package cn.iocoder.yudao.module.subscription.controller.admin.windowpublication;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublication.vo.SubscriptionWindowPublicationPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublication.vo.SubscriptionWindowPublicationRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublication.vo.SubscriptionWindowPublicationSaveReqVO;
import cn.iocoder.yudao.module.subscription.service.windowpublication.SubscriptionWindowPublicationService;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 窗口刊物")
@RestController
@RequestMapping("/subscription/window-publication")
@Validated
public class SubscriptionWindowPublicationController {

    @Resource
    private SubscriptionWindowPublicationService subscriptionWindowPublicationService;

    @PostMapping("/create")
    @Operation(summary = "创建窗口刊物关系")
    @PreAuthorize("@ss.hasPermission('subscription:window-publication:create')")
    public CommonResult<Long> createWindowPublication(@Valid @RequestBody SubscriptionWindowPublicationSaveReqVO createReqVO) {
        return success(subscriptionWindowPublicationService.createWindowPublication(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新窗口刊物关系")
    @PreAuthorize("@ss.hasPermission('subscription:window-publication:update')")
    public CommonResult<Boolean> updateWindowPublication(@Valid @RequestBody SubscriptionWindowPublicationSaveReqVO updateReqVO) {
        subscriptionWindowPublicationService.updateWindowPublication(updateReqVO);
        return success(true);
    }

    @GetMapping("/page-by-window")
    @Operation(summary = "获得窗口刊物分页")
    @PreAuthorize("@ss.hasPermission('subscription:window-publication:query')")
    public CommonResult<PageResult<SubscriptionWindowPublicationRespVO>> getWindowPublicationPage(
            @Valid SubscriptionWindowPublicationPageReqVO pageReqVO) {
        return success(subscriptionWindowPublicationService.getWindowPublicationPage(pageReqVO));
    }
}
