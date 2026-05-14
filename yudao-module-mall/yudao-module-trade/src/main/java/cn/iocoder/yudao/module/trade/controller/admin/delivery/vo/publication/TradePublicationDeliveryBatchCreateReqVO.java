package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 刊物学校配送批次创建并发货 Request VO")
@Data
public class TradePublicationDeliveryBatchCreateReqVO {

    @Schema(description = "配送方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "配送方式不能为空")
    private Integer deliveryType;

    @Schema(description = "学校编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "学校不能为空")
    private Long schoolId;

    @Schema(description = "学校配送仓库编号；学校配送必填", example = "200")
    private Long warehouseId;

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

    @Schema(description = "订刊期次编号；独立刊物可为空", example = "10000")
    private Long issueId;

    @Schema(description = "期号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "期号不能为空")
    private Integer issueNo;

    @Schema(description = "备注", example = "第一批到货")
    private String remark;

    @Schema(description = "快递逐单物流明细；快递刊物必填")
    private List<ExpressItem> expressItems;

    @Data
    public static class ExpressItem {

        @Schema(description = "订单期次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
        @NotNull(message = "订单期次不能为空")
        private Long orderIssueId;

        @Schema(description = "物流公司编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "物流公司不能为空")
        private Long logisticsId;

        @Schema(description = "物流单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SF100000")
        @NotNull(message = "物流单号不能为空")
        private String logisticsNo;

    }

}
