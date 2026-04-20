package cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeCalcRuleEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeResolveModeEnum;
import cn.iocoder.yudao.module.publication.enums.PublicationTargetPeriodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 订刊规则模板新增/修改 Request VO")
@Data
public class SubscriptionWindowTemplateSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板名称不能为空")
    private String name;

    @Schema(description = "目标周期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "目标周期不能为空")
    @InEnum(value = PublicationTargetPeriodEnum.class, message = "目标周期必须是 {value}")
    private String targetPeriod;

    @Schema(description = "年级判定", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "年级判定不能为空")
    @InEnum(value = SubscriptionGradeCalcRuleEnum.class, message = "年级判定必须是 {value}")
    private String gradeCalcRule;

    @Schema(description = "年级解析模式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "年级解析模式不能为空")
    @InEnum(value = SubscriptionGradeResolveModeEnum.class, message = "年级解析模式必须是 {value}")
    private String gradeResolveMode;

    @Schema(description = "模板说明")
    private String description;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
