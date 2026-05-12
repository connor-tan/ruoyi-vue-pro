package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderPublicationIssueDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderPublicationIssueMapper;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryStatusEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationFulfillmentStatusEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationReceiveStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderStatusAggregateSupport;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.minusTime;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_ISSUE_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_ISSUE_RECEIVE_FAIL_STATUS;

@Service
@Slf4j
public class TradeOrderPublicationIssueServiceImpl implements TradeOrderPublicationIssueService {

    @Resource
    private TradeOrderPublicationIssueMapper publicationIssueMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Resource
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderStatusAggregateSupport statusAggregateSupport;
    @Resource
    private TradeOrderProperties tradeOrderProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrderIssues(TradeOrderDO order, List<TradeOrderItemDO> orderItems,
                                  List<TradePriceCalculateRespBO.OrderItem> calculateItems) {
        if (CollUtil.isEmpty(orderItems) || CollUtil.isEmpty(calculateItems)) {
            return;
        }
        List<TradeOrderPublicationIssueDO> issues = new ArrayList<>();
        for (int i = 0; i < orderItems.size(); i++) {
            TradeOrderItemDO orderItem = orderItems.get(i);
            TradePriceCalculateRespBO.OrderItem calculateItem = calculateItems.get(i);
            if (!BizSceneEnum.isPublication(calculateItem.getBizScene())
                    || CollUtil.isEmpty(calculateItem.getPublicationIssues())) {
                continue;
            }
            for (TradePriceCalculateRespBO.PublicationIssueSnapshot issueSnapshot : calculateItem.getPublicationIssues()) {
                issues.add(new TradeOrderPublicationIssueDO()
                        .setOrderId(order.getId())
                        .setOrderNo(order.getNo())
                        .setOrderItemId(orderItem.getId())
                        .setDeliveryId(orderItem.getDeliveryId())
                        .setUserId(order.getUserId())
                        .setDeliveryType(calculateItem.getResolvedDeliveryType())
                        .setSpuId(orderItem.getSpuId())
                        .setSkuId(orderItem.getSkuId())
                        .setProductNameSnapshot(orderItem.getSpuName())
                        .setCount(orderItem.getCount())
                        .setStudentId(orderItem.getSubscriptionStudentId())
                        .setStudentNameSnapshot(orderItem.getSubscriptionStudentNameSnapshot())
                        .setSchoolId(orderItem.getSubscriptionSchoolId())
                        .setSchoolNameSnapshot(orderItem.getSubscriptionSchoolNameSnapshot())
                        .setClassId(orderItem.getSubscriptionClassId())
                        .setClassNameSnapshot(orderItem.getSubscriptionClassNameSnapshot())
                        .setStationId(calculateItem.getSubscriptionStationId())
                        .setStationNameSnapshot(calculateItem.getSubscriptionStationNameSnapshot())
                        .setWindowId(orderItem.getSubscriptionWindowId())
                        .setWindowNameSnapshot(orderItem.getSubscriptionWindowNameSnapshot())
                        .setTargetPeriod(orderItem.getSubscriptionTargetPeriod())
                        .setOfferId(orderItem.getSubscriptionOfferId())
                        .setOfferSkuId(orderItem.getSubscriptionOfferSkuId())
                        .setIssueId(issueSnapshot.getIssueId())
                        .setIssueNo(issueSnapshot.getIssueNo())
                        .setIssueName(issueSnapshot.getIssueName())
                        .setPlannedPublishDate(issueSnapshot.getPlannedPublishDate())
                        .setPlannedDeliveryDate(issueSnapshot.getPlannedDeliveryDate())
                        .setDeliveryStatus(PublicationDeliveryStatusEnum.UNDELIVERED.getStatus())
                        .setReceiveStatus(PublicationReceiveStatusEnum.UNRECEIVED.getStatus())
                        .setCanceled(false));
            }
        }
        if (CollUtil.isEmpty(issues)) {
            return;
        }
        publicationIssueMapper.insertBatch(issues);
        refreshOrderItemPublicationIssueStats(convertSet(orderItems, TradeOrderItemDO::getId));
    }

    @Override
    public List<TradeOrderPublicationIssueDO> getIssueListByOrderId(Long orderId) {
        return publicationIssueMapper.selectListByOrderId(orderId);
    }

    @Override
    public List<TradeOrderPublicationIssueDO> getIssueListByOrderIds(Collection<Long> orderIds) {
        return publicationIssueMapper.selectListByOrderIds(orderIds);
    }

