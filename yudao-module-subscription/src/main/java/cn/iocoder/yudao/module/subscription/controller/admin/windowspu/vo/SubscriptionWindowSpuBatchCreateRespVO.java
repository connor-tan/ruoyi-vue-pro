package cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo;

import lombok.Data;

import java.util.List;

@Data
public class SubscriptionWindowSpuBatchCreateRespVO {

    private Integer createdCount;

    private Integer skippedCount;

    private List<SkippedItem> skippedItems;

    @Data
    public static class SkippedItem {

        private Long productSpuId;

        private String productName;

        private String reason;
    }
}
