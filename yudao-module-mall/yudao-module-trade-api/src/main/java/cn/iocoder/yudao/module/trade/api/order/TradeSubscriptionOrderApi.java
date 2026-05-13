package cn.iocoder.yudao.module.trade.api.order;

/**
 * 订刊订单只读 API 接口
 *
 * @author xiaokanhui
 */
public interface TradeSubscriptionOrderApi {

    /**
     * 按学生和订刊窗口 SKU 统计有效订单中已购买的数量。
     *
     * @param studentId 学生编号
     * @param offerSkuId 订刊窗口 SKU 编号
     * @return 已购买数量
     */
    Integer getEffectiveSubscriptionOrderItemQuantity(Long studentId, Long offerSkuId);

}
