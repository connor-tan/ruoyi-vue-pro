package cn.iocoder.yudao.module.edu.service.student;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionAdjustmentReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionExecuteReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionExecuteRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionItemRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionPreviewReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionPreviewRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionSummaryRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentFlowDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentPromotionBatchDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolGradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentFlowMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentPromotionBatchMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.studentclass.StudentClassMapper;
import cn.iocoder.yudao.module.edu.service.school.SchoolClassUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_YEAR_NOT_BELONG_TO_SCHOOL;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_YEAR_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PROMOTION_ADJUST_ACTION_INVALID;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PROMOTION_ADJUST_TARGET_CLASS_INVALID;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PROMOTION_ADJUST_TARGET_CLASS_NOT_IN_TARGET_YEAR;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PROMOTION_ADJUST_TARGET_CLASS_REQUIRED;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PROMOTION_NO_ELIGIBLE_STUDENTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PROMOTION_TARGET_SCHOOL_YEAR_INVALID;

/**
 * 学生一键升班 Service 实现类
 */
@Service
@Validated
public class StudentPromotionServiceImpl implements StudentPromotionService {

    private static final Integer STUDENT_STATUS_READING = 1;
    private static final Integer STUDENT_STATUS_GRADUATED = 2;

    private static final Integer BATCH_STATUS_SUCCESS = 1;
    private static final Integer FLOW_STATUS_ACTIVE = 1;
    private static final String FLOW_TYPE_PROMOTE = "PROMOTE";
    private static final String FLOW_TYPE_REPEAT = "REPEAT";
    private static final String FLOW_TYPE_GRADUATE = "GRADUATE";

    private static final String ACTION_PROMOTE = "PROMOTE";
    private static final String ACTION_REPEAT = "REPEAT";
    private static final String ACTION_GRADUATE = "GRADUATE";
    private static final String ACTION_SKIP = "SKIP";

    private static final String REASON_READY = "READY";
    private static final String REASON_TARGET_CLASS_AUTO_CREATE = "TARGET_CLASS_AUTO_CREATE";
    private static final String REASON_TARGET_CLASS_NOT_FOUND = "TARGET_CLASS_NOT_FOUND";
    private static final String REASON_TERMINAL_GRADE_GRADUATE = "TERMINAL_GRADE_GRADUATE";
    private static final String REASON_TERMINAL_GRADE_SKIP = "TERMINAL_GRADE_SKIP";
    private static final String REASON_GRADE_SEQUENCE_GAP = "GRADE_SEQUENCE_GAP";
    private static final String REASON_MULTI_CURRENT_CLASS = "MULTI_CURRENT_CLASS";
    private static final String REASON_STUDENT_NOT_READING = "STUDENT_NOT_READING";
    private static final String REASON_MANUAL_TARGET_CLASS = "MANUAL_TARGET_CLASS";
    private static final String REASON_MANUAL_REPEAT = "MANUAL_REPEAT";

    @Resource
    private StudentMapper studentMapper;
    @Resource
    private StudentClassMapper studentClassMapper;
    @Resource
    private StudentFlowMapper studentFlowMapper;
    @Resource
    private StudentPromotionBatchMapper studentPromotionBatchMapper;
    @Resource
    private SchoolMapper schoolMapper;
    @Resource
    private SchoolClassMapper schoolClassMapper;
    @Resource
    private SchoolGradeMapper schoolGradeMapper;
    @Resource
    private SchoolYearMapper schoolYearMapper;
    @Resource
    private GradeCatalogMapper gradeCatalogMapper;

