package cn.iocoder.yudao.module.subscription.controller.admin.rule.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SubscriptionRulePageReqVO extends PageParam {

    @NotNull(message = "窗口编号不能为空")
    private Long windowId;

    @NotNull(message = "规则作用域不能为空")
    private String scope;

    private Long offerId;

    private String effectType;

    private Integer status;

}
