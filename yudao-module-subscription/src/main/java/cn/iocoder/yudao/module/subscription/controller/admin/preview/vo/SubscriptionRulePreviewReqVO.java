package cn.iocoder.yudao.module.subscription.controller.admin.preview.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 订刊规则预览 Request VO")
@Data
public class SubscriptionRulePreviewReqVO {

    @NotNull(message = "学生不能为空")
    private Long studentId;

    @NotNull(message = "窗口不能为空")
    private Long windowId;
}
