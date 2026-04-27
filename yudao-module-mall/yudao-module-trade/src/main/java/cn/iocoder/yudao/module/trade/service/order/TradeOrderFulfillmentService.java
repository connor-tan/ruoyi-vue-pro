package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderStationDeliveryReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;

/**
 * 交易订单履约发货 Service 接口
 */
public interface TradeOrderFulfillmentService {

    void deliveryOrder(TradeOrderDeliveryReqVO deliveryReqVO);

    void stationDeliveryOrder(TradeOrderStationDeliveryReqVO reqVO);

    void sendDeliveryOrderMessage(TradeOrderDO order, TradeOrderDeliveryReqVO deliveryReqVO);

}
