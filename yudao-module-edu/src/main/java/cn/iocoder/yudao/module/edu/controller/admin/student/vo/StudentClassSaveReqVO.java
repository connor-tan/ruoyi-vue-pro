package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 学生班级记录新增/修改 Request VO")
@Data
public class StudentClassSaveReqVO {

    @Schema(description = "学生班级记录编号", example = "1")
    private Long id;

    @Schema(description = "班级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "班级不能为空")
    private Long classId;

    @Schema(description = "入班日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023-09-01")
    @NotNull(message = "入班日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate startDate;

    @Schema(description = "离班日期", example = "2024-08-31")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate endDate;

}
