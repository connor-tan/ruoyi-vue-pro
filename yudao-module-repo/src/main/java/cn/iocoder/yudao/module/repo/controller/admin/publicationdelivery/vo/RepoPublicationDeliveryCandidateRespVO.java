package cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 仓库刊物发货候选 Response VO")
@Data
public class RepoPublicationDeliveryCandidateRespVO {

    @Schema(description = "配送方式", example = "3")
    private Integer deliveryType;

    @Schema(description = "学校编号", example = "100")
    private Long schoolId;

    @Schema(description = "学校名称快照", example = "实验小学")
    private String schoolNameSnapshot;

    @Schema(description = "站点编号", example = "300")
    private Long stationId;

    @Schema(description = "站点名称快照", example = "梁溪站点")
    private String stationNameSnapshot;

    @Schema(description = "学校配送仓库编号", example = "200")
    private Long warehouseId;

    @Schema(description = "学校配送仓库名称快照", example = "梁溪站")
    private String warehouseNameSnapshot;

    @Schema(description = "订刊窗口编号", example = "1")
    private Long windowId;

    @Schema(description = "订刊窗口名称快照", example = "2026 春季订刊")
    private String windowNameSnapshot;

    @Schema(description = "订刊窗口刊物编号", example = "10")
    private Long offerId;

    @Schema(description = "订刊窗口 SKU 编号", example = "100")
    private Long offerSkuId;

    @Schema(description = "商品 SKU 编号", example = "1000")
    private Long skuId;

    @Schema(description = "刊物商品名称快照", example = "读者")
    private String productNameSnapshot;

    @Schema(description = "商品 SKU 名称", example = "读者-全学年")
    private String productSkuName;

    @Schema(description = "ISBN", example = "ISBN978-7-5436-9310-0")
    private String isbn;

    @Schema(description = "订刊期次编号", example = "10000")
    private Long issueId;

    @Schema(description = "期号", example = "1")
    private Integer issueNo;

    @Schema(description = "期次名称", example = "第 1 期")
    private String issueName;

    @Schema(description = "计划配送日期")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY, timezone = TIME_ZONE_DEFAULT)
    private LocalDate plannedDeliveryDate;

    @Schema(description = "待发货数量", example = "20")
    private Integer totalCount;

    @Schema(description = "涉及订单数", example = "18")
    private Integer orderCount;

    @Schema(description = "涉及学生数", example = "18")
    private Integer studentCount;

    @Schema(description = "已到货数量", example = "100")
    private Integer receivedCount;

    @Schema(description = "已出库占用数量", example = "60")
    private Integer allocatedCount;

    @Schema(description = "可发余额", example = "40")
    private Integer availableCount;

    @Schema(description = "缺口数量", example = "0")
    private Integer shortageCount;

}
