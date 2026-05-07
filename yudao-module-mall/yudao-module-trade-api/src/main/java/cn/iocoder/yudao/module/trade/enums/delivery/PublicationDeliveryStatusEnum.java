package cn.iocoder.yudao.module.trade.enums.delivery;

import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单项刊物发货状态
 */
@Getter
@AllArgsConstructor
public enum PublicationDeliveryStatusEnum {

    UNDELIVERED(10, "待发货"),
    DELIVERED(20, "已发货");

    private final Integer status;
    private final String name;

    public static boolean isDelivered(Integer status) {
        return ObjectUtil.equal(DELIVERED.getStatus(), status);
    }

}
