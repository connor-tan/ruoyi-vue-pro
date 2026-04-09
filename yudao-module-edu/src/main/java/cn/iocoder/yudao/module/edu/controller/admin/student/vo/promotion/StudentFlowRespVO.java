package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 学生流转 Response VO")
@Data
public class StudentFlowRespVO {

    @Schema(description = "流转ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long id;

    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    @Schema(description = "批次ID", example = "10")
    private Long batchId;

    @Schema(description = "学生ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long studentId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "原班级ID", example = "11")
    private Long fromClassId;

    @Schema(description = "原班级名称", example = "2025级一年级1班")
    private String fromClassName;

    @Schema(description = "目标班级ID", example = "12")
    private Long toClassId;

    @Schema(description = "目标班级名称", example = "2025级二年级1班")
    private String toClassName;

    @Schema(description = "流转类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "PROMOTE")
    private String changeType;

    @Schema(description = "生效日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate effectiveDate;

    @Schema(description = "流转状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "目标班级是否由本次任务自动创建")
    private Boolean targetClassCreated;

    @Schema(description = "备注", example = "2026 学年统一升班")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

}
