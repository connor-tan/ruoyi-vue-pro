package cn.iocoder.yudao.module.trade.service.message;

import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.AfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.service.message.bo.TradeOrderMessageWhenDeliveryOrderReqBO;

/**
 * Trade 消息 service 接口
 *
 * @author HUIHUI
 */
public interface TradeMessageService {

    /**
     * 订单发货时发送通知
     *
     * @param reqBO 发送消息
     */
    void sendMessageWhenDeliveryOrder(TradeOrderMessageWhenDeliveryOrderReqBO reqBO);

    /**
     * 用户提交订单成功时发送后台通知
     *
     * @param order 订单
     */
    void sendMessageWhenOrderCreated(TradeOrderDO order);

    /**
     * 用户退款成功时发送后台通知
     *
     * @param afterSale 售后单
     */
    void sendMessageWhenAfterSaleRefunded(AfterSaleDO afterSale);

}
