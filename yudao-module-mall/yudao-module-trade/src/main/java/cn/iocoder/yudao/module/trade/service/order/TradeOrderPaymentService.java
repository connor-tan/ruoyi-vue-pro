package cn.iocoder.yudao.module.trade.service.order;

/**
 * 交易订单支付同步 Service 接口
 */
public interface TradeOrderPaymentService {

    void updateOrderPaid(Long id, Long payOrderId);

    void syncOrderPayStatusQuietly(Long id, Long payOrderId);

    void cancelPaidOrder(Long userId, Long orderId, Integer cancelType);

    void updatePaidOrderRefunded(Long id, Long payRefundId);

}
