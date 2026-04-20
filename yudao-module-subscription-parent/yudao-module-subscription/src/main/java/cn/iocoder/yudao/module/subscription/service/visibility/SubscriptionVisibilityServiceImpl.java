package cn.iocoder.yudao.module.subscription.service.visibility;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTitleDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuGradeDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuPublicationDO;
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
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSkuPeriodUtils;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionGradeResolveRespBO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionSpuVisibilityDecisionBO;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
public class SubscriptionVisibilityServiceImpl implements SubscriptionVisibilityService {

    private static final String REASON_BASE_GRADE_MATCH = "BASE_GRADE_MATCH";
    private static final String REASON_INCLUDE_RULE_MATCH = "INCLUDE_RULE_MATCH";
    private static final String REASON_EXCLUDE_RULE_MATCH = "EXCLUDE_RULE_MATCH";
    private static final String REASON_NO_ENABLED_MATCHING_PERIOD_SKU = "NO_ENABLED_MATCHING_PERIOD_SKU";
    private static final String REASON_GRADE_NOT_VISIBLE = "GRADE_NOT_VISIBLE";
    private static final String REASON_PRODUCT_NOT_ENABLED = "PRODUCT_NOT_ENABLED";

    private static final String DESC_BASE_GRADE_MATCH = "命中基础可见年级";
    private static final String DESC_INCLUDE_RULE_MATCH = "命中特殊允许规则";
    private static final String DESC_EXCLUDE_RULE_MATCH = "命中特殊排除规则";
    private static final String DESC_NO_ENABLED_MATCHING_PERIOD_SKU = "没有启用且匹配窗口周期的 SKU";
    private static final String DESC_GRADE_NOT_VISIBLE = "有效年级未命中基础可见年级或特殊允许规则";
    private static final String DESC_PRODUCT_NOT_ENABLED = "刊物商品不存在或未启用";

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
        Map<Long, List<SubscriptionWindowSpuRuleDO>> ruleMap = CollectionUtils.convertMultiMap(
                subscriptionWindowSpuRuleService.getWindowSpuRuleDOList(windowSpuIds),
                SubscriptionWindowSpuRuleDO::getWindowSpuId);
        Map<Long, List<SubscriptionWindowSkuDO>> skuMap = CollectionUtils.convertMultiMap(
                subscriptionWindowSkuService.getWindowSkuDOList(windowSpuIds),
                SubscriptionWindowSkuDO::getWindowSpuId);
        Map<Long, ProductSkuPublicationDO> skuPublicationMap = subscriptionSupportService.getSkuPublicationMap(
                skuMap.values().stream()
                        .flatMap(List::stream)
                        .map(SubscriptionWindowSkuDO::getProductSkuId)
                        .collect(Collectors.toSet()));
        Map<Long, ProductSpuDO> productSpuMap = subscriptionSupportService.getPublicationSpuMap(
                CollectionUtils.convertSet(windowSpus, SubscriptionWindowSpuDO::getProductSpuId));
        Map<Long, List<ProductSpuGradeDO>> productSpuGradeMap = subscriptionSupportService.getPublicationSpuGradeMap(
                CollectionUtils.convertSet(windowSpus, SubscriptionWindowSpuDO::getProductSpuId));

