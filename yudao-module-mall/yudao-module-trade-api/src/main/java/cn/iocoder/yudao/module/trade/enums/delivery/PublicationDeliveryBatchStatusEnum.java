package cn.iocoder.yudao.module.trade.enums.delivery;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 刊物批次发货状态
 */
@Getter
@AllArgsConstructor
public enum PublicationDeliveryBatchStatusEnum {

    DELIVERED(20, "已发货");

    private final Integer status;
    private final String name;

}
