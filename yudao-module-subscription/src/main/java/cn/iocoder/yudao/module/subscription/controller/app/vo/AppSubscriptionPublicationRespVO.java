package cn.iocoder.yudao.module.subscription.controller.app.vo;

import lombok.Data;

import java.util.List;

@Data
public class AppSubscriptionPublicationRespVO {

    private AppSubscriptionWindowRespVO.Window window;
    private AppSubscriptionWindowRespVO.Student student;
    private String blockedReason;
    private String blockedReasonDesc;
    private List<Offer> offers;

    @Data
    public static class Offer {
        private Long offerId;
        private Long productSpuId;
        private String productName;
        private String picUrl;
        private Boolean visible;
        private String reason;
        private String reasonDesc;
        private Boolean purchasable;
        private String purchaseUnavailableReasonDesc;
        private Integer totalOfferSkuCount;
        private Integer candidateSkuCount;
        private Integer finalSkuCount;
        private Long matchedRuleId;
        private String matchedRuleName;
        private Boolean gradeApplicabilityOverride;
        private List<OfferSku> finalSkus;
    }

    @Data
    public static class OfferSku {
        private Long offerSkuId;
        private Long productSkuId;
        private String productSkuName;
        private Integer price;
        private Boolean purchasable;
        private Integer maxQuantityPerStudent;
        private Integer orderedQuantity;
        private Integer remainingQuantity;
        private String purchaseUnavailableReasonDesc;
        private String isbn;
        private List<Long> applicableGradeCatalogIds;
        private List<String> applicableGradeNames;
        private String reason;
        private Long matchedRuleId;
        private String matchedRuleName;
        private Boolean gradeApplicabilityOverride;
    }

}
