package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

/**
 * 交易订单项 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class TradeOrderItemBaseVO {

    // ========== 订单项基本信息 ==========

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long orderId;

    @Schema(description = "配送组编号", example = "1")
    private Long deliveryId;

    // ========== 商品基本信息 ==========

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long spuId;

    @Schema(description = "商品 SPU 名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    private String spuName;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long skuId;

    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    private String picUrl;

    @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer count;

    // ========== 价格 + 支付基本信息 ==========

    @Schema(description = "商品原价（单）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer price;

    @Schema(description = "商品优惠（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer discountPrice;

    @Schema(description = "商品实付金额（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer payPrice;

    @Schema(description = "子订单分摊金额（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer orderPartPrice;

    @Schema(description = "分摊后子订单实付金额（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer orderDividePrice;

    // ========== 营销基本信息 ==========

    // TODO 芋艿：在捉摸一下

    // ========== 售后基本信息 ==========

    @Schema(description = "售后状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer afterSaleStatus;

    @Schema(description = "订刊学生编号", example = "1")
    private Long subscriptionStudentId;

    @Schema(description = "订刊学生名称快照", example = "张小明")
    private String subscriptionStudentNameSnapshot;

    @Schema(description = "订刊学校编号", example = "1")
    private Long subscriptionSchoolId;

    @Schema(description = "订刊学校名称快照", example = "实验小学")
    private String subscriptionSchoolNameSnapshot;

    @Schema(description = "订刊班级编号", example = "1")
    private Long subscriptionClassId;

    @Schema(description = "订刊班级名称快照", example = "2026级一年级1班")
    private String subscriptionClassNameSnapshot;

    @Schema(description = "订刊年级编号", example = "1")
    private Long subscriptionGradeCatalogId;

    @Schema(description = "订刊年级名称快照", example = "一年级")
    private String subscriptionGradeNameSnapshot;

    @Schema(description = "订刊窗口刊物编号（offer）", example = "1")
    private Long subscriptionOfferId;

    @Schema(description = "订刊窗口 SKU 编号（offerSku）", example = "1")
    private Long subscriptionOfferSkuId;

    @Schema(description = "刊物发货状态", example = "20")
    private Integer publicationDeliveryStatus;

    @Schema(description = "刊物发货批次编号", example = "1")
    private Long publicationDeliveryBatchId;

    @Schema(description = "刊物发货时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime publicationDeliveryTime;

}
