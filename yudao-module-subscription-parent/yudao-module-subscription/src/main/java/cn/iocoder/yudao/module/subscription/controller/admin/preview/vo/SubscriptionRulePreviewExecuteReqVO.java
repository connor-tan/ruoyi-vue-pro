package cn.iocoder.yudao.module.subscription.controller.admin.preview.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionRulePreviewExecuteReqVO {

    @NotNull(message = "窗口不能为空")
    private Long windowId;

    @NotNull(message = "学生不能为空")
    private Long studentId;
}
