package cn.iocoder.yudao.module.subscription.controller.app.window.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class AppSubscriptionCurrentWindowRespVO {

    private Boolean opened;

    private Long id;

    private String name;

    private Integer targetYearStart;

    private Integer targetYearEnd;

    private String targetYearName;

    private Integer targetSemester;

    private String gradeCalcRule;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;
}
