package cn.iocoder.yudao.module.subscription.controller.admin.window.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 订刊窗口 Response VO")
@Data
public class SubscriptionWindowRespVO {

    private Long id;

    private String name;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    private Integer targetYearStart;

    private Integer targetYearEnd;

    private String targetYearName;

    private Integer targetSemester;

    private String gradeCalcRule;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;
}
