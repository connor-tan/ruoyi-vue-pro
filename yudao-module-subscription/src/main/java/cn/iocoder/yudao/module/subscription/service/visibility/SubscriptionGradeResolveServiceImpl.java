package cn.iocoder.yudao.module.subscription.service.visibility;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import cn.iocoder.yudao.module.edu.dal.mysql.studentclass.StudentClassMapper;
import cn.iocoder.yudao.module.edu.enums.StudentStatusEnum;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionBlockedReasonEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeCalcRuleEnum;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionGradeResolveRespBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Validated
public class SubscriptionGradeResolveServiceImpl implements SubscriptionGradeResolveService {

    @Resource
    private SubscriptionSupportService subscriptionSupportService;
    @Resource
    private StudentClassMapper studentClassMapper;

    @Override
    public SubscriptionGradeResolveRespBO resolve(Long studentId, SubscriptionWindowDO window) {
        StudentDO student = subscriptionSupportService.getStudent(studentId);
        SubscriptionGradeResolveRespBO respBO = new SubscriptionGradeResolveRespBO();
        respBO.setStudentId(student.getId());
        respBO.setStudentName(student.getStudentName());
        respBO.setSchoolId(student.getCurrentSchoolId());

        SchoolDO school = subscriptionSupportService.getSchool(student.getCurrentSchoolId());
        respBO.setSchoolName(school != null ? school.getSchoolName() : null);
        if (school == null) {
            return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
        }
        if (Objects.equals(student.getStatus(), StudentStatusEnum.PENDING_ADVANCE.getStatus())) {
            return block(respBO, SubscriptionBlockedReasonEnum.PENDING_ADVANCE_BIND_REQUIRED);
        }
        if (!Objects.equals(student.getStatus(), StudentStatusEnum.READING.getStatus())) {
            return block(respBO, SubscriptionBlockedReasonEnum.STUDENT_STATUS_UNSUPPORTED);
        }

        List<StudentClassDO> currentClasses = studentClassMapper.selectCurrentListByStudentId(studentId);
        if (currentClasses.size() != 1) {
            return block(respBO, SubscriptionBlockedReasonEnum.NO_CURRENT_CLASS);
        }
        SchoolClassDO schoolClass = subscriptionSupportService.getSchoolClass(currentClasses.get(0).getClassId());
        if (schoolClass == null || !Objects.equals(schoolClass.getSchoolId(), student.getCurrentSchoolId())) {
            return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
        }
        SchoolGradeDO currentSchoolGrade = subscriptionSupportService.getSchoolGrade(schoolClass.getSchoolGradeId());
        if (currentSchoolGrade == null) {
            return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
        }
        GradeCatalogDO currentGradeCatalog = subscriptionSupportService.getGradeCatalog(currentSchoolGrade.getGradeCatalogId());
        if (currentGradeCatalog == null || !CommonStatusEnum.isEnable(currentGradeCatalog.getStatus())) {
            return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
        }
        if (Objects.equals(window.getGradeCalcRule(), SubscriptionGradeCalcRuleEnum.PROMOTED_GRADE.getRule())) {
            return resolvePromotedGrade(respBO, student.getCurrentSchoolId(), currentSchoolGrade);
        }
        return fillGrade(respBO, currentGradeCatalog);
    }

    private SubscriptionGradeResolveRespBO resolvePromotedGrade(SubscriptionGradeResolveRespBO respBO, Long schoolId,
                                                                SchoolGradeDO currentSchoolGrade) {
        List<SchoolGradeDO> schoolGrades = subscriptionSupportService.getSchoolGradeList(schoolId);
        if (CollUtil.isEmpty(schoolGrades)) {
            return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
        }
        Map<Long, GradeCatalogDO> gradeCatalogMap = subscriptionSupportService.getGradeCatalogMap(schoolGrades.stream()
                .map(SchoolGradeDO::getGradeCatalogId)
                .collect(Collectors.toSet()));
        List<SchoolGradeDO> orderedGrades = schoolGrades.stream()
                .filter(grade -> {
                    GradeCatalogDO gradeCatalog = gradeCatalogMap.get(grade.getGradeCatalogId());
                    return gradeCatalog != null && CommonStatusEnum.isEnable(gradeCatalog.getStatus());
                })
                .sorted(Comparator.comparing((SchoolGradeDO grade) -> gradeCatalogMap.get(grade.getGradeCatalogId()).getSort(),
                        Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SchoolGradeDO::getId))
                .toList();
        if (orderedGrades.isEmpty()) {
            return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
        }
        int currentIndex = -1;
        for (int i = 0; i < orderedGrades.size(); i++) {
            if (Objects.equals(orderedGrades.get(i).getId(), currentSchoolGrade.getId())) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex < 0) {
            return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
        }
        if (currentIndex >= orderedGrades.size() - 1) {
            return block(respBO, SubscriptionBlockedReasonEnum.TERMINAL_GRADE_PROMOTION_UNSUPPORTED);
        }
        GradeCatalogDO nextGradeCatalog = gradeCatalogMap.get(orderedGrades.get(currentIndex + 1).getGradeCatalogId());
        if (nextGradeCatalog == null) {
            return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
        }
        return fillGrade(respBO, nextGradeCatalog);
    }

    private SubscriptionGradeResolveRespBO fillGrade(SubscriptionGradeResolveRespBO respBO, GradeCatalogDO gradeCatalog) {
        respBO.setEffectiveGradeCatalogId(gradeCatalog.getId());
        respBO.setEffectiveGradeNo(gradeCatalog.getGradeNo());
        respBO.setEffectiveGradeName(gradeCatalog.getGradeName());
        respBO.setEffectiveGradeAliasName(gradeCatalog.getAliasName());
        return respBO;
    }

    private SubscriptionGradeResolveRespBO block(SubscriptionGradeResolveRespBO respBO,
                                                 SubscriptionBlockedReasonEnum blockedReason) {
        respBO.setBlockedReason(blockedReason.getReason());
        respBO.setBlockedReasonDesc(blockedReason.getDescription());
        return respBO;
    }
}
