package cn.iocoder.yudao.module.subscription.service.visibility;

/**
 * 订刊可见性 Service 接口
 */
public interface SubscriptionVisibilityService {

    SubscriptionVisibilityResultBO calculate(Long userId, Long studentId, Long windowId);

}
