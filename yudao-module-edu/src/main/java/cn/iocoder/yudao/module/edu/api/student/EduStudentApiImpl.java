package cn.iocoder.yudao.module.edu.api.student;

import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentOrderContextRespDTO;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentSubscriptionContextRespDTO;
import cn.iocoder.yudao.module.edu.service.student.StudentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Map;

@Service
@Validated
public class EduStudentApiImpl implements EduStudentApi {

    @Resource
    private StudentService studentService;

    @Override
    public Map<Long, EduStudentOrderContextRespDTO> getOrderStudentContextMap(Long parentUserId,
                                                                              Collection<Long> studentIds) {
        return studentService.getOrderStudentContextMap(parentUserId, studentIds);
    }

    @Override
    public Map<Long, EduStudentSubscriptionContextRespDTO> getSubscriptionStudentContextMap(
            Long parentUserId,
            Collection<Long> studentIds,
            Integer targetYearStart,
            Integer targetYearEnd,
            Long targetYearCatalogId) {
        return studentService.getSubscriptionStudentContextMap(parentUserId, studentIds, targetYearStart, targetYearEnd,
                targetYearCatalogId);
    }

    @Override
    public Map<Long, EduStudentSubscriptionContextRespDTO> getAdminSubscriptionStudentContextMap(
            Collection<Long> studentIds,
            Integer targetYearStart,
            Integer targetYearEnd,
            Long targetYearCatalogId) {
        return studentService.getAdminSubscriptionStudentContextMap(studentIds, targetYearStart, targetYearEnd,
                targetYearCatalogId);
    }

}
