package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 班级精简 Response VO")
@Data
public class SchoolClassSimpleRespVO {

    @Schema(description = "班级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "入学批次", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023")
    private Integer entryYear;

    @Schema(description = "学校年级ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long schoolGradeId;

    @Schema(description = "班级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023级一年级1班")
    private String className;

    @Schema(description = "年级阶段", example = "primary")
    private String stage;

    @Schema(description = "年级标识", example = "P1")
    private String gradeNo;

    @Schema(description = "年级名称", example = "一年级")
    private String gradeName;

    @Schema(description = "年级别名", example = "七年级")
    private String aliasName;

    @Schema(description = "所属学年名称", example = "2023-2024学年")
    private String schoolYearName;

}
