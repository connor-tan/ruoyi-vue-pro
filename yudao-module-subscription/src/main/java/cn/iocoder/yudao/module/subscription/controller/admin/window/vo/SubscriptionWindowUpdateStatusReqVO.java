package cn.iocoder.yudao.module.subscription.controller.admin.window.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionWindowUpdateStatusReqVO {

    @NotNull(message = "窗口编号不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;

}
