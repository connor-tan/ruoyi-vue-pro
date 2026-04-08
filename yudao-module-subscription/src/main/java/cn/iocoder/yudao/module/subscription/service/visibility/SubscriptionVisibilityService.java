package cn.iocoder.yudao.module.subscription.service.visibility;

import cn.iocoder.yudao.module.subscription.controller.admin.preview.vo.SubscriptionRulePreviewRespVO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionVisibilityResultBO;

public interface SubscriptionVisibilityService {

    SubscriptionVisibilityResultBO calculate(Long studentId, Long windowId);

    SubscriptionRulePreviewRespVO preview(Long studentId, Long windowId);
}
