package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 年级目录精简 Response VO")
@Data
public class GradeCatalogSimpleRespVO {

    @Schema(description = "目录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "阶段", requiredMode = Schema.RequiredMode.REQUIRED, example = "primary")
    private String stage;

    @Schema(description = "年级标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "P1")
    private String gradeNo;

    @Schema(description = "年级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一年级")
    private String gradeName;

}
