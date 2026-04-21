package cn.iocoder.yudao.module.subscription.service.visibility.bo;

import lombok.Data;

@Data
public class SubscriptionGradeResolveRespBO {

    private Long studentId;

    private String studentName;

    private Long schoolId;

    private String schoolName;

    private Long effectiveClassId;

    private String effectiveClassName;

    private Long effectiveGradeCatalogId;

    private String effectiveGradeNo;

    private String effectiveGradeName;

    private String effectiveGradeAliasName;

    private Integer effectiveGradeSort;

    private String blockedReason;

    private String blockedReasonDesc;
}
