package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.promotion.api.combination.CombinationRecordApi;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordRespDTO;
import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderOperateTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.framework.order.core.annotations.TradeOrderLog;
import cn.iocoder.yudao.module.trade.framework.order.core.utils.TradeOrderLogUtils;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryPickUpStoreService;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderDeliveryAccessSupport;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderStatusAggregateSupport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.minusTime;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_PICK_UP_DELIVERY_DUPLICATE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_PICK_UP_DELIVERY_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_PICK_UP_FAIL_COMBINATION_NOT_SUCCESS;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_PICK_UP_FAIL_NOT_VERIFY_USER;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_PICK_UP_FAIL_STATUS_NOT_UNDELIVERED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_RECEIVE_FAIL_DELIVERY_NOT_OWNED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_RECEIVE_FAIL_DELIVERY_STATUS_NOT_DELIVERED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_RECEIVE_FAIL_DELIVERY_TYPE_NOT_PICK_UP;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_RECEIVE_FAIL_SPLIT_DELIVERY_REQUIRED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED;

/**
 * 交易订单收货完成 Service 实现类
 */
@Service
@Slf4j
public class TradeOrderReceiveServiceImpl implements TradeOrderReceiveService {

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Resource
    private List<TradeOrderHandler> tradeOrderHandlers;

    @Resource
    private DeliveryPickUpStoreService pickUpStoreService;
    @Resource
    private CombinationRecordApi combinationRecordApi;
    @Resource
    private TradeOrderProperties tradeOrderProperties;
    @Resource
    private TradeOrderDeliveryAccessSupport deliveryAccessSupport;
    @Resource
    private TradeOrderStatusAggregateSupport statusAggregateSupport;
    @Resource
    private TradeOrderPublicationIssueService publicationIssueService;