    @Override
    public StudentPromotionPreviewRespVO previewStudentPromotion(StudentPromotionPreviewReqVO reqVO) {
        PromotionPreviewResult previewResult = buildPreviewResult(reqVO);
        StudentPromotionPreviewRespVO respVO = new StudentPromotionPreviewRespVO();
        respVO.setSummary(buildSummaryResp(previewResult));
        respVO.setItems(buildPreviewItems(previewResult.getCandidates()));
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentPromotionExecuteRespVO executeStudentPromotion(StudentPromotionExecuteReqVO reqVO) {
        return executeStudentPromotion(reqVO, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentPromotionExecuteRespVO executeStudentPromotion(StudentPromotionExecuteReqVO reqVO, Long taskId) {
        PromotionPreviewResult previewResult = buildPreviewResult(reqVO);
        if (!previewResult.hasExecutableCandidates()) {
            throw exception(STUDENT_PROMOTION_NO_ELIGIBLE_STUDENTS);
        }
        StudentPromotionBatchDO batch = StudentPromotionBatchDO.builder()
                .taskId(taskId)
                .schoolId(reqVO.getSchoolId())
                .fromSchoolYearId(reqVO.getFromSchoolYearId())
                .toSchoolYearId(reqVO.getToSchoolYearId())
                .autoCreateClass(reqVO.getAutoCreateClass())
                .graduateTerminalStudent(reqVO.getGraduateTerminalStudent())
                .totalCount(previewResult.getCandidates().size())
                .promotedCount(previewResult.getPromotedCount())
                .repeatCount(previewResult.getRepeatCount())
                .graduatedCount(previewResult.getGraduatedCount())
                .skippedCount(previewResult.getSkippedCount())
                .status(BATCH_STATUS_SUCCESS)
                .remark(reqVO.getRemark())
                .build();
        batch.clean();
        studentPromotionBatchMapper.insert(batch);

        Map<String, SchoolClassDO> ensuredTargetClassMap = new HashMap<>();
        for (PromotionCandidate candidate : previewResult.getCandidates()) {
            if (Objects.equals(candidate.getAction(), ACTION_PROMOTE)) {
                executePromote(reqVO, batch.getId(), previewResult, candidate, ensuredTargetClassMap);
                continue;
            }
            if (Objects.equals(candidate.getAction(), ACTION_REPEAT)) {
                executeRepeat(reqVO, batch.getId(), previewResult, candidate);
                continue;
            }
            if (Objects.equals(candidate.getAction(), ACTION_GRADUATE)) {
                executeGraduate(reqVO, batch.getId(), previewResult, candidate);
            }
        }

        StudentPromotionExecuteRespVO respVO = new StudentPromotionExecuteRespVO();
        respVO.setBatchId(batch.getId());
        respVO.setSummary(buildSummaryResp(previewResult));
        return respVO;
    }

    private void executePromote(StudentPromotionExecuteReqVO reqVO, Long batchId, PromotionPreviewResult previewResult,
                                PromotionCandidate candidate, Map<String, SchoolClassDO> ensuredTargetClassMap) {
        SchoolClassDO targetClass = candidate.getTargetClass();
        boolean targetClassCreated = false;
        if (targetClass == null) {
            String targetClassKey = buildTargetClassKey(candidate.getStudent().getEntryYear(),
                    candidate.getTargetSchoolGrade().getId(), candidate.getCurrentClass().getClassNo());
            targetClass = ensuredTargetClassMap.get(targetClassKey);
            if (targetClass == null) {
                ResolvedTargetClass resolvedTargetClass = createOrGetTargetClass(previewResult.getSchoolId(),
                        previewResult.getToSchoolYear(),
                        candidate.getStudent().getEntryYear(), candidate.getTargetSchoolGrade(),
                        candidate.getTargetGradeCatalog(), candidate.getCurrentClass().getClassNo());
                targetClass = resolvedTargetClass.getSchoolClass();
                targetClassCreated = resolvedTargetClass.getCreated();
                ensuredTargetClassMap.put(targetClassKey, targetClass);
            } else {
                targetClassCreated = false;
            }
        }

        studentClassMapper.updateById(StudentClassDO.builder()
                .id(candidate.getCurrentStudentClass().getId())
                .endDate(previewResult.getFromSchoolYear().getEndDate())
                .build());

        StudentClassDO newStudentClass = StudentClassDO.builder()
                .studentId(candidate.getStudent().getId())
                .classId(targetClass.getId())
                .startDate(previewResult.getToSchoolYear().getStartDate())
                .build();
        newStudentClass.clean();
        studentClassMapper.insert(newStudentClass);

        StudentFlowDO studentFlow = StudentFlowDO.builder()
                .studentId(candidate.getStudent().getId())
                .batchId(batchId)
                .fromClassId(candidate.getCurrentClass().getId())
                .toClassId(targetClass.getId())
                .changeType(FLOW_TYPE_PROMOTE)
                .effectiveDate(previewResult.getToSchoolYear().getStartDate())
                .status(FLOW_STATUS_ACTIVE)
                .targetClassCreated(targetClassCreated)
                .remark(reqVO.getRemark())
                .build();
        studentFlow.clean();
        studentFlowMapper.insert(studentFlow);
    }

    private void executeRepeat(StudentPromotionExecuteReqVO reqVO, Long batchId, PromotionPreviewResult previewResult,
                               PromotionCandidate candidate) {
        SchoolClassDO targetClass = candidate.getTargetClass();
        if (targetClass == null) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_REQUIRED);
        }

        studentClassMapper.updateById(StudentClassDO.builder()
                .id(candidate.getCurrentStudentClass().getId())
                .endDate(previewResult.getFromSchoolYear().getEndDate())
                .build());

        StudentClassDO newStudentClass = StudentClassDO.builder()
                .studentId(candidate.getStudent().getId())
                .classId(targetClass.getId())
                .startDate(previewResult.getToSchoolYear().getStartDate())
                .build();
        newStudentClass.clean();
        studentClassMapper.insert(newStudentClass);

        StudentFlowDO studentFlow = StudentFlowDO.builder()
                .studentId(candidate.getStudent().getId())
                .batchId(batchId)
                .fromClassId(candidate.getCurrentClass().getId())
                .toClassId(targetClass.getId())
                .changeType(FLOW_TYPE_REPEAT)
                .effectiveDate(previewResult.getToSchoolYear().getStartDate())
                .status(FLOW_STATUS_ACTIVE)
                .targetClassCreated(Boolean.FALSE)
                .remark(reqVO.getRemark())
                .build();
        studentFlow.clean();
        studentFlowMapper.insert(studentFlow);
    }

    private void executeGraduate(StudentPromotionExecuteReqVO reqVO, Long batchId,
                                 PromotionPreviewResult previewResult, PromotionCandidate candidate) {
        studentClassMapper.updateById(StudentClassDO.builder()
                .id(candidate.getCurrentStudentClass().getId())
                .endDate(previewResult.getFromSchoolYear().getEndDate())
                .build());
        studentMapper.updateById(StudentDO.builder()
                .id(candidate.getStudent().getId())
                .status(STUDENT_STATUS_GRADUATED)
                .build());

        StudentFlowDO studentFlow = StudentFlowDO.builder()
                .studentId(candidate.getStudent().getId())
                .batchId(batchId)
                .fromClassId(candidate.getCurrentClass().getId())
                .changeType(FLOW_TYPE_GRADUATE)
                .effectiveDate(previewResult.getFromSchoolYear().getEndDate())
                .status(FLOW_STATUS_ACTIVE)
                .targetClassCreated(Boolean.FALSE)
                .remark(reqVO.getRemark())
                .build();
        studentFlow.clean();
        studentFlowMapper.insert(studentFlow);
    }

    private PromotionPreviewResult buildPreviewResult(StudentPromotionPreviewReqVO reqVO) {
        SchoolDO school = validateSchoolExists(reqVO.getSchoolId());
        SchoolYearDO fromSchoolYear = validateSchoolYearExists(reqVO.getFromSchoolYearId());
        validateSchoolYearBelongsToSchool(fromSchoolYear, reqVO.getSchoolId());
        SchoolYearDO toSchoolYear = validateSchoolYearExists(reqVO.getToSchoolYearId());
        validateSchoolYearBelongsToSchool(toSchoolYear, reqVO.getSchoolId());
        if (Objects.equals(fromSchoolYear.getId(), toSchoolYear.getId())
                || !toSchoolYear.getStartDate().isAfter(fromSchoolYear.getEndDate())) {
            throw exception(STUDENT_PROMOTION_TARGET_SCHOOL_YEAR_INVALID);
        }

        List<SchoolClassDO> sourceClasses = schoolClassMapper.selectListBySchoolIdAndSchoolYearId(
                reqVO.getSchoolId(), reqVO.getFromSchoolYearId());
        Map<Long, StudentPromotionAdjustmentReqVO> adjustmentMap = buildAdjustmentMap(reqVO.getAdjustments());
        Map<Long, SchoolClassDO> adjustmentTargetClassMap = adjustmentMap.isEmpty()
                ? Collections.emptyMap()
                : schoolClassMapper.selectList(SchoolClassDO::getId,
                        convertSet(adjustmentMap.values(), StudentPromotionAdjustmentReqVO::getTargetClassId)).stream()
                .collect(Collectors.toMap(SchoolClassDO::getId, Function.identity(), (item1, item2) -> item1));
        List<PromotionCandidate> candidates = buildPromotionCandidates(reqVO, sourceClasses, toSchoolYear,
                adjustmentMap, adjustmentTargetClassMap);
        return new PromotionPreviewResult(school.getId(), fromSchoolYear, toSchoolYear, candidates);
    }

    private List<PromotionCandidate> buildPromotionCandidates(StudentPromotionPreviewReqVO reqVO,
                                                             List<SchoolClassDO> sourceClasses,
                                                             SchoolYearDO toSchoolYear,
                                                             Map<Long, StudentPromotionAdjustmentReqVO> adjustmentMap,
                                                             Map<Long, SchoolClassDO> adjustmentTargetClassMap) {
        if (CollUtil.isEmpty(sourceClasses)) {
            return Collections.emptyList();
        }
        Set<Long> sourceClassIds = convertSet(sourceClasses, SchoolClassDO::getId);
        List<StudentClassDO> currentStudentClasses = studentClassMapper.selectCurrentListByClassIds(sourceClassIds);
        if (CollUtil.isEmpty(currentStudentClasses)) {
            return Collections.emptyList();
        }

        Map<Long, SchoolClassDO> sourceClassMap = sourceClasses.stream()
                .collect(Collectors.toMap(SchoolClassDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, StudentDO> studentMap = studentMapper.selectList(StudentDO::getId,
                        convertSet(currentStudentClasses, StudentClassDO::getStudentId)).stream()
                .collect(Collectors.toMap(StudentDO::getId, Function.identity(), (item1, item2) -> item1));
        List<SchoolGradeDO> schoolGrades = schoolGradeMapper.selectListBySchoolId(reqVO.getSchoolId());
        Map<Long, SchoolGradeDO> schoolGradeMap = schoolGrades.stream()
                .collect(Collectors.toMap(SchoolGradeDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, GradeCatalogDO> gradeCatalogMap = gradeCatalogMapper
                .selectList(GradeCatalogDO::getId, convertSet(schoolGrades, SchoolGradeDO::getGradeCatalogId)).stream()
                .collect(Collectors.toMap(GradeCatalogDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, Long> nextGlobalGradeCatalogIdMap = buildNextGradeCatalogIdMap(
                gradeCatalogMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()));
        Map<Long, SchoolGradeDO> nextSchoolGradeMap = buildNextSchoolGradeMap(schoolGrades, gradeCatalogMap);
        Map<String, SchoolClassDO> targetClassMap = schoolClassMapper.selectListBySchoolIdAndSchoolYearId(
                        reqVO.getSchoolId(), toSchoolYear.getId()).stream()
                .collect(Collectors.toMap(
                        schoolClass -> buildTargetClassKey(schoolClass.getEntryYear(), schoolClass.getSchoolGradeId(), schoolClass.getClassNo()),
                        Function.identity(), (item1, item2) -> item1, LinkedHashMap::new));

        List<PromotionCandidate> candidates = new ArrayList<>();
        Map<Long, List<StudentClassDO>> studentClassMap = currentStudentClasses.stream()
                .collect(Collectors.groupingBy(StudentClassDO::getStudentId));
        for (Map.Entry<Long, List<StudentClassDO>> entry : studentClassMap.entrySet()) {
            StudentDO student = studentMap.get(entry.getKey());
            List<StudentClassDO> studentClasses = entry.getValue();
            StudentClassDO currentStudentClass = studentClasses.get(0);
            SchoolClassDO currentClass = sourceClassMap.get(currentStudentClass.getClassId());
            if (student == null || currentClass == null) {
                continue;
            }
            PromotionCandidate candidate = buildPromotionCandidate(reqVO, student, studentClasses, currentStudentClass,
                    currentClass, schoolGradeMap, gradeCatalogMap, nextGlobalGradeCatalogIdMap, nextSchoolGradeMap,
                    targetClassMap, toSchoolYear, adjustmentMap.get(student.getId()), adjustmentTargetClassMap);
            candidates.add(candidate);
        }
        candidates.sort(Comparator
                .comparing((PromotionCandidate item) -> item.getCurrentClass().getEntryYear(), Comparator.nullsLast(Integer::compareTo))
                .thenComparing(item -> item.getCurrentClass().getClassNo(), Comparator.nullsLast(Integer::compareTo))
                .thenComparing(item -> item.getStudent().getStudentName(), Comparator.nullsLast(String::compareTo))
                .thenComparing(item -> item.getStudent().getId(), Comparator.nullsLast(Long::compareTo)));
        return candidates;
    }

    private PromotionCandidate buildPromotionCandidate(StudentPromotionPreviewReqVO reqVO, StudentDO student,
                                                       List<StudentClassDO> studentClasses, StudentClassDO currentStudentClass,
                                                       SchoolClassDO currentClass, Map<Long, SchoolGradeDO> schoolGradeMap,
                                                       Map<Long, GradeCatalogDO> gradeCatalogMap,
                                                       Map<Long, Long> nextGlobalGradeCatalogIdMap,
                                                       Map<Long, SchoolGradeDO> nextSchoolGradeMap,
                                                       Map<String, SchoolClassDO> targetClassMap,
                                                       SchoolYearDO toSchoolYear,
                                                       StudentPromotionAdjustmentReqVO adjustment,
                                                       Map<Long, SchoolClassDO> adjustmentTargetClassMap) {
        PromotionCandidate candidate = new PromotionCandidate();
        candidate.setStudent(student);
        candidate.setCurrentStudentClass(currentStudentClass);
        candidate.setCurrentClass(currentClass);

        SchoolGradeDO currentSchoolGrade = schoolGradeMap.get(currentClass.getSchoolGradeId());
        GradeCatalogDO currentGradeCatalog = currentSchoolGrade == null ? null : gradeCatalogMap.get(currentSchoolGrade.getGradeCatalogId());
        candidate.setCurrentSchoolGrade(currentSchoolGrade);
        candidate.setCurrentGradeCatalog(currentGradeCatalog);

        if (studentClasses.size() > 1) {
            candidate.setAction(ACTION_SKIP);
            candidate.setReason(REASON_MULTI_CURRENT_CLASS);
            applyAdjustment(candidate, adjustment, adjustmentTargetClassMap, schoolGradeMap, gradeCatalogMap, toSchoolYear);
            return candidate;
        }
        if (!Objects.equals(student.getStatus(), STUDENT_STATUS_READING)) {
            candidate.setAction(ACTION_SKIP);
            candidate.setReason(REASON_STUDENT_NOT_READING);
            applyAdjustment(candidate, adjustment, adjustmentTargetClassMap, schoolGradeMap, gradeCatalogMap, toSchoolYear);
            return candidate;
        }
        if (currentSchoolGrade == null || currentGradeCatalog == null
                || !nextGlobalGradeCatalogIdMap.containsKey(currentGradeCatalog.getId())) {
            candidate.setAction(ACTION_SKIP);
            candidate.setReason(REASON_GRADE_SEQUENCE_GAP);
            applyAdjustment(candidate, adjustment, adjustmentTargetClassMap, schoolGradeMap, gradeCatalogMap, toSchoolYear);
            return candidate;
        }

        SchoolGradeDO targetSchoolGrade = nextSchoolGradeMap.get(currentSchoolGrade.getId());
        if (targetSchoolGrade == null) {
            if (Boolean.TRUE.equals(reqVO.getGraduateTerminalStudent())) {
                candidate.setAction(ACTION_GRADUATE);
                candidate.setReason(REASON_TERMINAL_GRADE_GRADUATE);
            } else {
                candidate.setAction(ACTION_SKIP);
                candidate.setReason(REASON_TERMINAL_GRADE_SKIP);
            }
            applyAdjustment(candidate, adjustment, adjustmentTargetClassMap, schoolGradeMap, gradeCatalogMap, toSchoolYear);
            return candidate;
        }

        Long expectedNextGradeCatalogId = nextGlobalGradeCatalogIdMap.get(currentGradeCatalog.getId());
        if (!Objects.equals(targetSchoolGrade.getGradeCatalogId(), expectedNextGradeCatalogId)) {
            candidate.setAction(ACTION_SKIP);
            candidate.setReason(REASON_GRADE_SEQUENCE_GAP);
            applyAdjustment(candidate, adjustment, adjustmentTargetClassMap, schoolGradeMap, gradeCatalogMap, toSchoolYear);
            return candidate;
        }
        GradeCatalogDO targetGradeCatalog = gradeCatalogMap.get(targetSchoolGrade.getGradeCatalogId());
        if (targetGradeCatalog == null) {
            candidate.setAction(ACTION_SKIP);
            candidate.setReason(REASON_GRADE_SEQUENCE_GAP);
            applyAdjustment(candidate, adjustment, adjustmentTargetClassMap, schoolGradeMap, gradeCatalogMap, toSchoolYear);
            return candidate;
        }
        candidate.setTargetSchoolGrade(targetSchoolGrade);
        candidate.setTargetGradeCatalog(targetGradeCatalog);

        String targetClassKey = buildTargetClassKey(student.getEntryYear(), targetSchoolGrade.getId(), currentClass.getClassNo());
        SchoolClassDO targetClass = targetClassMap.get(targetClassKey);
        candidate.setTargetClass(targetClass);
        if (targetClass == null) {
            candidate.setTargetClassMissing(true);
            candidate.setPredictedTargetClassName(SchoolClassUtils.buildClassName(student.getEntryYear(),
                    targetGradeCatalog.getGradeName(), currentClass.getClassNo()));
            if (Boolean.TRUE.equals(reqVO.getAutoCreateClass())) {
                candidate.setAction(ACTION_PROMOTE);
                candidate.setReason(REASON_TARGET_CLASS_AUTO_CREATE);
            } else {
                candidate.setAction(ACTION_SKIP);
                candidate.setReason(REASON_TARGET_CLASS_NOT_FOUND);
            }
            applyAdjustment(candidate, adjustment, adjustmentTargetClassMap, schoolGradeMap, gradeCatalogMap, toSchoolYear);
            return candidate;
        }

        candidate.setAction(ACTION_PROMOTE);
        candidate.setReason(REASON_READY);
        applyAdjustment(candidate, adjustment, adjustmentTargetClassMap, schoolGradeMap, gradeCatalogMap, toSchoolYear);
        return candidate;
    }

    private Map<Long, StudentPromotionAdjustmentReqVO> buildAdjustmentMap(List<StudentPromotionAdjustmentReqVO> adjustments) {
        if (CollUtil.isEmpty(adjustments)) {
            return Collections.emptyMap();
        }
        Map<Long, StudentPromotionAdjustmentReqVO> adjustmentMap = new LinkedHashMap<>();
        adjustments.stream()
                .filter(item -> item.getStudentId() != null)
                .forEach(item -> adjustmentMap.put(item.getStudentId(), item));
        return adjustmentMap;
    }

    private void applyAdjustment(PromotionCandidate candidate, StudentPromotionAdjustmentReqVO adjustment,
                                 Map<Long, SchoolClassDO> adjustmentTargetClassMap,
                                 Map<Long, SchoolGradeDO> schoolGradeMap,
                                 Map<Long, GradeCatalogDO> gradeCatalogMap,
                                 SchoolYearDO toSchoolYear) {
        if (adjustment == null || !isAdjustmentApplicable(candidate)) {
            return;
        }
        String action = adjustment.getAction();
        if (!Objects.equals(action, ACTION_PROMOTE) && !Objects.equals(action, ACTION_REPEAT)) {
            throw exception(STUDENT_PROMOTION_ADJUST_ACTION_INVALID);
        }
        if (adjustment.getTargetClassId() == null) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_REQUIRED);
        }
        SchoolClassDO targetClass = adjustmentTargetClassMap.get(adjustment.getTargetClassId());
        if (targetClass == null) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_INVALID);
        }
        if (!Objects.equals(targetClass.getSchoolId(), candidate.getCurrentClass().getSchoolId())) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_INVALID);
        }
        if (!Objects.equals(targetClass.getSchoolYearId(), toSchoolYear.getId())) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_NOT_IN_TARGET_YEAR);
        }
        SchoolGradeDO targetSchoolGrade = schoolGradeMap.get(targetClass.getSchoolGradeId());
        GradeCatalogDO targetGradeCatalog = targetSchoolGrade == null ? null
                : gradeCatalogMap.get(targetSchoolGrade.getGradeCatalogId());
        if (targetSchoolGrade == null || targetGradeCatalog == null) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_INVALID);
        }

        if (Objects.equals(action, ACTION_PROMOTE)) {
            if (candidate.getTargetSchoolGrade() == null
                    || !Objects.equals(targetClass.getSchoolGradeId(), candidate.getTargetSchoolGrade().getId())) {
                throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_INVALID);
            }
            candidate.setAction(ACTION_PROMOTE);
            candidate.setReason(REASON_MANUAL_TARGET_CLASS);
            candidate.setTargetClass(targetClass);
            candidate.setTargetSchoolGrade(targetSchoolGrade);
            candidate.setTargetGradeCatalog(targetGradeCatalog);
            candidate.setTargetClassMissing(false);
            candidate.setPredictedTargetClassName(null);
            return;
        }

        if (candidate.getCurrentSchoolGrade() == null
                || !Objects.equals(targetClass.getSchoolGradeId(), candidate.getCurrentSchoolGrade().getId())) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_INVALID);
        }
        candidate.setAction(ACTION_REPEAT);
        candidate.setReason(REASON_MANUAL_REPEAT);
        candidate.setTargetClass(targetClass);
        candidate.setTargetSchoolGrade(candidate.getCurrentSchoolGrade());
        candidate.setTargetGradeCatalog(candidate.getCurrentGradeCatalog());
        candidate.setTargetClassMissing(false);
        candidate.setPredictedTargetClassName(null);
    }

    private boolean isAdjustmentApplicable(PromotionCandidate candidate) {
        return !Objects.equals(candidate.getReason(), REASON_MULTI_CURRENT_CLASS)
                && !Objects.equals(candidate.getReason(), REASON_STUDENT_NOT_READING);
    }

    private Map<Long, Long> buildNextGradeCatalogIdMap(List<GradeCatalogDO> enabledGradeCatalogs) {
        Map<Long, Long> nextMap = new HashMap<>();
        for (int i = 0; i < enabledGradeCatalogs.size(); i++) {
            GradeCatalogDO current = enabledGradeCatalogs.get(i);
            GradeCatalogDO next = i + 1 < enabledGradeCatalogs.size() ? enabledGradeCatalogs.get(i + 1) : null;
            nextMap.put(current.getId(), next == null ? null : next.getId());
        }
        return nextMap;
    }

    private Map<Long, SchoolGradeDO> buildNextSchoolGradeMap(List<SchoolGradeDO> schoolGrades,
                                                             Map<Long, GradeCatalogDO> gradeCatalogMap) {
        List<SchoolGradeDO> sortedSchoolGrades = schoolGrades.stream()
                .filter(schoolGrade -> gradeCatalogMap.containsKey(schoolGrade.getGradeCatalogId()))
                .sorted(Comparator
                        .comparing((SchoolGradeDO item) -> gradeCatalogMap.get(item.getGradeCatalogId()).getSort())
                        .thenComparing(SchoolGradeDO::getId))
                .collect(Collectors.toList());
        Map<Long, SchoolGradeDO> nextMap = new HashMap<>();
        for (int i = 0; i < sortedSchoolGrades.size(); i++) {
            SchoolGradeDO current = sortedSchoolGrades.get(i);
            SchoolGradeDO next = i + 1 < sortedSchoolGrades.size() ? sortedSchoolGrades.get(i + 1) : null;
            nextMap.put(current.getId(), next);
        }
        return nextMap;
    }

    private List<StudentPromotionItemRespVO> buildPreviewItems(List<PromotionCandidate> candidates) {
        return candidates.stream().map(candidate -> {
            StudentPromotionItemRespVO itemRespVO = new StudentPromotionItemRespVO();
            itemRespVO.setStudentId(candidate.getStudent().getId());
            itemRespVO.setStudentName(candidate.getStudent().getStudentName());
            itemRespVO.setEntryYear(candidate.getStudent().getEntryYear());
            itemRespVO.setFromClassId(candidate.getCurrentClass().getId());
            itemRespVO.setFromSchoolGradeId(candidate.getCurrentSchoolGrade() == null ? null : candidate.getCurrentSchoolGrade().getId());
            itemRespVO.setFromClassName(candidate.getCurrentClass().getClassName());
            itemRespVO.setFromGradeName(candidate.getCurrentGradeCatalog() == null ? null : candidate.getCurrentGradeCatalog().getGradeName());
            itemRespVO.setToClassId(candidate.getTargetClass() == null ? null : candidate.getTargetClass().getId());
            itemRespVO.setToSchoolGradeId(candidate.getTargetSchoolGrade() == null ? null : candidate.getTargetSchoolGrade().getId());
            itemRespVO.setToClassName(candidate.getTargetClass() == null ? candidate.getPredictedTargetClassName() : candidate.getTargetClass().getClassName());
            itemRespVO.setToGradeName(candidate.getTargetGradeCatalog() == null ? null : candidate.getTargetGradeCatalog().getGradeName());
            itemRespVO.setTargetClassMissing(Boolean.TRUE.equals(candidate.getTargetClassMissing()));
            itemRespVO.setAction(candidate.getAction());
            itemRespVO.setReason(candidate.getReason());
            return itemRespVO;
        }).collect(Collectors.toList());
    }

    private StudentPromotionSummaryRespVO buildSummaryResp(PromotionPreviewResult previewResult) {
        StudentPromotionSummaryRespVO summaryRespVO = new StudentPromotionSummaryRespVO();
        summaryRespVO.setTotalCount(previewResult.getCandidates().size());
        summaryRespVO.setPromotedCount(previewResult.getPromotedCount());
        summaryRespVO.setGraduatedCount(previewResult.getGraduatedCount());
        summaryRespVO.setRepeatCount(previewResult.getRepeatCount());
        summaryRespVO.setSkippedCount(previewResult.getSkippedCount());
        summaryRespVO.setMissingTargetClassCount(previewResult.getMissingTargetClassCount());
        return summaryRespVO;
    }

    private ResolvedTargetClass createOrGetTargetClass(Long schoolId, SchoolYearDO toSchoolYear, Integer entryYear,
                                                       SchoolGradeDO targetSchoolGrade, GradeCatalogDO targetGradeCatalog,
                                                       Integer classNo) {
        SchoolClassDO existedClass = schoolClassMapper.selectByUniqueKey(schoolId, entryYear, toSchoolYear.getId(),
                targetSchoolGrade.getId(), classNo);
        if (existedClass != null) {
            return new ResolvedTargetClass(existedClass, false);
        }
        SchoolClassDO schoolClass = SchoolClassDO.builder()
                .schoolId(schoolId)
                .entryYear(entryYear)
                .schoolGradeId(targetSchoolGrade.getId())
                .schoolYearId(toSchoolYear.getId())
                .classNo(classNo)
                .className(SchoolClassUtils.buildClassName(entryYear, targetGradeCatalog.getGradeName(), classNo))
                .build();
        schoolClass.clean();
        schoolClassMapper.insert(schoolClass);
        return new ResolvedTargetClass(schoolClass, true);
    }

    private String buildTargetClassKey(Integer entryYear, Long schoolGradeId, Integer classNo) {
        return entryYear + "_" + schoolGradeId + "_" + classNo;
    }

    private SchoolDO validateSchoolExists(Long schoolId) {
        SchoolDO school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw exception(SCHOOL_NOT_EXISTS);
        }
        return school;
    }

    private SchoolYearDO validateSchoolYearExists(Long schoolYearId) {
        SchoolYearDO schoolYear = schoolYearMapper.selectById(schoolYearId);
        if (schoolYear == null) {
            throw exception(SCHOOL_YEAR_NOT_EXISTS);
        }
        return schoolYear;
    }

    private void validateSchoolYearBelongsToSchool(SchoolYearDO schoolYear, Long schoolId) {
        if (!Objects.equals(schoolYear.getSchoolId(), schoolId)) {
            throw exception(SCHOOL_YEAR_NOT_BELONG_TO_SCHOOL);
        }
    }

    private static class PromotionPreviewResult {

        private final Long schoolId;
        private final SchoolYearDO fromSchoolYear;
        private final SchoolYearDO toSchoolYear;
        private final List<PromotionCandidate> candidates;

        private PromotionPreviewResult(Long schoolId, SchoolYearDO fromSchoolYear, SchoolYearDO toSchoolYear,
                                       List<PromotionCandidate> candidates) {
            this.schoolId = schoolId;
            this.fromSchoolYear = fromSchoolYear;
            this.toSchoolYear = toSchoolYear;
            this.candidates = candidates;
        }

        public Long getSchoolId() {
            return schoolId;
        }

        public SchoolYearDO getFromSchoolYear() {
            return fromSchoolYear;
        }

        public SchoolYearDO getToSchoolYear() {
            return toSchoolYear;
        }

        public List<PromotionCandidate> getCandidates() {
            return candidates;
        }

        public Integer getPromotedCount() {
            return Math.toIntExact(candidates.stream().filter(item -> Objects.equals(item.getAction(), ACTION_PROMOTE)).count());
        }

        public Integer getGraduatedCount() {
            return Math.toIntExact(candidates.stream().filter(item -> Objects.equals(item.getAction(), ACTION_GRADUATE)).count());
        }

        public Integer getRepeatCount() {
            return Math.toIntExact(candidates.stream().filter(item -> Objects.equals(item.getAction(), ACTION_REPEAT)).count());
        }

        public Integer getSkippedCount() {
            return Math.toIntExact(candidates.stream().filter(item -> Objects.equals(item.getAction(), ACTION_SKIP)).count());
        }

        public Integer getMissingTargetClassCount() {
            return Math.toIntExact(candidates.stream().filter(item -> Boolean.TRUE.equals(item.getTargetClassMissing())).count());
        }

        public boolean hasExecutableCandidates() {
            return getPromotedCount() > 0 || getGraduatedCount() > 0 || getRepeatCount() > 0;
        }
    }

    private static class ResolvedTargetClass {

        private final SchoolClassDO schoolClass;
        private final boolean created;

        private ResolvedTargetClass(SchoolClassDO schoolClass, boolean created) {
            this.schoolClass = schoolClass;
            this.created = created;
        }

        public SchoolClassDO getSchoolClass() {
            return schoolClass;
        }

        public boolean getCreated() {
            return created;
        }
    }

    private static class PromotionCandidate {

        private StudentDO student;
        private StudentClassDO currentStudentClass;
        private SchoolClassDO currentClass;
        private SchoolGradeDO currentSchoolGrade;
        private GradeCatalogDO currentGradeCatalog;
        private SchoolGradeDO targetSchoolGrade;
        private GradeCatalogDO targetGradeCatalog;
        private SchoolClassDO targetClass;
        private Boolean targetClassMissing;
        private String predictedTargetClassName;
        private String action;
        private String reason;

        public StudentDO getStudent() {
            return student;
        }

        public void setStudent(StudentDO student) {
            this.student = student;
        }

        public StudentClassDO getCurrentStudentClass() {
            return currentStudentClass;
        }

        public void setCurrentStudentClass(StudentClassDO currentStudentClass) {
            this.currentStudentClass = currentStudentClass;
        }

        public SchoolClassDO getCurrentClass() {
            return currentClass;
        }

        public void setCurrentClass(SchoolClassDO currentClass) {
            this.currentClass = currentClass;
        }

        public SchoolGradeDO getCurrentSchoolGrade() {
            return currentSchoolGrade;
        }

        public void setCurrentSchoolGrade(SchoolGradeDO currentSchoolGrade) {
            this.currentSchoolGrade = currentSchoolGrade;
        }

        public GradeCatalogDO getCurrentGradeCatalog() {
            return currentGradeCatalog;
        }

        public void setCurrentGradeCatalog(GradeCatalogDO currentGradeCatalog) {
            this.currentGradeCatalog = currentGradeCatalog;
        }

        public SchoolGradeDO getTargetSchoolGrade() {
            return targetSchoolGrade;
        }

        public void setTargetSchoolGrade(SchoolGradeDO targetSchoolGrade) {
            this.targetSchoolGrade = targetSchoolGrade;
        }

        public GradeCatalogDO getTargetGradeCatalog() {
            return targetGradeCatalog;
        }

        public void setTargetGradeCatalog(GradeCatalogDO targetGradeCatalog) {
            this.targetGradeCatalog = targetGradeCatalog;
        }

        public SchoolClassDO getTargetClass() {
            return targetClass;
        }

        public void setTargetClass(SchoolClassDO targetClass) {
            this.targetClass = targetClass;
        }

        public Boolean getTargetClassMissing() {
            return targetClassMissing;
        }

        public void setTargetClassMissing(Boolean targetClassMissing) {
            this.targetClassMissing = targetClassMissing;
        }

        public String getPredictedTargetClassName() {
            return predictedTargetClassName;
        }

        public void setPredictedTargetClassName(String predictedTargetClassName) {
            this.predictedTargetClassName = predictedTargetClassName;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

}
