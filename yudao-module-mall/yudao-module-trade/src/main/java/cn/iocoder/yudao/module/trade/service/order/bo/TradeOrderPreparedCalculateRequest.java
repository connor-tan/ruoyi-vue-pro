package cn.iocoder.yudao.module.trade.service.order.bo;

import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;

import java.util.List;

public record TradeOrderPreparedCalculateRequest(TradePriceCalculateReqBO baseReqBO,
                                                 List<TradeOrderDeliveryGroupDraft> groupDrafts,
                                                 boolean publicationPresent) {
}