    @Resource
    @Lazy
    private TradeOrderReceiveService self;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_RECEIVE)
    public void receiveOrderByMember(Long userId, Long id) {
        TradeOrderDO order = validateOrderReceivable(userId, id);
        List<TradeOrderDeliveryDO> deliveries = deliveryAccessSupport.getDeliveryListByOrderId(order.getId());
        if (CollUtil.isEmpty(deliveries)) {
            receiveOrder0(order);
            return;
        }
        throw exception(ORDER_RECEIVE_FAIL_SPLIT_DELIVERY_REQUIRED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_RECEIVE)
    public void receiveDeliveryByMember(Long userId, Long deliveryId) {
        TradeOrderDeliveryDO delivery = deliveryAccessSupport.validateDeliveryExists(deliveryId);
        TradeOrderDO order = deliveryAccessSupport.validateDeliveryOrderOwned(
                delivery, userId, ORDER_RECEIVE_FAIL_DELIVERY_NOT_OWNED);
        if (!TradeOrderStatusEnum.isDelivered(delivery.getStatus())) {
            throw exception(ORDER_RECEIVE_FAIL_DELIVERY_STATUS_NOT_DELIVERED);
        }
        if (BizSceneEnum.isPublication(delivery.getBizScene())) {
            publicationIssueService.receiveDeliveryIssues(userId, deliveryId);
            TradeOrderDO refreshedOrder = statusAggregateSupport.refreshOrderStatusByDeliveries(order);
            if (TradeOrderStatusEnum.isCompleted(refreshedOrder.getStatus())) {
                TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.COMPLETED.getStatus());
                tradeOrderHandlers.forEach(handler -> handler.afterReceiveOrder(refreshedOrder));
            }
            return;
        }
        boolean changed = receiveDelivery0(order, delivery, true);
        if (changed) {
            TradeOrderDO refreshedOrder = statusAggregateSupport.refreshOrderStatusByDeliveries(order);
            if (TradeOrderStatusEnum.isCompleted(refreshedOrder.getStatus())) {
                TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.COMPLETED.getStatus());
                tradeOrderHandlers.forEach(handler -> handler.afterReceiveOrder(refreshedOrder));
            }
        }
    }

    @Override
    public int receiveOrderBySystem() {
        LocalDateTime expireTime = minusTime(tradeOrderProperties.getReceiveExpireTime());
        List<TradeOrderDO> orders = tradeOrderMapper.selectListByStatusAndDeliveryTimeLt(
                TradeOrderStatusEnum.DELIVERED.getStatus(), expireTime);
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }

        int count = 0;
        for (TradeOrderDO order : orders) {
            try {
                self.receiveOrderBySystem(order);
                count++;
            } catch (Throwable e) {
                log.error("[receiveOrderBySystem][order({}) 自动收货订单异常]", order.getId(), e);
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.SYSTEM_RECEIVE)
    public void receiveOrderBySystem(TradeOrderDO order) {
        receiveOrder0(order);
    }

    private void receiveOrder0(TradeOrderDO order) {
        List<TradeOrderDeliveryDO> deliveries = deliveryAccessSupport.getDeliveryListByOrderId(order.getId());
        if (CollUtil.isNotEmpty(deliveries)) {
            List<TradeOrderDeliveryDO> deliveredList = filterList(deliveries,
                    delivery -> TradeOrderStatusEnum.isDelivered(delivery.getStatus()));
            boolean changed = false;
            for (TradeOrderDeliveryDO delivery : deliveredList) {
                if (BizSceneEnum.isPublication(delivery.getBizScene())) {
                    publicationIssueService.receiveDeliveryIssues(order.getUserId(), delivery.getId());
                    changed = true;
                    continue;
                }
                changed |= receiveDelivery0(order, delivery, true);
            }
            if (!changed) {
                return;
            }
            TradeOrderDO refreshedOrder = statusAggregateSupport.refreshOrderStatusByDeliveries(order);
            if (TradeOrderStatusEnum.isCompleted(refreshedOrder.getStatus())) {
                TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.COMPLETED.getStatus());
                tradeOrderHandlers.forEach(handler -> handler.afterReceiveOrder(refreshedOrder));
            }
            return;
        }
        LocalDateTime receiveTime = LocalDateTime.now();
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(),
                new TradeOrderDO().setStatus(TradeOrderStatusEnum.COMPLETED.getStatus()).setReceiveTime(receiveTime));
        if (updateCount == 0) {
            throw exception(ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED);
        }

        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.COMPLETED.getStatus());

        order.setStatus(TradeOrderStatusEnum.COMPLETED.getStatus()).setReceiveTime(receiveTime);
        tradeOrderHandlers.forEach(handler -> handler.afterReceiveOrder(order));
    }

    private boolean receiveDelivery0(TradeOrderDO order, TradeOrderDeliveryDO delivery, boolean strictDeliveredStatus) {
        if (TradeOrderStatusEnum.isCompleted(delivery.getStatus())) {
            return false;
        }
        if (strictDeliveredStatus && !TradeOrderStatusEnum.isDelivered(delivery.getStatus())) {
            throw exception(ORDER_RECEIVE_FAIL_DELIVERY_STATUS_NOT_DELIVERED);
        }
        LocalDateTime receiveTime = LocalDateTime.now();
        int updateCount = tradeOrderDeliveryMapper.updateByIdAndStatus(delivery.getId(), delivery.getStatus(),
                new TradeOrderDeliveryDO().setStatus(TradeOrderStatusEnum.COMPLETED.getStatus())
                        .setReceiveTime(receiveTime));
        if (updateCount == 0) {
            throw exception(ORDER_RECEIVE_FAIL_DELIVERY_STATUS_NOT_DELIVERED);
        }
        delivery.setStatus(TradeOrderStatusEnum.COMPLETED.getStatus()).setReceiveTime(receiveTime);
        return true;
    }

    private TradeOrderDO validateOrderReceivable(Long userId, Long id) {
        TradeOrderDO order = tradeOrderMapper.selectByIdAndUserId(id, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (!TradeOrderStatusEnum.isDelivered(order.getStatus())) {
            throw exception(ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED);
        }
        return order;
    }

    @Override
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_PICK_UP_RECEIVE)
    public void pickUpOrderByAdmin(Long userId, Long id) {
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        self.pickUpOrder(userId, order, resolveUniquePickUpDelivery(order));
    }

    @Override
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_PICK_UP_RECEIVE)
    public void pickUpOrderByAdmin(Long userId, String pickUpVerifyCode) {
        TradeOrderDeliveryDO pickUpDelivery = tradeOrderDeliveryMapper.selectOneByPickUpVerifyCode(pickUpVerifyCode);
        if (pickUpDelivery == null) {
            throw exception(ORDER_PICK_UP_DELIVERY_NOT_FOUND);
        }
        self.pickUpOrder(userId, tradeOrderMapper.selectById(pickUpDelivery.getOrderId()), pickUpDelivery);
    }

    @Override
    public TradeOrderDO getByPickUpVerifyCode(String pickUpVerifyCode) {
        TradeOrderDeliveryDO pickUpDelivery = tradeOrderDeliveryMapper.selectOneByPickUpVerifyCode(pickUpVerifyCode);
        return pickUpDelivery == null ? null : tradeOrderMapper.selectById(pickUpDelivery.getOrderId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pickUpOrder(Long userId, TradeOrderDO order, TradeOrderDeliveryDO pickUpDelivery) {
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (pickUpDelivery == null) {
            throw exception(ORDER_PICK_UP_DELIVERY_NOT_FOUND);
        }
        if (!DeliveryTypeEnum.PICK_UP.getType().equals(pickUpDelivery.getDeliveryType())) {
            throw exception(ORDER_RECEIVE_FAIL_DELIVERY_TYPE_NOT_PICK_UP);
        }
        if (!TradeOrderStatusEnum.isUndelivered(order.getStatus())) {
            throw exception(ORDER_PICK_UP_FAIL_STATUS_NOT_UNDELIVERED);
        }
        if (!TradeOrderStatusEnum.isUndelivered(pickUpDelivery.getStatus())) {
            throw exception(ORDER_PICK_UP_FAIL_STATUS_NOT_UNDELIVERED);
        }
        if (TradeOrderTypeEnum.isCombination(order.getType())) {
            CombinationRecordRespDTO combinationRecord = combinationRecordApi.getCombinationRecordByOrderId(
                    order.getUserId(), order.getId());
            if (!CombinationRecordStatusEnum.isSuccess(combinationRecord.getStatus())) {
                throw exception(ORDER_PICK_UP_FAIL_COMBINATION_NOT_SUCCESS);
            }
        }
        DeliveryPickUpStoreDO deliveryPickUpStore = pickUpStoreService.getDeliveryPickUpStore(pickUpDelivery.getPickUpStoreId());
        if (deliveryPickUpStore == null || !CollUtil.contains(deliveryPickUpStore.getVerifyUserIds(), userId)) {
            throw exception(ORDER_PICK_UP_FAIL_NOT_VERIFY_USER);
        }
        receiveDelivery0(order, pickUpDelivery, false);
        TradeOrderDO refreshedOrder = statusAggregateSupport.refreshOrderStatusByDeliveries(order);
        if (TradeOrderStatusEnum.isCompleted(refreshedOrder.getStatus())) {
            TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.COMPLETED.getStatus());
            tradeOrderHandlers.forEach(handler -> handler.afterReceiveOrder(refreshedOrder));
        }
    }

    private TradeOrderDeliveryDO resolveUniquePickUpDelivery(TradeOrderDO order) {
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        List<TradeOrderDeliveryDO> deliveries = tradeOrderDeliveryMapper.selectListByOrderIdAndDeliveryType(
                order.getId(), DeliveryTypeEnum.PICK_UP.getType());
        if (CollUtil.isEmpty(deliveries)) {
            throw exception(ORDER_PICK_UP_DELIVERY_NOT_FOUND);
        }
        if (deliveries.size() > 1) {
            throw exception(ORDER_PICK_UP_DELIVERY_DUPLICATE);
        }
        return deliveries.get(0);
    }

}
