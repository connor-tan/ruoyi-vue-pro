package cn.iocoder.yudao.module.trade.service.message;

import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendToTemplateReceiverReqDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.AfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.enums.MessageTemplateConstants;
import cn.iocoder.yudao.module.trade.service.message.bo.TradeOrderMessageWhenDeliveryOrderReqBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trade 消息 service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
@Slf4j
public class TradeMessageServiceImpl implements TradeMessageService {

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Override
    public void sendMessageWhenDeliveryOrder(TradeOrderMessageWhenDeliveryOrderReqBO reqBO) {
        if (true) {
            return;
        }
        // 1、构造消息
        Map<String, Object> msgMap = new HashMap<>(2);
        msgMap.put("orderId", reqBO.getOrderId());
        msgMap.put("deliveryMessage", reqBO.getMessage());
        // TODO 芋艿：看下模版
        // 2、发送站内信
        notifyMessageSendApi.sendSingleMessageToMember(
                new NotifySendSingleToUserReqDTO()
                        .setUserId(reqBO.getUserId())
                        .setTemplateCode(MessageTemplateConstants.SMS_ORDER_DELIVERY)
                        .setTemplateParams(msgMap));
    }

    @Override
    public void sendMessageWhenOrderCreated(TradeOrderDO order) {
        Map<String, Object> msgMap = new HashMap<>(4);
        msgMap.put("userId", order.getUserId());
        msgMap.put("orderNo", order.getNo());
        msgMap.put("payPrice", formatPrice(order.getPayPrice()));
        sendAdminNotifyByTemplateReceivers(MessageTemplateConstants.NOTIFY_ORDER_CREATED_ADMIN, msgMap,
                "用户提交订单");
    }

    @Override
    public void sendMessageWhenAfterSaleRefunded(AfterSaleDO afterSale) {
        Map<String, Object> msgMap = new HashMap<>(6);
        msgMap.put("userId", afterSale.getUserId());
        msgMap.put("afterSaleNo", afterSale.getNo());
        msgMap.put("orderNo", afterSale.getOrderNo());
        msgMap.put("refundPrice", formatPrice(afterSale.getRefundPrice()));
        sendAdminNotifyByTemplateReceivers(MessageTemplateConstants.NOTIFY_AFTER_SALE_REFUNDED_ADMIN, msgMap,
                "用户退款成功");
    }

    private void sendAdminNotifyByTemplateReceivers(String templateCode, Map<String, Object> templateParams,
                                                    String scene) {
        try {
            List<Long> messageIds = notifyMessageSendApi.sendMessageToAdminTemplateReceivers(
                    new NotifySendToTemplateReceiverReqDTO()
                            .setTemplateCode(templateCode)
                            .setTemplateParams(templateParams));
            log.info("[sendAdminNotifyByTemplateReceivers][scene({}) templateCode({}) messageIds({})]",
                    scene, templateCode, messageIds);
        } catch (Exception ex) {
            log.warn("[sendAdminNotifyByTemplateReceivers][scene({}) templateCode({}) 发送站内信失败]",
                    scene, templateCode, ex);
        }
    }

    private String formatPrice(Integer price) {
        return BigDecimal.valueOf(price == null ? 0 : price, 2).toPlainString();
    }

}
