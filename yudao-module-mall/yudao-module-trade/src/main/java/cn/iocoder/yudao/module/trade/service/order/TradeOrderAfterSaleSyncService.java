package cn.iocoder.yudao.module.trade.service.order;

import jakarta.validation.constraints.NotNull;

/**
 * 交易订单售后同步 Service 接口
 */
public interface TradeOrderAfterSaleSyncService {

    void updateOrderItemWhenAfterSaleCreate(@NotNull Long id, @NotNull Long afterSaleId);

    void updateOrderItemWhenAfterSaleSuccess(@NotNull Long id, @NotNull Integer refundPrice);

    void updateOrderItemWhenAfterSaleCancel(@NotNull Long id);

}
