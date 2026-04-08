package cn.iocoder.yudao.module.subscription.controller.admin.window.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeCalcRuleEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionSemesterEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 订刊窗口新增/修改 Request VO")
@Data
public class SubscriptionWindowSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "窗口名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "窗口名称不能为空")
    private String name;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "目标学年开始年份", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标学年不能为空")
    private Integer targetYearStart;

    @Schema(description = "目标学年结束年份", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标学年不能为空")
    private Integer targetYearEnd;

    @Schema(description = "目标学期", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标学期不能为空")
    @InEnum(value = SubscriptionSemesterEnum.class, message = "目标学期必须是 {value}")
    private Integer targetSemester;

    @Schema(description = "年级计算规则")
    @InEnum(value = SubscriptionGradeCalcRuleEnum.class, message = "年级计算规则必须是 {value}")
    private String gradeCalcRule;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
