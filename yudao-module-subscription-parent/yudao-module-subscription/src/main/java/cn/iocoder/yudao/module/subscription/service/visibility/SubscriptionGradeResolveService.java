package cn.iocoder.yudao.module.subscription.service.visibility;

import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionGradeResolveRespBO;

import java.util.List;

public interface SubscriptionGradeResolveService {

    SubscriptionGradeResolveRespBO resolve(Long studentId, SubscriptionWindowDO window);

    List<SubscriptionGradeResolveRespBO> resolveList(List<StudentDO> students, SubscriptionWindowDO window);
}
