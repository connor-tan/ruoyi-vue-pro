package cn.iocoder.yudao.module.subscription.controller.admin.rule.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscriptionRuleConditionSaveReqVO {

    @NotBlank(message = "规则因子不能为空")
    private String factor;

    private String operator;

    @NotBlank(message = "规则值不能为空")
    private String value;

    private String valueName;

}
