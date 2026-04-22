package cn.iocoder.yudao.module.subscription.service.window;

import cn.iocoder.yudao.module.edu.service.school.YearCatalogUsageChecker;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionYearCatalogUsageChecker implements YearCatalogUsageChecker {

    @Resource
    private SubscriptionWindowMapper subscriptionWindowMapper;

    @Override
    public long countUsage(Long yearCatalogId) {
        return subscriptionWindowMapper.countByTargetYearCatalogId(yearCatalogId);
    }
}
