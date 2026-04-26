package cn.iocoder.yudao.module.subscription.enums;

public enum SubscriptionRuleEffectTypeEnum {

    INCLUDE("INCLUDE", "特殊允许"),
    EXCLUDE("EXCLUDE", "特殊排除");

    private final String type;
    private final String name;

    SubscriptionRuleEffectTypeEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static boolean isInclude(String type) {
        return INCLUDE.type.equals(type);
    }

    public static boolean isExclude(String type) {
        return EXCLUDE.type.equals(type);
    }
}
