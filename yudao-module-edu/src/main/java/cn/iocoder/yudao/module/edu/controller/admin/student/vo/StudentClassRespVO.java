package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 学生班级记录 Response VO")
@Data
public class StudentClassRespVO {

    @Schema(description = "学生班级记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学生编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long studentId;

    @Schema(description = "班级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long classId;

    @Schema(description = "班级名称", example = "2023级一年级1班")
    private String className;

    @Schema(description = "入班日期", example = "2023-09-01")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate startDate;

    @Schema(description = "离班日期", example = "2024-08-31")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate endDate;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

}
