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
import cn.iocoder.yudao.module.edu.service.school.SchoolGradeSequenceUtils;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionBlockedReasonEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeCalcRuleEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeResolveModeEnum;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionGradeResolveRespBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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
        List<SubscriptionGradeResolveRespBO> respList = resolveStudentList(Collections.singletonList(student), window);
        return respList.get(0);
    }

    @Override
    public List<SubscriptionGradeResolveRespBO> resolveList(List<StudentDO> students, SubscriptionWindowDO window) {
        return resolveStudentList(students, window);
    }

    private List<SubscriptionGradeResolveRespBO> resolveStudentList(List<StudentDO> students, SubscriptionWindowDO window) {
        if (CollUtil.isEmpty(students)) {
            return Collections.emptyList();
        }
        Set<Long> studentIdSet = students.stream().map(StudentDO::getId).collect(Collectors.toSet());
        Long minStudentId = students.stream().map(StudentDO::getId).filter(Objects::nonNull).min(Long::compareTo).orElse(null);
        Long maxStudentId = students.stream().map(StudentDO::getId).filter(Objects::nonNull).max(Long::compareTo).orElse(null);
        Map<Long, List<StudentClassDO>> targetYearClassMap = buildTargetYearClassMap(studentIdSet, minStudentId, maxStudentId, window);
        Set<Long> currentChainStudentIds = buildCurrentChainStudentIds(students, window, targetYearClassMap);
        Map<Long, List<StudentClassDO>> currentClassMap = buildCurrentClassMap(currentChainStudentIds, studentIdSet,
                minStudentId, maxStudentId);

        Set<Long> schoolClassIds = students.stream()
                .map(StudentDO::getId)
                .flatMap(studentId -> {
                    List<StudentClassDO> currentClasses = currentClassMap.getOrDefault(studentId, Collections.emptyList());
                    List<StudentClassDO> targetClasses = targetYearClassMap.getOrDefault(studentId, Collections.emptyList());
                    return java.util.stream.Stream.concat(currentClasses.stream(), targetClasses.stream());
                })
                .map(StudentClassDO::getClassId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SchoolClassDO> schoolClassMap = subscriptionSupportService.getSchoolClassMap(schoolClassIds);

        Set<Long> currentSchoolIds = students.stream()
                .map(StudentDO::getCurrentSchoolId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, List<SchoolGradeDO>> currentSchoolGradeListMap = subscriptionSupportService.getSchoolGradeListBySchoolIds(currentSchoolIds)
                .stream()
                .collect(Collectors.groupingBy(SchoolGradeDO::getSchoolId));

        Set<Long> schoolIds = currentSchoolIds.stream().collect(Collectors.toSet());
        schoolIds.addAll(schoolClassMap.values().stream()
                .map(SchoolClassDO::getSchoolId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        Map<Long, SchoolDO> schoolMap = subscriptionSupportService.getSchoolMap(schoolIds);

        Set<Long> schoolGradeIds = schoolClassMap.values().stream()
                .map(SchoolClassDO::getSchoolGradeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        schoolGradeIds.addAll(currentSchoolGradeListMap.values().stream()
                .flatMap(Collection::stream)
                .map(SchoolGradeDO::getId)
                .collect(Collectors.toSet()));
        Map<Long, SchoolGradeDO> schoolGradeMap = subscriptionSupportService.getSchoolGradeMap(schoolGradeIds);

        Set<Long> gradeCatalogIds = schoolGradeMap.values().stream()
                .map(SchoolGradeDO::getGradeCatalogId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, GradeCatalogDO> gradeCatalogMap = subscriptionSupportService.getGradeCatalogMap(gradeCatalogIds);
        List<GradeCatalogDO> enabledGradeCatalogs = subscriptionSupportService.getEnabledGradeCatalogList();
        Map<Long, GradeCatalogDO> enabledGradeCatalogMap = enabledGradeCatalogs.stream()
                .collect(Collectors.toMap(GradeCatalogDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, Long> nextGlobalGradeCatalogIdMap =
                SchoolGradeSequenceUtils.buildNextGradeCatalogIdMap(enabledGradeCatalogs);

        Map<Long, List<SchoolGradeDO>> orderedGradesMap = currentSchoolGradeListMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> buildOrderedGrades(entry.getValue(), gradeCatalogMap)));
        Map<Long, Map<Long, SchoolGradeDO>> nextSchoolGradeMap = currentSchoolGradeListMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> SchoolGradeSequenceUtils
                        .buildNextSchoolGradeMap(entry.getValue(), enabledGradeCatalogMap)));

        return students.stream()
                .map(student -> resolveStudent(student, window, currentClassMap.get(student.getId()),
                        targetYearClassMap.get(student.getId()), schoolMap, schoolClassMap, schoolGradeMap,
                        gradeCatalogMap, orderedGradesMap, nextGlobalGradeCatalogIdMap,
                        nextSchoolGradeMap.get(student.getCurrentSchoolId())))
                .toList();
    }

    private Set<Long> buildCurrentChainStudentIds(List<StudentDO> students, SubscriptionWindowDO window,
                                                  Map<Long, List<StudentClassDO>> targetYearClassMap) {
        if (!isTargetClassFirst(window)) {
            return students.stream().map(StudentDO::getId).collect(Collectors.toSet());
        }
        return students.stream()
                .filter(student -> Objects.equals(student.getStatus(), StudentStatusEnum.READING.getStatus()))
                .filter(student -> CollUtil.isEmpty(targetYearClassMap.get(student.getId())))
                .map(StudentDO::getId)
                .collect(Collectors.toSet());
    }

    private Map<Long, List<StudentClassDO>> buildTargetYearClassMap(Set<Long> studentIdSet, Long minStudentId,
                                                                    Long maxStudentId, SubscriptionWindowDO window) {
        if (!isTargetClassFirst(window) || window.getTargetYearStart() == null || window.getTargetYearEnd() == null
                || studentIdSet.isEmpty() || maxStudentId == null) {
            return Collections.emptyMap();
        }
        Long startExclusiveStudentId = minStudentId == null ? 0L : minStudentId - 1;
        return studentClassMapper.selectListByStudentIdRangeAndTargetYear(startExclusiveStudentId, maxStudentId,
                        window.getTargetYearStart(), window.getTargetYearEnd()).stream()
                .filter(item -> studentIdSet.contains(item.getStudentId()))
                .collect(Collectors.groupingBy(StudentClassDO::getStudentId));
    }

    private Map<Long, List<StudentClassDO>> buildCurrentClassMap(Set<Long> currentChainStudentIds, Set<Long> studentIdSet,
                                                                 Long minStudentId, Long maxStudentId) {
        if (currentChainStudentIds.isEmpty() || maxStudentId == null) {
            return Collections.emptyMap();
        }
        if (Objects.equals(currentChainStudentIds, studentIdSet)) {
            Long startExclusiveStudentId = minStudentId == null ? 0L : minStudentId - 1;
            return studentClassMapper.selectCurrentListByStudentIdRange(startExclusiveStudentId, maxStudentId).stream()
                .collect(Collectors.groupingBy(StudentClassDO::getStudentId));
        }
        return studentClassMapper.selectCurrentListByStudentIds(currentChainStudentIds).stream()
                .collect(Collectors.groupingBy(StudentClassDO::getStudentId));
    }

    private SubscriptionGradeResolveRespBO resolveStudent(StudentDO student, SubscriptionWindowDO window,
                                                          List<StudentClassDO> currentClasses,
                                                          List<StudentClassDO> targetYearClasses,
                                                          Map<Long, SchoolDO> schoolMap,
                                                          Map<Long, SchoolClassDO> schoolClassMap,
                                                          Map<Long, SchoolGradeDO> schoolGradeMap,
                                                          Map<Long, GradeCatalogDO> gradeCatalogMap,
                                                          Map<Long, List<SchoolGradeDO>> orderedGradesMap,
                                                          Map<Long, Long> nextGlobalGradeCatalogIdMap,
                                                          Map<Long, SchoolGradeDO> nextSchoolGradeMap) {
        SubscriptionGradeResolveRespBO respBO = new SubscriptionGradeResolveRespBO();
        respBO.setStudentId(student.getId());
        respBO.setStudentName(student.getStudentName());
        SchoolDO currentSchool = schoolMap.get(student.getCurrentSchoolId());
        respBO.setSchoolId(student.getCurrentSchoolId());
        respBO.setSchoolName(currentSchool == null ? null : currentSchool.getSchoolName());
        if (isTargetClassFirst(window)) {
            return resolveTargetClassFirst(respBO, student, currentSchool, currentClasses, targetYearClasses, schoolMap,
                    schoolClassMap, schoolGradeMap, gradeCatalogMap, orderedGradesMap, nextGlobalGradeCatalogIdMap,
                    nextSchoolGradeMap, window);
        }
        return resolveCurrentChain(respBO, student, currentSchool, currentClasses, schoolClassMap, schoolGradeMap,
                gradeCatalogMap, orderedGradesMap, nextGlobalGradeCatalogIdMap, nextSchoolGradeMap, window);
    }

    private SubscriptionGradeResolveRespBO resolveTargetClassFirst(SubscriptionGradeResolveRespBO respBO,
                                                                   StudentDO student,
                                                                   SchoolDO currentSchool,
                                                                   List<StudentClassDO> currentClasses,
                                                                   List<StudentClassDO> targetYearClasses,
                                                                   Map<Long, SchoolDO> schoolMap,
                                                                   Map<Long, SchoolClassDO> schoolClassMap,
                                                                   Map<Long, SchoolGradeDO> schoolGradeMap,
                                                                   Map<Long, GradeCatalogDO> gradeCatalogMap,
                                                                   Map<Long, List<SchoolGradeDO>> orderedGradesMap,
                                                                   Map<Long, Long> nextGlobalGradeCatalogIdMap,
                                                                   Map<Long, SchoolGradeDO> nextSchoolGradeMap,
                                                                   SubscriptionWindowDO window) {
        if (!isAllowedTargetClassStatus(student.getStatus())) {
            return block(respBO, SubscriptionBlockedReasonEnum.STUDENT_STATUS_UNSUPPORTED);
        }
        if (CollUtil.isNotEmpty(targetYearClasses)) {
            if (targetYearClasses.size() != 1) {
                return block(respBO, SubscriptionBlockedReasonEnum.MULTI_FUTURE_CLASS);
            }
            StudentClassDO targetYearClass = targetYearClasses.get(0);
            SchoolClassDO targetSchoolClass = schoolClassMap.get(targetYearClass.getClassId());
            if (targetSchoolClass == null) {
                return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
            }
            SchoolDO targetSchool = schoolMap.get(targetSchoolClass.getSchoolId());
            if (targetSchool == null) {
                return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
            }
            respBO.setSchoolId(targetSchool.getId());
            respBO.setSchoolName(targetSchool.getSchoolName());
            fillClass(respBO, targetSchoolClass);
            SchoolGradeDO targetSchoolGrade = schoolGradeMap.get(targetSchoolClass.getSchoolGradeId());
            if (targetSchoolGrade == null) {
                return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
            }
            GradeCatalogDO targetGradeCatalog = gradeCatalogMap.get(targetSchoolGrade.getGradeCatalogId());
            if (targetGradeCatalog == null || !CommonStatusEnum.isEnable(targetGradeCatalog.getStatus())) {
                return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
            }
            return fillGrade(respBO, targetGradeCatalog);
        }
        if (Objects.equals(student.getStatus(), StudentStatusEnum.PENDING_ADVANCE.getStatus())) {
            return block(respBO, SubscriptionBlockedReasonEnum.FUTURE_CLASS_BIND_REQUIRED);
        }
        return resolveCurrentChain(respBO, student, currentSchool, currentClasses, schoolClassMap, schoolGradeMap,
                gradeCatalogMap, orderedGradesMap, nextGlobalGradeCatalogIdMap, nextSchoolGradeMap, window);
    }

    private SubscriptionGradeResolveRespBO resolveCurrentChain(SubscriptionGradeResolveRespBO respBO,
                                                               StudentDO student,
                                                               SchoolDO currentSchool,
                                                               List<StudentClassDO> currentClasses,
                                                               Map<Long, SchoolClassDO> schoolClassMap,
                                                               Map<Long, SchoolGradeDO> schoolGradeMap,
                                                               Map<Long, GradeCatalogDO> gradeCatalogMap,
                                                               Map<Long, List<SchoolGradeDO>> orderedGradesMap,
                                                               Map<Long, Long> nextGlobalGradeCatalogIdMap,
                                                               Map<Long, SchoolGradeDO> nextSchoolGradeMap,
                                                               SubscriptionWindowDO window) {
        if (currentSchool == null) {
            return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
        }
        if (Objects.equals(student.getStatus(), StudentStatusEnum.PENDING_ADVANCE.getStatus())) {
            return block(respBO, SubscriptionBlockedReasonEnum.PENDING_ADVANCE_BIND_REQUIRED);
        }
        if (!Objects.equals(student.getStatus(), StudentStatusEnum.READING.getStatus())) {
            return block(respBO, SubscriptionBlockedReasonEnum.STUDENT_STATUS_UNSUPPORTED);
        }
        if (CollUtil.isEmpty(currentClasses) || currentClasses.size() != 1) {
            return block(respBO, SubscriptionBlockedReasonEnum.NO_CURRENT_CLASS);
        }
        SchoolClassDO currentSchoolClass = schoolClassMap.get(currentClasses.get(0).getClassId());
        if (currentSchoolClass == null || !Objects.equals(currentSchoolClass.getSchoolId(), currentSchool.getId())) {
            return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
        }
        SchoolGradeDO currentSchoolGrade = schoolGradeMap.get(currentSchoolClass.getSchoolGradeId());
        if (currentSchoolGrade == null) {
            return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
        }
        GradeCatalogDO currentGradeCatalog = gradeCatalogMap.get(currentSchoolGrade.getGradeCatalogId());
        if (currentGradeCatalog == null || !CommonStatusEnum.isEnable(currentGradeCatalog.getStatus())) {
            return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
        }
        respBO.setSchoolId(currentSchool.getId());
        respBO.setSchoolName(currentSchool.getSchoolName());
        fillClass(respBO, currentSchoolClass);
        if (Objects.equals(window.getGradeCalcRule(), SubscriptionGradeCalcRuleEnum.PROMOTED_GRADE.getRule())) {
            return resolvePromotedGrade(respBO, currentSchoolGrade, orderedGradesMap.get(currentSchool.getId()),
                    gradeCatalogMap, nextGlobalGradeCatalogIdMap, nextSchoolGradeMap);
        }
        return fillGrade(respBO, currentGradeCatalog);
    }

    private List<SchoolGradeDO> buildOrderedGrades(List<SchoolGradeDO> schoolGrades,
                                                   Map<Long, GradeCatalogDO> gradeCatalogMap) {
        if (CollUtil.isEmpty(schoolGrades)) {
            return Collections.emptyList();
        }
        return schoolGrades.stream()
                .filter(schoolGrade -> {
                    GradeCatalogDO gradeCatalog = gradeCatalogMap.get(schoolGrade.getGradeCatalogId());
                    return gradeCatalog != null && CommonStatusEnum.isEnable(gradeCatalog.getStatus());
                })
                .sorted(Comparator.comparing(
                                (SchoolGradeDO schoolGrade) -> gradeCatalogMap.get(schoolGrade.getGradeCatalogId()).getSort(),
                                Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SchoolGradeDO::getId))
                .toList();
    }

    private SubscriptionGradeResolveRespBO resolvePromotedGrade(SubscriptionGradeResolveRespBO respBO,
                                                                SchoolGradeDO currentSchoolGrade,
                                                                List<SchoolGradeDO> orderedGrades,
                                                                Map<Long, GradeCatalogDO> gradeCatalogMap,
                                                                Map<Long, Long> nextGlobalGradeCatalogIdMap,
                                                                Map<Long, SchoolGradeDO> nextSchoolGradeMap) {
        if (CollUtil.isEmpty(orderedGrades)) {
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
        Long expectedNextGradeCatalogId = nextGlobalGradeCatalogIdMap.get(currentSchoolGrade.getGradeCatalogId());
        if (expectedNextGradeCatalogId == null) {
            return block(respBO, SubscriptionBlockedReasonEnum.TERMINAL_GRADE_PROMOTION_UNSUPPORTED);
        }
        if (currentIndex >= orderedGrades.size() - 1) {
            return block(respBO, SubscriptionBlockedReasonEnum.NEXT_GRADE_NOT_ENABLED);
        }
        SchoolGradeDO nextSchoolGrade = nextSchoolGradeMap == null ? null : nextSchoolGradeMap.get(currentSchoolGrade.getId());
        if (nextSchoolGrade == null || !Objects.equals(nextSchoolGrade.getGradeCatalogId(), expectedNextGradeCatalogId)) {
            return block(respBO, SubscriptionBlockedReasonEnum.NEXT_GRADE_NOT_ENABLED);
        }
        GradeCatalogDO nextGradeCatalog = gradeCatalogMap.get(expectedNextGradeCatalogId);
        if (nextGradeCatalog == null || !CommonStatusEnum.isEnable(nextGradeCatalog.getStatus())) {
            return block(respBO, SubscriptionBlockedReasonEnum.SCHOOL_GRADE_NOT_EXISTS);
        }
        return fillGrade(respBO, nextGradeCatalog);
    }

    private SubscriptionGradeResolveRespBO fillGrade(SubscriptionGradeResolveRespBO respBO, GradeCatalogDO gradeCatalog) {
        respBO.setEffectiveGradeCatalogId(gradeCatalog.getId());
        respBO.setEffectiveGradeNo(gradeCatalog.getGradeNo());
        respBO.setEffectiveGradeName(gradeCatalog.getGradeName());
        respBO.setEffectiveGradeAliasName(gradeCatalog.getAliasName());
        respBO.setEffectiveGradeSort(gradeCatalog.getSort());
        return respBO;
    }

    private void fillClass(SubscriptionGradeResolveRespBO respBO, SchoolClassDO schoolClass) {
        respBO.setEffectiveClassId(schoolClass.getId());
        respBO.setEffectiveClassName(schoolClass.getClassName());
    }

    private SubscriptionGradeResolveRespBO block(SubscriptionGradeResolveRespBO respBO,
                                                 SubscriptionBlockedReasonEnum blockedReason) {
        respBO.setBlockedReason(blockedReason.getReason());
        respBO.setBlockedReasonDesc(blockedReason.getDescription());
        return respBO;
    }

    private boolean isTargetClassFirst(SubscriptionWindowDO window) {
        return Objects.equals(window.getGradeResolveMode(), SubscriptionGradeResolveModeEnum.TARGET_CLASS_FIRST.getMode());
    }

    private boolean isAllowedTargetClassStatus(Integer status) {
        return Objects.equals(status, StudentStatusEnum.READING.getStatus())
                || Objects.equals(status, StudentStatusEnum.PENDING_ADVANCE.getStatus());
    }
}
