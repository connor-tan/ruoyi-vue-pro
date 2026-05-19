package cn.iocoder.yudao.module.edu.api.student.dto;

import lombok.Data;

@Data
public class EduStudentSubscriptionContextRespDTO {

    private Long studentId;

    private String studentName;

    private Long parentUserId;

    private Integer status;

    private Long schoolId;

    private String schoolName;

    private String schoolAddress;

    private Long classId;

    private String className;

    private Long gradeCatalogId;

    private String gradeNo;

    private String gradeName;

    private String gradeAliasName;

    private Integer gradeSort;

    private String gradeResolveSource;

    private Long stationId;

    private String stationName;

    private String stationAddress;

    private String contactName;

    private String contactMobile;

    private Long warehouseId;

    private String warehouseName;

    private String warehouseAddress;

    private String warehousePrincipal;

    private String blockedReason;

    private String blockedReasonDesc;

}
