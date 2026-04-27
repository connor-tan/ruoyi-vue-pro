package cn.iocoder.yudao.module.subscription.service.rule;

import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionRuleConditionValueRespVO;

import java.util.List;

/**
 * 订刊规则条件值 Service 接口
 */
public interface SubscriptionRuleConditionValueService {

    List<SubscriptionRuleConditionValueRespVO> getConditionValueList(String factor, Long windowId, Long offerId);

    String validateAndGetValueName(String factor, String value, Long windowId, Long offerId);

}
