package cn.iocoder.yudao.module.subscription.controller.admin.window.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 订刊窗口状态更新 Request VO")
@Data
public class SubscriptionWindowUpdateStatusReqVO {

    @NotNull(message = "编号不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private Boolean confirmWarnings;
}
