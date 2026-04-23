package cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 学年目录分页 Request VO")
@Data
public class YearCatalogPageReqVO extends PageParam {

    @Schema(description = "开始年份", example = "2026")
    private Integer yearStart;

    @Schema(description = "结束年份", example = "2027")
    private Integer yearEnd;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
