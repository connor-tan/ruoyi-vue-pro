package cn.iocoder.yudao.module.edu.api.student;

import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentOrderContextRespDTO;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentSubscriptionContextRespDTO;

import java.util.Collection;
import java.util.Map;

public interface EduStudentApi {

    Map<Long, EduStudentOrderContextRespDTO> getOrderStudentContextMap(Long parentUserId, Collection<Long> studentIds);

    Map<Long, EduStudentSubscriptionContextRespDTO> getSubscriptionStudentContextMap(
            Long parentUserId,
            Collection<Long> studentIds,
            Integer targetYearStart,
            Integer targetYearEnd,
            Long targetYearCatalogId,
            String gradeCalcRule,
            String gradeResolveMode);

}
