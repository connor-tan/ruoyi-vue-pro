package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 班级新增/修改 Request VO")
@Data
public class SchoolClassSaveReqVO {

    @Schema(description = "班级编号", example = "1")
    private Long id;

    @Schema(description = "学校编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学校ID不能为空")
    private Long schoolId;

    @Schema(description = "入学批次", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023")
    @NotNull(message = "入学批次不能为空")
    private Integer entryYear;

    @Schema(description = "学校年级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学校年级不能为空")
    private Long schoolGradeId;

    @Schema(description = "学年编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学校学年不能为空")
    private Long schoolYearId;

    @Schema(description = "班级号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "班级号不能为空")
    private Integer classNo;

    @Schema(description = "班级名称", example = "2023级一年级1班")
    private String className;

}
