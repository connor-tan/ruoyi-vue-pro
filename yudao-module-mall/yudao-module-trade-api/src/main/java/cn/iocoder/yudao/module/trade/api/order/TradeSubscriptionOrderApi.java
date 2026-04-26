package cn.iocoder.yudao.module.trade.api.order;

/**
 * 订刊订单只读 API 接口
 *
 * @author xiaokanhui
 */
public interface TradeSubscriptionOrderApi {

    /**
     * 统计用户某学生在有效订单中已购买的订刊窗口 SKU 数量。
     *
     * @param userId 用户编号
     * @param studentId 学生编号
     * @param offerSkuId 订刊窗口 SKU 编号
     * @return 已购买数量
     */
    Integer getEffectiveSubscriptionOrderItemQuantity(Long userId, Long studentId, Long offerSkuId);

}
