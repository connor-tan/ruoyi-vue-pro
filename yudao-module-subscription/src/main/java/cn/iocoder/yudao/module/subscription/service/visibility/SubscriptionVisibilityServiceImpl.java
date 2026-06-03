package cn.iocoder.yudao.module.subscription.service.visibility;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.api.student.EduStudentApi;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentSubscriptionContextRespDTO;
import cn.iocoder.yudao.module.product.api.publication.ProductPublicationApi;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.publication.api.enums.PublicationIssueModeEnum;
import cn.iocoder.yudao.module.subscription.dal.dataobject.*;
import cn.iocoder.yudao.module.subscription.dal.mysql.*;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleFactorEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionSkuDecisionStatusEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionVisibilityReasonEnum;
import cn.iocoder.yudao.module.subscription.service.rule.SubscriptionRuleService;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

@Service
@Validated
public class SubscriptionVisibilityServiceImpl implements SubscriptionVisibilityService {

    @Resource
    private SubscriptionWindowService windowService;
    @Resource
    private SubscriptionWindowOfferMapper offerMapper;
    @Resource
    private SubscriptionWindowOfferSkuMapper offerSkuMapper;
    @Resource
    private SubscriptionWindowOfferGradeRelMapper offerGradeRelMapper;
    @Resource
    private SubscriptionRuleService ruleService;
    @Resource
    private EduStudentApi eduStudentApi;
    @Resource
    private ProductPublicationApi productPublicationApi;

    @Override
    public SubscriptionVisibilityResultBO calculate(Long userId, Long studentId, Long windowId) {
        return calculate0(userId, studentId, windowId, false);
    }

    @Override
    public SubscriptionVisibilityResultBO calculateForAdmin(Long studentId, Long windowId) {
        return calculate0(null, studentId, windowId, true);
    }

    private SubscriptionVisibilityResultBO calculate0(Long userId, Long studentId, Long windowId, boolean admin) {
        SubscriptionWindowDO window = windowId == null ? windowService.getCurrentOpenWindow() : windowService.getWindow(windowId);
        SubscriptionVisibilityResultBO result = new SubscriptionVisibilityResultBO();
        result.setWindow(window);
        if (!windowService.isOpen(window)) {
            result.setBlockedReason(SubscriptionVisibilityReasonEnum.WINDOW_NOT_OPEN.getReason());
            result.setBlockedReasonDesc(SubscriptionVisibilityReasonEnum.WINDOW_NOT_OPEN.getDescription());
            result.setDecisions(Collections.emptyList());
            result.setVisibleOffers(Collections.emptyList());
            return result;
        }

        Map<Long, EduStudentSubscriptionContextRespDTO> studentContextMap = admin
                ? eduStudentApi.getAdminSubscriptionStudentContextMap(Collections.singleton(studentId),
                window.getTargetYearStart(), window.getTargetYearEnd(), window.getTargetYearCatalogId())
                : eduStudentApi.getSubscriptionStudentContextMap(userId, Collections.singleton(studentId),
                window.getTargetYearStart(), window.getTargetYearEnd(), window.getTargetYearCatalogId());
        EduStudentSubscriptionContextRespDTO student = studentContextMap
                .get(studentId);
        result.setStudent(student);
        if (student == null || student.getBlockedReason() != null) {
            result.setBlockedReason(SubscriptionVisibilityReasonEnum.STUDENT_BLOCKED.getReason());
            result.setBlockedReasonDesc(student == null ? (admin ? "学生不存在" : "学生不存在或不属于当前家长")
                    : student.getBlockedReasonDesc());
            result.setDecisions(Collections.emptyList());
            result.setVisibleOffers(Collections.emptyList());
            return result;
        }

        List<SubscriptionWindowOfferDO> offers = offerMapper.selectListByWindowId(window.getId());
        if (CollUtil.isEmpty(offers)) {
            result.setDecisions(Collections.emptyList());
            result.setVisibleOffers(Collections.emptyList());
            return result;
        }
        Map<Long, ProductPublicationRespDTO> publicationMap = convertMap(
                productPublicationApi.getPublicationList(convertSet(offers, SubscriptionWindowOfferDO::getProductSpuId)),
                ProductPublicationRespDTO::getId);
        Map<Long, List<SubscriptionWindowOfferSkuDO>> offerSkuMap = convertMultiMap(
                offerSkuMapper.selectListByOfferIds(convertSet(offers, SubscriptionWindowOfferDO::getId)),
                SubscriptionWindowOfferSkuDO::getOfferId);
        Map<Long, List<SubscriptionWindowOfferGradeRelDO>> gradeRelMap = convertMultiMap(
                offerGradeRelMapper.selectListByOfferIds(convertSet(offers, SubscriptionWindowOfferDO::getId)),
                SubscriptionWindowOfferGradeRelDO::getOfferId);
        List<SubscriptionRuleDO> rules = ruleService.getRuleListByWindowId(window.getId());
        Map<Long, List<SubscriptionRuleConditionDO>> conditionMap = ruleService.getConditionMap(rules);

        List<SubscriptionVisibilityResultBO.OfferDecision> decisions = offers.stream()
                .map(offer -> buildDecision(student, offer, publicationMap.get(offer.getProductSpuId()),
                        offerSkuMap.get(offer.getId()), gradeRelMap.get(offer.getId()), rules, conditionMap))
                .toList();
        result.setDecisions(decisions);
        result.setVisibleOffers(decisions.stream()
                .filter(decision -> Boolean.TRUE.equals(decision.getVisible()))
                .map(this::buildVisibleOffer)
                .toList());
        return result;
    }

