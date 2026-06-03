package cn.iocoder.yudao.module.product.api.sku.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品 SKU 更新销量 Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuUpdateSalesCountReqDTO {

    /**
     * 商品 SKU
     */
    @NotNull(message = "商品 SKU 不能为空")
    private List<Item> items;

    @Data
    public static class Item {

        /**
         * 商品 SKU 编号
         */
        @NotNull(message = "商品 SKU 编号不能为空")
        private Long id;

        /**
         * 销量变化数量
         *
         * 正数：增加销量
         * 负数：减少销量
         */
        @NotNull(message = "销量变化数量不能为空")
        private Integer incrCount;

    }

}
