package cn.iocoder.yudao.module.subscription.api.order.dto;

import lombok.Data;

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
    private String targetPeriod;

    private Long offerId;
    private Long offerSkuId;
    private String visibilityReason;
    private Long matchedRuleId;
    private Boolean gradeApplicabilityOverride;
    private Integer maxQuantityPerStudent;
    private Integer orderedQuantity;

}
