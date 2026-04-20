package cn.iocoder.yudao.module.publication.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PublicationTargetPeriodEnum implements ArrayValuable<String> {

    FIRST_TERM("FIRST_TERM", "上学期"),
    SECOND_TERM("SECOND_TERM", "下学期"),
    FULL_YEAR("FULL_YEAR", "全学年");

    public static final String[] ARRAYS = new String[] {"FIRST_TERM", "SECOND_TERM", "FULL_YEAR"};

    private final String period;

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static String defaultPeriod() {
        return FULL_YEAR.period;
    }

    public static String normalize(String period) {
        if (FIRST_TERM.period.equals(period)) {
            return FIRST_TERM.period;
        }
        if (SECOND_TERM.period.equals(period)) {
            return SECOND_TERM.period;
        }
        if (FULL_YEAR.period.equals(period)) {
            return FULL_YEAR.period;
        }
        return defaultPeriod();
    }
}
