package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemCommentCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;

/**
 * 交易订单评价 Service 接口
 */
public interface TradeOrderCommentService {

    Long createOrderItemCommentByMember(Long userId, AppTradeOrderItemCommentCreateReqVO createReqVO);

    int createOrderItemCommentBySystem();

    void createOrderItemCommentBySystemBySystem(TradeOrderDO order);

}
