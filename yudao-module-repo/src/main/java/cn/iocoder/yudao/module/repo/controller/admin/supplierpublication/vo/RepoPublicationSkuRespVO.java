package cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 刊物 SKU 选择 Response VO")
@Data
public class RepoPublicationSkuRespVO {

    @Schema(description = "商品 SPU 编号", example = "100")
    private Long spuId;

    @Schema(description = "商品 SKU 编号", example = "1000")
    private Long skuId;

    @Schema(description = "刊物名称", example = "读者")
    private String productName;

    @Schema(description = "商品 SKU 名称", example = "读者-全学年")
    private String productSkuName;

    @Schema(description = "ISBN", example = "ISBN978-7-5436-9310-0")
    private String isbn;

}
