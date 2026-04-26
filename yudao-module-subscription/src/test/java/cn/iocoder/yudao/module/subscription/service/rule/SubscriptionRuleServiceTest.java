package cn.iocoder.yudao.module.subscription.service.rule;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.api.publication.ProductPublicationApi;
import cn.iocoder.yudao.module.subscription.controller.admin.rule.vo.SubscriptionRuleConditionSaveReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.rule.vo.SubscriptionRuleSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionRuleConditionDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionRuleDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionRuleConditionMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionRuleMapper;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleFactorEnum;
import cn.iocoder.yudao.module.subscription.service.offer.SubscriptionOfferService;
import cn.iocoder.yudao.module.subscription.service.offersku.SubscriptionOfferSkuService;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionRuleServiceTest {

    private static final long WINDOW_ID = 10L;
    private static final long OFFER_ID = 20L;
    private static final long OFFER_SKU_ID = 30L;

    @Mock
    private SubscriptionRuleMapper ruleMapper;
    @Mock
    private SubscriptionRuleConditionMapper conditionMapper;
    @Mock
    private SubscriptionWindowService windowService;
    @Mock
    private SubscriptionOfferService offerService;
    @Mock
    private SubscriptionOfferSkuService offerSkuService;
    @Mock
    private ProductPublicationApi productPublicationApi;
    @Mock
    private SubscriptionRuleConditionValueService conditionValueService;
    @InjectMocks
    private SubscriptionRuleService ruleService;

    @Test
    void createRule_shouldRejectOfferOutsideWindow() {
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(new SubscriptionWindowDO());
        when(offerService.validateOfferExists(OFFER_ID)).thenReturn(SubscriptionWindowOfferDO.builder()
                .id(OFFER_ID).windowId(WINDOW_ID + 1).build());
        SubscriptionRuleSaveReqVO reqVO = ruleReq();
        reqVO.setOfferId(OFFER_ID);

        assertServiceException(() -> ruleService.createRule(reqVO), RULE_OFFER_WINDOW_NOT_MATCHED);
    }

    @Test
    void createRule_shouldRejectInvalidFactor() {
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(new SubscriptionWindowDO());
        SubscriptionRuleSaveReqVO reqVO = ruleReq();
        reqVO.getConditions().get(0).setFactor("PRODUCT_PROPERTY_FAKE");

        assertServiceException(() -> ruleService.createRule(reqVO), RULE_FACTOR_INVALID);
    }

    @Test
    void createRule_shouldRejectInvalidOperator() {
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(new SubscriptionWindowDO());
        SubscriptionRuleSaveReqVO reqVO = ruleReq();
        reqVO.getConditions().get(0).setOperator("IN");

        assertServiceException(() -> ruleService.createRule(reqVO), RULE_OPERATOR_INVALID);
    }

    @Test
    void createRule_shouldRejectOfferSkuConditionOnWindowRule() {
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(new SubscriptionWindowDO());
        SubscriptionRuleSaveReqVO reqVO = ruleReq();
        reqVO.getConditions().get(0).setFactor(SubscriptionRuleFactorEnum.OFFER_SKU.getCode());
        reqVO.getConditions().get(0).setValue(String.valueOf(OFFER_SKU_ID));
        doThrow(exception(RULE_OFFER_SKU_SCOPE_INVALID)).when(conditionValueService)
                .validateAndGetValueName(SubscriptionRuleFactorEnum.OFFER_SKU.getCode(), String.valueOf(OFFER_SKU_ID),
                        WINDOW_ID, null);

        assertServiceException(() -> ruleService.createRule(reqVO), RULE_OFFER_SKU_SCOPE_INVALID);
    }

    @Test
    void createRule_shouldRejectOfferSkuOutsideOffer() {
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(new SubscriptionWindowDO());
        when(offerService.validateOfferExists(OFFER_ID)).thenReturn(SubscriptionWindowOfferDO.builder()
                .id(OFFER_ID).windowId(WINDOW_ID).build());
        SubscriptionRuleSaveReqVO reqVO = ruleReq();
        reqVO.setOfferId(OFFER_ID);
        reqVO.getConditions().get(0).setFactor(SubscriptionRuleFactorEnum.OFFER_SKU.getCode());
        reqVO.getConditions().get(0).setValue(String.valueOf(OFFER_SKU_ID));
        doThrow(exception(RULE_OFFER_SKU_NOT_MATCHED)).when(conditionValueService)
                .validateAndGetValueName(SubscriptionRuleFactorEnum.OFFER_SKU.getCode(), String.valueOf(OFFER_SKU_ID),
                        WINDOW_ID, OFFER_ID);

        assertServiceException(() -> ruleService.createRule(reqVO), RULE_OFFER_SKU_NOT_MATCHED);
    }

    @Test
    void createRule_shouldSaveOfferSkuCondition_whenOfferSkuBelongsToOffer() {
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(new SubscriptionWindowDO());
        when(offerService.validateOfferExists(OFFER_ID)).thenReturn(SubscriptionWindowOfferDO.builder()
                .id(OFFER_ID).windowId(WINDOW_ID).build());
        when(conditionValueService.validateAndGetValueName(SubscriptionRuleFactorEnum.OFFER_SKU.getCode(),
                String.valueOf(OFFER_SKU_ID), WINDOW_ID, OFFER_ID)).thenReturn("一年级上册");
        doAnswer(invocation -> {
            SubscriptionRuleDO rule = invocation.getArgument(0);
            rule.setId(99L);
            return 1;
        }).when(ruleMapper).insert(any(SubscriptionRuleDO.class));
        ArgumentCaptor<List<SubscriptionRuleConditionDO>> captor = ArgumentCaptor.forClass(List.class);
        SubscriptionRuleSaveReqVO reqVO = ruleReq();
        reqVO.setOfferId(OFFER_ID);
        reqVO.getConditions().get(0).setFactor(SubscriptionRuleFactorEnum.OFFER_SKU.getCode());
        reqVO.getConditions().get(0).setValue(String.valueOf(OFFER_SKU_ID));
        reqVO.getConditions().get(0).setValueName("一年级上册");

        ruleService.createRule(reqVO);

        verify(conditionMapper).insertBatch(captor.capture());
        assertEquals(SubscriptionRuleFactorEnum.OFFER_SKU.getCode(), captor.getValue().get(0).getFactor());
        assertEquals(String.valueOf(OFFER_SKU_ID), captor.getValue().get(0).getValue());
        assertEquals("一年级上册", captor.getValue().get(0).getValueName());
    }

    @Test
    void createRule_shouldDefaultBlankOperatorToEq() {
        when(windowService.validateWindowExists(WINDOW_ID)).thenReturn(new SubscriptionWindowDO());
        doAnswer(invocation -> {
            SubscriptionRuleDO rule = invocation.getArgument(0);
            rule.setId(99L);
            return 1;
        }).when(ruleMapper).insert(any(SubscriptionRuleDO.class));
        when(conditionValueService.validateAndGetValueName(SubscriptionRuleFactorEnum.STUDENT_GRADE.getCode(),
                "100", WINDOW_ID, null)).thenReturn("一年级");
        ArgumentCaptor<List<SubscriptionRuleConditionDO>> captor = ArgumentCaptor.forClass(List.class);
        SubscriptionRuleSaveReqVO reqVO = ruleReq();
        reqVO.getConditions().get(0).setOperator(null);

        ruleService.createRule(reqVO);

        verify(conditionMapper).insertBatch(captor.capture());
        assertEquals("EQ", captor.getValue().get(0).getOperator());
    }

    private SubscriptionRuleSaveReqVO ruleReq() {
        SubscriptionRuleSaveReqVO reqVO = new SubscriptionRuleSaveReqVO();
        reqVO.setWindowId(WINDOW_ID);
        reqVO.setName("特殊规则");
        reqVO.setEffectType(SubscriptionRuleEffectTypeEnum.EXCLUDE.getType());
        reqVO.setAllowGradeOverride(false);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setConditions(List.of(condition()));
        return reqVO;
    }

    private SubscriptionRuleConditionSaveReqVO condition() {
        SubscriptionRuleConditionSaveReqVO condition = new SubscriptionRuleConditionSaveReqVO();
        condition.setFactor(SubscriptionRuleFactorEnum.STUDENT_GRADE.getCode());
        condition.setOperator("EQ");
        condition.setValue("100");
        condition.setValueName("一年级");
        return condition;
    }

}
