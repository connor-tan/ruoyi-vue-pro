package cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 刊物收货单 Response VO")
@Data
public class RepoPublicationReceiptRespVO {

    @Schema(description = "收货单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "收货单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "pr123")
    private String receiptNo;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long supplierId;

    @Schema(description = "供应商名称快照", example = "新华印务")
    private String supplierNameSnapshot;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long warehouseId;

    @Schema(description = "仓库名称快照", example = "梁溪仓")
    private String warehouseNameSnapshot;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer status;

    @Schema(description = "应收数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "120")
    private Integer expectedCount;

    @Schema(description = "已到货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer receivedCount;

    @Schema(description = "已出库占用数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
    private Integer allocatedCount;

    @Schema(description = "提交时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime submitTime;

    @Schema(description = "关闭时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime closeTime;

    @Schema(description = "关闭原因")
    private String closeReason;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

    @Schema(description = "明细")
    private List<Item> items;

    @Schema(description = "管理后台 - 刊物收货单明细 Response VO")
    @Data
    public static class Item {

        @Schema(description = "明细编号", example = "10")
        private Long id;

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

        @Schema(description = "应收数量", example = "120")
        private Integer expectedCount;

        @Schema(description = "已到货数量", example = "100")
        private Integer receivedCount;

        @Schema(description = "已出库占用数量", example = "60")
        private Integer allocatedCount;

        @Schema(description = "可发余额", example = "40")
        private Integer availableCount;

        @Schema(description = "备注")
        private String remark;

    }

}
