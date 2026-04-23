package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 学生全局批量升班回滚 Response VO")
@Data
public class StudentGlobalPromotionRollbackRespVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long taskId;

    @Schema(description = "回滚学校数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer rolledBackSchoolCount;

    @Schema(description = "回滚学生数", requiredMode = Schema.RequiredMode.REQUIRED, example = "120")
    private Integer rolledBackStudentCount;

}
