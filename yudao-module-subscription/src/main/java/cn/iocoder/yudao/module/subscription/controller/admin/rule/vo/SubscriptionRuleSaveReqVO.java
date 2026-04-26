package cn.iocoder.yudao.module.subscription.controller.admin.rule.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubscriptionRuleSaveReqVO {

    private Long id;

    @NotNull(message = "窗口编号不能为空")
    private Long windowId;

    private Long offerId;

    @NotBlank(message = "规则名称不能为空")
    private String name;

    @NotBlank(message = "规则作用不能为空")
    private String effectType;

    private Boolean allowGradeOverride;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;

    @Valid
    @NotEmpty(message = "规则条件不能为空")
    private List<SubscriptionRuleConditionSaveReqVO> conditions;

}
