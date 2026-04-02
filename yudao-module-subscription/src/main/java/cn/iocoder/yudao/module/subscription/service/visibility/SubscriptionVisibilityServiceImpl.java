package cn.iocoder.yudao.module.subscription.service.visibility;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.subscription.controller.admin.preview.vo.SubscriptionRulePreviewPublicationRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.preview.vo.SubscriptionRulePreviewRespVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowPublicationDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowPublicationGradeDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowPublicationRuleDO;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionBlockedReasonEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleScopeTypeEnum;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionGradeResolveRespBO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import cn.iocoder.yudao.module.subscription.service.windowpublication.SubscriptionWindowPublicationService;
import cn.iocoder.yudao.module.subscription.service.windowpublicationrule.SubscriptionWindowPublicationRuleService;
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
    private SubscriptionWindowPublicationService subscriptionWindowPublicationService;
    @Resource
    private SubscriptionWindowPublicationRuleService subscriptionWindowPublicationRuleService;
    @Resource
    private SubscriptionSupportService subscriptionSupportService;
    @Resource
    private SubscriptionGradeResolveService subscriptionGradeResolveService;

    @Override
    public SubscriptionVisibilityResultBO calculate(Long studentId, Long windowId) {
        SubscriptionWindowDO window = subscriptionWindowService.getWindowDO(windowId);
        SubscriptionVisibilityResultBO resultBO = new SubscriptionVisibilityResultBO();
        resultBO.setWindow(window);

        SubscriptionGradeResolveRespBO gradeResolve = subscriptionGradeResolveService.resolve(studentId, window);
        resultBO.setGradeResolve(gradeResolve);
        if (gradeResolve.getBlockedReason() != null) {
            resultBO.setBlockedReason(gradeResolve.getBlockedReason());
            resultBO.setBlockedReasonDesc(gradeResolve.getBlockedReasonDesc());
            return resultBO;
        }
        if (!isWindowOpen(window)) {
            resultBO.setBlockedReason(SubscriptionBlockedReasonEnum.WINDOW_NOT_OPEN.getReason());
            resultBO.setBlockedReasonDesc(SubscriptionBlockedReasonEnum.WINDOW_NOT_OPEN.getDescription());
            return resultBO;
        }

        List<SubscriptionWindowPublicationDO> windowPublications = subscriptionWindowPublicationService
                .getWindowPublicationDOListByWindowId(windowId).stream()
                .filter(windowPublication -> CommonStatusEnum.isEnable(windowPublication.getStatus()))
                .toList();
        if (windowPublications.isEmpty()) {
            resultBO.setVisibleWindowPublications(Collections.emptyList());
            return resultBO;
        }

        List<Long> windowPublicationIds = windowPublications.stream()
                .map(SubscriptionWindowPublicationDO::getId)
                .toList();
        Map<Long, List<SubscriptionWindowPublicationGradeDO>> gradeMap =
                subscriptionWindowPublicationService.getGradeDOMap(windowPublicationIds);
        Map<Long, List<SubscriptionWindowPublicationRuleDO>> ruleMap =
                CollectionUtils.convertMultiMap(subscriptionWindowPublicationRuleService.getWindowPublicationRuleDOList(windowPublicationIds),
                        SubscriptionWindowPublicationRuleDO::getWindowPublicationId);
        Map<Long, ProductSpuDO> productSpuMap = subscriptionSupportService.getProductSpuMap(windowPublications.stream()
                .map(SubscriptionWindowPublicationDO::getProductSpuId)
                .distinct()
                .toList());
        Map<Long, ProductSkuDO> singleSpecSkuMap = subscriptionSupportService.getSingleSpecSkuMap(windowPublications.stream()
                .map(SubscriptionWindowPublicationDO::getProductSpuId)
                .distinct()
                .toList());
        List<SubscriptionWindowPublicationDO> visibleWindowPublications = windowPublications.stream()
                .filter(windowPublication -> isPublicationEnabled(windowPublication, productSpuMap, singleSpecSkuMap))
                .filter(windowPublication -> matchVisibility(windowPublication, gradeResolve, gradeMap, ruleMap))
                .toList();
        resultBO.setVisibleWindowPublications(visibleWindowPublications);
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
        if (CollUtil.isEmpty(resultBO.getVisibleWindowPublications())) {
            respVO.setPublications(Collections.emptyList());
            return respVO;
        }
        Map<Long, ProductSpuDO> productSpuMap = subscriptionSupportService.getProductSpuMap(resultBO.getVisibleWindowPublications().stream()
                .map(SubscriptionWindowPublicationDO::getProductSpuId)
                .distinct()
                .toList());
        Map<Long, ProductCategoryDO> categoryMap =
                subscriptionSupportService.getCategoryMap(productSpuMap.values().stream()
                        .map(ProductSpuDO::getCategoryId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));
        respVO.setPublications(resultBO.getVisibleWindowPublications().stream()
                .map(windowPublication -> buildPreviewPublication(windowPublication, productSpuMap, categoryMap))
                .filter(Objects::nonNull)
                .toList());
        return respVO;
    }

    private boolean isWindowOpen(SubscriptionWindowDO window) {
        LocalDateTime now = LocalDateTime.now();
        return CommonStatusEnum.isEnable(window.getStatus())
                && !now.isBefore(window.getStartTime())
                && !now.isAfter(window.getEndTime());
    }

    private boolean isPublicationEnabled(SubscriptionWindowPublicationDO windowPublication,
                                         Map<Long, ProductSpuDO> productSpuMap,
                                         Map<Long, ProductSkuDO> singleSpecSkuMap) {
        ProductSpuDO productSpu = productSpuMap.get(windowPublication.getProductSpuId());
        return productSpu != null
                && !Boolean.TRUE.equals(productSpu.getSpecType())
                && ProductSpuStatusEnum.isEnable(productSpu.getStatus())
                && singleSpecSkuMap.containsKey(windowPublication.getProductSpuId());
    }

    private boolean matchVisibility(SubscriptionWindowPublicationDO windowPublication,
                                    SubscriptionGradeResolveRespBO gradeResolve,
                                    Map<Long, List<SubscriptionWindowPublicationGradeDO>> gradeMap,
                                    Map<Long, List<SubscriptionWindowPublicationRuleDO>> ruleMap) {
        List<SubscriptionWindowPublicationRuleDO> rules = ruleMap.getOrDefault(windowPublication.getId(), Collections.emptyList());
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
        return gradeMap.getOrDefault(windowPublication.getId(), Collections.emptyList()).stream()
                .map(SubscriptionWindowPublicationGradeDO::getGradeCatalogId)
                .anyMatch(gradeCatalogId -> Objects.equals(gradeCatalogId, gradeResolve.getEffectiveGradeCatalogId()));
    }

    private boolean matchRule(SubscriptionWindowPublicationRuleDO rule, SubscriptionGradeResolveRespBO gradeResolve) {
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

    private SubscriptionRulePreviewPublicationRespVO buildPreviewPublication(
            SubscriptionWindowPublicationDO windowPublication,
            Map<Long, ProductSpuDO> productSpuMap,
            Map<Long, ProductCategoryDO> categoryMap) {
        ProductSpuDO productSpu = productSpuMap.get(windowPublication.getProductSpuId());
        if (productSpu == null) {
            return null;
        }
        SubscriptionRulePreviewPublicationRespVO respVO = new SubscriptionRulePreviewPublicationRespVO();
        respVO.setProductSpuId(productSpu.getId());
        respVO.setProductName(productSpu.getName());
        respVO.setCategoryId(productSpu.getCategoryId());
        ProductCategoryDO category = categoryMap.get(productSpu.getCategoryId());
        respVO.setCategoryName(category != null ? category.getName() : null);
        respVO.setPicUrl(productSpu.getPicUrl());
        respVO.setPrice(productSpu.getPrice());
        if (category != null) {
            respVO.setTypeCategoryId(category.getId());
            respVO.setTypeCategoryName(category.getName());
            respVO.setSupportsGift(Boolean.TRUE.equals(category.getSupportsGift()));
        }
        respVO.setRecommendFlag(Boolean.TRUE.equals(windowPublication.getRecommendFlag()));
        respVO.setMaxQuantityPerStudent(windowPublication.getMaxQuantityPerStudent() != null
                ? windowPublication.getMaxQuantityPerStudent() : 1);
        return respVO;
    }
}
