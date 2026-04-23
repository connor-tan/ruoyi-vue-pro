package cn.iocoder.yudao.module.edu.api.station.dto;

import lombok.Data;

@Data
public class EduSchoolStationRespDTO {

    private Long schoolId;

    private String schoolName;

    private Long schoolAreaId;

    private Long stationId;

    private String stationName;

    private Long stationAreaId;

    private String stationAddress;

    private String contactName;

    private String contactMobile;

    private Integer status;
}
