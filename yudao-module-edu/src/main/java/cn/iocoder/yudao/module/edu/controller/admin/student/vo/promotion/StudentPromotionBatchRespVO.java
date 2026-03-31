package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 单校升班批次 Response VO")
@Data
public class StudentPromotionBatchRespVO {

    @Schema(description = "批次ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long id;

    @Schema(description = "学校ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long schoolId;

    @Schema(description = "学校名称", example = "无锡市实验小学")
    private String schoolName;

    @Schema(description = "来源学年ID", example = "1")
    private Long fromSchoolYearId;

    @Schema(description = "来源学年名称", example = "2025-2026学年")
    private String fromSchoolYearName;

    @Schema(description = "目标学年ID", example = "2")
    private Long toSchoolYearId;

    @Schema(description = "目标学年名称", example = "2026-2027学年")
    private String toSchoolYearName;

    @Schema(description = "总人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer totalCount;

    @Schema(description = "升班人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "24")
    private Integer promotedCount;

    @Schema(description = "留级人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer repeatCount;

    @Schema(description = "待升学人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer pendingAdvanceCount;

    @Schema(description = "跳过人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer skippedCount;

    @Schema(description = "批次状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "状态原因", example = "NO_ELIGIBLE_STUDENTS")
    private String reason;

    @Schema(description = "备注", example = "批量升班")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
