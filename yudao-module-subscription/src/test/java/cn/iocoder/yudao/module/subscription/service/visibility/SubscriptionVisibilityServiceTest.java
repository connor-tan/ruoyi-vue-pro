package cn.iocoder.yudao.module.subscription.service.visibility;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.api.student.EduStudentApi;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentSubscriptionContextRespDTO;
import cn.iocoder.yudao.module.product.api.publication.ProductPublicationApi;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.*;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferGradeRelMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferSkuMapper;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleFactorEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionVisibilityReasonEnum;
import cn.iocoder.yudao.module.subscription.service.rule.SubscriptionRuleService;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionVisibilityServiceTest {

    private static final long WINDOW_ID = 10L;
    private static final long OFFER_ID = 20L;
    private static final long OFFER_SKU_ID = 30L;
    private static final long OFFER_SKU_ID_2 = 31L;
    private static final long PRODUCT_SPU_ID = 40L;
    private static final long PRODUCT_SKU_ID = 50L;
    private static final long PRODUCT_SKU_ID_2 = 51L;
    private static final long STUDENT_ID = 60L;
    private static final long SCHOOL_ID = 70L;
    private static final long GRADE_ID = 80L;
    private static final String TARGET_PERIOD = "FULL_YEAR";

    @Mock
    private SubscriptionWindowService windowService;
    @Mock
    private SubscriptionWindowOfferMapper offerMapper;
    @Mock
    private SubscriptionWindowOfferSkuMapper offerSkuMapper;
    @Mock
    private SubscriptionWindowOfferGradeRelMapper offerGradeRelMapper;
    @Mock
    private SubscriptionRuleService ruleService;
    @Mock
    private EduStudentApi eduStudentApi;
    @Mock
    private ProductPublicationApi productPublicationApi;
    @InjectMocks
    private SubscriptionVisibilityServiceImpl visibilityService;

    @Test
    void calculate_shouldReturnBaseVisibleSku_whenStudentGradeMatchesSkuGrade() {
        mockCommonFacts(publicationWithGrades(List.of(GRADE_ID)), List.of(), Map.of());

        SubscriptionVisibilityResultBO result = visibilityService.calculate(1L, STUDENT_ID, WINDOW_ID);

        assertNull(result.getBlockedReason());
        assertEquals(1, result.getVisibleOffers().size());
        SubscriptionVisibilityResultBO.OfferDecision decision = result.getDecisions().get(0);
        assertTrue(decision.getVisible());
        assertEquals(SubscriptionVisibilityReasonEnum.BASE_MATCH.getReason(), decision.getReason());
        assertEquals(OFFER_SKU_ID, decision.getFinalSkus().get(0).getOfferSku().getId());
        assertFalse(decision.getGradeApplicabilityOverride());
    }

    @Test
    void calculate_shouldExcludeSku_whenExcludeRuleMatches() {
        SubscriptionRuleDO excludeRule = rule(1L, SubscriptionRuleEffectTypeEnum.EXCLUDE.getType(), false);
        SubscriptionRuleConditionDO condition = condition(1L, SubscriptionRuleFactorEnum.STUDENT_SCHOOL.getCode(),
                String.valueOf(SCHOOL_ID));
        mockCommonFacts(publicationWithGrades(List.of(GRADE_ID)), List.of(excludeRule), Map.of(1L, List.of(condition)));

        SubscriptionVisibilityResultBO result = visibilityService.calculate(1L, STUDENT_ID, WINDOW_ID);

        SubscriptionVisibilityResultBO.OfferDecision decision = result.getDecisions().get(0);
        assertFalse(decision.getVisible());
        assertEquals(SubscriptionVisibilityReasonEnum.EXCLUDE_RULE_MATCH.getReason(), decision.getReason());
        assertEquals(excludeRule.getId(), decision.getMatchedRule().getId());
        assertTrue(result.getVisibleOffers().isEmpty());
    }

    @Test
    void calculate_shouldExcludeOnlyMatchedOfferSku_whenOfferSkuRuleMatches() {
        SubscriptionRuleDO excludeRule = rule(1L, SubscriptionRuleEffectTypeEnum.EXCLUDE.getType(), false);
        SubscriptionRuleConditionDO gradeCondition = condition(1L, SubscriptionRuleFactorEnum.STUDENT_GRADE.getCode(),
                String.valueOf(GRADE_ID));
        SubscriptionRuleConditionDO offerSkuCondition = condition(1L, SubscriptionRuleFactorEnum.OFFER_SKU.getCode(),
                String.valueOf(OFFER_SKU_ID));
        mockCommonFacts(publicationWithSkus(List.of(
                        publicationSku(PRODUCT_SKU_ID, "一年级上册", List.of(GRADE_ID)),
                        publicationSku(PRODUCT_SKU_ID_2, "一年级下册", List.of(GRADE_ID)))),
                List.of(excludeRule), Map.of(1L, List.of(gradeCondition, offerSkuCondition)),
                List.of(offerSku(OFFER_SKU_ID, PRODUCT_SKU_ID), offerSku(OFFER_SKU_ID_2, PRODUCT_SKU_ID_2)));

        SubscriptionVisibilityResultBO result = visibilityService.calculate(1L, STUDENT_ID, WINDOW_ID);

        SubscriptionVisibilityResultBO.OfferDecision decision = result.getDecisions().get(0);
        assertTrue(decision.getVisible());
        assertEquals(SubscriptionVisibilityReasonEnum.BASE_MATCH.getReason(), decision.getReason());
        assertEquals(1, decision.getFinalSkuCount());
        assertEquals(OFFER_SKU_ID_2, decision.getFinalSkus().get(0).getOfferSku().getId());
        assertEquals(PRODUCT_SKU_ID_2, decision.getFinalSkus().get(0).getProductSku().getId());
    }

    @Test
    void calculate_shouldOnlyOverrideGrade_whenIncludeRuleAllowsGradeOverride() {
        SubscriptionRuleDO includeWithoutOverride = rule(1L, SubscriptionRuleEffectTypeEnum.INCLUDE.getType(), false);
        SubscriptionRuleConditionDO condition = condition(1L, SubscriptionRuleFactorEnum.SKU_PUBLICATION_TYPE.getCode(), "11");
        mockCommonFacts(publicationWithGrades(List.of(999L)), List.of(includeWithoutOverride), Map.of(1L, List.of(condition)));

        SubscriptionVisibilityResultBO blocked = visibilityService.calculate(1L, STUDENT_ID, WINDOW_ID);

        SubscriptionVisibilityResultBO.OfferDecision blockedDecision = blocked.getDecisions().get(0);
        assertFalse(blockedDecision.getVisible());
        assertEquals(SubscriptionVisibilityReasonEnum.NO_AVAILABLE_SKU.getReason(), blockedDecision.getReason());

        SubscriptionRuleDO includeWithOverride = rule(2L, SubscriptionRuleEffectTypeEnum.INCLUDE.getType(), true);
        SubscriptionRuleConditionDO overrideCondition = condition(2L, SubscriptionRuleFactorEnum.SKU_PUBLICATION_TYPE.getCode(),
                "11");
        mockRules(List.of(includeWithOverride), Map.of(2L, List.of(overrideCondition)));

        SubscriptionVisibilityResultBO visible = visibilityService.calculate(1L, STUDENT_ID, WINDOW_ID);

        SubscriptionVisibilityResultBO.OfferDecision visibleDecision = visible.getDecisions().get(0);
        assertTrue(visibleDecision.getVisible());
        assertEquals(SubscriptionVisibilityReasonEnum.INCLUDE_RULE_MATCH.getReason(), visibleDecision.getReason());
        assertTrue(visibleDecision.getGradeApplicabilityOverride());
        assertTrue(visibleDecision.getFinalSkus().get(0).getGradeApplicabilityOverride());
    }

    private void mockCommonFacts(ProductPublicationRespDTO publication, List<SubscriptionRuleDO> rules,
                                 Map<Long, List<SubscriptionRuleConditionDO>> conditionMap) {
        mockCommonFacts(publication, rules, conditionMap, List.of(offerSku()));
    }

    private void mockCommonFacts(ProductPublicationRespDTO publication, List<SubscriptionRuleDO> rules,
                                 Map<Long, List<SubscriptionRuleConditionDO>> conditionMap,
                                 List<SubscriptionWindowOfferSkuDO> offerSkus) {
        SubscriptionWindowDO window = window();
        when(windowService.getWindow(WINDOW_ID)).thenReturn(window);
        when(windowService.isOpen(window)).thenReturn(true);
        when(eduStudentApi.getSubscriptionStudentContextMap(eq(1L), eq(Set.of(STUDENT_ID)), eq(2026), eq(2027),
                eq(100L), eq("AUTO_TARGET_YEAR_GRADE"), eq("AUTO_TARGET_YEAR_GRADE"))).thenReturn(Map.of(STUDENT_ID, student()));
        when(offerMapper.selectListByWindowId(WINDOW_ID)).thenReturn(List.of(offer()));
        when(productPublicationApi.getPublicationList(eq(Set.of(PRODUCT_SPU_ID)))).thenReturn(List.of(publication));
        when(offerSkuMapper.selectListByOfferIds(eq(Set.of(OFFER_ID)))).thenReturn(offerSkus);
        when(offerGradeRelMapper.selectListByOfferIds(eq(Set.of(OFFER_ID)))).thenReturn(List.of());
        mockRules(rules, conditionMap);
    }

    private void mockRules(List<SubscriptionRuleDO> rules, Map<Long, List<SubscriptionRuleConditionDO>> conditionMap) {
        when(ruleService.getRuleListByWindowId(WINDOW_ID)).thenReturn(rules);
        when(ruleService.getConditionMap(eq(rules))).thenReturn(conditionMap);
    }

    private SubscriptionWindowDO window() {
        return SubscriptionWindowDO.builder()
                .id(WINDOW_ID)
                .targetYearCatalogId(100L)
                .targetYearStart(2026)
                .targetYearEnd(2027)
                .targetPeriod(TARGET_PERIOD)
                .gradeCalcRule("AUTO_TARGET_YEAR_GRADE")
                .gradeResolveMode("AUTO_TARGET_YEAR_GRADE")
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(1))
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build();
    }

    private EduStudentSubscriptionContextRespDTO student() {
        EduStudentSubscriptionContextRespDTO student = new EduStudentSubscriptionContextRespDTO();
        student.setStudentId(STUDENT_ID);
        student.setSchoolId(SCHOOL_ID);
        student.setGradeCatalogId(GRADE_ID);
        return student;
    }

    private SubscriptionWindowOfferDO offer() {
        return SubscriptionWindowOfferDO.builder()
                .id(OFFER_ID)
                .windowId(WINDOW_ID)
                .productSpuId(PRODUCT_SPU_ID)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .sort(0)
                .build();
    }

    private SubscriptionWindowOfferSkuDO offerSku() {
        return offerSku(OFFER_SKU_ID, PRODUCT_SKU_ID);
    }

    private SubscriptionWindowOfferSkuDO offerSku(Long offerSkuId, Long productSkuId) {
        return SubscriptionWindowOfferSkuDO.builder()
                .id(offerSkuId)
                .offerId(OFFER_ID)
                .productSkuId(productSkuId)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .sort(0)
                .maxQuantityPerStudent(1)
                .build();
    }

    private ProductPublicationRespDTO publicationWithGrades(List<Long> gradeIds) {
        ProductPublicationRespDTO publication = new ProductPublicationRespDTO();
        publication.setId(PRODUCT_SPU_ID);
        publication.setName("测试刊物");
        publication.setStatus(CommonStatusEnum.ENABLE.getStatus());
        ProductPublicationRespDTO.PublicationSpuExtDTO spuExt = new ProductPublicationRespDTO.PublicationSpuExtDTO();
        spuExt.setPublisherId(10L);
        spuExt.setPublicationTypeId(11L);
        spuExt.setIssueCycle("MONTHLY");
        publication.setPublicationExt(spuExt);
        publication.setSkus(List.of(publicationSku(PRODUCT_SKU_ID, "一年级全学年", gradeIds)));
        return publication;
    }

    private ProductPublicationRespDTO publicationWithSkus(List<ProductPublicationRespDTO.PublicationSkuDTO> skus) {
        ProductPublicationRespDTO publication = new ProductPublicationRespDTO();
        publication.setId(PRODUCT_SPU_ID);
        publication.setName("测试刊物");
        publication.setStatus(CommonStatusEnum.ENABLE.getStatus());
        ProductPublicationRespDTO.PublicationSpuExtDTO spuExt = new ProductPublicationRespDTO.PublicationSpuExtDTO();
        spuExt.setPublisherId(10L);
        spuExt.setPublicationTypeId(11L);
        spuExt.setIssueCycle("MONTHLY");
        publication.setPublicationExt(spuExt);
        publication.setSkus(skus);
        return publication;
    }

    private ProductPublicationRespDTO.PublicationSkuDTO publicationSku(Long productSkuId, String name, List<Long> gradeIds) {
        ProductPublicationRespDTO.PublicationSkuExtDTO skuExt = new ProductPublicationRespDTO.PublicationSkuExtDTO();
        skuExt.setTargetPeriod(TARGET_PERIOD);
        skuExt.setVolumeLabel("上册");
        skuExt.setEditionLabel("苏教版");

        ProductPublicationRespDTO.PublicationSkuDTO sku = new ProductPublicationRespDTO.PublicationSkuDTO();
        sku.setId(productSkuId);
        sku.setName(name);
        sku.setStatus(CommonStatusEnum.ENABLE.getStatus());
        sku.setStock(10);
        sku.setPrice(1000);
        sku.setPublicationExt(skuExt);
        sku.setApplicableGradeCatalogIds(gradeIds);
        return sku;
    }

    private SubscriptionRuleDO rule(Long id, String effectType, boolean allowGradeOverride) {
        return SubscriptionRuleDO.builder()
                .id(id)
                .windowId(WINDOW_ID)
                .offerId(OFFER_ID)
                .name("规则" + id)
                .effectType(effectType)
                .allowGradeOverride(allowGradeOverride)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build();
    }

    private SubscriptionRuleConditionDO condition(Long ruleId, String factor, String value) {
        return SubscriptionRuleConditionDO.builder()
                .id(ruleId * 10)
                .ruleId(ruleId)
                .factor(factor)
                .operator("EQ")
                .value(value)
                .build();
    }
}
