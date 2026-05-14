package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 学校精简 Response VO")
@Data
public class SchoolSimpleRespVO {

    @Schema(description = "学校编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学校名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "无锡市实验小学")
    private String schoolName;

    @Schema(description = "归属站点编号", example = "1")
    private Long stationId;

    @Schema(description = "归属站点名称", example = "梁溪站")
    private String stationName;

    @Schema(description = "归属站点区域编号", example = "320205")
    private Long stationAreaId;

    @Schema(description = "归属站点区域名称", example = "江苏省 无锡市 梁溪区")
    private String stationAreaName;

    @Schema(description = "学校配送仓库编号", example = "1")
    private Long warehouseId;

    @Schema(description = "学校配送仓库名称", example = "滨湖履约仓")
    private String warehouseName;

    @Schema(description = "办学学段编码列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"primary\",\"middle\"]")
    private List<String> stageCodes;

}
