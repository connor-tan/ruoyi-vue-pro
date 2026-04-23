package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 学生一键升班预览 Request VO")
@Data
public class StudentPromotionPreviewReqVO {

    @Schema(description = "学校ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学校不能为空")
    private Long schoolId;

    @Schema(description = "来源学年ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源学年不能为空")
    private Long fromSchoolYearId;

    @Schema(description = "目标学年ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "目标学年不能为空")
    private Long toSchoolYearId;

    @Schema(description = "是否自动创建缺失目标班级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否自动创建目标班级不能为空")
    private Boolean autoCreateClass;

    @Schema(description = "末级学生是否自动转待升学", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "末级学生是否自动转待升学不能为空")
    private Boolean graduateTerminalStudent;

    @Schema(description = "备注", example = "2026年度升班")
    private String remark;

    @Schema(description = "逐人调整配置")
    @Valid
    private List<StudentPromotionAdjustmentReqVO> adjustments;

}
