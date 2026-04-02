package cn.iocoder.yudao.module.subscription.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubscriptionRuleScopeTypeEnum implements ArrayValuable<String> {

    ALL("ALL", "全员"),
    SCHOOL("SCHOOL", "学校"),
    GRADE("GRADE", "年级"),
    SCHOOL_GRADE("SCHOOL_GRADE", "学校+年级");

    public static final String[] ARRAYS = new String[] {"ALL", "SCHOOL", "GRADE", "SCHOOL_GRADE"};

    private final String type;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
