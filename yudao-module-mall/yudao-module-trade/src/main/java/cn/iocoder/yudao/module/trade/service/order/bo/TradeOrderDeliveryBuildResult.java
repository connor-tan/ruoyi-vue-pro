package cn.iocoder.yudao.module.trade.service.order.bo;

import java.util.List;
import java.util.Objects;

public record TradeOrderDeliveryBuildResult(List<TradeOrderDeliveryGroupDraft> plans, Integer summaryDeliveryType) {

    public TradeOrderDeliveryGroupDraft findByDeliveryType(Integer deliveryType) {
        return plans.stream()
                .filter(plan -> Objects.equals(plan.getDeliveryType(), deliveryType))
                .findFirst()
                .orElse(null);
    }

}
