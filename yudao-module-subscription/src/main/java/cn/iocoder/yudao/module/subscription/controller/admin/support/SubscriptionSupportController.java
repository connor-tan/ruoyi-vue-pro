package cn.iocoder.yudao.module.subscription.controller.admin.support;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportSchoolYearSimpleRespVO;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 订刊规则中心基础引用数据")
@RestController
@RequestMapping("/subscription/support")
@Validated
public class SubscriptionSupportController {

    @Resource
    private SubscriptionSupportService subscriptionSupportService;

    @GetMapping("/school-year/simple-list")
    @Operation(summary = "获得学年精简列表")
    @PreAuthorize("@ss.hasAnyPermissions('subscription:window:query', 'subscription:window:create', 'subscription:window:update')")
    public CommonResult<List<SubscriptionSupportSchoolYearSimpleRespVO>> getSchoolYearSimpleList(
            @RequestParam(value = "schoolId", required = false) Long schoolId) {
        return success(subscriptionSupportService.getSchoolYearSimpleList(schoolId));
    }
}
