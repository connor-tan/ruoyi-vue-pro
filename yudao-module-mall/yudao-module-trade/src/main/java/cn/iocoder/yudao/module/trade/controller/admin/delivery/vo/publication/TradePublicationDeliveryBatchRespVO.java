package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 刊物站点发货批次 Response VO")
@Data
public class TradePublicationDeliveryBatchRespVO {

    @Schema(description = "批次编号", example = "1")
    private Long id;

    @Schema(description = "批次号", example = "pd202605061200001")
    private String batchNo;

    @Schema(description = "学校编号", example = "100")
    private Long schoolId;

    @Schema(description = "学校名称快照", example = "实验小学")
    private String schoolNameSnapshot;

    @Schema(description = "站点编号", example = "200")
    private Long stationId;

    @Schema(description = "站点名称快照", example = "梁溪站")
    private String stationNameSnapshot;

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

    @Schema(description = "目标周期", example = "FULL_YEAR")
    private String targetPeriod;

    @Schema(description = "本批次数量", example = "20")
    private Integer totalCount;

    @Schema(description = "涉及订单数", example = "18")
    private Integer orderCount;

    @Schema(description = "涉及学生数", example = "18")
    private Integer studentCount;

    @Schema(description = "批次状态", example = "20")
    private Integer status;

    @Schema(description = "发货时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime deliveryTime;

    @Schema(description = "操作人", example = "1")
    private Long operatorUserId;

    @Schema(description = "备注", example = "第一批到货")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

    @Schema(description = "批次明细")
    private List<Item> items;

    @Schema(description = "管理后台 - 刊物站点发货批次明细 Response VO")
    @Data
    public static class Item {

        @Schema(description = "明细编号", example = "1")
        private Long id;

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

        @Schema(description = "商品数量", example = "1")
        private Integer count;

        @Schema(description = "学生编号", example = "1000")
        private Long studentId;

        @Schema(description = "学生名称快照", example = "张小明")
        private String studentNameSnapshot;

        @Schema(description = "班级编号", example = "2000")
        private Long classId;

        @Schema(description = "班级名称快照", example = "一年级 1 班")
        private String classNameSnapshot;

    }

}
