package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 学生全局批量升班汇总 Response VO")
@Data
public class StudentGlobalPromotionSummaryRespVO {

    @Schema(description = "总学校数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer totalSchoolCount;

    @Schema(description = "可执行学校数", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    private Integer readySchoolCount;

    @Schema(description = "跳过学校数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer skippedSchoolCount;

    @Schema(description = "失败学校数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer failedSchoolCount;

    @Schema(description = "总人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "120")
    private Integer totalCount;

    @Schema(description = "升班人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer promotedCount;

    @Schema(description = "毕业人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "18")
    private Integer graduatedCount;

    @Schema(description = "留级人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer repeatCount;

    @Schema(description = "跳过人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer skippedCount;

    @Schema(description = "缺失目标班级人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer missingTargetClassCount;

}
