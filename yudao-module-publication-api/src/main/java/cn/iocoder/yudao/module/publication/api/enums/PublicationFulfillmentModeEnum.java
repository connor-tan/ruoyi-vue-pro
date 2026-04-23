package cn.iocoder.yudao.module.publication.api.enums;

import java.util.Arrays;

public enum PublicationFulfillmentModeEnum {

    STANDARD_EXPRESS("STANDARD_EXPRESS", "快递发货"),
    STANDARD_PICK_UP("STANDARD_PICK_UP", "普通自提"),
    SCHOOL_STATION("SCHOOL_STATION", "学校站点");

    private final String code;
    private final String name;

    PublicationFulfillmentModeEnum(String code, String name) {
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
        return SCHOOL_STATION.code;
    }

    public static String normalize(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .map(PublicationFulfillmentModeEnum::getCode)
                .orElse(SCHOOL_STATION.code);
    }
}
