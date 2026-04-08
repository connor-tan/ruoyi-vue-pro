package cn.iocoder.yudao.module.subscription.service.visibility;

import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionGradeResolveRespBO;

public interface SubscriptionGradeResolveService {

    SubscriptionGradeResolveRespBO resolve(Long studentId, SubscriptionWindowDO window);
}
