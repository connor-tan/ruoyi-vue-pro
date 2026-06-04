package cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 刊物收货登记 Request VO")
@Data
public class RepoPublicationReceiptReceiveReqVO {

    @Schema(description = "收货单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "收货单不能为空")
    private Long receiptId;

    @Schema(description = "收货明细")
    @Valid
    @NotEmpty(message = "收货明细不能为空")
    private List<Item> items;

    @Schema(description = "管理后台 - 刊物收货登记明细 Request VO")
    @Data
    public static class Item {

        @Schema(description = "收货单明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "收货单明细不能为空")
        private Long receiptItemId;

        @Schema(description = "捆数", example = "8")
        private Integer bundleCount;

        @Schema(description = "本次收货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "120")
        @NotNull(message = "本次收货数量不能为空")
        private Integer receivedCount;

        @Schema(description = "备注")
        private String remark;

    }

}
