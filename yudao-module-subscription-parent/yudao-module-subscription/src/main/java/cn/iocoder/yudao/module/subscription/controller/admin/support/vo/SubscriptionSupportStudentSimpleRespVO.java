package cn.iocoder.yudao.module.subscription.controller.admin.support.vo;

import lombok.Data;

@Data
public class SubscriptionSupportStudentSimpleRespVO {

    private Long id;

    private String studentName;

    private String studentCode;

    private String gradeName;

    private Long currentSchoolId;

    private String currentSchoolName;

    private Integer status;
}
