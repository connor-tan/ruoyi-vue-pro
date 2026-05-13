package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 手动创建订单 Request VO")
@Data
public class TradeOrderManualCreateReqVO {

    @Schema(description = "商品项数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商品不能为空")
    @Valid
    private List<Item> items;

    @Schema(description = "整单自定义金额，单位：分；不填则按商品明细金额合计")
    @Min(value = 1, message = "整单自定义金额必须大于 0")
    private Integer manualOrderPrice;

    @Schema(description = "默认配送方式", example = "1")
    @InEnum(value = DeliveryTypeEnum.class, message = "配送方式不正确")
    private Integer deliveryType;

    @Schema(description = "收件人名称")
    private String receiverName;

    @Schema(description = "收件人手机")
    @Mobile(message = "收件人手机格式不正确")
    private String receiverMobile;

    @Schema(description = "收件地区编号")
    private Integer receiverAreaId;

    @Schema(description = "收件详细地址")
    private String receiverDetailAddress;

    @Schema(description = "自提门店编号")
    private Long pickUpStoreId;

    @Schema(description = "商家备注")
    private String remark;

    @Data
    public static class Item {

        @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "商品 SKU 编号不能为空")
        private Long skuId;

        @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "购买数量不能为空")
        @Min(value = 1, message = "购买数量必须大于 0")
        private Integer count;

        @Schema(description = "实际配送方式")
        @InEnum(value = DeliveryTypeEnum.class, message = "配送方式不正确")
        private Integer deliveryType;

        @Schema(description = "刊物订购学生编号")
        private Long studentId;

        @Schema(description = "订刊窗口 SKU 编号")
        private Long offerSkuId;

        @Schema(description = "商品自定义单价，单位：分；不填默认使用 SKU 销售价")
        @Min(value = 1, message = "商品自定义单价必须大于 0")
        private Integer manualUnitPrice;

    }

}
