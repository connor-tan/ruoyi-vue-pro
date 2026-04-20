package cn.iocoder.yudao.module.subscription.service.visibility.bo;

import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuRuleDO;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class SubscriptionSpuVisibilityDecisionBO {

    private SubscriptionWindowSpuDO windowSpu;

    private Boolean visible;

    private String reason;

    private String reasonDesc;

    private SubscriptionWindowSpuRuleDO matchedRule;

    private Boolean gradeApplicabilityOverride;

    private Integer enabledSkuCount;

    private Integer totalSkuCount;

    private Integer enabledPeriodMismatchedSkuCount;

    private String windowTargetPeriod;

    private List<SubscriptionWindowSkuDO> enabledSkus = Collections.emptyList();
}
