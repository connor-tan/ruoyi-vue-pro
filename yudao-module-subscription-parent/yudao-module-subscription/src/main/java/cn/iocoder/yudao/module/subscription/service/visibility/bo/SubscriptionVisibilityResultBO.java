package cn.iocoder.yudao.module.subscription.service.visibility.bo;

import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class SubscriptionVisibilityResultBO {

    private SubscriptionWindowDO window;

    private SubscriptionGradeResolveRespBO gradeResolve;

    private String blockedReason;

    private String blockedReasonDesc;

    private List<SubscriptionVisibleSpuBO> visibleSpus = Collections.emptyList();

    private List<SubscriptionSpuVisibilityDecisionBO> decisions = Collections.emptyList();
}
