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

    private List<Diagnostic> diagnostics;

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

        private String visibilityReason;

        private String visibilityReasonDesc;

        private Boolean gradeApplicabilityOverride;

        private MatchedRule matchedRule;

        private List<Sku> skus;
    }

    @Data
    public static class Diagnostic {

        private Long windowSpuId;

        private Long productSpuId;

        private String productName;

        private Boolean visible;

        private String reason;

        private String reasonDesc;

        private Boolean gradeApplicabilityOverride;

        private Integer enabledSkuCount;

        private Integer totalSkuCount;

        private String windowTargetPeriod;

        private Integer enabledPeriodMismatchedSkuCount;

        private MatchedRule matchedRule;
    }

    @Data
    public static class MatchedRule {

        private Long id;

        private String effectType;

        private String scopeType;

        private Long schoolId;

        private Long gradeCatalogId;
    }

    @Data
    public static class Sku {

        private Long windowSkuId;

        private Long productSkuId;

        private String volumeLabel;

        private String editionLabel;

        private String targetPeriod;

        private String isbn;

        private Integer price;

        private Integer stock;

        private Integer maxQuantityPerStudent;
    }
}
