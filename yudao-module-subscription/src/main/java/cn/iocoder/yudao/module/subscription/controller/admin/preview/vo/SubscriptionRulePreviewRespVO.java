package cn.iocoder.yudao.module.subscription.controller.admin.preview.vo;

import lombok.Data;

import java.util.List;

@Data
public class SubscriptionRulePreviewRespVO {

    private Long studentId;

    private String studentName;

    private Long schoolId;

    private String schoolName;

    private Long effectiveGradeCatalogId;

    private String effectiveGradeNo;

    private String effectiveGradeName;

    private String effectiveGradeAliasName;

    private String blockedReason;

    private List<Publication> publications;

    @Data
    public static class Publication {

        private Long windowSpuId;

        private Long productSpuId;

        private String productName;

        private String picUrl;

        private Long categoryId;

        private String categoryName;

        private String publicationTitleName;

        private Boolean recommendFlag;

        private List<Sku> skus;
    }

    @Data
    public static class Sku {

        private Long windowSkuId;

        private Long productSkuId;

        private String volumeLabel;

        private String editionLabel;

        private String isbn;

        private Integer price;

        private Integer stock;

        private Integer maxQuantityPerStudent;
    }
}
