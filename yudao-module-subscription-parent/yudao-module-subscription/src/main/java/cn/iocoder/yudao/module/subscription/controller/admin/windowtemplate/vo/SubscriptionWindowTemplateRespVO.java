package cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 订刊规则模板 Response VO")
@Data
public class SubscriptionWindowTemplateRespVO {

    private Long id;

    private String name;

    private String targetPeriod;

    private String gradeCalcRule;

    private String gradeResolveMode;

    private String description;

    private Integer status;

    private Integer sort;

    private Boolean builtIn;

    private String remark;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTime;
}