    @Override
    public TradeOrderPublicationIssueDO getIssue(Long userId, Long id) {
        return publicationIssueMapper.selectByIdAndUserId(id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void afterIssueDelivered(Collection<Long> orderIssueIds, LocalDateTime deliveryTime) {
        if (CollUtil.isEmpty(orderIssueIds)) {
            return;
        }
        List<TradeOrderPublicationIssueDO> issues = publicationIssueMapper.selectByIds(orderIssueIds);
        refreshOrderItemPublicationIssueStats(convertSet(issues, TradeOrderPublicationIssueDO::getOrderItemId));
        refreshDeliveredDeliveries(convertSet(issues, TradeOrderPublicationIssueDO::getDeliveryId), deliveryTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveIssueByMember(Long userId, Long orderIssueId) {
        TradeOrderPublicationIssueDO issue = publicationIssueMapper.selectByIdAndUserId(orderIssueId, userId);
        if (issue == null) {
            throw exception(PUBLICATION_ISSUE_NOT_FOUND);
        }
        if (Boolean.TRUE.equals(issue.getCanceled())
                || !Objects.equals(issue.getDeliveryStatus(), PublicationDeliveryStatusEnum.DELIVERED.getStatus())
                || !Objects.equals(issue.getReceiveStatus(), PublicationReceiveStatusEnum.UNRECEIVED.getStatus())) {
            throw exception(PUBLICATION_ISSUE_RECEIVE_FAIL_STATUS);
        }
        receiveIssues(List.of(issue), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveDeliveryIssues(Long userId, Long deliveryId) {
        List<TradeOrderPublicationIssueDO> issues = publicationIssueMapper.selectDeliveredUnreceivedListByDeliveryId(
                deliveryId, PublicationDeliveryStatusEnum.DELIVERED.getStatus(),
                PublicationReceiveStatusEnum.UNRECEIVED.getStatus());
        if (CollUtil.isEmpty(issues)) {
            return;
        }
        if (issues.stream().anyMatch(issue -> ObjectUtil.notEqual(issue.getUserId(), userId))) {
            throw exception(PUBLICATION_ISSUE_NOT_FOUND);
        }
        receiveIssues(issues, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int receiveIssueBySystem() {
        LocalDateTime expireTime = minusTime(tradeOrderProperties.getReceiveExpireTime());
        List<TradeOrderPublicationIssueDO> issues = publicationIssueMapper.selectAutoReceiveList(
                PublicationDeliveryStatusEnum.DELIVERED.getStatus(),
                PublicationReceiveStatusEnum.UNRECEIVED.getStatus(), expireTime);
        if (CollUtil.isEmpty(issues)) {
            return 0;
        }
        try {
            receiveIssues(issues, null);
        } catch (Throwable ex) {
            log.error("[receiveIssueBySystem][刊物期次自动收货异常]", ex);
            throw ex;
        }
        return issues.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelByOrderId(Long orderId) {
        publicationIssueMapper.cancelByOrderId(orderId);
        refreshOrderItemPublicationIssueStats(convertSet(tradeOrderItemMapper.selectListByOrderId(orderId),
                TradeOrderItemDO::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUnfinishedByOrderItemId(Long orderItemId) {
        List<TradeOrderPublicationIssueDO> issues = publicationIssueMapper.selectListByOrderItemIds(List.of(orderItemId));
        if (CollUtil.isEmpty(issues)) {
            return;
        }
        publicationIssueMapper.cancelUnfinishedByOrderItemId(orderItemId,
                PublicationDeliveryStatusEnum.DELIVERED.getStatus(), PublicationReceiveStatusEnum.RECEIVED.getStatus());
        refreshOrderItemPublicationIssueStats(List.of(orderItemId));
        refreshDeliveriesAfterIssueCancellation(convertSet(issues, TradeOrderPublicationIssueDO::getDeliveryId),
                LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshOrderItemPublicationIssueStats(Collection<Long> orderItemIds) {
        if (CollUtil.isEmpty(orderItemIds)) {
            return;
        }
        Map<Long, List<TradeOrderPublicationIssueDO>> issueMap = publicationIssueMapper.selectListByOrderItemIds(orderItemIds)
                .stream().collect(java.util.stream.Collectors.groupingBy(TradeOrderPublicationIssueDO::getOrderItemId));
        for (Long orderItemId : orderItemIds) {
            List<TradeOrderPublicationIssueDO> issues = issueMap.get(orderItemId);
            if (CollUtil.isEmpty(issues)) {
                continue;
            }
            long activeTotal = issues.stream().filter(issue -> !Boolean.TRUE.equals(issue.getCanceled())).count();
            long delivered = issues.stream()
                    .filter(issue -> !Boolean.TRUE.equals(issue.getCanceled()))
                    .filter(issue -> Objects.equals(issue.getDeliveryStatus(), PublicationDeliveryStatusEnum.DELIVERED.getStatus()))
                    .count();
            long received = issues.stream()
                    .filter(issue -> !Boolean.TRUE.equals(issue.getCanceled()))
                    .filter(issue -> Objects.equals(issue.getReceiveStatus(), PublicationReceiveStatusEnum.RECEIVED.getStatus()))
                    .count();
            Integer fulfillmentStatus = resolveFulfillmentStatus(activeTotal, delivered, received);
            tradeOrderItemMapper.updateById(new TradeOrderItemDO().setId(orderItemId)
                    .setPublicationIssueTotalCount(issues.size())
                    .setPublicationIssueDeliveredCount(Math.toIntExact(delivered))
                    .setPublicationIssueReceivedCount(Math.toIntExact(received))
                    .setPublicationFulfillmentStatus(fulfillmentStatus)
                    .setPublicationDeliveryStatus(delivered > 0
                            ? PublicationDeliveryStatusEnum.DELIVERED.getStatus()
                            : PublicationDeliveryStatusEnum.UNDELIVERED.getStatus()));
        }
    }

    private void receiveIssues(List<TradeOrderPublicationIssueDO> issues, Long receiverUserId) {
        LocalDateTime receiveTime = LocalDateTime.now();
        int updateCount = publicationIssueMapper.receiveByIds(convertSet(issues, TradeOrderPublicationIssueDO::getId),
                PublicationReceiveStatusEnum.UNRECEIVED.getStatus(), receiveTime, receiverUserId);
        if (updateCount != issues.size()) {
            throw exception(PUBLICATION_ISSUE_RECEIVE_FAIL_STATUS);
        }
        refreshOrderItemPublicationIssueStats(convertSet(issues, TradeOrderPublicationIssueDO::getOrderItemId));
        refreshCompletedDeliveries(convertSet(issues, TradeOrderPublicationIssueDO::getDeliveryId), receiveTime);
    }

    private void refreshDeliveredDeliveries(Collection<Long> deliveryIds, LocalDateTime deliveryTime) {
        if (CollUtil.isEmpty(deliveryIds)) {
            return;
        }
        List<TradeOrderDeliveryDO> deliveries = tradeOrderDeliveryMapper.selectByIds(deliveryIds);
        Map<Long, TradeOrderDO> orderMap = tradeOrderMapper.selectByIds(convertSet(deliveries, TradeOrderDeliveryDO::getOrderId))
                .stream().collect(java.util.stream.Collectors.toMap(TradeOrderDO::getId, order -> order));
        for (TradeOrderDeliveryDO delivery : deliveries) {
            Long notDeliveredCount = publicationIssueMapper.selectNotDeliveredCountByDeliveryId(
                    delivery.getId(), PublicationDeliveryStatusEnum.DELIVERED.getStatus());
            if (notDeliveredCount != null && notDeliveredCount > 0) {
                continue;
            }
            if (!TradeOrderStatusEnum.isUndelivered(delivery.getStatus())) {
                continue;
            }
            tradeOrderDeliveryMapper.updateByIdAndStatus(delivery.getId(), TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                    new TradeOrderDeliveryDO().setStatus(TradeOrderStatusEnum.DELIVERED.getStatus())
                            .setDeliveryTime(deliveryTime));
            TradeOrderDO order = orderMap.get(delivery.getOrderId());
            if (order != null) {
                statusAggregateSupport.refreshOrderStatusByDeliveries(order);
            }
        }
    }

    private void refreshCompletedDeliveries(Collection<Long> deliveryIds, LocalDateTime receiveTime) {
        if (CollUtil.isEmpty(deliveryIds)) {
            return;
        }
        List<TradeOrderDeliveryDO> deliveries = tradeOrderDeliveryMapper.selectByIds(deliveryIds);
        Map<Long, TradeOrderDO> orderMap = tradeOrderMapper.selectByIds(convertSet(deliveries, TradeOrderDeliveryDO::getOrderId))
                .stream().collect(java.util.stream.Collectors.toMap(TradeOrderDO::getId, order -> order));
        for (TradeOrderDeliveryDO delivery : deliveries) {
            Long notReceivedCount = publicationIssueMapper.selectNotReceivedCountByDeliveryId(
                    delivery.getId(), PublicationReceiveStatusEnum.RECEIVED.getStatus());
            if (notReceivedCount != null && notReceivedCount > 0) {
                continue;
            }
            if (!TradeOrderStatusEnum.isDelivered(delivery.getStatus())) {
                continue;
            }
            tradeOrderDeliveryMapper.updateByIdAndStatus(delivery.getId(), TradeOrderStatusEnum.DELIVERED.getStatus(),
                    new TradeOrderDeliveryDO().setStatus(TradeOrderStatusEnum.COMPLETED.getStatus())
                            .setReceiveTime(receiveTime));
            TradeOrderDO order = orderMap.get(delivery.getOrderId());
            if (order != null) {
                statusAggregateSupport.refreshOrderStatusByDeliveries(order);
            }
        }
    }

    private void refreshDeliveriesAfterIssueCancellation(Collection<Long> deliveryIds, LocalDateTime refreshTime) {
        if (CollUtil.isEmpty(deliveryIds)) {
            return;
        }
        List<TradeOrderDeliveryDO> deliveries = tradeOrderDeliveryMapper.selectByIds(deliveryIds);
        if (CollUtil.isEmpty(deliveries)) {
            return;
        }
        Map<Long, TradeOrderDO> orderMap = tradeOrderMapper.selectByIds(convertSet(deliveries, TradeOrderDeliveryDO::getOrderId))
                .stream().collect(java.util.stream.Collectors.toMap(TradeOrderDO::getId, order -> order));
        for (TradeOrderDeliveryDO delivery : deliveries) {
            boolean refreshed = false;
            Integer deliveryStatus = delivery.getStatus();
            if (TradeOrderStatusEnum.isUndelivered(deliveryStatus)) {
                Long notDeliveredCount = publicationIssueMapper.selectNotDeliveredCountByDeliveryId(
                        delivery.getId(), PublicationDeliveryStatusEnum.DELIVERED.getStatus());
                if (notDeliveredCount != null && notDeliveredCount == 0) {
                    int updateCount = tradeOrderDeliveryMapper.updateByIdAndStatus(delivery.getId(),
                            TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                            new TradeOrderDeliveryDO().setStatus(TradeOrderStatusEnum.DELIVERED.getStatus())
                                    .setDeliveryTime(refreshTime));
                    if (updateCount > 0) {
                        deliveryStatus = TradeOrderStatusEnum.DELIVERED.getStatus();
                        refreshed = true;
                    }
                }
            }
            if (TradeOrderStatusEnum.isDelivered(deliveryStatus)) {
                Long notReceivedCount = publicationIssueMapper.selectNotReceivedCountByDeliveryId(
                        delivery.getId(), PublicationReceiveStatusEnum.RECEIVED.getStatus());
                if (notReceivedCount != null && notReceivedCount == 0) {
                    int updateCount = tradeOrderDeliveryMapper.updateByIdAndStatus(delivery.getId(),
                            TradeOrderStatusEnum.DELIVERED.getStatus(),
                            new TradeOrderDeliveryDO().setStatus(TradeOrderStatusEnum.COMPLETED.getStatus())
                                    .setReceiveTime(refreshTime));
                    if (updateCount > 0) {
                        refreshed = true;
                    }
                }
            }
            if (refreshed) {
                TradeOrderDO order = orderMap.get(delivery.getOrderId());
                if (order != null) {
                    statusAggregateSupport.refreshOrderStatusByDeliveries(order);
                }
            }
        }
    }

    private Integer resolveFulfillmentStatus(long activeTotal, long delivered, long received) {
        if (activeTotal == 0) {
            return PublicationFulfillmentStatusEnum.CANCELED.getStatus();
        }
        if (received == activeTotal) {
            return PublicationFulfillmentStatusEnum.COMPLETED.getStatus();
        }
        if (received > 0) {
            return PublicationFulfillmentStatusEnum.PARTIAL_RECEIVED.getStatus();
        }
        if (delivered == activeTotal) {
            return PublicationFulfillmentStatusEnum.DELIVERED.getStatus();
        }
        if (delivered > 0) {
            return PublicationFulfillmentStatusEnum.PARTIAL_DELIVERED.getStatus();
        }
        return PublicationFulfillmentStatusEnum.UNDELIVERED.getStatus();
    }

}
