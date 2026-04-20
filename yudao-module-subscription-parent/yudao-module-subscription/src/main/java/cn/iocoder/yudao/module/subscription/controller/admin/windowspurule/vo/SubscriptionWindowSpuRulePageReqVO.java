package cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SubscriptionWindowSpuRulePageReqVO extends PageParam {

    @Schema(description = "窗口刊物编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long windowSpuId;

    @Schema(description = "规则效果")
    private String effectType;

    @Schema(description = "规则范围")
    private String scopeType;
}
