package cn.iocoder.yudao.module.trade.service.order.support;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_DELIVERY_NOT_FOUND;

/**
 * 交易订单配送组访问支撑类。
 */
@Component
public class TradeOrderDeliveryAccessSupport {

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;

    public TradeOrderDeliveryDO validateDeliveryExists(Long deliveryId) {
        TradeOrderDeliveryDO delivery = tradeOrderDeliveryMapper.selectById(deliveryId);
        if (delivery == null) {
            throw exception(ORDER_DELIVERY_NOT_FOUND);
        }
        return delivery;
    }

    public TradeOrderDO validateDeliveryOrderOwned(TradeOrderDeliveryDO delivery, Long userId, ErrorCode errorCode) {
        TradeOrderDO order = tradeOrderMapper.selectByIdAndUserId(delivery.getOrderId(), userId);
        if (order == null) {
            throw exception(errorCode);
        }
        return order;
    }

    public List<TradeOrderDeliveryDO> getDeliveryListByOrderId(Long orderId) {
        return tradeOrderDeliveryMapper.selectListByOrderId(orderId);
    }

    public TradeOrderDeliveryDO findDeliveryByType(List<TradeOrderDeliveryDO> deliveries, Integer deliveryType) {
        return CollUtil.emptyIfNull(deliveries).stream()
                .filter(delivery -> Objects.equals(delivery.getDeliveryType(), deliveryType))
                .findFirst()
                .orElse(null);
    }

}
