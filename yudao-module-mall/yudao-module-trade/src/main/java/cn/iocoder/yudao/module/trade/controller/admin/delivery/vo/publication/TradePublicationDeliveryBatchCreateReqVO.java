package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 刊物站点批次创建并发货 Request VO")
@Data
public class TradePublicationDeliveryBatchCreateReqVO {

    @Schema(description = "学校编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "学校不能为空")
    private Long schoolId;

    @Schema(description = "站点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    @NotNull(message = "站点不能为空")
    private Long stationId;

    @Schema(description = "订刊窗口编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "订刊窗口不能为空")
    private Long windowId;

    @Schema(description = "订刊窗口刊物编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "订刊刊物不能为空")
    private Long offerId;

    @Schema(description = "订刊窗口 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "订刊 SKU 不能为空")
    private Long offerSkuId;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    @NotNull(message = "商品 SKU 不能为空")
    private Long skuId;

    @Schema(description = "备注", example = "第一批到货")
    private String remark;

}
