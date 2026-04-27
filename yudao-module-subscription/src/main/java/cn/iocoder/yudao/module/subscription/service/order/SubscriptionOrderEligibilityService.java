package cn.iocoder.yudao.module.subscription.service.order;

import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;

/**
 * 订刊下单资格 Service 接口
 */
public interface SubscriptionOrderEligibilityService {

    SubscriptionOrderEligibilityRespDTO validateOrder(SubscriptionOrderEligibilityReqDTO reqDTO);

}
