package cn.iocoder.yudao.module.subscription.api.order;

import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import cn.iocoder.yudao.module.subscription.service.order.SubscriptionOrderEligibilityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class SubscriptionOrderEligibilityApiImpl implements SubscriptionOrderEligibilityApi {

    @Resource
    private SubscriptionOrderEligibilityService orderEligibilityService;

    @Override
    public SubscriptionOrderEligibilityRespDTO validateOrder(SubscriptionOrderEligibilityReqDTO reqDTO) {
        return orderEligibilityService.validateOrder(reqDTO);
    }

    @Override
    public List<SubscriptionOrderEligibilityRespDTO> validateOrderList(List<SubscriptionOrderEligibilityReqDTO> reqList) {
        return orderEligibilityService.validateOrderList(reqList);
    }

}
