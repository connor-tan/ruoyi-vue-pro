package cn.iocoder.yudao.module.edu.controller.app.school.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "用户 App - 学校学年精简 Response VO")
@Data
public class AppSchoolYearSimpleRespVO {

    @Schema(description = "学年编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学年开始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    private Integer yearStart;

    @Schema(description = "学年结束年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2027")
    private Integer yearEnd;

    @Schema(description = "学年名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-2027学年")
    private String name;

    @Schema(description = "开学日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-09-01")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2027-06-30")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate endDate;

    @Schema(description = "是否当前学年", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean current;

    @Schema(description = "是否未来学年", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean future;

}
