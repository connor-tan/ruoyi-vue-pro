package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 刊物期次批次发货候选明细 Response VO")
@Data
public class TradePublicationDeliveryCandidateItemRespVO {

    @Schema(description = "订单期次编号", example = "1000")
    private Long orderIssueId;

    @Schema(description = "订单编号", example = "1")
    private Long orderId;

    @Schema(description = "订单号", example = "o202605061200001")
    private String orderNo;

    @Schema(description = "订单项编号", example = "10")
    private Long orderItemId;

    @Schema(description = "配送组编号", example = "20")
    private Long deliveryId;

    @Schema(description = "用户编号", example = "100")
    private Long userId;

    @Schema(description = "配送方式", example = "1")
    private Integer deliveryType;

    @Schema(description = "商品数量", example = "1")
    private Integer count;

    @Schema(description = "学校编号", example = "100")
    private Long schoolId;

    @Schema(description = "学校名称快照", example = "实验小学")
    private String schoolNameSnapshot;

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

    @Schema(description = "学生编号", example = "1000")
    private Long studentId;

    @Schema(description = "学生名称快照", example = "张小明")
    private String studentNameSnapshot;

    @Schema(description = "班级编号", example = "2000")
    private Long classId;

    @Schema(description = "班级名称快照", example = "一年级 1 班")
    private String classNameSnapshot;

    @Schema(description = "订刊期次编号", example = "10000")
    private Long issueId;

    @Schema(description = "期号", example = "1")
    private Integer issueNo;

    @Schema(description = "期次名称", example = "第 1 期")
    private String issueName;

    @Schema(description = "计划配送日期")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY, timezone = TIME_ZONE_DEFAULT)
    private LocalDate plannedDeliveryDate;

}
