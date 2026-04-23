package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 学生全局批量升班回滚 Request VO")
@Data
public class StudentGlobalPromotionRollbackReqVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "任务不能为空")
    private Long taskId;

    @Schema(description = "回滚备注", example = "本次升班执行异常，回滚恢复")
    private String remark;

}
