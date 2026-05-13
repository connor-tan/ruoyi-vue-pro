package cn.iocoder.yudao.module.subscription.controller.admin.preview.vo;

import lombok.Data;

import java.util.List;

@Data
public class SubscriptionRulePreviewRespVO {

    private Window window;
    private Student student;
    private String blockedReason;
    private String blockedReasonDesc;
    private List<OfferDecision> decisions;

    @Data
    public static class Window {
        private Long id;
        private String name;
        private Long targetYearCatalogId;
        private String targetYearNameSnapshot;
        private Integer targetYearStart;
        private Integer targetYearEnd;
    }

    @Data
    public static class Student {
        private Long studentId;
        private String studentName;
        private Long schoolId;
        private String schoolName;
        private Long classId;
        private String className;
        private Long gradeCatalogId;
        private String gradeName;
        private String gradeResolveSource;
        private Long stationId;
        private String stationName;
        private String blockedReason;
        private String blockedReasonDesc;
    }

    @Data
    public static class OfferDecision {
        private Long offerId;
        private Long productSpuId;
        private String productName;
        private String picUrl;
        private Boolean visible;
        private String reason;
        private String reasonDesc;
        private Integer totalOfferSkuCount;
        private Integer candidateSkuCount;
        private Integer finalSkuCount;
        private Long matchedRuleId;
        private String matchedRuleName;
        private Boolean gradeApplicabilityOverride;
        private List<OfferSku> finalSkus;
        private List<OfferSku> diagnosticSkus;
    }

    @Data
    public static class OfferSku {
        private Long offerSkuId;
        private Long productSkuId;
        private String productSkuName;
        private String decisionStatus;
        private String decisionStatusName;
        private Integer price;
        private Integer stock;
        private String volumeLabel;
        private String editionLabel;
        private String isbn;
        private List<Long> applicableGradeCatalogIds;
        private List<String> applicableGradeNames;
        private String reason;
        private Long matchedRuleId;
        private String matchedRuleName;
        private Boolean gradeApplicabilityOverride;
    }

}
