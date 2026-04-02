package cn.iocoder.yudao.module.subscription.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubscriptionSemesterEnum implements ArrayValuable<Integer> {

    FIRST(1, "上学期"),
    SECOND(2, "下学期");

    public static final Integer[] ARRAYS = new Integer[]{1, 2};

    private final Integer semester;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
