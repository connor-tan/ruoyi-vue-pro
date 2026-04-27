package cn.iocoder.yudao.module.subscription.service.rule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.rule.vo.SubscriptionRulePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.rule.vo.SubscriptionRuleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.rule.vo.SubscriptionRuleSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionRuleConditionDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionRuleDO;

import java.util.List;
import java.util.Map;

/**
 * 订刊规则 Service 接口
 */
public interface SubscriptionRuleService {

    Long createRule(SubscriptionRuleSaveReqVO reqVO);

    void updateRule(SubscriptionRuleSaveReqVO reqVO);

    void deleteRule(Long id);

    SubscriptionRuleDO getRule(Long id);

    SubscriptionRuleDO validateRuleExists(Long id);

    PageResult<SubscriptionRuleRespVO> getRulePage(SubscriptionRulePageReqVO reqVO);

    SubscriptionRuleRespVO getRuleResp(Long id);

    List<SubscriptionRuleDO> getRuleListByWindowId(Long windowId);

    Map<Long, List<SubscriptionRuleConditionDO>> getConditionMap(List<SubscriptionRuleDO> rules);

}
