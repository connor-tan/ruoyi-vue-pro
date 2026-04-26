package cn.iocoder.yudao.module.subscription.enums;

public enum SubscriptionVisibilityReasonEnum {

    BASE_MATCH("BASE_MATCH", "命中基础 SKU 事实"),
    BASE_REJECTED("BASE_REJECTED", "未命中基础可见范围"),
    INCLUDE_RULE_MATCH("INCLUDE_RULE_MATCH", "命中特殊允许规则"),
    EXCLUDE_RULE_MATCH("EXCLUDE_RULE_MATCH", "命中特殊排除规则"),
    NO_AVAILABLE_SKU("NO_AVAILABLE_SKU", "没有最终可售 SKU"),
    STUDENT_BLOCKED("STUDENT_BLOCKED", "学生上下文不可订"),
    WINDOW_NOT_OPEN("WINDOW_NOT_OPEN", "订刊窗口未开放"),
    PRODUCT_NOT_ENABLED("PRODUCT_NOT_ENABLED", "商品或窗口刊物未启用");

    private final String reason;
    private final String description;

    SubscriptionVisibilityReasonEnum(String reason, String description) {
        this.reason = reason;
        this.description = description;
    }

    public String getReason() {
        return reason;
    }

    public String getDescription() {
        return description;
    }
}
