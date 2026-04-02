package cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleScopeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 窗口刊物特殊规则新增/修改 Request VO")
@Data
public class SubscriptionWindowPublicationRuleSaveReqVO {

    private Long id;

    @NotNull(message = "窗口刊物不能为空")
    private Long windowPublicationId;

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