    private SubscriptionVisibilityResultBO.OfferDecision buildDecision(
            EduStudentSubscriptionContextRespDTO student,
            SubscriptionWindowOfferDO offer,
            ProductPublicationRespDTO publication,
            List<SubscriptionWindowOfferSkuDO> offerSkus,
            List<SubscriptionWindowOfferGradeRelDO> gradeRels,
            List<SubscriptionRuleDO> rules,
            Map<Long, List<SubscriptionRuleConditionDO>> conditionMap) {
        SubscriptionVisibilityResultBO.OfferDecision decision = new SubscriptionVisibilityResultBO.OfferDecision();
        decision.setOffer(offer);
        decision.setPublication(publication);
        decision.setTotalOfferSkuCount(CollUtil.size(offerSkus));
        decision.setGradeApplicabilityOverride(false);
        if (publication == null || !ProductSpuStatusEnum.isEnable(publication.getStatus())
                || !CommonStatusEnum.isEnable(offer.getStatus())) {
            return fillDecision(decision, false, SubscriptionVisibilityReasonEnum.PRODUCT_NOT_ENABLED,
                    Collections.emptyList(), Collections.emptyList(), null, false);
        }

        Map<Long, ProductPublicationRespDTO.PublicationSkuDTO> productSkuMap = convertMap(publication.getSkus(),
                ProductPublicationRespDTO.PublicationSkuDTO::getId);
        List<SubscriptionVisibilityResultBO.VisibleOfferSku> candidateSkus = (offerSkus == null
                ? Collections.<SubscriptionWindowOfferSkuDO>emptyList() : offerSkus).stream()
                .filter(offerSku -> CommonStatusEnum.isEnable(offerSku.getStatus()))
                .map(offerSku -> buildCandidateSku(offerSku, productSkuMap.get(offerSku.getProductSkuId())))
                .filter(Objects::nonNull)
                .toList();
        decision.setCandidateSkuCount(candidateSkus.size());
        Set<Long> offerLimitGrades = gradeRels == null ? Collections.emptySet()
                : gradeRels.stream().map(SubscriptionWindowOfferGradeRelDO::getGradeCatalogId).collect(Collectors.toSet());
        candidateSkus.forEach(item -> fillSkuDecision(item, SubscriptionSkuDecisionStatusEnum.BASE_REJECTED,
                SubscriptionVisibilityReasonEnum.BASE_REJECTED, null, false));
        List<SubscriptionVisibilityResultBO.VisibleOfferSku> baseSkus = candidateSkus.stream()
                .filter(item -> matchBaseGrade(item.getProductSku(), student.getGradeCatalogId(), offerLimitGrades))
                .peek(item -> fillSkuDecision(item, SubscriptionSkuDecisionStatusEnum.FINAL,
                        SubscriptionVisibilityReasonEnum.BASE_MATCH, null, false))
                .toList();

        List<SubscriptionRuleDO> effectiveRules = rules.stream()
                .filter(rule -> rule.getOfferId() == null || Objects.equals(rule.getOfferId(), offer.getId()))
                .filter(rule -> CommonStatusEnum.isEnable(rule.getStatus()))
                .toList();
        Map<Long, SubscriptionVisibilityResultBO.VisibleOfferSku> finalSkuMap = new LinkedHashMap<>();
        baseSkus.forEach(item -> finalSkuMap.put(item.getOfferSku().getId(), item));
        SubscriptionRuleDO matchedInclude = null;
        boolean gradeOverride = false;
        for (SubscriptionRuleDO rule : effectiveRules) {
            if (!SubscriptionRuleEffectTypeEnum.isInclude(rule.getEffectType())) {
                continue;
            }
            List<SubscriptionRuleConditionDO> conditions = conditionMap.get(rule.getId());
            for (SubscriptionVisibilityResultBO.VisibleOfferSku candidateSku : candidateSkus) {
                boolean baseGradeMatched = matchBaseGrade(candidateSku.getProductSku(), student.getGradeCatalogId(), offerLimitGrades);
                if (!baseGradeMatched && !Boolean.TRUE.equals(rule.getAllowGradeOverride())) {
                    continue;
                }
                if (matchRule(rule, conditions, student, publication, candidateSku)) {
                    matchedInclude = rule;
                    gradeOverride = gradeOverride || !baseGradeMatched;
                    fillSkuDecision(candidateSku, SubscriptionSkuDecisionStatusEnum.FINAL,
                            SubscriptionVisibilityReasonEnum.INCLUDE_RULE_MATCH, rule, !baseGradeMatched);
                    finalSkuMap.put(candidateSku.getOfferSku().getId(), candidateSku);
                }
            }
        }
        SubscriptionRuleDO matchedExclude = null;
        for (SubscriptionRuleDO rule : effectiveRules) {
            if (!SubscriptionRuleEffectTypeEnum.isExclude(rule.getEffectType())) {
                continue;
            }
            List<SubscriptionRuleConditionDO> conditions = conditionMap.get(rule.getId());
            for (SubscriptionVisibilityResultBO.VisibleOfferSku candidateSku : candidateSkus) {
                if (matchRule(rule, conditions, student, publication, candidateSku)) {
                    matchedExclude = rule;
                    fillSkuDecision(candidateSku, SubscriptionSkuDecisionStatusEnum.EXCLUDED,
                            SubscriptionVisibilityReasonEnum.EXCLUDE_RULE_MATCH, rule,
                            Boolean.TRUE.equals(candidateSku.getGradeApplicabilityOverride()));
                    finalSkuMap.remove(candidateSku.getOfferSku().getId());
                }
            }
        }
        if (matchedExclude != null && finalSkuMap.isEmpty()) {
            return fillDecision(decision, false, SubscriptionVisibilityReasonEnum.EXCLUDE_RULE_MATCH,
                    Collections.emptyList(), candidateSkus, matchedExclude, false);
        }
        if (finalSkuMap.isEmpty()) {
            return fillDecision(decision, false, SubscriptionVisibilityReasonEnum.NO_AVAILABLE_SKU,
                    Collections.emptyList(), candidateSkus, matchedInclude, gradeOverride);
        }
        SubscriptionRuleDO matchedRule = matchedInclude;
        SubscriptionVisibilityReasonEnum reason = matchedInclude != null
                ? SubscriptionVisibilityReasonEnum.INCLUDE_RULE_MATCH : SubscriptionVisibilityReasonEnum.BASE_MATCH;
        return fillDecision(decision, true, reason, new ArrayList<>(finalSkuMap.values()), candidateSkus,
                matchedRule, gradeOverride);
    }

