package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 学年新增/修改 Request VO")
@Data
public class SchoolYearSaveReqVO {

    @Schema(description = "学年编号", example = "1")
    private Long id;

    @Schema(description = "学校编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学校ID不能为空")
    private Long schoolId;

    @Schema(description = "全局学年目录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "全局学年不能为空")
    private Long yearCatalogId;

    @Schema(description = "开学日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023-09-01")
    @NotNull(message = "开学日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate startDate;

    @Schema(description = "放假日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-08-31")
    @NotNull(message = "放假日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate endDate;

}
