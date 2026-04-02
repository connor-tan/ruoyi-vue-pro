package cn.iocoder.yudao.module.subscription.controller.admin.support;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportCategorySimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportGradeCatalogSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportProductSpuSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportPropertySimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportPropertyValueSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportSchoolSimpleRespVO;
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

    @GetMapping("/product-spu/simple-list")
    @Operation(summary = "获得刊物商品精简列表")
    @PreAuthorize("@ss.hasPermission('subscription:support:query')")
    public CommonResult<List<SubscriptionSupportProductSpuSimpleRespVO>> getProductSpuSimpleList(
            @RequestParam(value = "name", required = false) String name) {
        return success(subscriptionSupportService.getProductSpuSimpleList(name));
    }

    @GetMapping("/category/simple-list")
    @Operation(summary = "获得商品分类精简列表")
    @PreAuthorize("@ss.hasPermission('subscription:support:query')")
    public CommonResult<List<SubscriptionSupportCategorySimpleRespVO>> getCategorySimpleList() {
        return success(subscriptionSupportService.getCategorySimpleList());
    }

    @GetMapping("/publication-type-category/simple-list")
    @Operation(summary = "获得刊物类型分类精简列表")
    @PreAuthorize("@ss.hasPermission('subscription:support:query')")
    public CommonResult<List<SubscriptionSupportCategorySimpleRespVO>> getPublicationTypeCategorySimpleList() {
        return success(subscriptionSupportService.getPublicationTypeCategorySimpleList());
    }

    @GetMapping("/property/simple-list")
    @Operation(summary = "获得刊物属性项精简列表")
    @PreAuthorize("@ss.hasPermission('subscription:support:query')")
    public CommonResult<List<SubscriptionSupportPropertySimpleRespVO>> getPropertySimpleList() {
        return success(subscriptionSupportService.getPropertySimpleList());
    }

    @GetMapping("/property-value/simple-list")
    @Operation(summary = "获得刊物属性值精简列表")
    @PreAuthorize("@ss.hasPermission('subscription:support:query')")
    public CommonResult<List<SubscriptionSupportPropertyValueSimpleRespVO>> getPropertyValueSimpleList(
            @RequestParam(value = "propertyId", required = false) Long propertyId) {
        return success(subscriptionSupportService.getPropertyValueSimpleList(propertyId));
    }

    @GetMapping("/grade-catalog/simple-list")
    @Operation(summary = "获得年级目录精简列表")
    @PreAuthorize("@ss.hasPermission('subscription:support:query')")
    public CommonResult<List<SubscriptionSupportGradeCatalogSimpleRespVO>> getGradeCatalogSimpleList() {
        return success(subscriptionSupportService.getGradeCatalogSimpleList());
    }

    @GetMapping("/school/simple-list")
    @Operation(summary = "获得学校精简列表")
    @PreAuthorize("@ss.hasPermission('subscription:support:query')")
    public CommonResult<List<SubscriptionSupportSchoolSimpleRespVO>> getSchoolSimpleList() {
        return success(subscriptionSupportService.getSchoolSimpleList());
    }

    @GetMapping("/school-year/simple-list")
    @Operation(summary = "获得学年精简列表")
    @PreAuthorize("@ss.hasPermission('subscription:support:query')")
    public CommonResult<List<SubscriptionSupportSchoolYearSimpleRespVO>> getSchoolYearSimpleList(
            @RequestParam(value = "schoolId", required = false) Long schoolId) {
        return success(subscriptionSupportService.getSchoolYearSimpleList(schoolId));
    }
}