    private SubscriptionVisibilityResultBO.VisibleOfferSku buildCandidateSku(
            SubscriptionWindowOfferSkuDO offerSku,
            ProductPublicationRespDTO.PublicationSkuDTO productSku) {
        if (productSku == null || !CommonStatusEnum.isEnable(productSku.getStatus())) {
            return null;
        }
        SubscriptionVisibilityResultBO.VisibleOfferSku sku = new SubscriptionVisibilityResultBO.VisibleOfferSku();
        sku.setOfferSku(offerSku);
        sku.setProductSku(productSku);
        sku.setGradeApplicabilityOverride(false);
        return sku;
    }

    private boolean matchBaseGrade(ProductPublicationRespDTO.PublicationSkuDTO sku, Long gradeCatalogId,
                                   Set<Long> offerLimitGrades) {
        if (gradeCatalogId == null || CollUtil.isEmpty(sku.getApplicableGradeCatalogIds())) {
            return false;
        }
        if (!sku.getApplicableGradeCatalogIds().contains(gradeCatalogId)) {
            return false;
        }
        return CollUtil.isEmpty(offerLimitGrades) || offerLimitGrades.contains(gradeCatalogId);
    }

    private boolean matchRule(SubscriptionRuleDO rule, List<SubscriptionRuleConditionDO> conditions,
                              EduStudentSubscriptionContextRespDTO student, ProductPublicationRespDTO publication,
                              SubscriptionVisibilityResultBO.VisibleOfferSku visibleSku) {
        if (CollUtil.isEmpty(conditions)) {
            return false;
        }
        return conditions.stream().allMatch(condition -> matchCondition(condition, student, publication, visibleSku));
    }

