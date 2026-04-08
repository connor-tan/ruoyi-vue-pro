package cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 订刊规则模板状态更新 Request VO")
@Data
public class SubscriptionWindowTemplateUpdateStatusReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}
