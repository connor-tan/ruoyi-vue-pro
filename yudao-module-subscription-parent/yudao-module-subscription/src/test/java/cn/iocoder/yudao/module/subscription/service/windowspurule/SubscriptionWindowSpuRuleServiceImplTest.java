package cn.iocoder.yudao.module.subscription.service.windowspurule;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuGradeDO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo.SubscriptionWindowSpuRulePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo.SubscriptionWindowSpuRuleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo.SubscriptionWindowSpuRuleSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuRuleDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuRuleMapper;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleScopeTypeEnum;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubscriptionWindowSpuRuleServiceImplTest {

    private SubscriptionWindowSpuRuleServiceImpl service;
    private SubscriptionWindowSpuRuleMapper subscriptionWindowSpuRuleMapper;
    private SubscriptionWindowSpuMapper subscriptionWindowSpuMapper;
    private SubscriptionSupportService subscriptionSupportService;

    @BeforeEach
    void setUp() {
        service = new SubscriptionWindowSpuRuleServiceImpl();
        subscriptionWindowSpuRuleMapper = mock(SubscriptionWindowSpuRuleMapper.class);
        subscriptionWindowSpuMapper = mock(SubscriptionWindowSpuMapper.class);
        subscriptionSupportService = mock(SubscriptionSupportService.class);
        ReflectionTestUtils.setField(service, "subscriptionWindowSpuRuleMapper", subscriptionWindowSpuRuleMapper);
        ReflectionTestUtils.setField(service, "subscriptionWindowSpuMapper", subscriptionWindowSpuMapper);
        ReflectionTestUtils.setField(service, "subscriptionSupportService", subscriptionSupportService);
    }

    @Test
    void createShouldRejectOppositeEffectOnSameScope() {
        when(subscriptionWindowSpuMapper.selectById(20L)).thenReturn(windowSpu());
        when(subscriptionWindowSpuRuleMapper.selectList(any())).thenReturn(List.of(rule(300L,
                SubscriptionRuleEffectTypeEnum.EXCLUDE.getType())));
        SubscriptionWindowSpuRuleSaveReqVO reqVO = new SubscriptionWindowSpuRuleSaveReqVO();
        reqVO.setWindowSpuId(20L);
        reqVO.setEffectType(SubscriptionRuleEffectTypeEnum.INCLUDE.getType());
        reqVO.setScopeType(SubscriptionRuleScopeTypeEnum.GRADE.getType());
        reqVO.setGradeCatalogId(2L);
        reqVO.setSort(0);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.createWindowSpuRule(reqVO));

        assertEquals(ErrorCodeConstants.WINDOW_SPU_RULE_SCOPE_CONFLICT.getCode(), exception.getCode());
    }

    @Test
    void pageShouldMarkIncludeRuleGradeApplicabilityOverride() {
        when(subscriptionWindowSpuMapper.selectById(20L)).thenReturn(windowSpu());
        when(subscriptionWindowSpuRuleMapper.selectPage(any())).thenReturn(new PageResult<>(
                List.of(rule(300L, SubscriptionRuleEffectTypeEnum.INCLUDE.getType())), 1L));
        when(subscriptionWindowSpuMapper.selectListByIds(any())).thenReturn(List.of(windowSpu()));
        when(subscriptionSupportService.getSchoolMap(any())).thenReturn(Collections.emptyMap());
        when(subscriptionSupportService.getGradeCatalogMap(any())).thenReturn(Collections.emptyMap());
        when(subscriptionSupportService.getPublicationSpuGradeMap(any())).thenReturn(Map.of(30L,
                List.of(ProductSpuGradeDO.builder()
                        .productSpuId(30L)
                        .gradeCatalogId(1L)
                        .build())));
        SubscriptionWindowSpuRulePageReqVO reqVO = new SubscriptionWindowSpuRulePageReqVO();
        reqVO.setWindowSpuId(20L);

        PageResult<SubscriptionWindowSpuRuleRespVO> pageResult = service.getWindowSpuRulePage(reqVO);

        assertEquals(1, pageResult.getTotal());
        assertTrue(pageResult.getList().get(0).getGradeApplicabilityOverride());
        assertEquals("该允许规则突破了刊物商品适用年级", pageResult.getList().get(0).getWarningReason());
    }

    private SubscriptionWindowSpuDO windowSpu() {
        return SubscriptionWindowSpuDO.builder()
                .id(20L)
                .productSpuId(30L)
                .build();
    }

    private SubscriptionWindowSpuRuleDO rule(Long id, String effectType) {
        return SubscriptionWindowSpuRuleDO.builder()
                .id(id)
                .windowSpuId(20L)
                .effectType(effectType)
                .scopeType(SubscriptionRuleScopeTypeEnum.GRADE.getType())
                .gradeCatalogId(2L)
                .sort(0)
                .build();
    }
}