    private boolean matchCondition(SubscriptionRuleConditionDO condition,
                                   EduStudentSubscriptionContextRespDTO student,
                                   ProductPublicationRespDTO publication,
                                   SubscriptionVisibilityResultBO.VisibleOfferSku visibleSku) {
        String factor = condition.getFactor();
        String expected = condition.getValue();
        if (SubscriptionRuleFactorEnum.STUDENT_SCHOOL.getCode().equals(factor)) {
            return Objects.equals(expected, String.valueOf(student.getSchoolId()));
        }
        if (SubscriptionRuleFactorEnum.STUDENT_GRADE.getCode().equals(factor)) {
            return Objects.equals(expected, String.valueOf(student.getGradeCatalogId()));
        }
        if (SubscriptionRuleFactorEnum.OFFER_SKU.getCode().equals(factor)) {
            return visibleSku.getOfferSku() != null
                    && Objects.equals(expected, String.valueOf(visibleSku.getOfferSku().getId()));
        }
        ProductPublicationRespDTO.PublicationSpuExtDTO spuExt = publication.getPublicationExt();
        if (SubscriptionRuleFactorEnum.SKU_PUBLISHER.getCode().equals(factor)) {
            return spuExt != null && Objects.equals(expected, String.valueOf(spuExt.getPublisherId()));
        }
        if (SubscriptionRuleFactorEnum.SKU_PUBLICATION_TYPE.getCode().equals(factor)) {
            return spuExt != null && Objects.equals(expected, String.valueOf(spuExt.getPublicationTypeId()));
        }
        if (SubscriptionRuleFactorEnum.SKU_ISSUE_CYCLE.getCode().equals(factor)) {
            return spuExt != null
                    && PublicationIssueModeEnum.isPeriodical(spuExt.getIssueMode())
                    && Objects.equals(expected, spuExt.getIssueCycle());
        }
        return false;
    }

    private SubscriptionVisibilityResultBO.OfferDecision fillDecision(
            SubscriptionVisibilityResultBO.OfferDecision decision,
            boolean visible,
            SubscriptionVisibilityReasonEnum reason,
            List<SubscriptionVisibilityResultBO.VisibleOfferSku> finalSkus,
            List<SubscriptionVisibilityResultBO.VisibleOfferSku> diagnosticSkus,
            SubscriptionRuleDO matchedRule,
            boolean gradeOverride) {
        decision.setVisible(visible);
        decision.setReason(reason.getReason());
        decision.setReasonDesc(reason.getDescription());
        decision.setFinalSkuCount(finalSkus.size());
        decision.setFinalSkus(finalSkus);
        decision.setDiagnosticSkus(diagnosticSkus);
        decision.setMatchedRule(matchedRule);
        decision.setGradeApplicabilityOverride(gradeOverride);
        return decision;
    }

    private SubscriptionVisibilityResultBO.OfferDecision fillDecision(
            SubscriptionVisibilityResultBO.OfferDecision decision,
            boolean visible,
            SubscriptionVisibilityReasonEnum reason,
            List<SubscriptionVisibilityResultBO.VisibleOfferSku> finalSkus,
            SubscriptionRuleDO matchedRule,
            boolean gradeOverride) {
        return fillDecision(decision, visible, reason, finalSkus, finalSkus, matchedRule, gradeOverride);
    }

    private void fillSkuDecision(SubscriptionVisibilityResultBO.VisibleOfferSku sku,
                                 SubscriptionSkuDecisionStatusEnum status,
                                 SubscriptionVisibilityReasonEnum reason,
                                 SubscriptionRuleDO matchedRule,
                                 boolean gradeOverride) {
        sku.setDecisionStatus(status.getStatus());
        sku.setDecisionStatusName(status.getName());
        sku.setReason(reason.getReason());
        sku.setMatchedRule(matchedRule);
        sku.setGradeApplicabilityOverride(gradeOverride);
    }

    private SubscriptionVisibilityResultBO.VisibleOffer buildVisibleOffer(
            SubscriptionVisibilityResultBO.OfferDecision decision) {
        SubscriptionVisibilityResultBO.VisibleOffer visibleOffer = new SubscriptionVisibilityResultBO.VisibleOffer();
        visibleOffer.setOffer(decision.getOffer());
        visibleOffer.setPublication(decision.getPublication());
        visibleOffer.setReason(decision.getReason());
        visibleOffer.setReasonDesc(decision.getReasonDesc());
        visibleOffer.setMatchedRule(decision.getMatchedRule());
        visibleOffer.setGradeApplicabilityOverride(decision.getGradeApplicabilityOverride());
        visibleOffer.setSkus(decision.getFinalSkus());
        return visibleOffer;
    }
}
