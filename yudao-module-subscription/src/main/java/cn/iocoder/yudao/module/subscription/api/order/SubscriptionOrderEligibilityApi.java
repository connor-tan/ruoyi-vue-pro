package cn.iocoder.yudao.module.subscription.api.order;

import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface SubscriptionOrderEligibilityApi {

    SubscriptionOrderEligibilityRespDTO validateOrder(@Valid SubscriptionOrderEligibilityReqDTO reqDTO);

    List<SubscriptionOrderEligibilityRespDTO> validateOrderList(@Valid List<@Valid SubscriptionOrderEligibilityReqDTO> reqList);

}
