package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Schema(description = "管理后台 - 在线订刊下单结算 Request VO")
@Data
@Valid
public class TradeOrderAdminOnlineSettlementReqVO {

    @Schema(description = "学生编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "学生编号不能为空")
    private Long studentId;

    @Schema(description = "商品项数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "刊物不能为空")
    @Valid
    private List<Item> items;

    @Schema(description = "配送方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "配送方式不能为空")
    @InEnum(value = DeliveryTypeEnum.class, message = "配送方式不正确")
    private Integer deliveryType;

    @Schema(description = "家长地址簿地址编号", example = "1")
    private Long addressId;

    @Schema(description = "手填收件人名称", example = "张三")
    private String receiverName;

    @Schema(description = "手填收件人手机", example = "13800000000")
    @Mobile(message = "收件人手机格式不正确")
    private String receiverMobile;

    @Schema(description = "手填收件地区编号", example = "110101")
    private Integer receiverAreaId;

    @Schema(description = "手填收件详细地址", example = "示例路 1 号")
    private String receiverDetailAddress;

    @AssertTrue(message = "后台在线下单仅支持刊物学校配送或快递配送")
    @JsonIgnore
    public boolean isDeliveryTypeSupported() {
        return Objects.equals(deliveryType, DeliveryTypeEnum.SCHOOL.getType())
                || Objects.equals(deliveryType, DeliveryTypeEnum.EXPRESS.getType());
    }

    @AssertTrue(message = "快递配送必须选择家长地址或填写完整收件信息")
    @JsonIgnore
    public boolean isExpressAddressValid() {
        if (!Objects.equals(deliveryType, DeliveryTypeEnum.EXPRESS.getType())) {
            return true;
        }
        if (addressId != null) {
            return true;
        }
        return hasText(receiverName) && hasText(receiverMobile) && receiverAreaId != null && hasText(receiverDetailAddress);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Data
    @Schema(description = "管理后台 - 在线订刊下单商品项")
    @Valid
    public static class Item {

        @Schema(description = "订刊窗口 SKU 编号（offerSku）", requiredMode = Schema.RequiredMode.REQUIRED, example = "4096")
        @NotNull(message = "订刊窗口 SKU 编号不能为空")
        private Long offerSkuId;

        @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
        @NotNull(message = "商品 SKU 编号不能为空")
        private Long skuId;

        @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "购买数量不能为空")
        @Min(value = 1, message = "购买数量最小值为 {value}")
        private Integer count;

    }

}
