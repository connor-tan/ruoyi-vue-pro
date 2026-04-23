package cn.iocoder.yudao.module.publication.api.enums;

import java.util.Arrays;

public enum PublicationTargetPeriodEnum {

    FULL_YEAR("FULL_YEAR", "全学年"),
    FIRST_TERM("FIRST_TERM", "上学期"),
    SECOND_TERM("SECOND_TERM", "下学期");

    private final String code;
    private final String name;

    PublicationTargetPeriodEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String defaultCode() {
        return FULL_YEAR.code;
    }

    public static String normalize(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .map(PublicationTargetPeriodEnum::getCode)
                .orElse(FULL_YEAR.code);
    }
}
