package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 学年 Response VO")
@Data
public class SchoolYearRespVO {

    @Schema(description = "学年编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学校编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long schoolId;

    @Schema(description = "学年开始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023")
    private Integer yearStart;

    @Schema(description = "学年结束年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024")
    private Integer yearEnd;

    @Schema(description = "开学日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023-09-01")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate startDate;

    @Schema(description = "放假日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-08-31")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate endDate;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

}
