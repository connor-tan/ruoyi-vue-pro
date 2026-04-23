package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 学生一键升班执行 Response VO")
@Data
public class StudentPromotionExecuteRespVO {

    @Schema(description = "批次ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long batchId;

    @Schema(description = "汇总")
    private StudentPromotionSummaryRespVO summary;

}
