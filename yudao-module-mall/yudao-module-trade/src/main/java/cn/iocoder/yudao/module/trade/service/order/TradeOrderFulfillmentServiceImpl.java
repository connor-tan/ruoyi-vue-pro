package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderStationDeliveryReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderOperateTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.core.annotations.TradeOrderLog;
import cn.iocoder.yudao.module.trade.framework.order.core.utils.TradeOrderLogUtils;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import cn.iocoder.yudao.module.trade.service.message.TradeMessageService;
import cn.iocoder.yudao.module.trade.service.message.bo.TradeOrderMessageWhenDeliveryOrderReqBO;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderDeliveryAccessSupport;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderStatusAggregateSupport;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_EXPRESS;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_STATION;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_DELIVERY_FAIL_REFUND_STATUS_NOT_NONE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.MessageTemplateConstants.WXA_ORDER_DELIVERY;

/**
 * 交易订单履约发货 Service 实现类
 */
@Service
public class TradeOrderFulfillmentServiceImpl implements TradeOrderFulfillmentService {

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Resource
    private List<TradeOrderHandler> tradeOrderHandlers;

    @Resource
    private DeliveryExpressService deliveryExpressService;
    @Resource
    private TradeMessageService tradeMessageService;
    @Resource
    private SocialClientApi socialClientApi;
    @Resource
    private TradeOrderDeliveryAccessSupport deliveryAccessSupport;
    @Resource
    private TradeOrderStatusAggregateSupport statusAggregateSupport;

