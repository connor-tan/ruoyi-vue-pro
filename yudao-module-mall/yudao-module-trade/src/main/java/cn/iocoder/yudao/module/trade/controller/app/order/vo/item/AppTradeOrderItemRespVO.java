package cn.iocoder.yudao.module.trade.controller.app.order.vo.item;

import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 订单交易项 Response VO")
@Data
public class AppTradeOrderItemRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long orderId;

    @Schema(description = "配送单编号", example = "1")
    private Long deliveryId;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long spuId;
    @Schema(description = "商品 SPU 名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    private String spuName;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long skuId;

    /**
     * 属性数组
     */
    private List<AppProductPropertyValueDetailRespVO> properties;

    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    private String picUrl;

    @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer count;

    @Schema(description = "是否评价", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean commentStatus;

    // ========== 价格 + 支付基本信息 ==========

    @Schema(description = "商品原价（单）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer price;

    @Schema(description = "应付金额（总），单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer payPrice;

    // ========== 营销基本信息 ==========

    // TODO 芋艿：在捉摸一下

    // ========== 售后基本信息 ==========

    @Schema(description = "售后编号", example = "1024")
    private Long afterSaleId;

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

}
