package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderRemarkReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderUpdateAddressReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderUpdatePriceReqVO;

/**
 * 交易订单后台调整 Service 接口
 */
public interface TradeOrderAdminAdjustService {

    void updateOrderRemark(TradeOrderRemarkReqVO reqVO);

    void updateOrderPrice(TradeOrderUpdatePriceReqVO reqVO);

    void updateOrderAddress(TradeOrderUpdateAddressReqVO reqVO);

}
