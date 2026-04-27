package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_NOT_FOUND;

/**
 * 交易订单营销同步 Service 实现类
 */
@Service
public class TradeOrderPromotionSyncServiceImpl implements TradeOrderPromotionSyncService {

    @Resource
    private TradeOrderMapper tradeOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderCombinationInfo(Long orderId, Long activityId, Long combinationRecordId, Long headId) {
        tradeOrderMapper.updateById(new TradeOrderDO().setId(orderId).setCombinationActivityId(activityId)
                .setCombinationRecordId(combinationRecordId).setCombinationHeadId(headId));
    }

    @Override
    public void updateOrderGiveCouponIds(Long userId, Long orderId, List<Long> giveCouponIds) {
        TradeOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(orderId, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        tradeOrderMapper.updateById(new TradeOrderDO().setId(orderId).setGiveCouponIds(giveCouponIds));
    }

}
