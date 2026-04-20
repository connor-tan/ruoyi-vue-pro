package cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo;

import lombok.Data;

import java.util.List;

@Data
public class SubscriptionWindowSpuBatchCreateRespVO {

    private Integer createdWindowSpuCount;

    private Integer createdGradeCount;

    private Integer skippedCount;

    private List<SkippedItem> skippedItems;

    @Data
    public static class SkippedItem {

        private Long productSpuId;

        private String productName;

        private List<Long> skippedGradeCatalogIds;

        private String skippedGradeNames;

        private String reason;
    }
}
