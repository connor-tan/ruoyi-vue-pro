package cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 刊物供应商新增/修改 Request VO")
@Data
public class RepoSupplierPublicationSkuSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    @NotNull(message = "刊物 SKU 不能为空")
    private Long skuId;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "开启状态不能为空")
    private Integer status;

    @Schema(description = "排序", example = "10")
    @NotNull(message = "排序不能为空")
    private Long sort;

    @Schema(description = "备注")
    private String remark;

}
