package cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 刊物收货需求 Response VO")
@Data
public class RepoPublicationReceiptDemandRespVO {

    @Schema(description = "仓库编号", example = "100")
    private Long warehouseId;

    @Schema(description = "仓库名称快照", example = "梁溪仓")
    private String warehouseNameSnapshot;

    @Schema(description = "订刊窗口编号", example = "1")
    private Long windowId;

    @Schema(description = "订刊窗口名称快照", example = "2026 春季订刊")
    private String windowNameSnapshot;

    @Schema(description = "订刊窗口刊物编号", example = "10")
    private Long offerId;

    @Schema(description = "订刊窗口 SKU 编号", example = "100")
    private Long offerSkuId;

    @Schema(description = "商品 SPU 编号", example = "100")
    private Long spuId;

    @Schema(description = "商品 SKU 编号", example = "1000")
    private Long skuId;

    @Schema(description = "刊物名称", example = "读者")
    private String productNameSnapshot;

    @Schema(description = "商品 SKU 名称", example = "读者-全学年")
    private String productSkuNameSnapshot;

    @Schema(description = "ISBN", example = "ISBN978-7-5436-9310-0")
    private String isbn;

    @Schema(description = "订刊期次编号", example = "10000")
    private Long issueId;

    @Schema(description = "期号", example = "1")
    private Integer issueNo;

    @Schema(description = "期次名称", example = "第 1 期")
    private String issueName;

    @Schema(description = "订单待发需求数量", example = "120")
    private Integer demandCount;

    @Schema(description = "已到货数量", example = "100")
    private Integer receivedCount;

    @Schema(description = "已出库占用数量", example = "60")
    private Integer allocatedCount;

    @Schema(description = "可发余额", example = "40")
    private Integer availableCount;

    @Schema(description = "缺口数量", example = "80")
    private Integer shortageCount;

    @Schema(description = "建议本次应收数量", example = "80")
    private Integer suggestExpectedCount;

}
