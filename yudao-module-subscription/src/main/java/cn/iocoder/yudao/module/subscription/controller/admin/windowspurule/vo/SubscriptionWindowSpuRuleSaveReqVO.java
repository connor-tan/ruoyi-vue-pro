package cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleScopeTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionWindowSpuRuleSaveReqVO {

    private Long id;

    @NotNull(message = "窗口刊物不能为空")
    private Long windowSpuId;

    @NotBlank(message = "规则效果不能为空")
    @InEnum(value = SubscriptionRuleEffectTypeEnum.class, message = "规则效果必须是 {value}")
    private String effectType;

    @NotBlank(message = "规则范围不能为空")
    @InEnum(value = SubscriptionRuleScopeTypeEnum.class, message = "规则范围必须是 {value}")
    private String scopeType;

    private Long schoolId;

    private Long gradeCatalogId;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    private String remark;
}
