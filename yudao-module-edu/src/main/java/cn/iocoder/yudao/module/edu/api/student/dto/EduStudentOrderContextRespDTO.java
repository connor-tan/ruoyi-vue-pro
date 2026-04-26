package cn.iocoder.yudao.module.edu.api.student.dto;

import lombok.Data;

@Data
public class EduStudentOrderContextRespDTO {

    private Long studentId;

    private String studentName;

    private Integer status;

    private Long schoolId;

    private String schoolName;

    private Long classId;

    private String className;

    private Long gradeCatalogId;

    private String gradeName;

    private Long stationId;

    private String stationName;

    private String stationAddress;

    private String contactName;

    private String contactMobile;

}
