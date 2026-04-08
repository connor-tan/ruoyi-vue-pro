package cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeCalcRuleEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionTargetPeriodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 订刊规则模板分页 Request VO")
@Data
public class SubscriptionWindowTemplatePageReqVO extends PageParam {

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "目标周期")
    @InEnum(value = SubscriptionTargetPeriodEnum.class, message = "目标周期必须是 {value}")
    private String targetPeriod;

    @Schema(description = "年级判定")
    @InEnum(value = SubscriptionGradeCalcRuleEnum.class, message = "年级判定必须是 {value}")
    private String gradeCalcRule;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
