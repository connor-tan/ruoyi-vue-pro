package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 学校年级精简 Response VO")
@Data
public class SchoolGradeSimpleRespVO {

    @Schema(description = "学校年级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "年级目录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long gradeCatalogId;

    @Schema(description = "阶段", requiredMode = Schema.RequiredMode.REQUIRED, example = "primary")
    private String stage;

    @Schema(description = "年级标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "P1")
    private String gradeNo;

    @Schema(description = "年级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一年级")
    private String gradeName;

    @Schema(description = "年级别名", example = "七年级")
    private String aliasName;

    @Schema(description = "最大班号/班级容量，0 表示暂不开放 APP 选择或自动建班", example = "25")
    private Integer maxClassNo;

}
