package cn.iocoder.yudao.module.edu.controller.app.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 学校班级精简 Response VO")
@Data
public class AppSchoolClassSimpleRespVO {

    @Schema(description = "班级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学校年级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long schoolGradeId;

    @Schema(description = "学校学年编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long schoolYearId;

    @Schema(description = "入学年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025")
    private Integer entryYear;

    @Schema(description = "班级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025级一年级1班")
    private String className;

}
