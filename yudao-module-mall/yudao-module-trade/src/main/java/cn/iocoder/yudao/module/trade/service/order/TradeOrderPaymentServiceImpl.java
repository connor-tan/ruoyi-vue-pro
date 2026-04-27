package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundRespDTO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderCancelTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderOperateTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.framework.order.core.annotations.TradeOrderLog;
import cn.iocoder.yudao.module.trade.framework.order.core.utils.TradeOrderLogUtils;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_CANCEL_PAID_FAIL;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_UPDATE_PAID_ORDER_REFUNDED_FAIL_REFUND_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_UPDATE_PAID_ORDER_REFUNDED_FAIL_REFUND_STATUS_NOT_SUCCESS;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_UPDATE_PAID_STATUS_NOT_UNPAID;

/**
 * 交易订单支付同步 Service 实现类
 */
@Service
@Slf4j
public class TradeOrderPaymentServiceImpl implements TradeOrderPaymentService {

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Resource
    private List<TradeOrderHandler> tradeOrderHandlers;

    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private PayRefundApi payRefundApi;
    @Resource
    private TradeOrderProperties tradeOrderProperties;
    @Resource
    private TradeOrderLifecycleService tradeOrderLifecycleService;

    @Resource
    @Lazy
    private TradeOrderPaymentService self;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_PAY)
    public void updateOrderPaid(Long id, Long payOrderId) {
        TradeOrderDO order = validateOrderExists(id);
        if (!TradeOrderStatusEnum.isUnpaid(order.getStatus()) || order.getPayStatus()) {
            if (ObjectUtil.equals(order.getPayOrderId(), payOrderId)) {
                log.warn("[updateOrderPaid][order({}) 已支付，且支付单号相同({})，直接返回]", order, payOrderId);
                return;
            }
            log.error("[updateOrderPaid][order({}) 支付单不匹配({})，请进行处理！order 数据是：{}]",
                    id, payOrderId, JsonUtils.toJsonString(order));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }

        PayOrderRespDTO payOrder = validatePayOrderPaid(order, payOrderId);

        int updateCount = tradeOrderMapper.updateByIdAndStatus(id, order.getStatus(),
                new TradeOrderDO().setStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus()).setPayStatus(true)
                        .setPayTime(LocalDateTime.now()).setPayChannelCode(payOrder.getChannelCode()));
        if (updateCount == 0) {
            throw exception(ORDER_UPDATE_PAID_STATUS_NOT_UNPAID);
        }
        updateDeliveryStatusWhenPaid(id);

        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(id);
        tradeOrderHandlers.forEach(handler -> handler.afterPayOrder(order, orderItems));

        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.UNDELIVERED.getStatus());
        TradeOrderLogUtils.setUserInfo(order.getUserId(), UserTypeEnum.MEMBER.getValue());
    }

    @Override
    public void syncOrderPayStatusQuietly(Long id, Long payOrderId) {
        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        if (payOrder == null || !PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            return;
        }
        try {
            self.updateOrderPaid(id, payOrderId);
        } catch (Throwable e) {
            log.warn("[syncOrderPayStatusQuietly][id({}) payOrderId({}) 同步支付状态失败]", id, payOrderId, e);
        }
    }

    private PayOrderRespDTO validatePayOrderPaid(TradeOrderDO order, Long payOrderId) {
        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        if (payOrder == null) {
            log.error("[validatePayOrderPaid][order({}) payOrder({}) 不存在，请进行处理！]", order.getId(), payOrderId);
            throw exception(ORDER_NOT_FOUND);
        }
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            log.error("[validatePayOrderPaid][order({}) payOrder({}) 未支付，请进行处理！payOrder 数据是：{}]",
                    order.getId(), payOrderId, JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS);
        }
        if (ObjectUtil.notEqual(payOrder.getPrice(), order.getPayPrice())) {
            log.error("[validatePayOrderPaid][order({}) payOrder({}) 支付金额不匹配，请进行处理！order 数据是：{}，payOrder 数据是：{}]",
                    order.getId(), payOrderId, JsonUtils.toJsonString(order), JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH);
        }
        if (ObjectUtil.notEqual(payOrder.getMerchantOrderId(), order.getId().toString())) {
            log.error("[validatePayOrderPaid][order({}) 支付单不匹配({})，请进行处理！payOrder 数据是：{}]",
                    order.getId(), payOrderId, JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }
        return payOrder;
    }

    private void updateDeliveryStatusWhenPaid(Long orderId) {
        List<TradeOrderDeliveryDO> deliveries = tradeOrderDeliveryMapper.selectListByOrderId(orderId);
        for (TradeOrderDeliveryDO delivery : deliveries) {
            if (TradeOrderStatusEnum.isUnpaid(delivery.getStatus())) {
                tradeOrderDeliveryMapper.updateById(new TradeOrderDeliveryDO().setId(delivery.getId())
                        .setStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus()));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPaidOrder(Long userId, Long orderId, Integer cancelType) {
        if (ObjUtil.notEqual(TradeOrderCancelTypeEnum.COMBINATION_CLOSE.getType(), cancelType)) {
            return;
        }
        TradeOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(orderId, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (!order.getPayStatus()) {
            throw exception(ORDER_CANCEL_PAID_FAIL, "已支付");
        }
        if (ObjUtil.notEqual(TradeOrderRefundStatusEnum.NONE.getStatus(), order.getRefundStatus())) {
            throw exception(ORDER_CANCEL_PAID_FAIL, "未退款");
        }

        tradeOrderLifecycleService.cancelOrderForPaymentClose(order, TradeOrderCancelTypeEnum.COMBINATION_CLOSE);
        payRefundApi.createRefund(new PayRefundCreateReqDTO()
                .setAppKey(tradeOrderProperties.getPayAppKey())
                .setUserIp(NetUtil.getLocalhostStr())
                .setUserId(order.getUserId()).setUserType(UserTypeEnum.MEMBER.getValue())
                .setMerchantOrderId(String.valueOf(order.getId()))
                .setMerchantRefundId("order-" + order.getId())
                .setReason(TradeOrderCancelTypeEnum.COMBINATION_CLOSE.getName()).setPrice(order.getPayPrice()));
    }

    @Override
    public void updatePaidOrderRefunded(Long id, Long payRefundId) {
        PayRefundRespDTO payRefund = payRefundApi.getRefund(payRefundId);
        if (payRefund == null) {
            throw exception(ORDER_UPDATE_PAID_ORDER_REFUNDED_FAIL_REFUND_NOT_FOUND);
        }
        if (!PayRefundStatusEnum.isSuccess(payRefund.getStatus())) {
            throw exception(ORDER_UPDATE_PAID_ORDER_REFUNDED_FAIL_REFUND_STATUS_NOT_SUCCESS);
        }
    }

    private TradeOrderDO validateOrderExists(Long id) {
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        return order;
    }

}
