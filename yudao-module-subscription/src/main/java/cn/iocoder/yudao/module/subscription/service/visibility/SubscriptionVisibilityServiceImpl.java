package cn.iocoder.yudao.module.subscription.service.visibility;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.*;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.subscription.controller.admin.preview.vo.SubscriptionRulePreviewRespVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuGradeDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuRuleDO;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionBlockedReasonEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleScopeTypeEnum;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionGradeResolveRespBO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionVisibleSpuBO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import cn.iocoder.yudao.module.subscription.service.windowsku.SubscriptionWindowSkuService;
import cn.iocoder.yudao.module.subscription.service.windowspu.SubscriptionWindowSpuService;
import cn.iocoder.yudao.module.subscription.service.windowspurule.SubscriptionWindowSpuRuleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Validated
public class SubscriptionVisibilityServiceImpl implements SubscriptionVisibilityService {

    @Resource
    private SubscriptionWindowService subscriptionWindowService;
    @Resource
    private SubscriptionWindowSpuService subscriptionWindowSpuService;
    @Resource
    private SubscriptionWindowSkuService subscriptionWindowSkuService;
    @Resource
    private SubscriptionWindowSpuRuleService subscriptionWindowSpuRuleService;
    @Resource
    private SubscriptionSupportService subscriptionSupportService;
    @Resource
    private SubscriptionGradeResolveService subscriptionGradeResolveService;

    @Override
    public SubscriptionVisibilityResultBO calculate(Long studentId, Long windowId) {
        SubscriptionWindowDO window = subscriptionWindowService.getWindowDO(windowId);
        SubscriptionVisibilityResultBO resultBO = new SubscriptionVisibilityResultBO();
        resultBO.setWindow(window);
        if (!isWindowOpen(window)) {
            resultBO.setBlockedReason(SubscriptionBlockedReasonEnum.WINDOW_NOT_OPEN.getReason());
            resultBO.setBlockedReasonDesc(SubscriptionBlockedReasonEnum.WINDOW_NOT_OPEN.getDescription());
            return resultBO;
        }

        SubscriptionGradeResolveRespBO gradeResolve = subscriptionGradeResolveService.resolve(studentId, window);
        resultBO.setGradeResolve(gradeResolve);
        if (gradeResolve.getBlockedReason() != null) {
            resultBO.setBlockedReason(gradeResolve.getBlockedReason());
            resultBO.setBlockedReasonDesc(gradeResolve.getBlockedReasonDesc());
            return resultBO;
        }

        List<SubscriptionWindowSpuDO> windowSpus = subscriptionWindowSpuService.getWindowSpuDOListByWindowId(windowId);
        if (windowSpus.isEmpty()) {
            resultBO.setVisibleSpus(Collections.emptyList());
            return resultBO;
        }
        List<Long> windowSpuIds = CollectionUtils.convertList(windowSpus, SubscriptionWindowSpuDO::getId);
        Map<Long, List<SubscriptionWindowSpuGradeDO>> gradeMap = subscriptionWindowSpuService.getGradeDOMap(windowSpuIds);
        Map<Long, List<SubscriptionWindowSpuRuleDO>> ruleMap =
                CollectionUtils.convertMultiMap(subscriptionWindowSpuRuleService.getWindowSpuRuleDOList(windowSpuIds),
                        SubscriptionWindowSpuRuleDO::getWindowSpuId);
        Map<Long, List<SubscriptionWindowSkuDO>> skuMap =
                CollectionUtils.convertMultiMap(subscriptionWindowSkuService.getWindowSkuDOList(windowSpuIds),
                        SubscriptionWindowSkuDO::getWindowSpuId);
        Map<Long, ProductSpuDO> productSpuMap = subscriptionSupportService.getPublicationSpuMap(CollectionUtils.convertSet(
                windowSpus, SubscriptionWindowSpuDO::getProductSpuId));

        List<SubscriptionVisibleSpuBO> visibleSpus = windowSpus.stream()
                .filter(windowSpu -> isSpuEnabled(windowSpu, productSpuMap))
                .map(windowSpu -> buildVisibleSpu(windowSpu, gradeResolve, gradeMap, ruleMap, skuMap))
                .filter(Objects::nonNull)
                .toList();
        resultBO.setVisibleSpus(visibleSpus);
        return resultBO;
    }

