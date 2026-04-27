package cn.iocoder.yudao.module.subscription.controller.admin.offer.vo;

import lombok.Data;

import java.util.List;

@Data
public class SubscriptionOfferBatchCreateRespVO {

    private Integer createdOfferCount;

    private Integer createdOfferSkuCount;

    private Integer skippedCount;

    private List<Long> createdOfferIds;

    private List<SkippedItem> skippedItems;

    @Data
    public static class SkippedItem {

        private Long productSpuId;

        private String productName;

        private String reason;

    }

}
