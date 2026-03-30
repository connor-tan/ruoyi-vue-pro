package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 学生升班逐人调整 Request VO")
@Data
public class StudentPromotionAdjustmentReqVO {

    @Schema(description = "学生ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学生不能为空")
    private Long studentId;

    @Schema(description = "调整动作", requiredMode = Schema.RequiredMode.REQUIRED, example = "REPEAT")
    @NotBlank(message = "调整动作不能为空")
    private String action;

    @Schema(description = "目标班级ID", example = "10")
    private Long targetClassId;

}
