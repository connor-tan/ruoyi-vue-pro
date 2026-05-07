package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;

/**
 * 交易订单收货完成 Service 接口
 */
public interface TradeOrderReceiveService {

    void receiveOrderByMember(Long userId, Long id);

    void receiveDeliveryByMember(Long userId, Long deliveryId);

    int receiveOrderBySystem();

    void receiveOrderBySystem(TradeOrderDO order);

    void pickUpOrderByAdmin(Long userId, Long id);

    void pickUpOrderByAdmin(Long userId, String pickUpVerifyCode);

    TradeOrderDO getByPickUpVerifyCode(String pickUpVerifyCode);

    void pickUpOrder(Long userId, TradeOrderDO order, TradeOrderDeliveryDO pickUpDelivery);

}
