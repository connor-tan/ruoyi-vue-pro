package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 学生全局批量升班学校维度 Response VO")
@Data
public class StudentGlobalPromotionSchoolRespVO {

    @Schema(description = "学校ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long schoolId;

    @Schema(description = "学校名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "无锡市实验小学")
    private String schoolName;

    @Schema(description = "来源学年ID", example = "1")
    private Long fromSchoolYearId;

    @Schema(description = "目标学年ID", example = "2")
    private Long toSchoolYearId;

    @Schema(description = "单校批次ID", example = "10")
    private Long batchId;

    @Schema(description = "学校状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "READY")
    private String status;

    @Schema(description = "学校原因", example = "SOURCE_SCHOOL_YEAR_NOT_FOUND")
    private String reason;

    @Schema(description = "总人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer totalCount;

    @Schema(description = "升班人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "25")
    private Integer promotedCount;

    @Schema(description = "毕业人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Integer graduatedCount;

    @Schema(description = "留级人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer repeatCount;

    @Schema(description = "跳过人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer skippedCount;

    @Schema(description = "缺失目标班级人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer missingTargetClassCount;

}
