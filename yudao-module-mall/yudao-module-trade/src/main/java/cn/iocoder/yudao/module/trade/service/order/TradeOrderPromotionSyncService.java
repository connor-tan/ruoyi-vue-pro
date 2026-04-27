package cn.iocoder.yudao.module.trade.service.order;

import java.util.List;

/**
 * 交易订单营销同步 Service 接口
 */
public interface TradeOrderPromotionSyncService {

    void updateOrderCombinationInfo(Long orderId, Long activityId, Long combinationRecordId, Long headId);

    void updateOrderGiveCouponIds(Long userId, Long orderId, List<Long> giveCouponIds);

}
