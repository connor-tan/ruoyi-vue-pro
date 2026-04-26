package cn.iocoder.yudao.module.trade.api.order;

import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 订刊订单只读 API 实现类
 *
 * @author xiaokanhui
 */
@Service
@Validated
public class TradeSubscriptionOrderApiImpl implements TradeSubscriptionOrderApi {

    @Resource
    private TradeOrderQueryService tradeOrderQueryService;

    @Override
    public Integer getEffectiveSubscriptionOrderItemQuantity(Long userId, Long studentId, Long offerSkuId) {
        return tradeOrderQueryService.getEffectiveSubscriptionOrderItemQuantity(userId, studentId, offerSkuId);
    }

}
