package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderPublicationIssueDO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TradeOrderPublicationIssueService {

    void createOrderIssues(TradeOrderDO order, List<cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO> orderItems,
                           List<TradePriceCalculateRespBO.OrderItem> calculateItems);

    List<TradeOrderPublicationIssueDO> getIssueListByOrderId(Long orderId);

    List<TradeOrderPublicationIssueDO> getIssueListByOrderIds(Collection<Long> orderIds);

    TradeOrderPublicationIssueDO getIssue(Long userId, Long id);

    void afterIssueDelivered(Collection<Long> orderIssueIds, LocalDateTime deliveryTime);

    void receiveIssueByMember(Long userId, Long orderIssueId);

    void receiveDeliveryIssues(Long userId, Long deliveryId);

    int receiveIssueBySystem();

    void cancelByOrderId(Long orderId);

    void cancelUnfinishedByOrderItemId(Long orderItemId);

    void refreshOrderItemPublicationIssueStats(Collection<Long> orderItemIds);

}
