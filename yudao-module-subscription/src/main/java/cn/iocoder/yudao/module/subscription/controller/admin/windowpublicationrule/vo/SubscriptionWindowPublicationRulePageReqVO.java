package cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 窗口刊物特殊规则列表 Request VO")
@Data
public class SubscriptionWindowPublicationRulePageReqVO extends PageParam {

    @Schema(description = "窗口刊物ID")
    private Long windowPublicationId;

    @Schema(description = "规则效果")
    private String effectType;

    @Schema(description = "规则范围")
    private String scopeType;
}