        List<SubscriptionSpuVisibilityDecisionBO> decisions = windowSpus.stream()
                .map(windowSpu -> buildDecision(windowSpu, gradeResolve, gradeMap, ruleMap, skuMap,
                        skuPublicationMap, window.getTargetPeriod(), productSpuMap, productSpuGradeMap))
                .toList();
        List<SubscriptionVisibleSpuBO> visibleSpus = decisions.stream()
                .filter(decision -> Boolean.TRUE.equals(decision.getVisible()))
                .map(this::buildVisibleSpu)
                .toList();
        resultBO.setDecisions(decisions);
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
            respVO.setDiagnostics(Collections.emptyList());
            return respVO;
        }

        List<Long> productSpuIds = resultBO.getDecisions().stream()
                .map(item -> item.getWindowSpu().getProductSpuId())
                .distinct()
                .toList();
        List<Long> productSkuIds = resultBO.getVisibleSpus().stream()
                .flatMap(item -> item.getWindowSkus().stream())
                .map(SubscriptionWindowSkuDO::getProductSkuId)
                .distinct()
                .toList();
        Map<Long, ProductSpuDO> productSpuMap = subscriptionSupportService.getPublicationSpuMap(productSpuIds);
        Set<Long> categoryIds = productSpuMap.values().stream()
                .map(ProductSpuDO::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ProductCategoryDO> categoryMap = subscriptionSupportService.getCategoryMap(categoryIds);
        Map<Long, List<ProductSkuDO>> productSkuMap = subscriptionSupportService.getSkuMapBySpuIds(productSpuIds);
        Map<Long, ProductSkuPublicationDO> skuPublicationMap = subscriptionSupportService.getSkuPublicationMap(productSkuIds);
        Map<Long, ProductSpuPublicationDO> spuPublicationMap = subscriptionSupportService.getSpuPublicationMap(productSpuIds);
        Map<Long, ProductPublicationTitleDO> titleMap = buildTitleMap(spuPublicationMap);

        respVO.setPublications(CollUtil.isEmpty(resultBO.getVisibleSpus())
                ? Collections.emptyList()
                : resultBO.getVisibleSpus().stream()
                        .map(item -> buildPreviewPublication(item, productSpuMap, categoryMap, productSkuMap, skuPublicationMap,
                                spuPublicationMap, titleMap))
                        .filter(Objects::nonNull)
                        .toList());
        respVO.setDiagnostics(resultBO.getDecisions().stream()
                .map(decision -> buildDiagnostic(decision, productSpuMap))
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

    private SubscriptionSpuVisibilityDecisionBO buildDecision(SubscriptionWindowSpuDO windowSpu,
                                                             SubscriptionGradeResolveRespBO gradeResolve,
                                                             Map<Long, List<SubscriptionWindowSpuGradeDO>> gradeMap,
                                                             Map<Long, List<SubscriptionWindowSpuRuleDO>> ruleMap,
                                                             Map<Long, List<SubscriptionWindowSkuDO>> skuMap,
                                                             Map<Long, ProductSkuPublicationDO> skuPublicationMap,
                                                             String windowTargetPeriod,
                                                             Map<Long, ProductSpuDO> productSpuMap,
                                                             Map<Long, List<ProductSpuGradeDO>> productSpuGradeMap) {
        List<SubscriptionWindowSkuDO> allWindowSkus = skuMap.getOrDefault(windowSpu.getId(), Collections.emptyList());
        List<SubscriptionWindowSkuDO> enabledSkus = allWindowSkus.stream()
                .filter(item -> CommonStatusEnum.isEnable(item.getStatus()))
                .filter(item -> SubscriptionSkuPeriodUtils.isMatched(
                        skuPublicationMap.get(item.getProductSkuId()), windowTargetPeriod))
                .sorted((item1, item2) -> {
                    int sortCompare = Integer.compare(item1.getSort(), item2.getSort());
                    return sortCompare != 0 ? sortCompare : Long.compare(item1.getId(), item2.getId());
                })
                .toList();
        int enabledMismatchedSkuCount = (int) allWindowSkus.stream()
                .filter(item -> CommonStatusEnum.isEnable(item.getStatus()))
                .filter(item -> !SubscriptionSkuPeriodUtils.isMatched(
                        skuPublicationMap.get(item.getProductSkuId()), windowTargetPeriod))
                .count();
        SubscriptionSpuVisibilityDecisionBO decision = new SubscriptionSpuVisibilityDecisionBO();
        decision.setWindowSpu(windowSpu);
        decision.setEnabledSkus(enabledSkus);
        decision.setEnabledSkuCount(enabledSkus.size());
        decision.setTotalSkuCount(allWindowSkus.size());
        decision.setEnabledPeriodMismatchedSkuCount(enabledMismatchedSkuCount);
        decision.setWindowTargetPeriod(SubscriptionSkuPeriodUtils.normalizeWindowTargetPeriod(windowTargetPeriod));
        decision.setGradeApplicabilityOverride(false);
        if (!isSpuEnabled(windowSpu, productSpuMap)) {
            return fillDecision(decision, false, REASON_PRODUCT_NOT_ENABLED, DESC_PRODUCT_NOT_ENABLED, null, false);
        }
        List<SubscriptionWindowSpuRuleDO> rules = ruleMap.getOrDefault(windowSpu.getId(), Collections.emptyList());
        SubscriptionWindowSpuRuleDO excludeRule = rules.stream()
                .filter(rule -> Objects.equals(rule.getEffectType(), SubscriptionRuleEffectTypeEnum.EXCLUDE.getType()))
                .filter(rule -> matchRule(rule, gradeResolve))
                .findFirst()
                .orElse(null);
        if (excludeRule != null) {
            return fillDecision(decision, false, REASON_EXCLUDE_RULE_MATCH, DESC_EXCLUDE_RULE_MATCH,
                    excludeRule, false);
        }
        SubscriptionWindowSpuRuleDO includeRule = rules.stream()
                .filter(rule -> Objects.equals(rule.getEffectType(), SubscriptionRuleEffectTypeEnum.INCLUDE.getType()))
                .filter(rule -> matchRule(rule, gradeResolve))
                .findFirst()
                .orElse(null);
        if (includeRule != null) {
            boolean gradeOverride = !isGradeSupported(productSpuGradeMap.get(windowSpu.getProductSpuId()),
                    gradeResolve.getEffectiveGradeCatalogId());
            if (enabledSkus.isEmpty()) {
                return fillDecision(decision, false, REASON_NO_ENABLED_MATCHING_PERIOD_SKU,
                        DESC_NO_ENABLED_MATCHING_PERIOD_SKU, includeRule, gradeOverride);
            }
            return fillDecision(decision, true, REASON_INCLUDE_RULE_MATCH, DESC_INCLUDE_RULE_MATCH,
                    includeRule, gradeOverride);
        }
        boolean baseGradeMatch = gradeMap.getOrDefault(windowSpu.getId(), Collections.emptyList()).stream()
                .map(SubscriptionWindowSpuGradeDO::getGradeCatalogId)
                .anyMatch(gradeCatalogId -> Objects.equals(gradeCatalogId, gradeResolve.getEffectiveGradeCatalogId()));
        if (!baseGradeMatch) {
            return fillDecision(decision, false, REASON_GRADE_NOT_VISIBLE, DESC_GRADE_NOT_VISIBLE, null, false);
        }
        if (enabledSkus.isEmpty()) {
            return fillDecision(decision, false, REASON_NO_ENABLED_MATCHING_PERIOD_SKU,
                    DESC_NO_ENABLED_MATCHING_PERIOD_SKU, null, false);
        }
        return fillDecision(decision, true, REASON_BASE_GRADE_MATCH, DESC_BASE_GRADE_MATCH, null, false);
    }

    private SubscriptionSpuVisibilityDecisionBO fillDecision(SubscriptionSpuVisibilityDecisionBO decision,
                                                            boolean visible,
                                                            String reason,
                                                            String reasonDesc,
                                                            SubscriptionWindowSpuRuleDO matchedRule,
                                                            boolean gradeApplicabilityOverride) {
        decision.setVisible(visible);
        decision.setReason(reason);
        decision.setReasonDesc(reasonDesc);
        decision.setMatchedRule(matchedRule);
        decision.setGradeApplicabilityOverride(gradeApplicabilityOverride);
        return decision;
    }

    private SubscriptionVisibleSpuBO buildVisibleSpu(SubscriptionSpuVisibilityDecisionBO decision) {
        SubscriptionVisibleSpuBO visibleSpu = new SubscriptionVisibleSpuBO();
        visibleSpu.setWindowSpu(decision.getWindowSpu());
        visibleSpu.setWindowSkus(decision.getEnabledSkus());
        visibleSpu.setVisibilityReason(decision.getReason());
        visibleSpu.setVisibilityReasonDesc(decision.getReasonDesc());
        SubscriptionWindowSpuRuleDO matchedRule = decision.getMatchedRule();
        if (matchedRule != null) {
            visibleSpu.setMatchedRuleId(matchedRule.getId());
            visibleSpu.setMatchedRuleEffectType(matchedRule.getEffectType());
            visibleSpu.setMatchedRuleScopeType(matchedRule.getScopeType());
        }
        visibleSpu.setGradeApplicabilityOverride(decision.getGradeApplicabilityOverride());
        return visibleSpu;
    }

    private boolean isGradeSupported(List<ProductSpuGradeDO> spuGrades, Long gradeCatalogId) {
        if (gradeCatalogId == null) {
            return false;
        }
        return spuGrades != null && spuGrades.stream()
                .map(ProductSpuGradeDO::getGradeCatalogId)
                .anyMatch(id -> Objects.equals(id, gradeCatalogId));
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

    private SubscriptionRulePreviewRespVO.Diagnostic buildDiagnostic(
            SubscriptionSpuVisibilityDecisionBO decision, Map<Long, ProductSpuDO> productSpuMap) {
        SubscriptionWindowSpuDO windowSpu = decision.getWindowSpu();
        ProductSpuDO productSpu = productSpuMap.get(windowSpu.getProductSpuId());
        SubscriptionRulePreviewRespVO.Diagnostic diagnostic = new SubscriptionRulePreviewRespVO.Diagnostic();
        diagnostic.setWindowSpuId(windowSpu.getId());
        diagnostic.setProductSpuId(windowSpu.getProductSpuId());
        diagnostic.setProductName(productSpu == null ? null : productSpu.getName());
        diagnostic.setVisible(decision.getVisible());
        diagnostic.setReason(decision.getReason());
        diagnostic.setReasonDesc(decision.getReasonDesc());
        diagnostic.setGradeApplicabilityOverride(Boolean.TRUE.equals(decision.getGradeApplicabilityOverride()));
        diagnostic.setEnabledSkuCount(decision.getEnabledSkuCount());
        diagnostic.setTotalSkuCount(decision.getTotalSkuCount());
        diagnostic.setWindowTargetPeriod(decision.getWindowTargetPeriod());
        diagnostic.setEnabledPeriodMismatchedSkuCount(decision.getEnabledPeriodMismatchedSkuCount());
        diagnostic.setMatchedRule(buildMatchedRule(decision.getMatchedRule()));
        return diagnostic;
    }

    private Map<Long, ProductPublicationTitleDO> buildTitleMap(Map<Long, ProductSpuPublicationDO> spuPublicationMap) {
        if (spuPublicationMap.isEmpty()) {
            return Collections.emptyMap();
        }
        return subscriptionSupportService.getPublicationTitleMap(spuPublicationMap.values().stream()
                .map(ProductSpuPublicationDO::getPublicationTitleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
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
        publication.setVisibilityReason(visibleSpu.getVisibilityReason());
        publication.setVisibilityReasonDesc(visibleSpu.getVisibilityReasonDesc());
        publication.setGradeApplicabilityOverride(Boolean.TRUE.equals(visibleSpu.getGradeApplicabilityOverride()));
        publication.setMatchedRule(buildMatchedRule(visibleSpu));
        Map<Long, ProductSkuDO> skuIdMap = CollectionUtils.convertMap(
                productSkuMap.getOrDefault(productSpu.getId(), Collections.emptyList()), ProductSkuDO::getId);
        publication.setSkus(visibleSpu.getWindowSkus().stream()
                .map(windowSku -> buildPreviewSku(windowSku, skuIdMap.get(windowSku.getProductSkuId()),
                        skuPublicationMap.get(windowSku.getProductSkuId())))
                .filter(Objects::nonNull)
                .toList());
        return publication;
    }

    private SubscriptionRulePreviewRespVO.MatchedRule buildMatchedRule(SubscriptionWindowSpuRuleDO rule) {
        if (rule == null) {
            return null;
        }
        SubscriptionRulePreviewRespVO.MatchedRule matchedRule = new SubscriptionRulePreviewRespVO.MatchedRule();
        matchedRule.setId(rule.getId());
        matchedRule.setEffectType(rule.getEffectType());
        matchedRule.setScopeType(rule.getScopeType());
        matchedRule.setSchoolId(rule.getSchoolId());
        matchedRule.setGradeCatalogId(rule.getGradeCatalogId());
        return matchedRule;
    }

    private SubscriptionRulePreviewRespVO.MatchedRule buildMatchedRule(SubscriptionVisibleSpuBO visibleSpu) {
        if (visibleSpu.getMatchedRuleId() == null) {
            return null;
        }
        SubscriptionRulePreviewRespVO.MatchedRule matchedRule = new SubscriptionRulePreviewRespVO.MatchedRule();
        matchedRule.setId(visibleSpu.getMatchedRuleId());
        matchedRule.setEffectType(visibleSpu.getMatchedRuleEffectType());
        matchedRule.setScopeType(visibleSpu.getMatchedRuleScopeType());
        return matchedRule;
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
        sku.setTargetPeriod(SubscriptionSkuPeriodUtils.normalizeSkuTargetPeriod(skuPublication));
        sku.setIsbn(skuPublication == null ? null : skuPublication.getIsbn());
        sku.setPrice(productSku.getPrice());
        sku.setStock(productSku.getStock());
        sku.setMaxQuantityPerStudent(windowSku.getMaxQuantityPerStudent());
        return sku;
    }
}
