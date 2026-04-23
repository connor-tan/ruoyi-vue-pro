package cn.iocoder.yudao.module.product.enums.sku;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductSkuStatusEnum {

    ENABLE(CommonStatusEnum.ENABLE.getStatus(), "启用"),
    DISABLE(CommonStatusEnum.DISABLE.getStatus(), "停用");

    private final Integer status;
    private final String name;

    public static boolean isEnable(Integer status) {
        return ENABLE.status.equals(status);
    }
}
