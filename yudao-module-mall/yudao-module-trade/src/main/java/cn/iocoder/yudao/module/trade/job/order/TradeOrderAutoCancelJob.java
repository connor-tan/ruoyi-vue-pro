package cn.iocoder.yudao.module.trade.job.order;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderLifecycleService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 交易订单的自动过期 Job
 *
 * @author 芋道源码
 */
@Component
public class TradeOrderAutoCancelJob implements JobHandler {

    @Resource
    private TradeOrderLifecycleService tradeOrderLifecycleService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = tradeOrderLifecycleService.cancelOrderBySystem();
        return String.format("过期订单 %s 个", count);
    }

}
