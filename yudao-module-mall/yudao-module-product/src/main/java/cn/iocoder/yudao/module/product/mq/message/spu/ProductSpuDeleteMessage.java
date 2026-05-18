package cn.iocoder.yudao.module.product.mq.message.spu;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品 SPU 删除消息
 *
 * @author Connor
 */
@Data
public class ProductSpuDeleteMessage {

    /**
     * 商品 SPU 编号
     */
    @NotNull(message = "商品 SPU 编号不能为空")
    private Long spuId;

}
