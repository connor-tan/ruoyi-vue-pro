package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 学年精简 Response VO")
@Data
public class SchoolYearSimpleRespVO {

    @Schema(description = "学年编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "全局学年目录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long yearCatalogId;

    @Schema(description = "学年名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023-2024学年")
    private String name;

    @Schema(description = "学年开始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023")
    private Integer yearStart;

    @Schema(description = "学年结束年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024")
    private Integer yearEnd;

    @Schema(description = "开学日期", example = "2023-09-01")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2024-08-31")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate endDate;

}
