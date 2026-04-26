package cn.iocoder.yudao.module.trade.controller.app.cart.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 App - 购物车添加购物项 Request VO")
@Data
public class AppCartAddReqVO {

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED,example = "1024")
    @NotNull(message = "商品 SKU 编号不能为空")
    private Long skuId;

    @Schema(description = "新增商品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "商品数量必须大于等于 1")
    private Integer count;

    @Schema(description = "订刊学生编号", example = "2048")
    private Long studentId;

    @Deprecated
    @Schema(description = "订刊窗口 SKU 编号（历史兼容字段，新链路使用 offerSkuId）", example = "4096")
    private Long windowSkuId;

    @Schema(description = "订刊窗口 SKU 编号（offerSku）", example = "4096")
    private Long offerSkuId;

}
