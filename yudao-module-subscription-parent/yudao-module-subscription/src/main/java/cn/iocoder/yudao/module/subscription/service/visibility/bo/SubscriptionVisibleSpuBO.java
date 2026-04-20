package cn.iocoder.yudao.module.subscription.service.visibility.bo;

import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import lombok.Data;

import java.util.List;

@Data
public class SubscriptionVisibleSpuBO {

    private SubscriptionWindowSpuDO windowSpu;

    private List<SubscriptionWindowSkuDO> windowSkus;

    private String visibilityReason;

    private String visibilityReasonDesc;

    private Long matchedRuleId;

    private String matchedRuleEffectType;

    private String matchedRuleScopeType;

    private Boolean gradeApplicabilityOverride;
}
