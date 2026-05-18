package cn.iocoder.yudao.module.promotion.mq.consumer.diy;

import cn.iocoder.yudao.module.product.mq.message.spu.ProductSpuDeleteMessage;
import cn.iocoder.yudao.module.promotion.service.diy.DiyPropertyCleanService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 商品删除时，清理装修页商品引用的消费者，基于 {@link ProductSpuDeleteMessage} 消息
 *
 * @author Connor
 */
@Component
@Slf4j
public class DiyProductSpuDeleteConsumer {

    @Resource
    private DiyPropertyCleanService diyPropertyCleanService;

    @EventListener
    @Async // Spring Event 默认在 Producer 发送的线程，通过 @Async 实现异步
    public void onMessage(ProductSpuDeleteMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        int updateCount = diyPropertyCleanService.removeSpuIdFromAllPages(message.getSpuId());
        log.info("[onMessage][商品({})装修引用清理完成，更新页面数量({})]", message.getSpuId(), updateCount);
    }

}