    @Resource
    @Lazy
    private TradeOrderFulfillmentService self;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_DELIVERY)
    public void deliveryOrder(TradeOrderDeliveryReqVO deliveryReqVO) {
        TradeOrderDeliveryDO delivery = resolveExpressDelivery(deliveryReqVO);
        if (delivery == null) {
            deliveryOrderLegacy(deliveryReqVO);
            return;
        }
        TradeOrderDO order = validateOrderDeliverable(delivery.getOrderId());
        if (ObjectUtil.notEqual(delivery.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            throw exception(ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_EXPRESS);
        }

        TradeOrderDeliveryDO updateDeliveryObj = new TradeOrderDeliveryDO();
        DeliveryExpressDO express = null;
        if (ObjectUtil.notEqual(deliveryReqVO.getLogisticsId(), TradeOrderDO.LOGISTICS_ID_NULL)) {
            express = deliveryExpressService.validateDeliveryExpress(deliveryReqVO.getLogisticsId());
            updateDeliveryObj.setLogisticsId(deliveryReqVO.getLogisticsId()).setLogisticsNo(deliveryReqVO.getLogisticsNo());
        } else {
            updateDeliveryObj.setLogisticsId(0L).setLogisticsNo("");
        }
        updateDeliveryObj.setStatus(TradeOrderStatusEnum.DELIVERED.getStatus()).setDeliveryTime(LocalDateTime.now());
        int updateCount = tradeOrderDeliveryMapper.updateByIdAndStatus(delivery.getId(), delivery.getStatus(), updateDeliveryObj);
        if (updateCount == 0) {
            throw exception(ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED);
        }
        delivery.setLogisticsId(updateDeliveryObj.getLogisticsId()).setLogisticsNo(updateDeliveryObj.getLogisticsNo())
                .setStatus(updateDeliveryObj.getStatus()).setDeliveryTime(updateDeliveryObj.getDeliveryTime());
        TradeOrderDO refreshedOrder = statusAggregateSupport.refreshOrderStatusByDeliveries(order);

        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.DELIVERED.getStatus(),
                MapUtil.<String, Object>builder().put("expressName", express != null ? express.getName() : "")
                        .put("logisticsNo", express != null ? deliveryReqVO.getLogisticsNo() : "").build());

        tradeMessageService.sendMessageWhenDeliveryOrder(new TradeOrderMessageWhenDeliveryOrderReqBO()
                .setOrderId(order.getId()).setUserId(order.getUserId()).setMessage(null));
        self.sendDeliveryOrderMessage(refreshedOrder, deliveryReqVO);

        refreshedOrder.setLogisticsId(updateDeliveryObj.getLogisticsId()).setLogisticsNo(updateDeliveryObj.getLogisticsNo())
                .setDeliveryTime(updateDeliveryObj.getDeliveryTime());
        tradeOrderHandlers.forEach(handler -> handler.afterDeliveryOrder(refreshedOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_DELIVERY)
    public void stationDeliveryOrder(TradeOrderStationDeliveryReqVO reqVO) {
        TradeOrderDeliveryDO delivery = deliveryAccessSupport.validateDeliveryExists(reqVO.getDeliveryId());
        if (!Objects.equals(delivery.getDeliveryType(), DeliveryTypeEnum.STATION.getType())) {
            throw exception(ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_STATION);
        }
        TradeOrderDO order = validateOrderDeliverable(delivery.getOrderId());
        int updateCount = tradeOrderDeliveryMapper.updateByIdAndStatus(delivery.getId(), delivery.getStatus(),
                new TradeOrderDeliveryDO().setStatus(TradeOrderStatusEnum.DELIVERED.getStatus())
                        .setDeliveryTime(LocalDateTime.now()));
        if (updateCount == 0) {
            throw exception(ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED);
        }
        statusAggregateSupport.refreshOrderStatusByDeliveries(order);
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.DELIVERED.getStatus(),
                MapUtil.<String, Object>builder().put("stationName", delivery.getStationNameSnapshot())
                        .put("schoolName", delivery.getSchoolNameSnapshot()).build());
    }

    @Override
    @Async
    public void sendDeliveryOrderMessage(TradeOrderDO order, TradeOrderDeliveryReqVO deliveryReqVO) {
        Long orderId = order.getId();
        socialClientApi.sendWxaSubscribeMessage(new SocialWxaSubscribeMessageSendReqDTO()
                .setUserId(order.getUserId()).setUserType(UserTypeEnum.MEMBER.getValue())
                .setTemplateTitle(WXA_ORDER_DELIVERY)
                .setPage("pages/order/detail?id=" + orderId)
                .addMessage("character_string3", String.valueOf(orderId))
                .addMessage("phrase6", TradeOrderStatusEnum.DELIVERED.getName())
                .addMessage("date4", LocalDateTimeUtil.formatNormal(LocalDateTime.now()))
                .addMessage("character_string5", StrUtil.blankToDefault(deliveryReqVO.getLogisticsNo(), "-"))
                .addMessage("thing9", order.getReceiverDetailAddress()));
    }

    private TradeOrderDO validateOrderDeliverable(Long id) {
        TradeOrderDO order = validateOrderExists(id);
        if (ObjectUtil.notEqual(TradeOrderRefundStatusEnum.NONE.getStatus(), order.getRefundStatus())) {
            throw exception(ORDER_DELIVERY_FAIL_REFUND_STATUS_NOT_NONE);
        }
        tradeOrderHandlers.forEach(handler -> handler.beforeDeliveryOrder(order));
        return order;
    }

    private TradeOrderDO validateOrderExists(Long id) {
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        return order;
    }

    private TradeOrderDeliveryDO resolveExpressDelivery(TradeOrderDeliveryReqVO deliveryReqVO) {
        if (deliveryReqVO.getDeliveryId() != null) {
            return deliveryAccessSupport.validateDeliveryExists(deliveryReqVO.getDeliveryId());
        }
        List<TradeOrderDeliveryDO> deliveries = deliveryAccessSupport.getDeliveryListByOrderId(deliveryReqVO.getId());
        if (cn.hutool.core.collection.CollUtil.isEmpty(deliveries)) {
            return null;
        }
        TradeOrderDeliveryDO expressDelivery = deliveryAccessSupport.findDeliveryByType(
                deliveries, DeliveryTypeEnum.EXPRESS.getType());
        if (expressDelivery == null) {
            throw exception(ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_EXPRESS);
        }
        return expressDelivery;
    }

    private void deliveryOrderLegacy(TradeOrderDeliveryReqVO deliveryReqVO) {
        TradeOrderDO order = validateOrderDeliverable(deliveryReqVO.getId());
        if (ObjectUtil.notEqual(order.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            throw exception(ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_EXPRESS);
        }

        TradeOrderDO updateOrderObj = new TradeOrderDO();
        DeliveryExpressDO express = null;
        if (ObjectUtil.notEqual(deliveryReqVO.getLogisticsId(), TradeOrderDO.LOGISTICS_ID_NULL)) {
            express = deliveryExpressService.validateDeliveryExpress(deliveryReqVO.getLogisticsId());
            updateOrderObj.setLogisticsId(deliveryReqVO.getLogisticsId()).setLogisticsNo(deliveryReqVO.getLogisticsNo());
        } else {
            updateOrderObj.setLogisticsId(0L).setLogisticsNo("");
        }
        updateOrderObj.setStatus(TradeOrderStatusEnum.DELIVERED.getStatus()).setDeliveryTime(LocalDateTime.now());
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(), updateOrderObj);
        if (updateCount == 0) {
            throw exception(ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED);
        }

        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.DELIVERED.getStatus(),
                MapUtil.<String, Object>builder().put("expressName", express != null ? express.getName() : "")
                        .put("logisticsNo", express != null ? deliveryReqVO.getLogisticsNo() : "").build());

        tradeMessageService.sendMessageWhenDeliveryOrder(new TradeOrderMessageWhenDeliveryOrderReqBO()
                .setOrderId(order.getId()).setUserId(order.getUserId()).setMessage(null));
        self.sendDeliveryOrderMessage(order, deliveryReqVO);

        order.setLogisticsId(updateOrderObj.getLogisticsId()).setLogisticsNo(updateOrderObj.getLogisticsNo())
                .setStatus(updateOrderObj.getStatus()).setDeliveryTime(updateOrderObj.getDeliveryTime());
        tradeOrderHandlers.forEach(handler -> handler.afterDeliveryOrder(order));
    }

}
