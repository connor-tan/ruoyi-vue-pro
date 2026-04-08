package cn.iocoder.yudao.module.edu.service.student;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.dynamic.datasource.annotation.Master;
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
import cn.iocoder.yudao.module.edu.enums.StudentStatusEnum;
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

    private static final int BATCH_WRITE_SIZE = 500;

    private static final Integer STUDENT_STATUS_READING = StudentStatusEnum.READING.getStatus();
    private static final Integer STUDENT_STATUS_PENDING_ADVANCE = StudentStatusEnum.PENDING_ADVANCE.getStatus();

    private static final Integer BATCH_STATUS_SUCCESS = 1;
    private static final Integer FLOW_STATUS_ACTIVE = 1;
    private static final String FLOW_TYPE_PROMOTE = "PROMOTE";
    private static final String FLOW_TYPE_REPEAT = "REPEAT";
    private static final String FLOW_TYPE_PENDING_ADVANCE = "PENDING_ADVANCE";

    private static final String ACTION_PROMOTE = "PROMOTE";
    private static final String ACTION_REPEAT = "REPEAT";
    private static final String ACTION_PENDING_ADVANCE = "PENDING_ADVANCE";
    private static final String ACTION_SKIP = "SKIP";

    private static final String REASON_READY = "READY";
    private static final String REASON_TARGET_CLASS_AUTO_CREATE = "TARGET_CLASS_AUTO_CREATE";
    private static final String REASON_TARGET_CLASS_NOT_FOUND = "TARGET_CLASS_NOT_FOUND";
    private static final String REASON_TERMINAL_GRADE_PENDING_ADVANCE = "TERMINAL_GRADE_PENDING_ADVANCE";
    private static final String REASON_TERMINAL_GRADE_SKIP = "TERMINAL_GRADE_SKIP";
    private static final String REASON_GRADE_SEQUENCE_GAP = "GRADE_SEQUENCE_GAP";
    private static final String REASON_MULTI_CURRENT_CLASS = "MULTI_CURRENT_CLASS";
    private static final String REASON_STUDENT_NOT_READING = "STUDENT_NOT_READING";
    private static final String REASON_MANUAL_TARGET_CLASS = "MANUAL_TARGET_CLASS";
    private static final String REASON_MANUAL_REPEAT = "MANUAL_REPEAT";

    private final List<PromotionCandidateRule> promotionCandidateRules = List.of(
            this::applyMultiCurrentClassRule,
            this::applyStudentReadingRule,
            this::applyCurrentGradeSequenceRule,
            this::applyTargetGradeResolutionRule,
            this::applyTargetClassResolutionRule
    );

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
    @Master
    @Transactional(rollbackFor = Exception.class)
    public StudentPromotionExecuteRespVO executeStudentPromotion(StudentPromotionExecuteReqVO reqVO) {
        return executeStudentPromotion(reqVO, null);
    }

    @Override
    @Master
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
                .graduatedCount(previewResult.getPendingAdvanceCount())
                .skippedCount(previewResult.getSkippedCount())
                .status(BATCH_STATUS_SUCCESS)
                .remark(reqVO.getRemark())
                .build();
        batch.clean();
        studentPromotionBatchMapper.insert(batch);

        Map<String, SchoolClassDO> ensuredTargetClassMap = new HashMap<>();
        PromotionExecutionPlan executionPlan = new PromotionExecutionPlan();
        for (PromotionCandidate candidate : previewResult.getCandidates()) {
            if (Objects.equals(candidate.getAction(), ACTION_PROMOTE)) {
                executePromote(reqVO, batch.getId(), previewResult, candidate, ensuredTargetClassMap, executionPlan);
                continue;
            }
            if (Objects.equals(candidate.getAction(), ACTION_REPEAT)) {
                executeRepeat(reqVO, batch.getId(), previewResult, candidate, executionPlan);
                continue;
            }
            if (Objects.equals(candidate.getAction(), ACTION_PENDING_ADVANCE)) {
                executePendingAdvance(reqVO, batch.getId(), previewResult, candidate, executionPlan);
            }
        }
        persistExecutionPlan(executionPlan);

        StudentPromotionExecuteRespVO respVO = new StudentPromotionExecuteRespVO();
        respVO.setBatchId(batch.getId());
        respVO.setSummary(buildSummaryResp(previewResult));
        return respVO;
    }

    private void executePromote(StudentPromotionExecuteReqVO reqVO, Long batchId, PromotionPreviewResult previewResult,
                                PromotionCandidate candidate, Map<String, SchoolClassDO> ensuredTargetClassMap,
                                PromotionExecutionPlan executionPlan) {
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

        executionPlan.addCurrentStudentClassUpdate(StudentClassDO.builder()
                .id(candidate.getCurrentStudentClass().getId())
                .endDate(previewResult.getFromSchoolYear().getEndDate())
                .build());

        StudentClassDO newStudentClass = StudentClassDO.builder()
                .studentId(candidate.getStudent().getId())
                .classId(targetClass.getId())
                .startDate(previewResult.getToSchoolYear().getStartDate())
                .build();
        newStudentClass.clean();
        executionPlan.addNewStudentClass(newStudentClass);

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
        executionPlan.addStudentFlow(studentFlow);
    }

    private void executeRepeat(StudentPromotionExecuteReqVO reqVO, Long batchId, PromotionPreviewResult previewResult,
                               PromotionCandidate candidate, PromotionExecutionPlan executionPlan) {
        SchoolClassDO targetClass = candidate.getTargetClass();
        if (targetClass == null) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_REQUIRED);
        }

        executionPlan.addCurrentStudentClassUpdate(StudentClassDO.builder()
                .id(candidate.getCurrentStudentClass().getId())
                .endDate(previewResult.getFromSchoolYear().getEndDate())
                .build());

        StudentClassDO newStudentClass = StudentClassDO.builder()
                .studentId(candidate.getStudent().getId())
                .classId(targetClass.getId())
                .startDate(previewResult.getToSchoolYear().getStartDate())
                .build();
        newStudentClass.clean();
        executionPlan.addNewStudentClass(newStudentClass);

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
        executionPlan.addStudentFlow(studentFlow);
    }

    private void executePendingAdvance(StudentPromotionExecuteReqVO reqVO, Long batchId,
                                       PromotionPreviewResult previewResult, PromotionCandidate candidate,
                                       PromotionExecutionPlan executionPlan) {
        executionPlan.addCurrentStudentClassUpdate(StudentClassDO.builder()
                .id(candidate.getCurrentStudentClass().getId())
                .endDate(previewResult.getFromSchoolYear().getEndDate())
                .build());
        executionPlan.addStudentStatusUpdate(StudentDO.builder()
                .id(candidate.getStudent().getId())
                .status(STUDENT_STATUS_PENDING_ADVANCE)
                .build());

        StudentFlowDO studentFlow = StudentFlowDO.builder()
                .studentId(candidate.getStudent().getId())
                .batchId(batchId)
                .fromClassId(candidate.getCurrentClass().getId())
                .changeType(FLOW_TYPE_PENDING_ADVANCE)
                .effectiveDate(previewResult.getFromSchoolYear().getEndDate())
                .status(FLOW_STATUS_ACTIVE)
                .targetClassCreated(Boolean.FALSE)
                .remark(reqVO.getRemark())
                .build();
        studentFlow.clean();
        executionPlan.addStudentFlow(studentFlow);
    }

    private void persistExecutionPlan(PromotionExecutionPlan executionPlan) {
        if (CollUtil.isNotEmpty(executionPlan.getCurrentStudentClassUpdates())) {
            studentClassMapper.updateBatch(executionPlan.getCurrentStudentClassUpdates(), BATCH_WRITE_SIZE);
        }
        if (CollUtil.isNotEmpty(executionPlan.getNewStudentClasses())) {
            studentClassMapper.insertBatch(executionPlan.getNewStudentClasses(), BATCH_WRITE_SIZE);
        }
        if (CollUtil.isNotEmpty(executionPlan.getStudentStatusUpdates())) {
            studentMapper.updateBatch(executionPlan.getStudentStatusUpdates(), BATCH_WRITE_SIZE);
        }
        if (CollUtil.isNotEmpty(executionPlan.getStudentFlows())) {
            studentFlowMapper.insertBatch(executionPlan.getStudentFlows(), BATCH_WRITE_SIZE);
        }
    }

    private PromotionPreviewResult buildPreviewResult(StudentPromotionPreviewReqVO reqVO) {
        SchoolDO school = validateSchoolExists(reqVO.getSchoolId());
        SchoolYearDO fromSchoolYear = validateSchoolYearExists(reqVO.getFromSchoolYearId());
        validateSchoolYearBelongsToSchool(fromSchoolYear, reqVO.getSchoolId());
        SchoolYearDO toSchoolYear = validateSchoolYearExists(reqVO.getToSchoolYearId());
        validateSchoolYearBelongsToSchool(toSchoolYear, reqVO.getSchoolId());
        if (!isNextSchoolYear(fromSchoolYear, toSchoolYear)) {
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

        PromotionCandidateBuildContext context = new PromotionCandidateBuildContext(reqVO, toSchoolYear,
                schoolGradeMap, gradeCatalogMap, nextGlobalGradeCatalogIdMap, nextSchoolGradeMap, targetClassMap,
                adjustmentTargetClassMap);
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
            PromotionCandidateSubject subject = new PromotionCandidateSubject(student, studentClasses,
                    currentStudentClass, currentClass);
            PromotionCandidate candidate = buildPromotionCandidate(context, subject, adjustmentMap.get(student.getId()));
            candidates.add(candidate);
        }
        candidates.sort(Comparator
                .comparing((PromotionCandidate item) -> item.getCurrentClass().getEntryYear(), Comparator.nullsLast(Integer::compareTo))
                .thenComparing(item -> item.getCurrentClass().getClassNo(), Comparator.nullsLast(Integer::compareTo))
                .thenComparing(item -> item.getStudent().getStudentName(), Comparator.nullsLast(String::compareTo))
                .thenComparing(item -> item.getStudent().getId(), Comparator.nullsLast(Long::compareTo)));
        return candidates;
    }

    private PromotionCandidate buildPromotionCandidate(PromotionCandidateBuildContext context,
                                                       PromotionCandidateSubject subject,
                                                       StudentPromotionAdjustmentReqVO adjustment) {
        PromotionCandidate candidate = new PromotionCandidate();
        candidate.setStudent(subject.student);
        candidate.setCurrentStudentClass(subject.currentStudentClass);
        candidate.setCurrentClass(subject.currentClass);

        SchoolGradeDO currentSchoolGrade = context.schoolGradeMap.get(subject.currentClass.getSchoolGradeId());
        GradeCatalogDO currentGradeCatalog = currentSchoolGrade == null ? null
                : context.gradeCatalogMap.get(currentSchoolGrade.getGradeCatalogId());
        candidate.setCurrentSchoolGrade(currentSchoolGrade);
        candidate.setCurrentGradeCatalog(currentGradeCatalog);

        for (PromotionCandidateRule rule : promotionCandidateRules) {
            if (rule.apply(candidate, context, subject)) {
                applyAdjustment(candidate, adjustment, context);
                return candidate;
            }
        }

        candidate.setAction(ACTION_PROMOTE);
        candidate.setReason(REASON_READY);
        applyAdjustment(candidate, adjustment, context);
        return candidate;
    }

    private boolean applyMultiCurrentClassRule(PromotionCandidate candidate, PromotionCandidateBuildContext context,
                                               PromotionCandidateSubject subject) {
        if (subject.studentClasses.size() <= 1) {
            return false;
        }
        candidate.setAction(ACTION_SKIP);
        candidate.setReason(REASON_MULTI_CURRENT_CLASS);
        return true;
    }

    private boolean applyStudentReadingRule(PromotionCandidate candidate, PromotionCandidateBuildContext context,
                                            PromotionCandidateSubject subject) {
        if (Objects.equals(subject.student.getStatus(), STUDENT_STATUS_READING)) {
            return false;
        }
        candidate.setAction(ACTION_SKIP);
        candidate.setReason(REASON_STUDENT_NOT_READING);
        return true;
    }

    private boolean applyCurrentGradeSequenceRule(PromotionCandidate candidate, PromotionCandidateBuildContext context,
                                                  PromotionCandidateSubject subject) {
        if (candidate.getCurrentSchoolGrade() != null && candidate.getCurrentGradeCatalog() != null
                && context.nextGlobalGradeCatalogIdMap.containsKey(candidate.getCurrentGradeCatalog().getId())) {
            return false;
        }
        candidate.setAction(ACTION_SKIP);
        candidate.setReason(REASON_GRADE_SEQUENCE_GAP);
        return true;
    }

    private boolean applyTargetGradeResolutionRule(PromotionCandidate candidate, PromotionCandidateBuildContext context,
                                                   PromotionCandidateSubject subject) {
        SchoolGradeDO targetSchoolGrade = context.nextSchoolGradeMap.get(candidate.getCurrentSchoolGrade().getId());
        if (targetSchoolGrade == null) {
            if (Boolean.TRUE.equals(context.reqVO.getGraduateTerminalStudent())) {
                candidate.setAction(ACTION_PENDING_ADVANCE);
                candidate.setReason(REASON_TERMINAL_GRADE_PENDING_ADVANCE);
            } else {
                candidate.setAction(ACTION_SKIP);
                candidate.setReason(REASON_TERMINAL_GRADE_SKIP);
            }
            return true;
        }

        Long expectedNextGradeCatalogId = context.nextGlobalGradeCatalogIdMap.get(candidate.getCurrentGradeCatalog().getId());
        if (!Objects.equals(targetSchoolGrade.getGradeCatalogId(), expectedNextGradeCatalogId)) {
            candidate.setAction(ACTION_SKIP);
            candidate.setReason(REASON_GRADE_SEQUENCE_GAP);
            return true;
        }
        GradeCatalogDO targetGradeCatalog = context.gradeCatalogMap.get(targetSchoolGrade.getGradeCatalogId());
        if (targetGradeCatalog == null) {
            candidate.setAction(ACTION_SKIP);
            candidate.setReason(REASON_GRADE_SEQUENCE_GAP);
            return true;
        }
        candidate.setTargetSchoolGrade(targetSchoolGrade);
        candidate.setTargetGradeCatalog(targetGradeCatalog);
        return false;
    }

    private boolean applyTargetClassResolutionRule(PromotionCandidate candidate, PromotionCandidateBuildContext context,
                                                   PromotionCandidateSubject subject) {
        String targetClassKey = buildTargetClassKey(subject.student.getEntryYear(), candidate.getTargetSchoolGrade().getId(),
                subject.currentClass.getClassNo());
        SchoolClassDO targetClass = context.targetClassMap.get(targetClassKey);
        candidate.setTargetClass(targetClass);
        if (targetClass != null) {
            return false;
        }

        candidate.setTargetClassMissing(true);
        candidate.setPredictedTargetClassName(SchoolClassUtils.buildClassName(subject.student.getEntryYear(),
                candidate.getTargetGradeCatalog().getGradeName(), subject.currentClass.getClassNo()));
        if (Boolean.TRUE.equals(context.reqVO.getAutoCreateClass())) {
            candidate.setAction(ACTION_PROMOTE);
            candidate.setReason(REASON_TARGET_CLASS_AUTO_CREATE);
        } else {
            candidate.setAction(ACTION_SKIP);
            candidate.setReason(REASON_TARGET_CLASS_NOT_FOUND);
        }
        return true;
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
                                 PromotionCandidateBuildContext context) {
        if (adjustment == null || !isAdjustmentApplicable(candidate)) {
            return;
        }
        String action = adjustment.getAction();
        if (!Objects.equals(action, ACTION_PROMOTE) && !Objects.equals(action, ACTION_REPEAT)) {
            throw exception(STUDENT_PROMOTION_ADJUST_ACTION_INVALID);
        }
        AdjustmentTarget adjustmentTarget = resolveAdjustmentTarget(candidate, adjustment, context);
        if (Objects.equals(action, ACTION_PROMOTE)) {
            applyPromoteAdjustment(candidate, adjustmentTarget);
            return;
        }

        applyRepeatAdjustment(candidate, adjustmentTarget);
    }

    private AdjustmentTarget resolveAdjustmentTarget(PromotionCandidate candidate,
                                                     StudentPromotionAdjustmentReqVO adjustment,
                                                     PromotionCandidateBuildContext context) {
        if (adjustment.getTargetClassId() == null) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_REQUIRED);
        }
        SchoolClassDO targetClass = context.adjustmentTargetClassMap.get(adjustment.getTargetClassId());
        if (targetClass == null) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_INVALID);
        }
        if (!Objects.equals(targetClass.getSchoolId(), candidate.getCurrentClass().getSchoolId())) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_INVALID);
        }
        if (!Objects.equals(targetClass.getSchoolYearId(), context.toSchoolYear.getId())) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_NOT_IN_TARGET_YEAR);
        }
        SchoolGradeDO targetSchoolGrade = context.schoolGradeMap.get(targetClass.getSchoolGradeId());
        GradeCatalogDO targetGradeCatalog = targetSchoolGrade == null ? null
                : context.gradeCatalogMap.get(targetSchoolGrade.getGradeCatalogId());
        if (targetSchoolGrade == null || targetGradeCatalog == null) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_INVALID);
        }
        return new AdjustmentTarget(targetClass, targetSchoolGrade, targetGradeCatalog);
    }

    private void applyPromoteAdjustment(PromotionCandidate candidate, AdjustmentTarget adjustmentTarget) {
        if (candidate.getTargetSchoolGrade() == null
                || !Objects.equals(adjustmentTarget.targetClass.getSchoolGradeId(), candidate.getTargetSchoolGrade().getId())) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_INVALID);
        }
        candidate.setAction(ACTION_PROMOTE);
        candidate.setReason(REASON_MANUAL_TARGET_CLASS);
        candidate.setTargetClass(adjustmentTarget.targetClass);
        candidate.setTargetSchoolGrade(adjustmentTarget.targetSchoolGrade);
        candidate.setTargetGradeCatalog(adjustmentTarget.targetGradeCatalog);
        candidate.setTargetClassMissing(false);
        candidate.setPredictedTargetClassName(null);
    }

    private void applyRepeatAdjustment(PromotionCandidate candidate, AdjustmentTarget adjustmentTarget) {
        if (candidate.getCurrentSchoolGrade() == null
                || !Objects.equals(adjustmentTarget.targetClass.getSchoolGradeId(), candidate.getCurrentSchoolGrade().getId())) {
            throw exception(STUDENT_PROMOTION_ADJUST_TARGET_CLASS_INVALID);
        }
        candidate.setAction(ACTION_REPEAT);
        candidate.setReason(REASON_MANUAL_REPEAT);
        candidate.setTargetClass(adjustmentTarget.targetClass);
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

    private boolean isNextSchoolYear(SchoolYearDO fromSchoolYear, SchoolYearDO toSchoolYear) {
        return toSchoolYear.getYearStart() == fromSchoolYear.getYearStart() + 1
                && toSchoolYear.getYearEnd() == fromSchoolYear.getYearEnd() + 1
                && toSchoolYear.getStartDate().isAfter(fromSchoolYear.getEndDate());
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
            itemRespVO.setFromGradeAliasName(candidate.getCurrentGradeCatalog() == null ? null : candidate.getCurrentGradeCatalog().getAliasName());
            itemRespVO.setToClassId(candidate.getTargetClass() == null ? null : candidate.getTargetClass().getId());
            itemRespVO.setToSchoolGradeId(candidate.getTargetSchoolGrade() == null ? null : candidate.getTargetSchoolGrade().getId());
            itemRespVO.setToClassName(candidate.getTargetClass() == null ? candidate.getPredictedTargetClassName() : candidate.getTargetClass().getClassName());
            itemRespVO.setToGradeName(candidate.getTargetGradeCatalog() == null ? null : candidate.getTargetGradeCatalog().getGradeName());
            itemRespVO.setToGradeAliasName(candidate.getTargetGradeCatalog() == null ? null : candidate.getTargetGradeCatalog().getAliasName());
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
        summaryRespVO.setPendingAdvanceCount(previewResult.getPendingAdvanceCount());
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

    @FunctionalInterface
    private interface PromotionCandidateRule {

        boolean apply(PromotionCandidate candidate, PromotionCandidateBuildContext context,
                      PromotionCandidateSubject subject);
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

        public Integer getPendingAdvanceCount() {
            return Math.toIntExact(candidates.stream().filter(item -> Objects.equals(item.getAction(), ACTION_PENDING_ADVANCE)).count());
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
            return getPromotedCount() > 0 || getPendingAdvanceCount() > 0 || getRepeatCount() > 0;
        }
    }

    private static class PromotionCandidateBuildContext {

        private final StudentPromotionPreviewReqVO reqVO;
        private final SchoolYearDO toSchoolYear;
        private final Map<Long, SchoolGradeDO> schoolGradeMap;
        private final Map<Long, GradeCatalogDO> gradeCatalogMap;
        private final Map<Long, Long> nextGlobalGradeCatalogIdMap;
        private final Map<Long, SchoolGradeDO> nextSchoolGradeMap;
        private final Map<String, SchoolClassDO> targetClassMap;
        private final Map<Long, SchoolClassDO> adjustmentTargetClassMap;

        private PromotionCandidateBuildContext(StudentPromotionPreviewReqVO reqVO, SchoolYearDO toSchoolYear,
                                               Map<Long, SchoolGradeDO> schoolGradeMap,
                                               Map<Long, GradeCatalogDO> gradeCatalogMap,
                                               Map<Long, Long> nextGlobalGradeCatalogIdMap,
                                               Map<Long, SchoolGradeDO> nextSchoolGradeMap,
                                               Map<String, SchoolClassDO> targetClassMap,
                                               Map<Long, SchoolClassDO> adjustmentTargetClassMap) {
            this.reqVO = reqVO;
            this.toSchoolYear = toSchoolYear;
            this.schoolGradeMap = schoolGradeMap;
            this.gradeCatalogMap = gradeCatalogMap;
            this.nextGlobalGradeCatalogIdMap = nextGlobalGradeCatalogIdMap;
            this.nextSchoolGradeMap = nextSchoolGradeMap;
            this.targetClassMap = targetClassMap;
            this.adjustmentTargetClassMap = adjustmentTargetClassMap;
        }
    }

    private static class PromotionCandidateSubject {

        private final StudentDO student;
        private final List<StudentClassDO> studentClasses;
        private final StudentClassDO currentStudentClass;
        private final SchoolClassDO currentClass;

        private PromotionCandidateSubject(StudentDO student, List<StudentClassDO> studentClasses,
                                          StudentClassDO currentStudentClass, SchoolClassDO currentClass) {
            this.student = student;
            this.studentClasses = studentClasses;
            this.currentStudentClass = currentStudentClass;
            this.currentClass = currentClass;
        }
    }

    private static class AdjustmentTarget {

        private final SchoolClassDO targetClass;
        private final SchoolGradeDO targetSchoolGrade;
        private final GradeCatalogDO targetGradeCatalog;

        private AdjustmentTarget(SchoolClassDO targetClass, SchoolGradeDO targetSchoolGrade,
                                 GradeCatalogDO targetGradeCatalog) {
            this.targetClass = targetClass;
            this.targetSchoolGrade = targetSchoolGrade;
            this.targetGradeCatalog = targetGradeCatalog;
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

    private static class PromotionExecutionPlan {

        private final List<StudentClassDO> currentStudentClassUpdates = new ArrayList<>();
        private final List<StudentClassDO> newStudentClasses = new ArrayList<>();
        private final List<StudentDO> studentStatusUpdates = new ArrayList<>();
        private final List<StudentFlowDO> studentFlows = new ArrayList<>();

        public List<StudentClassDO> getCurrentStudentClassUpdates() {
            return currentStudentClassUpdates;
        }

        public List<StudentClassDO> getNewStudentClasses() {
            return newStudentClasses;
        }

        public List<StudentDO> getStudentStatusUpdates() {
            return studentStatusUpdates;
        }

        public List<StudentFlowDO> getStudentFlows() {
            return studentFlows;
        }

        public void addCurrentStudentClassUpdate(StudentClassDO studentClass) {
            this.currentStudentClassUpdates.add(studentClass);
        }

        public void addNewStudentClass(StudentClassDO studentClass) {
            this.newStudentClasses.add(studentClass);
        }

        public void addStudentStatusUpdate(StudentDO student) {
            this.studentStatusUpdates.add(student);
        }

        public void addStudentFlow(StudentFlowDO studentFlow) {
            this.studentFlows.add(studentFlow);
        }
    }

}
