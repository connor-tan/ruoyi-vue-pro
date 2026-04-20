package cn.iocoder.yudao.module.subscription.api.order.dto;

import lombok.Data;

@Data
public class SubscriptionOrderEligibilityRespDTO {

    private Integer requestIndex;

    private Long studentId;

    private String studentName;

    private Long schoolId;

    private String schoolName;

    private Long gradeCatalogId;

    private String gradeNo;

    private String gradeName;

    private Long windowId;

    private String windowNameSnapshot;

    private Integer targetYearStart;

    private Integer targetYearEnd;

    private String targetPeriod;

    private Long windowSpuId;

    private Long windowSkuId;

    private Long productSpuId;

    private Long productSkuId;

    private String visibilityReason;

    private String visibilityReasonDesc;

    private Long matchedRuleId;

    private String matchedRuleEffectType;

    private String matchedRuleScopeType;

    private Boolean gradeApplicabilityOverride;

    private Integer maxQuantityPerStudent;
}
