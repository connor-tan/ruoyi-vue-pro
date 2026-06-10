package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderSummaryRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderPublicationIssueDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderPublicationIssueMapper;
import cn.iocoder.yudao.module.trade.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressClientFactory;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeSubscriptionOrderItemQuantityBO;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderDeliveryAccessSupport;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.EXPRESS_NOT_EXISTS;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_NOT_FOUND;

/**
 * 交易订单【读】 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class TradeOrderQueryServiceImpl implements TradeOrderQueryService {

    @Resource
    private ExpressClientFactory expressClientFactory;

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Resource
    private TradeOrderPublicationIssueMapper publicationIssueMapper;

    @Resource
    private DeliveryExpressService deliveryExpressService;

    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private TradeOrderDeliveryAccessSupport deliveryAccessSupport;

    // =================== Order ===================

    @Override
    public TradeOrderDO getOrder(Long id) {
        return tradeOrderMapper.selectById(id);
    }

    @Override
    public TradeOrderDO getOrder(Long userId, Long id) {
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order != null
                && ObjectUtil.notEqual(order.getUserId(), userId)) {
            return null;
        }
        return order;
    }

    @Override
    public TradeOrderDO getOrderByUserIdAndStatusAndCombination(Long userId, Long combinationActivityId, Integer status) {
        return tradeOrderMapper.selectByUserIdAndCombinationActivityIdAndStatus(userId, combinationActivityId, status);
    }

    @Override
    public List<TradeOrderDO> getOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return tradeOrderMapper.selectByIds(ids);
    }

    @Override
    public PageResult<TradeOrderDO> getOrderPage(TradeOrderPageReqVO reqVO) {
        // 根据用户查询条件构建用户编号列表
        Set<Long> userIds = buildQueryConditionUserIds(reqVO);
        if (userIds == null) { // 没查询到用户，说明肯定也没他的订单
            return PageResult.empty();
        }
        Set<Long> deliveryOrderIds = buildQueryConditionDeliveryOrderIds(reqVO);
        if (deliveryOrderIds != null && deliveryOrderIds.isEmpty()) {
            return PageResult.empty();
        }

        // 分页查询
        return tradeOrderMapper.selectPage(reqVO, userIds, deliveryOrderIds);
    }

    private Set<Long> buildQueryConditionUserIds(TradeOrderPageReqVO reqVO) {
        // 获得 userId 相关的查询
        Set<Long> userIds = new HashSet<>();
        if (StrUtil.isNotEmpty(reqVO.getUserMobile())) {
            MemberUserRespDTO user = memberUserApi.getUserByMobile(reqVO.getUserMobile());
            if (user == null) { // 没查询到用户，说明肯定也没他的订单
                return null;
            }
            userIds.add(user.getId());
        }
        if (StrUtil.isNotEmpty(reqVO.getUserNickname())) {
            List<MemberUserRespDTO> users = memberUserApi.getUserListByNickname(reqVO.getUserNickname());
            if (CollUtil.isEmpty(users)) { // 没查询到用户，说明肯定也没他的订单
                return null;
            }
            userIds.addAll(convertSet(users, MemberUserRespDTO::getId));
        }
        return userIds;
    }

    private Set<Long> buildQueryConditionDeliveryOrderIds(TradeOrderPageReqVO reqVO) {
        boolean filterByDeliveryType = reqVO.getDeliveryType() != null
                && !Objects.equals(reqVO.getDeliveryType(), DeliveryTypeEnum.MIXED.getType());
        boolean filterByPickUp = CollUtil.isNotEmpty(reqVO.getPickUpStoreIds())
                || StrUtil.isNotEmpty(reqVO.getPickUpVerifyCode());
        if (!filterByDeliveryType && reqVO.getLogisticsId() == null && !filterByPickUp) {
            return null;
        }
        List<TradeOrderDeliveryDO> deliveries = tradeOrderDeliveryMapper.selectListByAdminFilter(
                filterByDeliveryType ? reqVO.getDeliveryType() : null, reqVO.getLogisticsId(),
                reqVO.getPickUpStoreIds(), reqVO.getPickUpVerifyCode());
        if (CollUtil.isEmpty(deliveries)) {
            return Collections.emptySet();
        }
        return convertSet(deliveries, TradeOrderDeliveryDO::getOrderId);
    }

    @Override
    public TradeOrderSummaryRespVO getOrderSummary(TradeOrderPageReqVO reqVO) {
        // 根据用户查询条件构建用户编号列表
        Set<Long> userIds = buildQueryConditionUserIds(reqVO);
        if (userIds == null) { // 没查询到用户，说明肯定也没他的订单
            return new TradeOrderSummaryRespVO();
        }
        Set<Long> deliveryOrderIds = buildQueryConditionDeliveryOrderIds(reqVO);
        if (deliveryOrderIds != null && deliveryOrderIds.isEmpty()) {
            return new TradeOrderSummaryRespVO();
        }
        // 查询每个售后状态对应的数量、金额
        List<Map<String, Object>> list = tradeOrderMapper.selectOrderSummaryGroupByRefundStatus(reqVO, userIds,
                deliveryOrderIds);

        TradeOrderSummaryRespVO vo = new TradeOrderSummaryRespVO().setAfterSaleCount(0L).setAfterSalePrice(0L);
        for (Map<String, Object> map : list) {
            Long count = MapUtil.getLong(map, "count", 0L);
            Long price = MapUtil.getLong(map, "price", 0L);
            // 未退款的计入订单，部分退款、全部退款计入售后
            if (TradeOrderRefundStatusEnum.NONE.getStatus().equals(MapUtil.getInt(map, "refundStatus"))) {
                vo.setOrderCount(count).setOrderPayPrice(price);
            } else {
                vo.setAfterSaleCount(vo.getAfterSaleCount() + count).setAfterSalePrice(vo.getAfterSalePrice() + price);
            }
        }
        return vo;
    }

    @Override
    public PageResult<TradeOrderDO> getOrderPage(Long userId, AppTradeOrderPageReqVO reqVO) {
        return tradeOrderMapper.selectPage(reqVO, userId);
    }

    @Override
    public Long getOrderCount(Long userId, Integer status, Boolean commentStatus) {
        return tradeOrderMapper.selectCountByUserIdAndStatus(userId, status, commentStatus);
    }

    @Override
    public List<ExpressTrackRespDTO> getExpressTrackList(Long id, Long userId) {
        // 查询订单
        TradeOrderDO order = tradeOrderMapper.selectByIdAndUserId(id, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 查询物流
        return getExpressTrackList(order);
    }

    @Override
    public List<ExpressTrackRespDTO> getDeliveryExpressTrackList(Long deliveryId, Long userId) {
        TradeOrderDeliveryDO delivery = deliveryAccessSupport.validateDeliveryExists(deliveryId);
        deliveryAccessSupport.validateDeliveryOrderOwned(delivery, userId, ORDER_NOT_FOUND);
        return getExpressTrackList(delivery);
    }

    @Override
    public List<ExpressTrackRespDTO> getPublicationIssueExpressTrackList(Long orderIssueId, Long userId) {
        TradeOrderPublicationIssueDO issue = publicationIssueMapper.selectByIdAndUserId(orderIssueId, userId);
        if (issue == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (issue.getLogisticsId() == null || StrUtil.isBlank(issue.getLogisticsNo())) {
            return Collections.emptyList();
        }
        TradeOrderDO order = tradeOrderMapper.selectById(issue.getOrderId());
        DeliveryExpressDO express = deliveryExpressService.getDeliveryExpress(issue.getLogisticsId());
        if (express == null) {
            throw exception(EXPRESS_NOT_EXISTS);
        }
        return getSelf().getExpressTrackList(express.getCode(), issue.getLogisticsNo(),
                order == null ? null : order.getReceiverMobile());
    }

    @Override
    public List<ExpressTrackRespDTO> getExpressTrackList(Long id) {
        // 查询订单
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 查询物流
        return getExpressTrackList(order);
    }

    @Override
    public int getActivityProductCount(Long userId, Long activityId, TradeOrderTypeEnum type) {
        // 获得订单列表
        List<TradeOrderDO> orders = tradeOrderMapper.selectListByUserIdAndActivityId(userId, activityId, type);
        orders.removeIf(order -> TradeOrderStatusEnum.isCanceled(order.getStatus())); // 过滤掉【已取消】的订单
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }
        // 获得订单项列表
        return tradeOrderItemMapper.selectProductSumByOrderId(convertSet(orders, TradeOrderDO::getId));
    }

    /**
     * 获得订单的物流轨迹
     *
     * @param order 订单
     * @return 物流轨迹
     */
    private List<ExpressTrackRespDTO> getExpressTrackList(TradeOrderDO order) {
        if (order.getLogisticsId() == null) {
            return Collections.emptyList();
        }
        // 查询物流公司
        DeliveryExpressDO express = deliveryExpressService.getDeliveryExpress(order.getLogisticsId());
        if (express == null) {
            throw exception(EXPRESS_NOT_EXISTS);
        }
        // 查询物流轨迹
        return getSelf().getExpressTrackList(express.getCode(), order.getLogisticsNo(), order.getReceiverMobile());
    }

    /**
     * 获得订单配送组的物流轨迹
     *
     * @param delivery 配送组
     * @return 物流轨迹
     */
    private List<ExpressTrackRespDTO> getExpressTrackList(TradeOrderDeliveryDO delivery) {
        if (delivery.getLogisticsId() == null || StrUtil.isBlank(delivery.getLogisticsNo())) {
            return Collections.emptyList();
        }
        DeliveryExpressDO express = deliveryExpressService.getDeliveryExpress(delivery.getLogisticsId());
        if (express == null) {
            throw exception(EXPRESS_NOT_EXISTS);
        }
        return getSelf().getExpressTrackList(express.getCode(), delivery.getLogisticsNo(), delivery.getReceiverMobile());
    }

    /**
     * 查询物流轨迹
     * <p>
     * 缓存的目的：考虑及时性要求不高，但是每次调用需要钱
     *
     * @param code           快递公司编码
     * @param logisticsNo    发货快递单号
     * @param receiverMobile 收、寄件人的电话号码
     * @return 物流轨迹
     */
    @Cacheable(cacheNames = RedisKeyConstants.EXPRESS_TRACK, key = "#code + '-' + #logisticsNo + '-' + #receiverMobile",
            unless = "#result == null")
    public List<ExpressTrackRespDTO> getExpressTrackList(String code, String logisticsNo, String receiverMobile) {
        return expressClientFactory.getDefaultExpressClient().getExpressTrackList(new ExpressTrackQueryReqDTO()
                .setExpressCode(code).setLogisticsNo(logisticsNo).setPhone(receiverMobile));
    }

    // =================== Order Item ===================

    @Override
    public TradeOrderItemDO getOrderItem(Long userId, Long itemId) {
        TradeOrderItemDO orderItem = tradeOrderItemMapper.selectById(itemId);
        if (orderItem != null
                && ObjectUtil.notEqual(orderItem.getUserId(), userId)) {
            return null;
        }
        return orderItem;
    }

    @Override
    public TradeOrderItemDO getOrderItem(Long id) {
        return tradeOrderItemMapper.selectById(id);
    }

    @Override
    public List<TradeOrderItemDO> getOrderItemListByOrderId(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return tradeOrderItemMapper.selectListByOrderId(orderIds);
    }

    @Override
    public List<TradeOrderDeliveryDO> getOrderDeliveryListByOrderId(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return tradeOrderDeliveryMapper.selectListByOrderId(orderIds);
    }

    @Override
    public Integer getEffectiveSubscriptionOrderItemQuantity(Long studentId, Long offerSkuId) {
        return tradeOrderItemMapper.selectEffectiveSubscriptionOrderItemQuantity(studentId, offerSkuId,
                TradeOrderStatusEnum.CANCELED.getStatus());
    }

    @Override
    public Map<Long, Integer> getEffectiveSubscriptionOrderItemQuantityMap(Long studentId, Collection<Long> offerSkuIds) {
        if (studentId == null || CollUtil.isEmpty(offerSkuIds)) {
            return Collections.emptyMap();
        }
        List<TradeSubscriptionOrderItemQuantityBO> quantities =
                tradeOrderItemMapper.selectEffectiveSubscriptionOrderItemQuantityList(studentId, offerSkuIds,
                        TradeOrderStatusEnum.CANCELED.getStatus());
        Map<Long, Integer> quantityMap = new HashMap<>(offerSkuIds.size());
        for (TradeSubscriptionOrderItemQuantityBO quantity : quantities) {
            quantityMap.put(quantity.getOfferSkuId(), quantity.getQuantity());
        }
        return quantityMap;
    }

    @Override
    public boolean hasPublicationOrderReferenceByProductSpuId(Long productSpuId) {
        if (productSpuId == null) {
            return false;
        }
        Long count = tradeOrderItemMapper.selectPublicationOrderReferenceCountBySpuId(productSpuId);
        return count != null && count > 0;
    }

    @Override
    public Set<Long> getPublicationOrderReferencedProductSkuIds(Collection<Long> productSkuIds) {
        if (CollUtil.isEmpty(productSkuIds)) {
            return Collections.emptySet();
        }
        return tradeOrderItemMapper.selectPublicationOrderReferencedSkuIds(productSkuIds);
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private TradeOrderQueryServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
