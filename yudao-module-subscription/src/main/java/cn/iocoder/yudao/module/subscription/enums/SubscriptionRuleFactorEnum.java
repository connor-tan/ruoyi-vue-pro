package cn.iocoder.yudao.module.subscription.enums;

public enum SubscriptionRuleFactorEnum {

    STUDENT_SCHOOL("STUDENT_SCHOOL", "学生学校"),
    STUDENT_GRADE("STUDENT_GRADE", "学生年级"),
    OFFER_SKU("OFFER_SKU", "窗口 SKU"),
    SKU_PUBLISHER("SKU_PUBLISHER", "出版社"),
    SKU_PUBLICATION_TYPE("SKU_PUBLICATION_TYPE", "刊物类型"),
    SKU_ISSUE_CYCLE("SKU_ISSUE_CYCLE", "出刊周期");

    private final String code;
    private final String name;

    SubscriptionRuleFactorEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static boolean isValid(String code) {
        for (SubscriptionRuleFactorEnum item : values()) {
            if (item.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static SubscriptionRuleFactorEnum getByCode(String code) {
        for (SubscriptionRuleFactorEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }
}
