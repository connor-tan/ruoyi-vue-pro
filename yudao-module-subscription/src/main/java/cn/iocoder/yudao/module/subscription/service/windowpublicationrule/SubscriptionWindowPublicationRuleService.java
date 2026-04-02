package cn.iocoder.yudao.module.subscription.service.windowpublicationrule;

import cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule.vo.SubscriptionWindowPublicationRulePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule.vo.SubscriptionWindowPublicationRuleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule.vo.SubscriptionWindowPublicationRuleSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowPublicationRuleDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

public interface SubscriptionWindowPublicationRuleService {

    Long createWindowPublicationRule(@Valid SubscriptionWindowPublicationRuleSaveReqVO createReqVO);

    void updateWindowPublicationRule(@Valid SubscriptionWindowPublicationRuleSaveReqVO updateReqVO);

    List<SubscriptionWindowPublicationRuleRespVO> getWindowPublicationRuleList(SubscriptionWindowPublicationRulePageReqVO pageReqVO);

    List<SubscriptionWindowPublicationRuleDO> getWindowPublicationRuleDOList(Collection<Long> windowPublicationIds);
}
