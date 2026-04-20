package cn.iocoder.yudao.module.subscription.api.order;

import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;

import java.util.List;

public interface SubscriptionOrderEligibilityApi {

    List<SubscriptionOrderEligibilityRespDTO> validateOrderItems(SubscriptionOrderEligibilityReqDTO reqDTO);
}
