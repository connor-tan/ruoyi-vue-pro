package cn.iocoder.yudao.module.trade.enums;

/**
 * 通知模板枚举类
 *
 * @author HUIHUI
 */
public interface MessageTemplateConstants {

    // ======================= 站内信消息模版 =======================

    String NOTIFY_ORDER_CREATED_ADMIN = "trade_order_created_admin"; // 用户提交订单通知

    String NOTIFY_AFTER_SALE_REFUNDED_ADMIN = "trade_after_sale_refunded_admin"; // 用户退款成功通知

    // ======================= 短信消息模版 =======================

    String SMS_ORDER_DELIVERY = "order_delivery"; // 短信模版编号

    // ======================= 小程序订阅消息模版 =======================

    String WXA_ORDER_DELIVERY = "订单发货通知";

}
