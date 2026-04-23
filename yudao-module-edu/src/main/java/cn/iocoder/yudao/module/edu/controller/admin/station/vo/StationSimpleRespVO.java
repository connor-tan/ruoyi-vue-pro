package cn.iocoder.yudao.module.edu.controller.admin.station.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 站点精简 Response VO")
@Data
public class StationSimpleRespVO {

    @Schema(description = "站点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "梁溪站")
    private String stationName;

    @Schema(description = "区域编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "320205")
    private Long areaId;

    @Schema(description = "区域名称", example = "江苏省 无锡市 梁溪区")
    private String areaName;
}
