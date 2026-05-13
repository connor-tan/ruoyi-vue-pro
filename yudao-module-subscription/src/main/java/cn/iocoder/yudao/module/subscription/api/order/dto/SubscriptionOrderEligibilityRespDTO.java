package cn.iocoder.yudao.module.subscription.api.order.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SubscriptionOrderEligibilityRespDTO {

    private Long studentId;
    private String studentNameSnapshot;
    private Long schoolId;
    private String schoolNameSnapshot;
    private Long classId;
    private String classNameSnapshot;
    private Long gradeCatalogId;
    private String gradeNameSnapshot;
    private Long stationId;
    private String stationNameSnapshot;
    private String stationAddressSnapshot;
    private String contactName;
    private String contactMobile;

    private Long windowId;
    private String windowNameSnapshot;
    private Integer targetYearStart;
    private Integer targetYearEnd;

    private Long offerId;
    private Long offerSkuId;
    private String visibilityReason;
    private Long matchedRuleId;
    private Boolean gradeApplicabilityOverride;
    private Integer maxQuantityPerStudent;
    private Integer orderedQuantity;
    private String issueMode;
    private Integer issueCount;
    private List<Issue> issues;

    @Data
    public static class Issue {

        private Long issueId;
        private Integer issueNo;
        private String issueName;
        private LocalDate plannedPublishDate;
        private LocalDate plannedDeliveryDate;

    }

}
