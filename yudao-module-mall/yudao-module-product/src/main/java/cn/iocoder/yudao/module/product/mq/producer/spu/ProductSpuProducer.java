package cn.iocoder.yudao.module.product.mq.producer.spu;

import cn.iocoder.yudao.module.product.mq.message.spu.ProductSpuDeleteMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 商品 SPU Producer
 *
 * @author Connor
 */
@Slf4j
@Component
public class ProductSpuProducer {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 发送 {@link ProductSpuDeleteMessage} 消息
     *
     * @param spuId 商品 SPU 编号
     */
    public void sendProductSpuDeleteMessage(Long spuId) {
        applicationContext.publishEvent(new ProductSpuDeleteMessage().setSpuId(spuId));
    }

}
