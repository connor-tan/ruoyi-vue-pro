package cn.iocoder.yudao.module.subscription.api.order;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuRuleDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSkuMapper;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleScopeTypeEnum;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityService;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionGradeResolveRespBO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionSpuVisibilityDecisionBO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import cn.iocoder.yudao.module.subscription.service.windowspu.SubscriptionWindowSpuService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubscriptionOrderEligibilityApiImplTest {

    private SubscriptionOrderEligibilityApiImpl api;
    private SubscriptionWindowService subscriptionWindowService;
    private SubscriptionWindowSpuService subscriptionWindowSpuService;
    private SubscriptionWindowSkuMapper subscriptionWindowSkuMapper;
    private SubscriptionVisibilityService subscriptionVisibilityService;
    private SubscriptionSupportService subscriptionSupportService;

    @BeforeEach
    void setUp() {
        api = new SubscriptionOrderEligibilityApiImpl();
        subscriptionWindowService = mock(SubscriptionWindowService.class);
        subscriptionWindowSpuService = mock(SubscriptionWindowSpuService.class);
        subscriptionWindowSkuMapper = mock(SubscriptionWindowSkuMapper.class, CALLS_REAL_METHODS);
        subscriptionVisibilityService = mock(SubscriptionVisibilityService.class);
        subscriptionSupportService = mock(SubscriptionSupportService.class);
        ReflectionTestUtils.setField(api, "subscriptionWindowService", subscriptionWindowService);
        ReflectionTestUtils.setField(api, "subscriptionWindowSpuService", subscriptionWindowSpuService);
        ReflectionTestUtils.setField(api, "subscriptionWindowSkuMapper", subscriptionWindowSkuMapper);
        ReflectionTestUtils.setField(api, "subscriptionVisibilityService", subscriptionVisibilityService);
        ReflectionTestUtils.setField(api, "subscriptionSupportService", subscriptionSupportService);
    }

    @Test
    void validateOrderItemsShouldReturnSnapshotWhenVisibleAndSkuEnabled() {
        mockOrderEligibilityBase(true, 50L, CommonStatusEnum.ENABLE.getStatus());
        SubscriptionOrderEligibilityReqDTO reqDTO = new SubscriptionOrderEligibilityReqDTO()
                .setUserId(100L)
                .setItems(List.of(item(7, 50L)));

        List<SubscriptionOrderEligibilityRespDTO> result = api.validateOrderItems(reqDTO);

        assertEquals(1, result.size());
        SubscriptionOrderEligibilityRespDTO respDTO = result.get(0);
        assertEquals(7, respDTO.getRequestIndex());
        assertEquals(1L, respDTO.getStudentId());
        assertEquals("小明", respDTO.getStudentName());
        assertEquals(2L, respDTO.getSchoolId());
        assertEquals(3L, respDTO.getGradeCatalogId());
        assertEquals(10L, respDTO.getWindowId());
        assertEquals("春季订刊", respDTO.getWindowNameSnapshot());
        assertEquals(2026, respDTO.getTargetYearStart());
        assertEquals(2027, respDTO.getTargetYearEnd());
        assertEquals("FIRST_TERM", respDTO.getTargetPeriod());
        assertEquals(20L, respDTO.getWindowSpuId());
        assertEquals(40L, respDTO.getWindowSkuId());
        assertEquals(30L, respDTO.getProductSpuId());
        assertEquals(50L, respDTO.getProductSkuId());
        assertEquals("INCLUDE_RULE_MATCH", respDTO.getVisibilityReason());
        assertEquals(300L, respDTO.getMatchedRuleId());
        assertEquals(SubscriptionRuleEffectTypeEnum.INCLUDE.getType(), respDTO.getMatchedRuleEffectType());
        assertEquals(SubscriptionRuleScopeTypeEnum.GRADE.getType(), respDTO.getMatchedRuleScopeType());
        assertEquals(true, respDTO.getGradeApplicabilityOverride());
        assertEquals(2, respDTO.getMaxQuantityPerStudent());
    }

    @Test
    void validateOrderItemsShouldRejectSkuMismatch() {
        mockOrderEligibilityBase(true, 50L, CommonStatusEnum.ENABLE.getStatus());
        SubscriptionOrderEligibilityReqDTO reqDTO = new SubscriptionOrderEligibilityReqDTO()
                .setUserId(100L)
                .setItems(List.of(item(0, 51L)));

        ServiceException exception = assertThrows(ServiceException.class, () -> api.validateOrderItems(reqDTO));

        assertEquals(ErrorCodeConstants.ORDER_WINDOW_SKU_PRODUCT_SKU_MISMATCH.getCode(), exception.getCode());
    }

    @Test
    void validateOrderItemsShouldRejectInvisiblePublication() {
        mockOrderEligibilityBase(false, 50L, CommonStatusEnum.ENABLE.getStatus());
        SubscriptionOrderEligibilityReqDTO reqDTO = new SubscriptionOrderEligibilityReqDTO()
                .setUserId(100L)
                .setItems(List.of(item(0, 50L)));

        ServiceException exception = assertThrows(ServiceException.class, () -> api.validateOrderItems(reqDTO));

        assertEquals(ErrorCodeConstants.APP_PUBLICATION_NOT_VISIBLE.getCode(), exception.getCode());
    }

    @Test
    void validateOrderItemsShouldRejectSkuTargetPeriodMismatch() {
        mockOrderEligibilityBase(true, 50L, CommonStatusEnum.ENABLE.getStatus(), "SECOND_TERM");
        SubscriptionOrderEligibilityReqDTO reqDTO = new SubscriptionOrderEligibilityReqDTO()
                .setUserId(100L)
                .setItems(List.of(item(0, 50L)));

        ServiceException exception = assertThrows(ServiceException.class, () -> api.validateOrderItems(reqDTO));

        assertEquals(ErrorCodeConstants.ORDER_WINDOW_SKU_TARGET_PERIOD_NOT_MATCHED.getCode(), exception.getCode());
    }

    private void mockOrderEligibilityBase(boolean visible, Long productSkuId, Integer windowSkuStatus) {
        mockOrderEligibilityBase(visible, productSkuId, windowSkuStatus, "FIRST_TERM");
    }

    private void mockOrderEligibilityBase(boolean visible, Long productSkuId, Integer windowSkuStatus,
                                          String skuTargetPeriod) {
        SubscriptionWindowDO currentWindow = SubscriptionWindowDO.builder()
                .id(10L)
                .name("春季订刊")
                .targetYearStart(2026)
                .targetYearEnd(2027)
                .targetPeriod("FIRST_TERM")
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(1))
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build();
        SubscriptionWindowSpuDO windowSpu = SubscriptionWindowSpuDO.builder()
                .id(20L)
                .windowId(10L)
                .productSpuId(30L)
                .build();
        SubscriptionWindowSkuDO windowSku = SubscriptionWindowSkuDO.builder()
                .id(40L)
                .windowSpuId(20L)
                .productSkuId(productSkuId)
                .status(windowSkuStatus)
                .maxQuantityPerStudent(2)
                .build();
        when(subscriptionWindowService.getCurrentOpenWindow()).thenReturn(currentWindow);
        when(subscriptionWindowSpuService.getWindowSpuDOListByWindowId(10L)).thenReturn(List.of(windowSpu));
        when(subscriptionWindowSkuMapper.selectList(any(Wrapper.class))).thenReturn(List.of(windowSku));
        when(subscriptionSupportService.getSkuPublicationMap(any())).thenReturn(Map.of(productSkuId,
                ProductSkuPublicationDO.builder()
                        .productSkuId(productSkuId)
                        .targetPeriod(skuTargetPeriod)
                        .build()));
        when(subscriptionSupportService.getStudent(1L)).thenReturn(StudentDO.builder()
                .id(1L)
                .studentName("小明")
                .belongTo(100L)
                .build());
        when(subscriptionVisibilityService.calculate(1L, 10L)).thenReturn(visibilityResult(windowSpu, windowSku, visible));
    }

    private SubscriptionVisibilityResultBO visibilityResult(SubscriptionWindowSpuDO windowSpu,
                                                            SubscriptionWindowSkuDO windowSku,
                                                            boolean visible) {
        SubscriptionGradeResolveRespBO gradeResolve = new SubscriptionGradeResolveRespBO();
        gradeResolve.setStudentId(1L);
        gradeResolve.setStudentName("小明");
        gradeResolve.setSchoolId(2L);
        gradeResolve.setSchoolName("第一小学");
        gradeResolve.setEffectiveGradeCatalogId(3L);
        gradeResolve.setEffectiveGradeNo("P1");
        gradeResolve.setEffectiveGradeName("一年级");

        SubscriptionWindowSpuRuleDO rule = SubscriptionWindowSpuRuleDO.builder()
                .id(300L)
                .windowSpuId(windowSpu.getId())
                .effectType(SubscriptionRuleEffectTypeEnum.INCLUDE.getType())
                .scopeType(SubscriptionRuleScopeTypeEnum.GRADE.getType())
                .gradeCatalogId(3L)
                .build();
        SubscriptionSpuVisibilityDecisionBO decision = new SubscriptionSpuVisibilityDecisionBO();
        decision.setWindowSpu(windowSpu);
        decision.setVisible(visible);
        decision.setReason(visible ? "INCLUDE_RULE_MATCH" : "EXCLUDE_RULE_MATCH");
        decision.setReasonDesc(visible ? "允许规则命中" : "排除规则命中");
        decision.setMatchedRule(rule);
        decision.setGradeApplicabilityOverride(true);
        decision.setEnabledSkus(visible ? List.of(windowSku) : List.of());
        decision.setEnabledSkuCount(visible ? 1 : 0);
        decision.setTotalSkuCount(1);

        SubscriptionVisibilityResultBO resultBO = new SubscriptionVisibilityResultBO();
        resultBO.setGradeResolve(gradeResolve);
        resultBO.setDecisions(List.of(decision));
        return resultBO;
    }

    private SubscriptionOrderEligibilityReqDTO.Item item(Integer requestIndex, Long skuId) {
        return new SubscriptionOrderEligibilityReqDTO.Item()
                .setRequestIndex(requestIndex)
                .setStudentId(1L)
                .setWindowSkuId(40L)
                .setSkuId(skuId)
                .setCount(1);
    }

}
