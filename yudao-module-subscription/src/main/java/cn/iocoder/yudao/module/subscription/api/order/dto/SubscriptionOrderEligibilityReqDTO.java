package cn.iocoder.yudao.module.subscription.api.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionOrderEligibilityReqDTO {

    private Long userId;

    /**
     * 是否后台人工下单。
     *
     * 后台人工下单不要求学生绑定当前家长，但仍然复用订刊窗口、规则、SKU 和限购校验。
     */
    private Boolean admin;

    @NotNull(message = "学生编号不能为空")
    private Long studentId;

    @NotNull(message = "窗口 SKU 编号不能为空")
    private Long offerSkuId;

    @NotNull(message = "商品 SKU 编号不能为空")
    private Long skuId;

    @NotNull(message = "订购数量不能为空")
    @Min(value = 1, message = "订购数量必须大于 0")
    private Integer count;

}
