package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 升班任务分页 Request VO")
@Data
public class StudentPromotionTaskPageReqVO extends PageParam {

    @Schema(description = "任务ID", example = "1")
    private Long id;

    @Schema(description = "来源学年开始年份", example = "2025")
    private Integer fromYearStart;

    @Schema(description = "目标学年开始年份", example = "2026")
    private Integer toYearStart;

    @Schema(description = "任务状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
