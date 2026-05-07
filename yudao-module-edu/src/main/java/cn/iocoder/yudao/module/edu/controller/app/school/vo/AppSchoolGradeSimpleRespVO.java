package cn.iocoder.yudao.module.edu.controller.app.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 学校年级精简 Response VO")
@Data
public class AppSchoolGradeSimpleRespVO {

    @Schema(description = "学校年级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "年级目录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long gradeCatalogId;

    @Schema(description = "阶段", example = "primary")
    private String stage;

    @Schema(description = "年级标识", example = "P1")
    private String gradeNo;

    @Schema(description = "年级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一年级")
    private String gradeName;

    @Schema(description = "年级别名", example = "七年级")
    private String aliasName;

}
