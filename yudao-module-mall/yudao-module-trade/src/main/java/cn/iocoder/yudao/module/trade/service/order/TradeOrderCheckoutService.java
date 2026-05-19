package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementRespVO;
import cn.iocoder.yudao.module.member.api.address.dto.MemberAddressRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderAdminOnlineCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderAdminOnlineSettlementReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;

import java.util.List;

/**
 * 交易订单结算下单 Service 接口
 */
public interface TradeOrderCheckoutService {

    AppTradeOrderSettlementRespVO settlementOrder(Long userId, AppTradeOrderSettlementReqVO settlementReqVO);

    TradeOrderDO createOrder(Long userId, AppTradeOrderCreateReqVO createReqVO);

    List<MemberAddressRespDTO> getAdminOnlineAddressList(Long studentId);

    AppTradeOrderSettlementRespVO settlementAdminOnlineOrder(TradeOrderAdminOnlineSettlementReqVO settlementReqVO);

    TradeOrderDO createAdminOnlineOrder(TradeOrderAdminOnlineCreateReqVO createReqVO);

}
