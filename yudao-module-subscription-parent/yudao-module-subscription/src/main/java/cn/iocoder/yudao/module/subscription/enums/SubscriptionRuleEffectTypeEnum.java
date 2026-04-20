package cn.iocoder.yudao.module.subscription.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubscriptionRuleEffectTypeEnum implements ArrayValuable<String> {

    INCLUDE("INCLUDE", "包含"),
    EXCLUDE("EXCLUDE", "排除");

    public static final String[] ARRAYS = new String[] {"INCLUDE", "EXCLUDE"};

    private final String type;

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
