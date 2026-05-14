package cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 应用商品 SKU 默认期次模板 Request VO")
@Data
public class SubscriptionOfferSkuIssueApplyDefaultTemplateReqVO {

    @Schema(description = "窗口 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "窗口 SKU 不能为空")
    private Long offerSkuId;

    @Schema(description = "是否覆盖已有窗口期次计划", example = "false")
    private Boolean overwrite;

}
