package cn.iocoder.yudao.module.subscription.enums;

public enum SubscriptionRuleScopeEnum {

    WINDOW,
    OFFER;

    public static boolean isWindow(String scope) {
        return WINDOW.name().equals(scope);
    }

    public static boolean isOffer(String scope) {
        return OFFER.name().equals(scope);
    }

    public static boolean isValid(String scope) {
        return isWindow(scope) || isOffer(scope);
    }
}
