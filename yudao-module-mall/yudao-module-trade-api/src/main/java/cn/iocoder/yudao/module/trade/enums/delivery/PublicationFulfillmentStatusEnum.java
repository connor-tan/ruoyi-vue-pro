package cn.iocoder.yudao.module.trade.enums.delivery;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单项刊物期次履约聚合状态。
 */
@Getter
@AllArgsConstructor
public enum PublicationFulfillmentStatusEnum {

    UNDELIVERED(10, "待发货"),
    PARTIAL_DELIVERED(20, "部分发货"),
    DELIVERED(30, "已发货"),
    PARTIAL_RECEIVED(40, "部分收货"),
    COMPLETED(50, "已完成"),
    CANCELED(90, "已取消");

    private final Integer status;
    private final String name;

}
