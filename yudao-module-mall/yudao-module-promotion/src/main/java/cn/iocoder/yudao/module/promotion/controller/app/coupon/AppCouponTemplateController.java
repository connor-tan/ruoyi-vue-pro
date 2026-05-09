package cn.iocoder.yudao.module.promotion.controller.app.coupon;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.product.api.category.ProductCategoryApi;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.template.AppCouponTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.template.AppCouponTemplateRespVO;
import cn.iocoder.yudao.module.promotion.convert.coupon.CouponTemplateConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponTemplateDO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponTakeTypeEnum;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponService;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static java.util.Collections.singletonList;

@Tag(name = "用户 App - 优惠劵模板")
@RestController
@RequestMapping("/promotion/coupon-template")
@Validated
public class AppCouponTemplateController {

    @Resource
    private CouponTemplateService couponTemplateService;
    @Resource
    private CouponService couponService;

    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductCategoryApi productCategoryApi;

    @GetMapping("/get")
    @Operation(summary = "获得优惠劵模版")
    @Parameter(name = "id", description = "优惠券模板编号", required = true, example = "1024")
    @PermitAll
    public CommonResult<AppCouponTemplateRespVO> getCouponTemplate(Long id) {
        CouponTemplateDO template = couponTemplateService.getCouponTemplate(id);
        if (template == null) {
            return success(null);
        }
        // 处理是否可领取
        Map<Long, Boolean> canCanTakeMap = couponService.getUserCanCanTakeMap(getLoginUserId(), singletonList(template));
        return success(BeanUtils.toBean(template, AppCouponTemplateRespVO.class)
                .setCanTake(canCanTakeMap.get(template.getId())));
    }

    @GetMapping("/list")
    @Operation(summary = "获得优惠劵模版列表")
    @Parameters({
            @Parameter(name = "spuId", description = "商品 SPU 编号"), // 目前主要给商品详情使用
            @Parameter(name = "productScope", description = "使用类型"),
            @Parameter(name = "count", description = "数量", required = true)
    })
    @PermitAll
    public CommonResult<List<AppCouponTemplateRespVO>> getCouponTemplateList(
            @RequestParam(value = "spuId", required = false) Long spuId,
            @RequestParam(value = "productScope", required = false) Integer productScope,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {
        // 1.1 处理查询条件：领取方式 = 直接领取
        List<Integer> canTakeTypes = singletonList(CouponTakeTypeEnum.USER.getType());

        // 1.2 商品详情场景：返回全场券、指定商品券、命中任一分类或父分类的分类券
        if (productScope == null && spuId != null) {
            List<CouponTemplateDO> list = getCouponTemplateListBySpu(canTakeTypes, spuId, count);
            Map<Long, Boolean> canCanTakeMap = couponService.getUserCanCanTakeMap(getLoginUserId(), list);
            return success(CouponTemplateConvert.INSTANCE.convertAppList(list, canCanTakeMap));
        }

        // 1.3 处理查询条件：商品范围编号
        List<Long> productScopeValues = getProductScopeValues(productScope, spuId);
        if (isSpecificScopeWithoutValues(productScope, productScopeValues)) {
            return success(Collections.emptyList());
        }

        // 2. 查询
        List<CouponTemplateDO> list = couponTemplateService.getCouponTemplateList(canTakeTypes, productScope,
                productScopeValues, count);

        // 3.1 领取数量
        Map<Long, Boolean> canCanTakeMap = couponService.getUserCanCanTakeMap(getLoginUserId(), list);
        // 3.2 拼接返回
        return success(CouponTemplateConvert.INSTANCE.convertAppList(list, canCanTakeMap));
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "获得优惠劵模版列表")
    @Parameter(name = "ids", description = "优惠券模板编号列表")
    @PermitAll
    public CommonResult<List<AppCouponTemplateRespVO>> getCouponTemplateList(
            @RequestParam(value = "ids", required = false) Set<Long> ids) {
        // 1. 查询
        List<CouponTemplateDO> list = couponTemplateService.getCouponTemplateList(ids);

        // 2.1 领取数量
        Map<Long, Boolean> canCanTakeMap = couponService.getUserCanCanTakeMap(getLoginUserId(), list);
        // 2.2 拼接返回
        return success(CouponTemplateConvert.INSTANCE.convertAppList(list, canCanTakeMap));
    }

    @GetMapping("/page")
    @Operation(summary = "获得优惠劵模版分页")
    @PermitAll
    public CommonResult<PageResult<AppCouponTemplateRespVO>> getCouponTemplatePage(AppCouponTemplatePageReqVO pageReqVO) {
        // 1.1 处理查询条件：商品范围编号
        List<Long> productScopeValues = getProductScopeValues(pageReqVO.getProductScope(), pageReqVO.getSpuId());
        if (isSpecificScopeWithoutValues(pageReqVO.getProductScope(), productScopeValues)) {
            return success(PageResult.empty());
        }
        // 1.2 处理查询条件：领取方式 = 直接领取
        List<Integer> canTakeTypes = singletonList(CouponTakeTypeEnum.USER.getType());

        // 2. 分页查询
        PageResult<CouponTemplateDO> pageResult = couponTemplateService.getCouponTemplatePage(
                CouponTemplateConvert.INSTANCE.convert(pageReqVO, canTakeTypes, pageReqVO.getProductScope(), productScopeValues));

        // 3.1 领取数量
        Map<Long, Boolean> canCanTakeMap = couponService.getUserCanCanTakeMap(getLoginUserId(), pageResult.getList());
        // 3.2 拼接返回
        return success(CouponTemplateConvert.INSTANCE.convertAppPage(pageResult, canCanTakeMap));
    }

    /**
     * 获得商品的使用范围编号
     *
     * @param productScope 商品范围
     * @param spuId        商品 SPU 编号
     * @return 商品范围编号数组
     */
    private List<Long> getProductScopeValues(Integer productScope, Long spuId) {
        // 通用券：没有商品范围
        if (ObjectUtils.equalsAny(productScope, PromotionProductScopeEnum.ALL.getScope(), null)) {
            return null;
        }
        if (spuId == null) {
            return Collections.emptyList();
        }
        // 品类券：查询商品的品类编号
        if (Objects.equals(productScope, PromotionProductScopeEnum.CATEGORY.getScope())) {
            ProductSpuRespDTO spu = productSpuApi.getSpu(spuId);
            return spu == null ? Collections.emptyList()
                    : new ArrayList<>(productCategoryApi.getSelfAndAncestorCategoryIds(spu.getCategoryIds()));
        }
        // 商品劵：直接返回
        return Objects.equals(productScope, PromotionProductScopeEnum.SPU.getScope())
                ? List.of(spuId) : Collections.emptyList();
    }

    private List<CouponTemplateDO> getCouponTemplateListBySpu(List<Integer> canTakeTypes, Long spuId, Integer count) {
        ProductSpuRespDTO spu = productSpuApi.getSpu(spuId);
        if (spu == null) {
            return Collections.emptyList();
        }
        List<Long> categoryScopeIds = new ArrayList<>(productCategoryApi.getSelfAndAncestorCategoryIds(spu.getCategoryIds()));
        return couponTemplateService.getCouponTemplateListBySpu(canTakeTypes, spuId, categoryScopeIds, count);
    }

    private boolean isSpecificScopeWithoutValues(Integer productScope, List<Long> productScopeValues) {
        return productScope != null && !PromotionProductScopeEnum.isAll(productScope) && CollUtil.isEmpty(productScopeValues);
    }

}
