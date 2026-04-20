package cn.iocoder.yudao.module.subscription.api.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubscriptionOrderEligibilityReqDTO {

    private Long userId;

    private List<Item> items;

    @Data
    public static class Item {

        private Integer requestIndex;

        private Long studentId;

        private Long windowSkuId;

        private Long skuId;

        private Integer count;
    }
}
