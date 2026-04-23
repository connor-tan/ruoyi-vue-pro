package cn.iocoder.yudao.module.publication.api.enums;

import java.util.Arrays;

public enum BizSceneEnum {

    NORMAL("NORMAL", "普通商品"),
    PUBLICATION("PUBLICATION", "刊物商品");

    private final String code;
    private final String name;

    BizSceneEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static BizSceneEnum valueOfCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public static boolean isPublication(String code) {
        return PUBLICATION == valueOfCode(code);
    }

    public static boolean isNormal(String code) {
        return NORMAL == valueOfCode(code);
    }
}
