package cn.iocoder.yudao.module.subscription.enums;

public enum SubscriptionGradeCalcRuleEnum {

    AUTO_TARGET_YEAR_GRADE("AUTO_TARGET_YEAR_GRADE", "按目标学年自动解析"),
    CURRENT_GRADE("CURRENT_GRADE", "使用当前年级"),
    PROMOTED_GRADE("PROMOTED_GRADE", "使用升学后年级");

    private final String rule;
    private final String name;

    SubscriptionGradeCalcRuleEnum(String rule, String name) {
        this.rule = rule;
        this.name = name;
    }

    public String getRule() {
        return rule;
    }

    public String getName() {
        return name;
    }

    public static String defaultRule() {
        return AUTO_TARGET_YEAR_GRADE.rule;
    }
}
