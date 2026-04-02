package cn.iocoder.yudao.module.subscription.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubscriptionGradeCalcRuleEnum implements ArrayValuable<String> {

    CURRENT_GRADE("CURRENT_GRADE", "当前年级"),
    PROMOTED_GRADE("PROMOTED_GRADE", "升学后年级");

    public static final String[] ARRAYS = new String[] {"CURRENT_GRADE", "PROMOTED_GRADE"};

    private final String rule;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
