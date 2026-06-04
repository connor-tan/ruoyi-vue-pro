package cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 刊物收货单创建 Request VO")
@Data
public class RepoPublicationReceiptCreateReqVO {

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "收货仓库不能为空")
    private Long warehouseId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "收货明细")
    @Valid
    @NotEmpty(message = "收货单明细不能为空")
    private List<Item> items;

    @Schema(description = "管理后台 - 刊物收货单创建明细 Request VO")
    @Data
    public static class Item {

        @Schema(description = "订刊窗口编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "订刊窗口不能为空")
        private Long windowId;

        @Schema(description = "订刊窗口刊物编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "订刊窗口刊物不能为空")
        private Long offerId;

        @Schema(description = "订刊窗口 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @NotNull(message = "订刊窗口 SKU 不能为空")
        private Long offerSkuId;

        @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
        @NotNull(message = "商品 SKU 不能为空")
        private Long skuId;

        @Schema(description = "订刊期次编号", example = "10000")
        private Long issueId;

        @Schema(description = "期号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "期号不能为空")
        private Integer issueNo;

        @Schema(description = "应收数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "120")
        @NotNull(message = "应收数量不能为空")
        private Integer expectedCount;

        @Schema(description = "备注")
        private String remark;

    }

}
