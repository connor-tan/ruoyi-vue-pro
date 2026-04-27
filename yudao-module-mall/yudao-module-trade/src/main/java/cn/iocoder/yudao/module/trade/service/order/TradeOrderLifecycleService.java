package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderCancelTypeEnum;

/**
 * 交易订单取消删除 Service 接口
 */
public interface TradeOrderLifecycleService {

    void cancelOrderByMember(Long userId, Long id);

    int cancelOrderBySystem();

    void cancelOrderBySystem(TradeOrderDO order);

    void cancelOrderForPaymentClose(TradeOrderDO order, TradeOrderCancelTypeEnum cancelType);

    void cancelOrderByAfterSale(TradeOrderDO order, Integer refundPrice);

    void deleteOrder(Long userId, Long id);

}
