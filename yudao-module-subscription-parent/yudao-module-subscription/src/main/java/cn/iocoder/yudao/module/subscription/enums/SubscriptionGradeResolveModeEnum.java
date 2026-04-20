package cn.iocoder.yudao.module.subscription.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubscriptionGradeResolveModeEnum implements ArrayValuable<String> {

    CURRENT_CHAIN("CURRENT_CHAIN", "当前班级链路"),
    TARGET_CLASS_FIRST("TARGET_CLASS_FIRST", "未来班级优先");

    public static final String[] ARRAYS = new String[] {"CURRENT_CHAIN", "TARGET_CLASS_FIRST"};

    private final String mode;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
