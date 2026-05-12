package cn.iocoder.yudao.module.trade.enums.delivery;

import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 刊物期次收货状态。
 */
@Getter
@AllArgsConstructor
public enum PublicationReceiveStatusEnum {

    UNRECEIVED(10, "待收货"),
    RECEIVED(20, "已收货");

    private final Integer status;
    private final String name;

    public static boolean isReceived(Integer status) {
        return ObjectUtil.equal(RECEIVED.getStatus(), status);
    }

}