    @Override
    public SubscriptionRulePreviewRespVO preview(Long studentId, Long windowId) {
        SubscriptionVisibilityResultBO resultBO = calculate(studentId, windowId);
        SubscriptionRulePreviewRespVO respVO = new SubscriptionRulePreviewRespVO();
        SubscriptionGradeResolveRespBO gradeResolve = resultBO.getGradeResolve();
        if (gradeResolve != null) {
            respVO.setStudentId(gradeResolve.getStudentId());
            respVO.setStudentName(gradeResolve.getStudentName());
            respVO.setSchoolId(gradeResolve.getSchoolId());
            respVO.setSchoolName(gradeResolve.getSchoolName());
            respVO.setEffectiveGradeCatalogId(gradeResolve.getEffectiveGradeCatalogId());
            respVO.setEffectiveGradeNo(gradeResolve.getEffectiveGradeNo());
            respVO.setEffectiveGradeName(gradeResolve.getEffectiveGradeName());
            respVO.setEffectiveGradeAliasName(gradeResolve.getEffectiveGradeAliasName());
        }
        if (resultBO.getBlockedReasonDesc() != null) {
            respVO.setBlockedReason(resultBO.getBlockedReasonDesc());
            respVO.setPublications(Collections.emptyList());
            return respVO;
        }
        if (CollUtil.isEmpty(resultBO.getVisibleSpus())) {
            respVO.setPublications(Collections.emptyList());
            return respVO;
        }

        List<Long> productSpuIds = resultBO.getVisibleSpus().stream()
                .map(item -> item.getWindowSpu().getProductSpuId())
                .distinct()
                .toList();
        List<Long> productSkuIds = resultBO.getVisibleSpus().stream()
                .flatMap(item -> item.getWindowSkus().stream())
                .map(SubscriptionWindowSkuDO::getProductSkuId)
                .distinct()
                .toList();
        Map<Long, ProductSpuDO> productSpuMap = subscriptionSupportService.getPublicationSpuMap(productSpuIds);
        Map<Long, ProductCategoryDO> categoryMap = subscriptionSupportService.getCategoryMap(productSpuMap.values().stream()
                .map(ProductSpuDO::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        Map<Long, List<ProductSkuDO>> productSkuMap = subscriptionSupportService.getSkuMapBySpuIds(productSpuIds);
        Map<Long, ProductSkuPublicationDO> skuPublicationMap = subscriptionSupportService.getSkuPublicationMap(productSkuIds);
        Map<Long, ProductSpuPublicationDO> spuPublicationMap = subscriptionSupportService.getSpuPublicationMap(productSpuIds);
        Map<Long, ProductPublicationTitleDO> titleMap = buildTitleMap(spuPublicationMap);

        respVO.setPublications(resultBO.getVisibleSpus().stream()
                .map(item -> buildPreviewPublication(item, productSpuMap, categoryMap, productSkuMap, skuPublicationMap,
                        spuPublicationMap, titleMap))
                .filter(Objects::nonNull)
                .toList());
        return respVO;
    }

    private boolean isWindowOpen(SubscriptionWindowDO window) {
        LocalDateTime now = LocalDateTime.now();
        return CommonStatusEnum.isEnable(window.getStatus())
                && !now.isBefore(window.getStartTime())
                && now.isBefore(window.getEndTime());
    }

    private boolean isSpuEnabled(SubscriptionWindowSpuDO windowSpu, Map<Long, ProductSpuDO> productSpuMap) {
        ProductSpuDO productSpu = productSpuMap.get(windowSpu.getProductSpuId());
        return productSpu != null && ProductSpuStatusEnum.isEnable(productSpu.getStatus());
    }

    private SubscriptionVisibleSpuBO buildVisibleSpu(SubscriptionWindowSpuDO windowSpu,
                                                     SubscriptionGradeResolveRespBO gradeResolve,
                                                     Map<Long, List<SubscriptionWindowSpuGradeDO>> gradeMap,
                                                     Map<Long, List<SubscriptionWindowSpuRuleDO>> ruleMap,
                                                     Map<Long, List<SubscriptionWindowSkuDO>> skuMap) {
        if (!matchVisibility(windowSpu, gradeResolve, gradeMap, ruleMap)) {
            return null;
        }
        List<SubscriptionWindowSkuDO> enabledSkus = skuMap.getOrDefault(windowSpu.getId(), Collections.emptyList()).stream()
                .filter(item -> CommonStatusEnum.isEnable(item.getStatus()))
                .sorted((item1, item2) -> {
                    int sortCompare = Integer.compare(item1.getSort(), item2.getSort());
                    return sortCompare != 0 ? sortCompare : Long.compare(item1.getId(), item2.getId());
                })
                .toList();
        if (enabledSkus.isEmpty()) {
            return null;
        }
        SubscriptionVisibleSpuBO visibleSpu = new SubscriptionVisibleSpuBO();
        visibleSpu.setWindowSpu(windowSpu);
        visibleSpu.setWindowSkus(enabledSkus);
        return visibleSpu;
    }

    private boolean matchVisibility(SubscriptionWindowSpuDO windowSpu,
                                    SubscriptionGradeResolveRespBO gradeResolve,
                                    Map<Long, List<SubscriptionWindowSpuGradeDO>> gradeMap,
                                    Map<Long, List<SubscriptionWindowSpuRuleDO>> ruleMap) {
        List<SubscriptionWindowSpuRuleDO> rules = ruleMap.getOrDefault(windowSpu.getId(), Collections.emptyList());
        boolean excludeHit = rules.stream()
                .filter(rule -> Objects.equals(rule.getEffectType(), SubscriptionRuleEffectTypeEnum.EXCLUDE.getType()))
                .anyMatch(rule -> matchRule(rule, gradeResolve));
        if (excludeHit) {
            return false;
        }
        boolean includeHit = rules.stream()
                .filter(rule -> Objects.equals(rule.getEffectType(), SubscriptionRuleEffectTypeEnum.INCLUDE.getType()))
                .anyMatch(rule -> matchRule(rule, gradeResolve));
        if (includeHit) {
            return true;
        }
        return gradeMap.getOrDefault(windowSpu.getId(), Collections.emptyList()).stream()
                .map(SubscriptionWindowSpuGradeDO::getGradeCatalogId)
                .anyMatch(gradeCatalogId -> Objects.equals(gradeCatalogId, gradeResolve.getEffectiveGradeCatalogId()));
    }

    private boolean matchRule(SubscriptionWindowSpuRuleDO rule, SubscriptionGradeResolveRespBO gradeResolve) {
        if (Objects.equals(rule.getScopeType(), SubscriptionRuleScopeTypeEnum.ALL.getType())) {
            return true;
        }
        if (Objects.equals(rule.getScopeType(), SubscriptionRuleScopeTypeEnum.SCHOOL.getType())) {
            return Objects.equals(rule.getSchoolId(), gradeResolve.getSchoolId());
        }
        if (Objects.equals(rule.getScopeType(), SubscriptionRuleScopeTypeEnum.GRADE.getType())) {
            return Objects.equals(rule.getGradeCatalogId(), gradeResolve.getEffectiveGradeCatalogId());
        }
        if (Objects.equals(rule.getScopeType(), SubscriptionRuleScopeTypeEnum.SCHOOL_GRADE.getType())) {
            return Objects.equals(rule.getSchoolId(), gradeResolve.getSchoolId())
                    && Objects.equals(rule.getGradeCatalogId(), gradeResolve.getEffectiveGradeCatalogId());
        }
        return false;
    }

    private Map<Long, ProductPublicationTitleDO> buildTitleMap(Map<Long, ProductSpuPublicationDO> spuPublicationMap) {
        Map<Long, ProductPublicationTitleDO> titleMap = Collections.emptyMap();
        if (!spuPublicationMap.isEmpty()) {
            titleMap = subscriptionSupportService.getPublicationTitleMap(spuPublicationMap.values().stream()
                    .map(ProductSpuPublicationDO::getPublicationTitleId)
                    .collect(Collectors.toSet()));
        }
        return titleMap;
    }

    private SubscriptionRulePreviewRespVO.Publication buildPreviewPublication(
            SubscriptionVisibleSpuBO visibleSpu,
            Map<Long, ProductSpuDO> productSpuMap,
            Map<Long, ProductCategoryDO> categoryMap,
            Map<Long, List<ProductSkuDO>> productSkuMap,
            Map<Long, ProductSkuPublicationDO> skuPublicationMap,
            Map<Long, ProductSpuPublicationDO> spuPublicationMap,
            Map<Long, ProductPublicationTitleDO> titleMap) {
        SubscriptionWindowSpuDO windowSpu = visibleSpu.getWindowSpu();
        ProductSpuDO productSpu = productSpuMap.get(windowSpu.getProductSpuId());
        if (productSpu == null) {
            return null;
        }
        SubscriptionRulePreviewRespVO.Publication publication = new SubscriptionRulePreviewRespVO.Publication();
        publication.setWindowSpuId(windowSpu.getId());
        publication.setProductSpuId(productSpu.getId());
        publication.setProductName(productSpu.getName());
        publication.setPicUrl(productSpu.getPicUrl());
        publication.setCategoryId(productSpu.getCategoryId());
        ProductCategoryDO category = categoryMap.get(productSpu.getCategoryId());
        publication.setCategoryName(category == null ? null : category.getName());
        ProductSpuPublicationDO spuPublication = spuPublicationMap.get(productSpu.getId());
        ProductPublicationTitleDO title = spuPublication == null ? null : titleMap.get(spuPublication.getPublicationTitleId());
        publication.setPublicationTitleName(title == null ? null : title.getName());
        publication.setRecommendFlag(Boolean.TRUE.equals(windowSpu.getRecommendFlag()));
        Map<Long, ProductSkuDO> skuIdMap = CollectionUtils.convertMap(productSkuMap.getOrDefault(productSpu.getId(), Collections.emptyList()),
                ProductSkuDO::getId);
        publication.setSkus(visibleSpu.getWindowSkus().stream()
                .map(windowSku -> buildPreviewSku(windowSku, skuIdMap.get(windowSku.getProductSkuId()),
                        skuPublicationMap.get(windowSku.getProductSkuId())))
                .filter(Objects::nonNull)
                .toList());
        return publication;
    }

    private SubscriptionRulePreviewRespVO.Sku buildPreviewSku(SubscriptionWindowSkuDO windowSku,
                                                              ProductSkuDO productSku,
                                                              ProductSkuPublicationDO skuPublication) {
        if (productSku == null) {
            return null;
        }
        SubscriptionRulePreviewRespVO.Sku sku = new SubscriptionRulePreviewRespVO.Sku();
        sku.setWindowSkuId(windowSku.getId());
        sku.setProductSkuId(productSku.getId());
        sku.setVolumeLabel(skuPublication == null ? null : skuPublication.getVolumeLabel());
        sku.setEditionLabel(skuPublication == null ? null : skuPublication.getEditionLabel());
        sku.setIsbn(skuPublication == null ? null : skuPublication.getIsbn());
        sku.setPrice(productSku.getPrice());
        sku.setStock(productSku.getStock());
        sku.setMaxQuantityPerStudent(windowSku.getMaxQuantityPerStudent());
        return sku;
    }
}
