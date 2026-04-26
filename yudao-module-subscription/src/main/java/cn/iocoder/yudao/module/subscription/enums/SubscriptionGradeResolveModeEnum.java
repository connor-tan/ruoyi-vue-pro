package cn.iocoder.yudao.module.subscription.enums;

public enum SubscriptionGradeResolveModeEnum {

    AUTO_TARGET_YEAR_GRADE("AUTO_TARGET_YEAR_GRADE", "按目标学年自动解析"),
    CURRENT_CHAIN("CURRENT_CHAIN", "当前班级链路"),
    TARGET_CLASS_FIRST("TARGET_CLASS_FIRST", "未来班级优先");

    private final String mode;
    private final String name;

    SubscriptionGradeResolveModeEnum(String mode, String name) {
        this.mode = mode;
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public String getName() {
        return name;
    }

    public static String defaultMode() {
        return AUTO_TARGET_YEAR_GRADE.mode;
    }
}
