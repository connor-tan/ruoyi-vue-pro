package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 学生全局批量升班执行 Response VO")
@Data
public class StudentGlobalPromotionExecuteRespVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long taskId;

    @Schema(description = "汇总")
    private StudentGlobalPromotionSummaryRespVO summary;

    @Schema(description = "学校执行结果")
    private List<StudentGlobalPromotionSchoolRespVO> schools;

}
