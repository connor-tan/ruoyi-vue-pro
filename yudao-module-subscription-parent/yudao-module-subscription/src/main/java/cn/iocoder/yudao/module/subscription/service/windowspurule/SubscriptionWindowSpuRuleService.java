package cn.iocoder.yudao.module.subscription.service.windowspurule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo.SubscriptionWindowSpuRulePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo.SubscriptionWindowSpuRuleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo.SubscriptionWindowSpuRuleSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuRuleDO;

import java.util.Collection;
import java.util.List;

public interface SubscriptionWindowSpuRuleService {

    PageResult<SubscriptionWindowSpuRuleRespVO> getWindowSpuRulePage(SubscriptionWindowSpuRulePageReqVO reqVO);

    Long createWindowSpuRule(SubscriptionWindowSpuRuleSaveReqVO reqVO);

    void updateWindowSpuRule(SubscriptionWindowSpuRuleSaveReqVO reqVO);

    void deleteWindowSpuRule(Long id);

    List<SubscriptionWindowSpuRuleDO> getWindowSpuRuleDOList(Collection<Long> windowSpuIds);
}
