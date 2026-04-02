package cn.iocoder.yudao.module.subscription.controller.admin.window.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeCalcRuleEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionSemesterEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 订刊窗口分页 Request VO")
@Data
public class SubscriptionWindowPageReqVO extends PageParam {

    @Schema(description = "窗口名称")
    private String name;

    @Schema(description = "目标学年ID")
    private Long targetSchoolYearId;

    @Schema(description = "目标学期")
    @InEnum(value = SubscriptionSemesterEnum.class, message = "目标学期必须是 {value}")
    private Integer targetSemester;

    @Schema(description = "年级计算规则")
    @InEnum(value = SubscriptionGradeCalcRuleEnum.class, message = "年级计算规则必须是 {value}")
    private String gradeCalcRule;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
