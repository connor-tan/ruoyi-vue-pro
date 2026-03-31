package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 升班任务 Response VO")
@Data
public class StudentPromotionTaskRespVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "来源学年开始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025")
    private Integer fromYearStart;

    @Schema(description = "目标学年开始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    private Integer toYearStart;

    @Schema(description = "范围类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "ALL")
    private String scopeType;

    @Schema(description = "是否自动创建目标班级", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean autoCreateClass;

    @Schema(description = "末级学生是否自动转待升学", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean graduateTerminalStudent;

    @Schema(description = "总学校数", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Integer totalSchoolCount;

    @Schema(description = "成功学校数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer successSchoolCount;

    @Schema(description = "跳过学校数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer skippedSchoolCount;

    @Schema(description = "失败学校数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer failedSchoolCount;

    @Schema(description = "总人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Integer totalCount;

    @Schema(description = "升班人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "180")
    private Integer promotedCount;

    @Schema(description = "留级人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    private Integer repeatCount;

    @Schema(description = "待升学人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer pendingAdvanceCount;

    @Schema(description = "跳过人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer skippedCount;

    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "是否允许回滚", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean rollbackable;

    @Schema(description = "备注", example = "2026 学年统一升班")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
