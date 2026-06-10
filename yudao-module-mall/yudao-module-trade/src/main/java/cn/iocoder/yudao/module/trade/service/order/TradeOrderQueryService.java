package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderSummaryRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singleton;

/**
 * 交易订单【读】 Service 接口
 *
 * @author 芋道源码
 */
public interface TradeOrderQueryService {

    // =================== Order ===================

    /**
     * 获得指定编号的交易订单
     *
     * @param id 交易订单编号
     * @return 交易订单
     */
    TradeOrderDO getOrder(Long id);

    /**
     * 获得指定用户，指定的交易订单
     *
     * @param userId 用户编号
     * @param id     交易订单编号
     * @return 交易订单
     */
    TradeOrderDO getOrder(Long userId, Long id);

    /**
     * 获得指定用户，指定活动，指定状态的交易订单
     *
     * @param userId                用户编号
     * @param combinationActivityId 活动编号
     * @param status                订单状态
     * @return 交易订单
     */
    TradeOrderDO getOrderByUserIdAndStatusAndCombination(Long userId, Long combinationActivityId, Integer status);

    /**
     * 获得订单列表
     *
     * @param ids 订单编号数组
     * @return 订单列表
     */
    List<TradeOrderDO> getOrderList(Collection<Long> ids);

    /**
     * 【管理员】获得交易订单分页
     *
     * @param reqVO 分页请求
     * @return 交易订单
     */
    PageResult<TradeOrderDO> getOrderPage(TradeOrderPageReqVO reqVO);

    /**
     * 获得订单统计
     *
     * @param reqVO 请求参数
     * @return 订单统计
     */
    TradeOrderSummaryRespVO getOrderSummary(TradeOrderPageReqVO reqVO);

    /**
     * 【会员】获得交易订单分页
     *
     * @param userId 用户编号
     * @param reqVO  分页请求
     * @return 交易订单
     */
    PageResult<TradeOrderDO> getOrderPage(Long userId, AppTradeOrderPageReqVO reqVO);

    /**
     * 【会员】获得交易订单数量
     *
     * @param userId       用户编号
     * @param status       订单状态。如果为空，则不进行筛选
     * @param commonStatus 评价状态。如果为空，则不进行筛选
     * @return 订单数量
     */
    Long getOrderCount(Long userId, Integer status, Boolean commonStatus);

    /**
     * 【前台】获得订单的物流轨迹
     *
     * @param id     订单编号
     * @param userId 用户编号
     * @return 物流轨迹数组
     */
    List<ExpressTrackRespDTO> getExpressTrackList(Long id, Long userId);

    /**
     * 【前台】获得订单配送组的物流轨迹
     *
     * @param deliveryId 配送组编号
     * @param userId 用户编号
     * @return 物流轨迹数组
     */
    List<ExpressTrackRespDTO> getDeliveryExpressTrackList(Long deliveryId, Long userId);

    /**
     * 【前台】获得刊物订单期次物流轨迹
     *
     * @param orderIssueId 订单期次编号
     * @param userId 用户编号
     * @return 物流轨迹数组
     */
    List<ExpressTrackRespDTO> getPublicationIssueExpressTrackList(Long orderIssueId, Long userId);

    /**
     * 【后台】获得订单的物流轨迹
     *
     * @param id 订单编号
     * @return 物流轨迹数组
     */
    List<ExpressTrackRespDTO> getExpressTrackList(Long id);

    /**
     * 【会员】在指定活动下，用户购买的商品数量
     *
     * @param userId     用户编号
     * @param activityId 活动编号
     * @param type       订单类型
     * @return 活动商品数量
     */
    int getActivityProductCount(Long userId, Long activityId, TradeOrderTypeEnum type);

    // =================== Order Item ===================

    /**
     * 获得指定用户，指定的交易订单项
     *
     * @param userId 用户编号
     * @param itemId 交易订单项编号
     * @return 交易订单项
     */
    TradeOrderItemDO getOrderItem(Long userId, Long itemId);

    /**
     * 获得交易订单项
     *
     * @param id 交易订单项编号 itemId
     * @return 交易订单项
     */
    TradeOrderItemDO getOrderItem(Long id);

    /**
     * 根据交易订单编号，查询交易订单项
     *
     * @param orderId 交易订单编号
     * @return 交易订单项数组
     */
    default List<TradeOrderItemDO> getOrderItemListByOrderId(Long orderId) {
        return getOrderItemListByOrderId(singleton(orderId));
    }

    /**
     * 根据交易订单编号数组，查询交易订单项
     *
     * @param orderIds 交易订单编号数组
     * @return 交易订单项数组
     */
    List<TradeOrderItemDO> getOrderItemListByOrderId(Collection<Long> orderIds);

    /**
     * 根据交易订单编号，查询配送组
     *
     * @param orderId 交易订单编号
     * @return 配送组数组
     */
    default List<TradeOrderDeliveryDO> getOrderDeliveryListByOrderId(Long orderId) {
        return getOrderDeliveryListByOrderId(singleton(orderId));
    }

    /**
     * 根据交易订单编号数组，查询配送组
     *
     * @param orderIds 交易订单编号数组
     * @return 配送组数组
     */
    List<TradeOrderDeliveryDO> getOrderDeliveryListByOrderId(Collection<Long> orderIds);

    /**
     * 按学生和订刊窗口 SKU 统计有效订单中已购买的数量。
     *
     * @param studentId 学生编号
     * @param offerSkuId 窗口 SKU 编号
     * @return 已购买数量
     */
    Integer getEffectiveSubscriptionOrderItemQuantity(Long studentId, Long offerSkuId);

    /**
     * 按学生和订刊窗口 SKU 批量统计有效订单中已购买的数量。
     *
     * @param studentId 学生编号
     * @param offerSkuIds 窗口 SKU 编号集合
     * @return key 为窗口 SKU 编号，value 为已购买数量
     */
    Map<Long, Integer> getEffectiveSubscriptionOrderItemQuantityMap(Long studentId, Collection<Long> offerSkuIds);

    /**
     * 判断刊物 SPU 是否已经被订单事实引用。
     *
     * @param productSpuId 商品 SPU 编号
     * @return 是否存在订单引用
     */
    boolean hasPublicationOrderReferenceByProductSpuId(Long productSpuId);

    /**
     * 从候选 SKU 中筛选已经被刊物订单事实引用的 SKU 编号。
     *
     * @param productSkuIds 商品 SKU 编号集合
     * @return 已被订单引用的 SKU 编号集合
     */
    Set<Long> getPublicationOrderReferencedProductSkuIds(Collection<Long> productSkuIds);

}
