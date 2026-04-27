package cn.iocoder.yudao.module.trade.service.order.support;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class TradeOrderStatusAggregateSupport {

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Resource
    private TradeOrderDeliveryAccessSupport deliveryAccessSupport;

    public TradeOrderDO refreshOrderStatusByDeliveries(TradeOrderDO order) {
        List<TradeOrderDeliveryDO> deliveries = tradeOrderDeliveryMapper.selectListByOrderId(order.getId());
        if (CollUtil.isEmpty(deliveries)) {
            return order;
        }
        Integer orderStatus = calculateAggregateOrderStatus(deliveries);
        TradeOrderDeliveryDO expressDelivery = deliveryAccessSupport.findDeliveryByType(
                deliveries, DeliveryTypeEnum.EXPRESS.getType());
        LocalDateTime deliveryTime = deliveries.stream().map(TradeOrderDeliveryDO::getDeliveryTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime receiveTime = deliveries.stream().map(TradeOrderDeliveryDO::getReceiveTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        TradeOrderDO update = new TradeOrderDO().setId(order.getId()).setStatus(orderStatus).setDeliveryTime(deliveryTime);
        if (TradeOrderStatusEnum.isCompleted(orderStatus)) {
            update.setReceiveTime(receiveTime);
        }
        if (expressDelivery != null) {
            update.setLogisticsId(expressDelivery.getLogisticsId()).setLogisticsNo(expressDelivery.getLogisticsNo())
                    .setReceiverName(expressDelivery.getReceiverName()).setReceiverMobile(expressDelivery.getReceiverMobile())
                    .setReceiverAreaId(expressDelivery.getReceiverAreaId())
                    .setReceiverDetailAddress(expressDelivery.getReceiverDetailAddress());
        }
        tradeOrderMapper.updateById(update);
        return tradeOrderMapper.selectById(order.getId());
    }

    private Integer calculateAggregateOrderStatus(List<TradeOrderDeliveryDO> deliveries) {
        if (deliveries.stream().allMatch(delivery -> TradeOrderStatusEnum.isUnpaid(delivery.getStatus()))) {
            return TradeOrderStatusEnum.UNPAID.getStatus();
        }
        if (deliveries.stream().allMatch(delivery -> TradeOrderStatusEnum.isCanceled(delivery.getStatus()))) {
            return TradeOrderStatusEnum.CANCELED.getStatus();
        }
        if (deliveries.stream().allMatch(delivery -> TradeOrderStatusEnum.isCompleted(delivery.getStatus()))) {
            return TradeOrderStatusEnum.COMPLETED.getStatus();
        }
        if (deliveries.stream().allMatch(delivery -> TradeOrderStatusEnum.isDelivered(delivery.getStatus())
                || TradeOrderStatusEnum.isCompleted(delivery.getStatus()))) {
            return TradeOrderStatusEnum.DELIVERED.getStatus();
        }
        return TradeOrderStatusEnum.UNDELIVERED.getStatus();
    }

}
