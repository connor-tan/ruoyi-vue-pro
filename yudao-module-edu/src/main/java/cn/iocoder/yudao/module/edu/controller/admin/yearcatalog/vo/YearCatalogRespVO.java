package cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 学年目录 Response VO")
@Data
public class YearCatalogRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "开始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    private Integer yearStart;

    @Schema(description = "结束年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2027")
    private Integer yearEnd;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-2027学年")
    private String name;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;
}
