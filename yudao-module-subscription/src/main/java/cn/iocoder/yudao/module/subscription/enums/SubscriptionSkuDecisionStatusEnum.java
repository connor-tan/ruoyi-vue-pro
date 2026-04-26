package cn.iocoder.yudao.module.subscription.enums;

public enum SubscriptionSkuDecisionStatusEnum {

    FINAL("FINAL", "最终可售"),
    EXCLUDED("EXCLUDED", "特殊规则排除"),
    BASE_REJECTED("BASE_REJECTED", "基础范围拒绝");

    private final String status;
    private final String name;

    SubscriptionSkuDecisionStatusEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }
}
