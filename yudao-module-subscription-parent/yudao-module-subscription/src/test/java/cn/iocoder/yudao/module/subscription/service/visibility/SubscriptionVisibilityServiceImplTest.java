package cn.iocoder.yudao.module.subscription.service.visibility;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuGradeDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.publication.ProductDomainTypeEnum;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuRuleDO;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleScopeTypeEnum;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionGradeResolveRespBO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import cn.iocoder.yudao.module.subscription.service.windowsku.SubscriptionWindowSkuService;
import cn.iocoder.yudao.module.subscription.service.windowspu.SubscriptionWindowSpuService;
import cn.iocoder.yudao.module.subscription.service.windowspurule.SubscriptionWindowSpuRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubscriptionVisibilityServiceImplTest {

    private SubscriptionVisibilityServiceImpl service;
    private SubscriptionWindowService subscriptionWindowService;
    private SubscriptionWindowSpuService subscriptionWindowSpuService;
    private SubscriptionWindowSkuService subscriptionWindowSkuService;
    private SubscriptionWindowSpuRuleService subscriptionWindowSpuRuleService;
    private SubscriptionSupportService subscriptionSupportService;
    private SubscriptionGradeResolveService subscriptionGradeResolveService;

    @BeforeEach
    void setUp() {
        service = new SubscriptionVisibilityServiceImpl();
        subscriptionWindowService = mock(SubscriptionWindowService.class);
        subscriptionWindowSpuService = mock(SubscriptionWindowSpuService.class);
        subscriptionWindowSkuService = mock(SubscriptionWindowSkuService.class);
        subscriptionWindowSpuRuleService = mock(SubscriptionWindowSpuRuleService.class);
        subscriptionSupportService = mock(SubscriptionSupportService.class);
        subscriptionGradeResolveService = mock(SubscriptionGradeResolveService.class);
        ReflectionTestUtils.setField(service, "subscriptionWindowService", subscriptionWindowService);
        ReflectionTestUtils.setField(service, "subscriptionWindowSpuService", subscriptionWindowSpuService);
        ReflectionTestUtils.setField(service, "subscriptionWindowSkuService", subscriptionWindowSkuService);
        ReflectionTestUtils.setField(service, "subscriptionWindowSpuRuleService", subscriptionWindowSpuRuleService);
        ReflectionTestUtils.setField(service, "subscriptionSupportService", subscriptionSupportService);
        ReflectionTestUtils.setField(service, "subscriptionGradeResolveService", subscriptionGradeResolveService);
    }

    @Test
    void calculateShouldExposeIncludeRuleGradeOverrideDiagnostic() {
        mockVisibleBaseData(List.of(rule(300L, SubscriptionRuleEffectTypeEnum.INCLUDE.getType(),
                        SubscriptionRuleScopeTypeEnum.GRADE.getType(), 2L)),
                List.of(sku(CommonStatusEnum.ENABLE.getStatus())));

        SubscriptionVisibilityResultBO resultBO = service.calculate(1L, 10L);

        assertEquals(1, resultBO.getVisibleSpus().size());
        assertTrue(resultBO.getVisibleSpus().get(0).getGradeApplicabilityOverride());
        assertEquals("INCLUDE_RULE_MATCH", resultBO.getVisibleSpus().get(0).getVisibilityReason());
        assertEquals(1, resultBO.getDecisions().size());
        assertTrue(resultBO.getDecisions().get(0).getGradeApplicabilityOverride());
    }

    @Test
    void calculateShouldHideWhenRuleMatchesButNoEnabledSku() {
        mockVisibleBaseData(List.of(rule(300L, SubscriptionRuleEffectTypeEnum.INCLUDE.getType(),
                        SubscriptionRuleScopeTypeEnum.GRADE.getType(), 2L)),
                List.of(sku(CommonStatusEnum.DISABLE.getStatus())));

        SubscriptionVisibilityResultBO resultBO = service.calculate(1L, 10L);

        assertEquals(0, resultBO.getVisibleSpus().size());
        assertEquals("NO_ENABLED_MATCHING_PERIOD_SKU", resultBO.getDecisions().get(0).getReason());
        assertEquals(0, resultBO.getDecisions().get(0).getEnabledSkuCount());
    }

    @Test
    void calculateShouldHideWhenEnabledSkuTargetPeriodNotMatched() {
        mockVisibleBaseData(List.of(rule(300L, SubscriptionRuleEffectTypeEnum.INCLUDE.getType(),
                        SubscriptionRuleScopeTypeEnum.GRADE.getType(), 2L)),
                List.of(sku(CommonStatusEnum.ENABLE.getStatus())),
                Map.of(50L, ProductSkuPublicationDO.builder()
                        .productSkuId(50L)
                        .targetPeriod("SECOND_TERM")
                        .build()));

        SubscriptionVisibilityResultBO resultBO = service.calculate(1L, 10L);

        assertEquals(0, resultBO.getVisibleSpus().size());
        assertEquals("NO_ENABLED_MATCHING_PERIOD_SKU", resultBO.getDecisions().get(0).getReason());
        assertEquals(0, resultBO.getDecisions().get(0).getEnabledSkuCount());
        assertEquals(1, resultBO.getDecisions().get(0).getEnabledPeriodMismatchedSkuCount());
    }

    private void mockVisibleBaseData(List<SubscriptionWindowSpuRuleDO> rules, List<SubscriptionWindowSkuDO> skus) {
        mockVisibleBaseData(rules, skus, Map.of(50L, ProductSkuPublicationDO.builder()
                .productSkuId(50L)
                .targetPeriod("FIRST_TERM")
                .build()));
    }

    private void mockVisibleBaseData(List<SubscriptionWindowSpuRuleDO> rules, List<SubscriptionWindowSkuDO> skus,
                                     Map<Long, ProductSkuPublicationDO> skuPublicationMap) {
        SubscriptionWindowDO window = SubscriptionWindowDO.builder()
                .id(10L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(1))
                .targetPeriod("FIRST_TERM")
                .build();
        SubscriptionWindowSpuDO windowSpu = SubscriptionWindowSpuDO.builder()
                .id(20L)
                .windowId(10L)
                .productSpuId(30L)
                .build();
        ProductSpuDO productSpu = ProductSpuDO.builder()
                .id(30L)
                .name("测试刊物")
                .domainType(ProductDomainTypeEnum.PUBLICATION.getCode())
                .status(ProductSpuStatusEnum.ENABLE.getStatus())
                .build();
        SubscriptionGradeResolveRespBO gradeResolve = new SubscriptionGradeResolveRespBO();
        gradeResolve.setSchoolId(1L);
        gradeResolve.setEffectiveGradeCatalogId(2L);
        when(subscriptionWindowService.getWindowDO(10L)).thenReturn(window);
        when(subscriptionGradeResolveService.resolve(1L, window)).thenReturn(gradeResolve);
        when(subscriptionWindowSpuService.getWindowSpuDOListByWindowId(10L)).thenReturn(List.of(windowSpu));
        when(subscriptionWindowSpuService.getGradeDOMap(List.of(20L))).thenReturn(Collections.emptyMap());
        when(subscriptionWindowSpuRuleService.getWindowSpuRuleDOList(List.of(20L))).thenReturn(rules);
        when(subscriptionWindowSkuService.getWindowSkuDOList(List.of(20L))).thenReturn(skus);
        when(subscriptionSupportService.getSkuPublicationMap(any())).thenReturn(skuPublicationMap);
        when(subscriptionSupportService.getPublicationSpuMap(any())).thenReturn(Map.of(30L, productSpu));
        when(subscriptionSupportService.getPublicationSpuGradeMap(any())).thenReturn(Map.of(30L,
                List.of(ProductSpuGradeDO.builder()
                        .productSpuId(30L)
                        .gradeCatalogId(1L)
                        .build())));
    }

    private SubscriptionWindowSpuRuleDO rule(Long id, String effectType, String scopeType, Long gradeCatalogId) {
        return SubscriptionWindowSpuRuleDO.builder()
                .id(id)
                .windowSpuId(20L)
                .effectType(effectType)
                .scopeType(scopeType)
                .gradeCatalogId(gradeCatalogId)
                .build();
    }

    private SubscriptionWindowSkuDO sku(Integer status) {
        return SubscriptionWindowSkuDO.builder()
                .id(40L)
                .windowSpuId(20L)
                .productSkuId(50L)
                .status(status)
                .sort(1)
                .maxQuantityPerStudent(1)
                .build();
    }
}
