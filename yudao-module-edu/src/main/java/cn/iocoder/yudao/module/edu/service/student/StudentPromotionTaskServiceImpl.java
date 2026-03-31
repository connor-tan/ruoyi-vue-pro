package cn.iocoder.yudao.module.edu.service.student;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.baomidou.dynamic.datasource.annotation.Master;
import com.baomidou.dynamic.datasource.annotation.Slave;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionRollbackReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionRollbackRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionExecuteReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionExecuteRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionItemRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionPreviewReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionPreviewRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionSchoolRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionSummaryRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentFlowPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentFlowRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionBatchRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionExecuteReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionExecuteRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionItemRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionPreviewReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionPreviewRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionTaskPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionTaskRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionSummaryRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionYearOptionRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentFlowDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentPromotionBatchDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentPromotionTaskDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentFlowMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentPromotionTaskMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentPromotionBatchMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.studentclass.StudentClassMapper;
import cn.iocoder.yudao.module.edu.enums.StudentStatusEnum;
import cn.iocoder.yudao.module.edu.service.school.SchoolService;
import cn.iocoder.yudao.module.system.api.ip.AreaApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PROMOTION_SCOPE_INVALID;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PROMOTION_NO_ELIGIBLE_STUDENTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PROMOTION_TASK_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PROMOTION_TASK_NOT_ROLLBACKABLE;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PROMOTION_TASK_ROLLBACK_STATE_INVALID;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PROMOTION_SCHOOL_IDS_EMPTY;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PROMOTION_TARGET_YEAR_INVALID;

/**
 * 学生全局批量升班 Service 实现类
 */
@Service
@Validated
public class StudentPromotionTaskServiceImpl implements StudentPromotionTaskService {

    private static final String SCOPE_TYPE_ALL = "ALL";
    private static final String SCOPE_TYPE_SCHOOL = "SCHOOL";
    private static final String SCOPE_TYPE_AREA = "AREA";

    private static final String SCHOOL_STATUS_READY = "READY";
    private static final String SCHOOL_STATUS_SKIP = "SKIP";
    private static final String SCHOOL_STATUS_SUCCESS = "SUCCESS";
    private static final String SCHOOL_STATUS_FAILED = "FAILED";

    private static final String SCHOOL_REASON_SOURCE_YEAR_NOT_FOUND = "SOURCE_SCHOOL_YEAR_NOT_FOUND";
    private static final String SCHOOL_REASON_TARGET_YEAR_NOT_FOUND = "TARGET_SCHOOL_YEAR_NOT_FOUND";
    private static final String SCHOOL_REASON_NO_ELIGIBLE_STUDENTS = "NO_ELIGIBLE_STUDENTS";

    private static final Integer TASK_STATUS_RUNNING = 0;
    private static final Integer TASK_STATUS_SUCCESS = 1;
    private static final Integer TASK_STATUS_PARTIAL = 2;
    private static final Integer TASK_STATUS_FAILED = 3;
    private static final Integer TASK_STATUS_ROLLED_BACK = 4;

    private static final Integer BATCH_STATUS_SUCCESS = 1;
    private static final Integer BATCH_STATUS_SKIPPED = 2;
    private static final Integer BATCH_STATUS_FAILED = 3;
    private static final Integer BATCH_STATUS_ROLLED_BACK = 4;

    private static final Integer STUDENT_STATUS_READING = StudentStatusEnum.READING.getStatus();
    private static final Integer STUDENT_STATUS_GRADUATED = StudentStatusEnum.GRADUATED.getStatus();
    private static final Integer STUDENT_STATUS_PENDING_ADVANCE = StudentStatusEnum.PENDING_ADVANCE.getStatus();

    private static final Integer FLOW_STATUS_ROLLED_BACK = 2;
    private static final Integer BATCH_REASON_MAX_LENGTH = 255;

    private static final String FLOW_TYPE_PROMOTE = "PROMOTE";
    private static final String FLOW_TYPE_REPEAT = "REPEAT";
    private static final String FLOW_TYPE_GRADUATE_LEGACY = "GRADUATE";
    private static final String FLOW_TYPE_PENDING_ADVANCE = "PENDING_ADVANCE";

    @Resource
    private StudentPromotionService studentPromotionService;
    @Resource
    private StudentPromotionTaskMapper studentPromotionTaskMapper;
    @Resource
    private StudentPromotionBatchMapper studentPromotionBatchMapper;
    @Resource
    private StudentFlowMapper studentFlowMapper;
    @Resource
    private StudentMapper studentMapper;
    @Resource
    private StudentClassMapper studentClassMapper;
    @Resource
    private SchoolMapper schoolMapper;
    @Resource
    private SchoolClassMapper schoolClassMapper;
    @Resource
    private SchoolYearMapper schoolYearMapper;
    @Resource
    private SchoolService schoolService;
    @Resource
    private AreaApi areaApi;

    @Override
    @Slave
    public List<StudentPromotionYearOptionRespVO> getPromotionYearOptions() {
        return schoolYearMapper.selectList().stream()
                .collect(Collectors.toMap(SchoolYearDO::getYearStart, Function.identity(), (item1, item2) -> item1,
                        LinkedHashMap::new))
                .values().stream()
                .sorted(Comparator.comparing(SchoolYearDO::getYearStart).reversed())
                .map(this::buildYearOptionResp)
                .collect(Collectors.toList());
    }

    @Override
    @Slave
    public PageResult<StudentPromotionTaskRespVO> getPromotionTaskPage(StudentPromotionTaskPageReqVO reqVO) {
        PageResult<StudentPromotionTaskDO> pageResult = studentPromotionTaskMapper.selectPage(reqVO);
        return new PageResult<>(convertList(pageResult.getList(), task -> {
            StudentPromotionTaskRespVO respVO = BeanUtils.toBean(task, StudentPromotionTaskRespVO.class);
            respVO.setPendingAdvanceCount(task.getGraduatedCount());
            respVO.setRollbackable(isTaskRollbackable(task.getStatus()));
            return respVO;
        }), pageResult.getTotal());
    }

    @Override
    @Slave
    public List<StudentPromotionBatchRespVO> getPromotionBatchListByTaskId(Long taskId) {
        List<StudentPromotionBatchDO> batches = studentPromotionBatchMapper.selectListByTaskId(taskId);
        if (CollUtil.isEmpty(batches)) {
            return Collections.emptyList();
        }
        Map<Long, String> schoolNameMap = schoolMapper.selectList(SchoolDO::getId,
                        convertSet(batches, StudentPromotionBatchDO::getSchoolId)).stream()
                .collect(Collectors.toMap(SchoolDO::getId, SchoolDO::getSchoolName, (item1, item2) -> item1));
        Map<Long, SchoolYearDO> schoolYearMap = schoolYearMapper.selectList(SchoolYearDO::getId,
                        buildSchoolYearIds(batches)).stream()
                .collect(Collectors.toMap(SchoolYearDO::getId, Function.identity(), (item1, item2) -> item1));
        return convertList(batches, batch -> buildBatchResp(batch, schoolNameMap, schoolYearMap));
    }

    @Override
    @Slave
    public PageResult<StudentFlowRespVO> getStudentFlowPage(StudentFlowPageReqVO reqVO) {
        List<Long> studentIds = null;
        if (reqVO.getStudentName() != null && !reqVO.getStudentName().isBlank()) {
            studentIds = convertList(studentMapper.selectListByStudentName(reqVO.getStudentName()), StudentDO::getId);
            if (CollUtil.isEmpty(studentIds)) {
                return PageResult.empty();
            }
        }

        List<Long> batchIds = null;
        if (reqVO.getTaskId() != null) {
            batchIds = convertList(studentPromotionBatchMapper.selectListByTaskId(reqVO.getTaskId()),
                    StudentPromotionBatchDO::getId);
            if (CollUtil.isEmpty(batchIds)) {
                return PageResult.empty();
            }
        }

        PageResult<StudentFlowDO> pageResult = studentFlowMapper.selectPage(reqVO, studentIds, batchIds);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }

        Map<Long, StudentDO> studentMap = studentMapper.selectList(StudentDO::getId,
                        convertSet(pageResult.getList(), StudentFlowDO::getStudentId)).stream()
                .collect(Collectors.toMap(StudentDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, StudentPromotionBatchDO> batchMap = studentPromotionBatchMapper
                .selectListByIds(buildBatchIds(pageResult.getList())).stream()
                .collect(Collectors.toMap(StudentPromotionBatchDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, SchoolClassDO> classMap = schoolClassMapper.selectList(SchoolClassDO::getId,
                        buildSchoolClassIds(pageResult.getList())).stream()
                .collect(Collectors.toMap(SchoolClassDO::getId, Function.identity(), (item1, item2) -> item1));

        return new PageResult<>(convertList(pageResult.getList(), flow -> buildFlowResp(flow, studentMap, batchMap, classMap)),
                pageResult.getTotal());
    }

    @Override
    public StudentGlobalPromotionPreviewRespVO previewGlobalStudentPromotion(StudentGlobalPromotionPreviewReqVO reqVO) {
        GlobalPreviewResult previewResult = buildGlobalPreviewResult(reqVO);
        StudentGlobalPromotionPreviewRespVO respVO = new StudentGlobalPromotionPreviewRespVO();
        respVO.setSummary(buildGlobalSummaryResp(previewResult.getSchools()));
        respVO.setSchools(buildSchoolRespList(previewResult.getSchools()));
        respVO.setItems(buildGlobalItemRespList(previewResult.getSchools()));
        return respVO;
    }

    @Override
    @Master
    public StudentGlobalPromotionExecuteRespVO executeGlobalStudentPromotion(StudentGlobalPromotionExecuteReqVO reqVO) {
        GlobalPreviewResult previewResult = buildGlobalPreviewResult(reqVO);
        StudentGlobalPromotionSummaryRespVO previewSummary = buildGlobalSummaryResp(previewResult.getSchools());
        if (!hasExecutableStudents(previewSummary)) {
            throw exception(STUDENT_PROMOTION_NO_ELIGIBLE_STUDENTS);
        }
        StudentPromotionTaskDO task = StudentPromotionTaskDO.builder()
                .fromYearStart(reqVO.getFromYearStart())
                .toYearStart(reqVO.getToYearStart())
                .scopeType(reqVO.getScopeType())
                .scopeSnapshot(buildScopeSnapshot(reqVO))
                .autoCreateClass(reqVO.getAutoCreateClass())
                .graduateTerminalStudent(reqVO.getGraduateTerminalStudent())
                .status(TASK_STATUS_RUNNING)
                .remark(reqVO.getRemark())
                .build();
        task.clean();
        studentPromotionTaskMapper.insert(task);

        for (GlobalSchoolPreview schoolPreview : previewResult.getSchools()) {
            if (Objects.equals(schoolPreview.getStatus(), SCHOOL_STATUS_SKIP)) {
                StudentPromotionBatchDO batch = createTaskSchoolBatch(task.getId(), schoolPreview, reqVO,
                        BATCH_STATUS_SKIPPED, schoolPreview.getReason());
                schoolPreview.setBatchId(batch.getId());
                continue;
            }
            if (!Objects.equals(schoolPreview.getStatus(), SCHOOL_STATUS_READY)) {
                continue;
            }
            try {
                StudentPromotionExecuteRespVO executeRespVO = studentPromotionService.executeStudentPromotion(
                        buildSingleSchoolExecuteReq(schoolPreview, reqVO), task.getId());
                schoolPreview.setStatus(SCHOOL_STATUS_SUCCESS);
                schoolPreview.setBatchId(executeRespVO.getBatchId());
                applySchoolSummary(schoolPreview, executeRespVO.getSummary());
            } catch (Exception ex) {
                String reason = buildFailureReason(ex);
                schoolPreview.setStatus(SCHOOL_STATUS_FAILED);
                schoolPreview.setReason(reason);
                StudentPromotionBatchDO batch = createTaskSchoolBatch(task.getId(), schoolPreview, reqVO,
                        BATCH_STATUS_FAILED, reason);
                schoolPreview.setBatchId(batch.getId());
            }
        }

        StudentGlobalPromotionSummaryRespVO summaryRespVO = buildGlobalSummaryResp(previewResult.getSchools());
        studentPromotionTaskMapper.updateById(StudentPromotionTaskDO.builder()
                .id(task.getId())
                .totalSchoolCount(summaryRespVO.getTotalSchoolCount())
                .successSchoolCount(countSchools(previewResult.getSchools(), SCHOOL_STATUS_SUCCESS))
                .skippedSchoolCount(countSchools(previewResult.getSchools(), SCHOOL_STATUS_SKIP))
                .failedSchoolCount(countSchools(previewResult.getSchools(), SCHOOL_STATUS_FAILED))
                .totalCount(summaryRespVO.getTotalCount())
                .promotedCount(summaryRespVO.getPromotedCount())
                .repeatCount(summaryRespVO.getRepeatCount())
                .graduatedCount(summaryRespVO.getPendingAdvanceCount())
                .skippedCount(summaryRespVO.getSkippedCount())
                .status(buildTaskStatus(previewResult.getSchools()))
                .build());

        StudentGlobalPromotionExecuteRespVO respVO = new StudentGlobalPromotionExecuteRespVO();
        respVO.setTaskId(task.getId());
        respVO.setSummary(summaryRespVO);
        respVO.setSchools(buildSchoolRespList(previewResult.getSchools()));
        return respVO;
    }

    @Override
    @Master
    @Transactional(rollbackFor = Exception.class)
    public StudentGlobalPromotionRollbackRespVO rollbackGlobalStudentPromotion(StudentGlobalPromotionRollbackReqVO reqVO) {
        StudentPromotionTaskDO task = validateRollbackTask(reqVO.getTaskId());
        List<StudentPromotionBatchDO> batches = studentPromotionBatchMapper.selectListByTaskId(reqVO.getTaskId()).stream()
                .filter(batch -> Objects.equals(batch.getStatus(), BATCH_STATUS_SUCCESS))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(batches)) {
            throw exception(STUDENT_PROMOTION_TASK_NOT_ROLLBACKABLE);
        }
        List<Long> batchIds = convertList(batches, StudentPromotionBatchDO::getId);
        List<StudentFlowDO> flows = studentFlowMapper.selectListByBatchIds(batchIds);
        if (CollUtil.isEmpty(flows)) {
            throw exception(STUDENT_PROMOTION_TASK_NOT_ROLLBACKABLE);
        }

        Map<Long, StudentPromotionBatchDO> batchMap = batches.stream()
                .collect(Collectors.toMap(StudentPromotionBatchDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, SchoolYearDO> fromSchoolYearMap = schoolYearMapper.selectList(SchoolYearDO::getId,
                        convertSet(batches, StudentPromotionBatchDO::getFromSchoolYearId)).stream()
                .collect(Collectors.toMap(SchoolYearDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, StudentDO> studentMap = studentMapper.selectList(StudentDO::getId,
                        convertSet(flows, StudentFlowDO::getStudentId)).stream()
                .collect(Collectors.toMap(StudentDO::getId, Function.identity(), (item1, item2) -> item1));

        validateRollbackFlows(flows, batchMap, fromSchoolYearMap, studentMap);

        Set<Long> autoCreatedClassIds = convertSet(flows.stream()
                .filter(flow -> Boolean.TRUE.equals(flow.getTargetClassCreated()))
                .filter(flow -> flow.getToClassId() != null)
                .collect(Collectors.toList()), StudentFlowDO::getToClassId);
        for (StudentFlowDO flow : flows) {
            rollbackFlow(flow, batchMap.get(flow.getBatchId()), fromSchoolYearMap);
        }
        studentFlowMapper.updateStatusByBatchIds(batchIds, FLOW_STATUS_ROLLED_BACK);
        autoCreatedClassIds.forEach(this::deleteAutoCreatedClassIfUnused);
        batches.forEach(batch -> studentPromotionBatchMapper.updateById(StudentPromotionBatchDO.builder()
                .id(batch.getId())
                .status(BATCH_STATUS_ROLLED_BACK)
                .build()));
        studentPromotionTaskMapper.updateById(StudentPromotionTaskDO.builder()
                .id(task.getId())
                .status(TASK_STATUS_ROLLED_BACK)
                .remark(appendRollbackRemark(task.getRemark(), reqVO.getRemark()))
                .build());

        StudentGlobalPromotionRollbackRespVO respVO = new StudentGlobalPromotionRollbackRespVO();
        respVO.setTaskId(task.getId());
        respVO.setRolledBackSchoolCount(batches.size());
        respVO.setRolledBackStudentCount(flows.size());
        return respVO;
    }

    private GlobalPreviewResult buildGlobalPreviewResult(StudentGlobalPromotionPreviewReqVO reqVO) {
        validateGlobalReq(reqVO);
        List<SchoolSimpleRespVO> schools = resolveSchools(reqVO);
        if (CollUtil.isEmpty(schools)) {
            return new GlobalPreviewResult(Collections.emptyList());
        }
        Map<Long, Map<Integer, SchoolYearDO>> schoolYearMap = schoolYearMapper.selectListBySchoolIdsAndYearStarts(
                        schools.stream().map(SchoolSimpleRespVO::getId).collect(Collectors.toList()),
                        Arrays.asList(reqVO.getFromYearStart(), reqVO.getToYearStart())).stream()
                .collect(Collectors.groupingBy(SchoolYearDO::getSchoolId,
                        Collectors.toMap(SchoolYearDO::getYearStart, Function.identity(), (item1, item2) -> item1)));

        List<GlobalSchoolPreview> schoolPreviews = new ArrayList<>();
        for (SchoolSimpleRespVO school : schools) {
            Map<Integer, SchoolYearDO> yearMap = schoolYearMap.getOrDefault(school.getId(), Collections.emptyMap());
            SchoolYearDO fromSchoolYear = yearMap.get(reqVO.getFromYearStart());
            SchoolYearDO toSchoolYear = yearMap.get(reqVO.getToYearStart());
            if (fromSchoolYear == null) {
                schoolPreviews.add(buildSkippedSchoolPreview(school, SCHOOL_REASON_SOURCE_YEAR_NOT_FOUND));
                continue;
            }
            if (toSchoolYear == null) {
                schoolPreviews.add(buildSkippedSchoolPreview(school, SCHOOL_REASON_TARGET_YEAR_NOT_FOUND));
                continue;
            }
            StudentPromotionPreviewRespVO previewRespVO = studentPromotionService.previewStudentPromotion(
                    buildSingleSchoolPreviewReq(school.getId(), fromSchoolYear.getId(), toSchoolYear.getId(), reqVO));
            schoolPreviews.add(buildReadySchoolPreview(school, fromSchoolYear, toSchoolYear, previewRespVO));
        }
        return new GlobalPreviewResult(schoolPreviews);
    }

    private void validateGlobalReq(StudentGlobalPromotionPreviewReqVO reqVO) {
        if (reqVO.getToYearStart() != reqVO.getFromYearStart() + 1) {
            throw exception(STUDENT_PROMOTION_TARGET_YEAR_INVALID);
        }
        if (!Objects.equals(reqVO.getScopeType(), SCOPE_TYPE_ALL)
                && !Objects.equals(reqVO.getScopeType(), SCOPE_TYPE_SCHOOL)
                && !Objects.equals(reqVO.getScopeType(), SCOPE_TYPE_AREA)) {
            throw exception(STUDENT_PROMOTION_SCOPE_INVALID);
        }
        if (Objects.equals(reqVO.getScopeType(), SCOPE_TYPE_SCHOOL) && CollUtil.isEmpty(reqVO.getSchoolIds())) {
            throw exception(STUDENT_PROMOTION_SCHOOL_IDS_EMPTY);
        }
        if (Objects.equals(reqVO.getScopeType(), SCOPE_TYPE_AREA) && reqVO.getAreaId() == null) {
            throw exception(STUDENT_PROMOTION_SCOPE_INVALID);
        }
        if (Objects.equals(reqVO.getScopeType(), SCOPE_TYPE_AREA) && reqVO.getAreaId() != null) {
            areaApi.validateAreaSelectable(Math.toIntExact(reqVO.getAreaId()));
        }
    }

    private List<SchoolSimpleRespVO> resolveSchools(StudentGlobalPromotionPreviewReqVO reqVO) {
        if (Objects.equals(reqVO.getScopeType(), SCOPE_TYPE_ALL)) {
            return schoolService.getSchoolSimpleList();
        }
        if (Objects.equals(reqVO.getScopeType(), SCOPE_TYPE_SCHOOL)) {
            return schoolMapper.selectList(SchoolDO::getId, reqVO.getSchoolIds()).stream()
                    .sorted(Comparator.comparing(SchoolDO::getId))
                    .map(this::buildSchoolSimpleResp)
                    .collect(Collectors.toList());
        }
        List<Long> areaIds = convertList(areaApi.getSelectableAreaIds(Math.toIntExact(reqVO.getAreaId())), Long::valueOf);
        return schoolMapper.selectListByAreaIds(areaIds).stream()
                .map(this::buildSchoolSimpleResp)
                .collect(Collectors.toList());
    }

    private GlobalSchoolPreview buildSkippedSchoolPreview(SchoolSimpleRespVO school, String reason) {
        GlobalSchoolPreview preview = new GlobalSchoolPreview();
        preview.setSchoolId(school.getId());
        preview.setSchoolName(school.getSchoolName());
        preview.setStatus(SCHOOL_STATUS_SKIP);
        preview.setReason(reason);
        preview.setItems(Collections.emptyList());
        return preview;
    }

    private GlobalSchoolPreview buildReadySchoolPreview(SchoolSimpleRespVO school, SchoolYearDO fromSchoolYear,
                                                        SchoolYearDO toSchoolYear, StudentPromotionPreviewRespVO previewRespVO) {
        GlobalSchoolPreview preview = new GlobalSchoolPreview();
        preview.setSchoolId(school.getId());
        preview.setSchoolName(school.getSchoolName());
        preview.setFromSchoolYearId(fromSchoolYear.getId());
        preview.setToSchoolYearId(toSchoolYear.getId());
        applySchoolSummary(preview, previewRespVO.getSummary());
        preview.setItems(previewRespVO.getItems());
        if (hasExecutableStudents(previewRespVO.getSummary())) {
            preview.setStatus(SCHOOL_STATUS_READY);
            return preview;
        }
        preview.setStatus(SCHOOL_STATUS_SKIP);
        preview.setReason(SCHOOL_REASON_NO_ELIGIBLE_STUDENTS);
        return preview;
    }

    private StudentPromotionPreviewReqVO buildSingleSchoolPreviewReq(Long schoolId, Long fromSchoolYearId,
                                                                     Long toSchoolYearId,
                                                                     StudentGlobalPromotionPreviewReqVO reqVO) {
        StudentPromotionPreviewReqVO singleReqVO = new StudentPromotionPreviewReqVO();
        singleReqVO.setSchoolId(schoolId);
        singleReqVO.setFromSchoolYearId(fromSchoolYearId);
        singleReqVO.setToSchoolYearId(toSchoolYearId);
        singleReqVO.setAutoCreateClass(reqVO.getAutoCreateClass());
        singleReqVO.setGraduateTerminalStudent(reqVO.getGraduateTerminalStudent());
        singleReqVO.setRemark(reqVO.getRemark());
        singleReqVO.setAdjustments(reqVO.getAdjustments());
        return singleReqVO;
    }

    private StudentPromotionExecuteReqVO buildSingleSchoolExecuteReq(GlobalSchoolPreview schoolPreview,
                                                                     StudentGlobalPromotionExecuteReqVO reqVO) {
        StudentPromotionExecuteReqVO singleReqVO = new StudentPromotionExecuteReqVO();
        singleReqVO.setSchoolId(schoolPreview.getSchoolId());
        singleReqVO.setFromSchoolYearId(schoolPreview.getFromSchoolYearId());
        singleReqVO.setToSchoolYearId(schoolPreview.getToSchoolYearId());
        singleReqVO.setAutoCreateClass(reqVO.getAutoCreateClass());
        singleReqVO.setGraduateTerminalStudent(reqVO.getGraduateTerminalStudent());
        singleReqVO.setRemark(reqVO.getRemark());
        singleReqVO.setAdjustments(reqVO.getAdjustments());
        return singleReqVO;
    }

    private void applySchoolSummary(GlobalSchoolPreview preview, StudentPromotionSummaryRespVO summary) {
        preview.setTotalCount(summary.getTotalCount());
        preview.setPromotedCount(summary.getPromotedCount());
        preview.setGraduatedCount(summary.getPendingAdvanceCount());
        preview.setRepeatCount(summary.getRepeatCount());
        preview.setSkippedCount(summary.getSkippedCount());
        preview.setMissingTargetClassCount(summary.getMissingTargetClassCount());
    }

    private StudentGlobalPromotionSummaryRespVO buildGlobalSummaryResp(List<GlobalSchoolPreview> schools) {
        StudentGlobalPromotionSummaryRespVO summaryRespVO = new StudentGlobalPromotionSummaryRespVO();
        summaryRespVO.setTotalSchoolCount(schools.size());
        summaryRespVO.setReadySchoolCount(countSchools(schools, SCHOOL_STATUS_READY, SCHOOL_STATUS_SUCCESS));
        summaryRespVO.setSkippedSchoolCount(countSchools(schools, SCHOOL_STATUS_SKIP));
        summaryRespVO.setFailedSchoolCount(countSchools(schools, SCHOOL_STATUS_FAILED));
        summaryRespVO.setTotalCount(sumInt(schools, GlobalSchoolPreview::getTotalCount));
        summaryRespVO.setPromotedCount(sumInt(schools, GlobalSchoolPreview::getPromotedCount));
        summaryRespVO.setPendingAdvanceCount(sumInt(schools, GlobalSchoolPreview::getGraduatedCount));
        summaryRespVO.setRepeatCount(sumInt(schools, GlobalSchoolPreview::getRepeatCount));
        summaryRespVO.setSkippedCount(sumInt(schools, GlobalSchoolPreview::getSkippedCount));
        summaryRespVO.setMissingTargetClassCount(sumInt(schools, GlobalSchoolPreview::getMissingTargetClassCount));
        return summaryRespVO;
    }

    private boolean hasExecutableStudents(StudentPromotionSummaryRespVO summary) {
        return defaultInt(summary.getPromotedCount()) > 0
                || defaultInt(summary.getPendingAdvanceCount()) > 0
                || defaultInt(summary.getRepeatCount()) > 0;
    }

    private boolean hasExecutableStudents(StudentGlobalPromotionSummaryRespVO summary) {
        return defaultInt(summary.getPromotedCount()) > 0
                || defaultInt(summary.getPendingAdvanceCount()) > 0
                || defaultInt(summary.getRepeatCount()) > 0;
    }

    private List<StudentGlobalPromotionSchoolRespVO> buildSchoolRespList(List<GlobalSchoolPreview> schools) {
        return schools.stream().map(item -> {
            StudentGlobalPromotionSchoolRespVO respVO = new StudentGlobalPromotionSchoolRespVO();
            respVO.setSchoolId(item.getSchoolId());
            respVO.setSchoolName(item.getSchoolName());
            respVO.setFromSchoolYearId(item.getFromSchoolYearId());
            respVO.setToSchoolYearId(item.getToSchoolYearId());
            respVO.setBatchId(item.getBatchId());
            respVO.setStatus(item.getStatus());
            respVO.setReason(item.getReason());
            respVO.setTotalCount(defaultInt(item.getTotalCount()));
            respVO.setPromotedCount(defaultInt(item.getPromotedCount()));
            respVO.setPendingAdvanceCount(defaultInt(item.getGraduatedCount()));
            respVO.setRepeatCount(defaultInt(item.getRepeatCount()));
            respVO.setSkippedCount(defaultInt(item.getSkippedCount()));
            respVO.setMissingTargetClassCount(defaultInt(item.getMissingTargetClassCount()));
            return respVO;
        }).collect(Collectors.toList());
    }

    private List<StudentGlobalPromotionItemRespVO> buildGlobalItemRespList(List<GlobalSchoolPreview> schools) {
        List<StudentGlobalPromotionItemRespVO> items = new ArrayList<>();
        for (GlobalSchoolPreview school : schools) {
            if (CollUtil.isEmpty(school.getItems())) {
                continue;
            }
            for (StudentPromotionItemRespVO item : school.getItems()) {
                StudentGlobalPromotionItemRespVO itemRespVO = new StudentGlobalPromotionItemRespVO();
                itemRespVO.setSchoolId(school.getSchoolId());
                itemRespVO.setSchoolName(school.getSchoolName());
                itemRespVO.setStudentId(item.getStudentId());
                itemRespVO.setStudentName(item.getStudentName());
                itemRespVO.setEntryYear(item.getEntryYear());
                itemRespVO.setFromSchoolGradeId(item.getFromSchoolGradeId());
                itemRespVO.setFromClassName(item.getFromClassName());
                itemRespVO.setFromGradeName(item.getFromGradeName());
                itemRespVO.setFromGradeAliasName(item.getFromGradeAliasName());
                itemRespVO.setToSchoolGradeId(item.getToSchoolGradeId());
                itemRespVO.setToClassId(item.getToClassId());
                itemRespVO.setToClassName(item.getToClassName());
                itemRespVO.setToGradeName(item.getToGradeName());
                itemRespVO.setToGradeAliasName(item.getToGradeAliasName());
                itemRespVO.setTargetClassMissing(item.getTargetClassMissing());
                itemRespVO.setAction(item.getAction());
                itemRespVO.setReason(item.getReason());
                items.add(itemRespVO);
            }
        }
        return items;
    }

    private StudentPromotionYearOptionRespVO buildYearOptionResp(SchoolYearDO schoolYear) {
        StudentPromotionYearOptionRespVO respVO = new StudentPromotionYearOptionRespVO();
        respVO.setYearStart(schoolYear.getYearStart());
        respVO.setYearEnd(schoolYear.getYearEnd());
        respVO.setName(schoolYear.getYearStart() + "-" + schoolYear.getYearEnd() + "学年");
        return respVO;
    }

    private SchoolSimpleRespVO buildSchoolSimpleResp(SchoolDO school) {
        SchoolSimpleRespVO respVO = new SchoolSimpleRespVO();
        respVO.setId(school.getId());
        respVO.setSchoolName(school.getSchoolName());
        return respVO;
    }

    private StudentPromotionBatchRespVO buildBatchResp(StudentPromotionBatchDO batch, Map<Long, String> schoolNameMap,
                                                       Map<Long, SchoolYearDO> schoolYearMap) {
        StudentPromotionBatchRespVO respVO = BeanUtils.toBean(batch, StudentPromotionBatchRespVO.class);
        respVO.setPendingAdvanceCount(batch.getGraduatedCount());
        respVO.setSchoolName(schoolNameMap.get(batch.getSchoolId()));
        respVO.setFromSchoolYearName(buildSchoolYearName(schoolYearMap.get(batch.getFromSchoolYearId())));
        respVO.setToSchoolYearName(buildSchoolYearName(schoolYearMap.get(batch.getToSchoolYearId())));
        return respVO;
    }

    private StudentFlowRespVO buildFlowResp(StudentFlowDO flow, Map<Long, StudentDO> studentMap,
                                            Map<Long, StudentPromotionBatchDO> batchMap,
                                            Map<Long, SchoolClassDO> classMap) {
        StudentFlowRespVO respVO = BeanUtils.toBean(flow, StudentFlowRespVO.class);
        StudentDO student = studentMap.get(flow.getStudentId());
        StudentPromotionBatchDO batch = batchMap.get(flow.getBatchId());
        SchoolClassDO fromClass = classMap.get(flow.getFromClassId());
        SchoolClassDO toClass = classMap.get(flow.getToClassId());
        respVO.setTaskId(batch == null ? null : batch.getTaskId());
        respVO.setStudentName(student == null ? null : student.getStudentName());
        respVO.setFromClassName(fromClass == null ? null : fromClass.getClassName());
        respVO.setToClassName(toClass == null ? null : toClass.getClassName());
        return respVO;
    }

    private Set<Long> buildSchoolYearIds(List<StudentPromotionBatchDO> batches) {
        return batches.stream()
                .flatMap(item -> Arrays.stream(new Long[]{item.getFromSchoolYearId(), item.getToSchoolYearId()}))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Long> buildSchoolClassIds(List<StudentFlowDO> flows) {
        return flows.stream()
                .flatMap(item -> Arrays.stream(new Long[]{item.getFromClassId(), item.getToClassId()}))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Long> buildBatchIds(List<StudentFlowDO> flows) {
        return flows.stream()
                .map(StudentFlowDO::getBatchId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private String buildSchoolYearName(SchoolYearDO schoolYear) {
        if (schoolYear == null) {
            return null;
        }
        return schoolYear.getYearStart() + "-" + schoolYear.getYearEnd() + "学年";
    }

    private boolean isTaskRollbackable(Integer status) {
        return Objects.equals(status, TASK_STATUS_SUCCESS) || Objects.equals(status, TASK_STATUS_PARTIAL);
    }

    private StudentPromotionBatchDO createTaskSchoolBatch(Long taskId, GlobalSchoolPreview schoolPreview,
                                                          StudentGlobalPromotionExecuteReqVO reqVO,
                                                          Integer status, String reason) {
        StudentPromotionBatchDO batch = StudentPromotionBatchDO.builder()
                .taskId(taskId)
                .schoolId(schoolPreview.getSchoolId())
                .fromSchoolYearId(schoolPreview.getFromSchoolYearId())
                .toSchoolYearId(schoolPreview.getToSchoolYearId())
                .autoCreateClass(reqVO.getAutoCreateClass())
                .graduateTerminalStudent(reqVO.getGraduateTerminalStudent())
                .totalCount(defaultInt(schoolPreview.getTotalCount()))
                .promotedCount(defaultInt(schoolPreview.getPromotedCount()))
                .repeatCount(defaultInt(schoolPreview.getRepeatCount()))
                .graduatedCount(defaultInt(schoolPreview.getGraduatedCount()))
                .skippedCount(defaultInt(schoolPreview.getSkippedCount()))
                .status(status)
                .reason(normalizeBatchReason(reason))
                .remark(reqVO.getRemark())
                .build();
        batch.clean();
        studentPromotionBatchMapper.insert(batch);
        return batch;
    }

    private String buildFailureReason(Exception ex) {
        return normalizeBatchReason(ExceptionUtil.getRootCauseMessage(ex));
    }

    private String normalizeBatchReason(String reason) {
        return StrUtils.maxLength(reason, BATCH_REASON_MAX_LENGTH);
    }

    private StudentPromotionTaskDO validateRollbackTask(Long taskId) {
        StudentPromotionTaskDO task = studentPromotionTaskMapper.selectById(taskId);
        if (task == null) {
            throw exception(STUDENT_PROMOTION_TASK_NOT_EXISTS);
        }
        if (Objects.equals(task.getStatus(), TASK_STATUS_RUNNING)
                || Objects.equals(task.getStatus(), TASK_STATUS_ROLLED_BACK)) {
            throw exception(STUDENT_PROMOTION_TASK_ROLLBACK_STATE_INVALID);
        }
        if (!Objects.equals(task.getStatus(), TASK_STATUS_SUCCESS)
                && !Objects.equals(task.getStatus(), TASK_STATUS_PARTIAL)) {
            throw exception(STUDENT_PROMOTION_TASK_NOT_ROLLBACKABLE);
        }
        return task;
    }

    private void validateRollbackFlows(List<StudentFlowDO> flows, Map<Long, StudentPromotionBatchDO> batchMap,
                                       Map<Long, SchoolYearDO> fromSchoolYearMap,
                                       Map<Long, StudentDO> studentMap) {
        for (StudentFlowDO flow : flows) {
            StudentPromotionBatchDO batch = batchMap.get(flow.getBatchId());
            SchoolYearDO fromSchoolYear = batch == null ? null : fromSchoolYearMap.get(batch.getFromSchoolYearId());
            StudentDO student = studentMap.get(flow.getStudentId());
            if (batch == null || fromSchoolYear == null || student == null) {
                throw exception(STUDENT_PROMOTION_TASK_NOT_ROLLBACKABLE);
            }
            StudentClassDO sourceStudentClass = studentClassMapper.selectLatestEndedByStudentIdAndClassIdAndEndDate(
                    flow.getStudentId(), flow.getFromClassId(), fromSchoolYear.getEndDate());
            if (sourceStudentClass == null) {
                throw exception(STUDENT_PROMOTION_TASK_ROLLBACK_STATE_INVALID);
            }
            if (Objects.equals(flow.getChangeType(), FLOW_TYPE_PROMOTE)
                    || Objects.equals(flow.getChangeType(), FLOW_TYPE_REPEAT)) {
                StudentClassDO targetStudentClass = studentClassMapper.selectCurrentByStudentIdAndClassIdAndStartDate(
                        flow.getStudentId(), flow.getToClassId(), flow.getEffectiveDate());
                if (targetStudentClass == null) {
                    throw exception(STUDENT_PROMOTION_TASK_ROLLBACK_STATE_INVALID);
                }
                continue;
            }
            if (Objects.equals(flow.getChangeType(), FLOW_TYPE_PENDING_ADVANCE)
                    || Objects.equals(flow.getChangeType(), FLOW_TYPE_GRADUATE_LEGACY)) {
                if (!Objects.equals(student.getStatus(), STUDENT_STATUS_PENDING_ADVANCE)
                        && !Objects.equals(student.getStatus(), STUDENT_STATUS_GRADUATED)
                        || CollUtil.isNotEmpty(studentClassMapper.selectCurrentListByStudentId(flow.getStudentId()))) {
                    throw exception(STUDENT_PROMOTION_TASK_ROLLBACK_STATE_INVALID);
                }
                continue;
            }
            throw exception(STUDENT_PROMOTION_TASK_NOT_ROLLBACKABLE);
        }
    }

    private void rollbackFlow(StudentFlowDO flow, StudentPromotionBatchDO batch,
                              Map<Long, SchoolYearDO> fromSchoolYearMap) {
        SchoolYearDO fromSchoolYear = fromSchoolYearMap.get(batch.getFromSchoolYearId());
        StudentClassDO sourceStudentClass = studentClassMapper.selectLatestEndedByStudentIdAndClassIdAndEndDate(
                flow.getStudentId(), flow.getFromClassId(), fromSchoolYear.getEndDate());
        if (Objects.equals(flow.getChangeType(), FLOW_TYPE_PROMOTE)
                || Objects.equals(flow.getChangeType(), FLOW_TYPE_REPEAT)) {
            StudentClassDO targetStudentClass = studentClassMapper.selectCurrentByStudentIdAndClassIdAndStartDate(
                    flow.getStudentId(), flow.getToClassId(), flow.getEffectiveDate());
            studentClassMapper.deletePhysicallyById(targetStudentClass.getId());
            studentClassMapper.restoreEndDateById(sourceStudentClass.getId());
            return;
        }
        if (Objects.equals(flow.getChangeType(), FLOW_TYPE_PENDING_ADVANCE)
                || Objects.equals(flow.getChangeType(), FLOW_TYPE_GRADUATE_LEGACY)) {
            studentMapper.updateStatusById(flow.getStudentId(), STUDENT_STATUS_READING);
            studentClassMapper.restoreEndDateById(sourceStudentClass.getId());
            return;
        }
        throw exception(STUDENT_PROMOTION_TASK_NOT_ROLLBACKABLE);
    }

    private void deleteAutoCreatedClassIfUnused(Long classId) {
        if (classId == null) {
            return;
        }
        if (studentClassMapper.countByClassId(classId) > 0) {
            return;
        }
        schoolClassMapper.deletePhysicallyById(classId);
    }

    private String appendRollbackRemark(String originRemark, String rollbackRemark) {
        if (rollbackRemark == null || rollbackRemark.isBlank()) {
            return originRemark;
        }
        String mergedRemark = (originRemark == null || originRemark.isBlank())
                ? rollbackRemark
                : originRemark + " | 回滚：" + rollbackRemark;
        return mergedRemark.length() > 255 ? mergedRemark.substring(0, 255) : mergedRemark;
    }

    private int countSchools(List<GlobalSchoolPreview> schools, String... statuses) {
        List<String> statusList = Arrays.asList(statuses);
        return Math.toIntExact(schools.stream().filter(item -> statusList.contains(item.getStatus())).count());
    }

    private int sumInt(List<GlobalSchoolPreview> schools, Function<GlobalSchoolPreview, Integer> getter) {
        return schools.stream().map(getter).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private Integer buildTaskStatus(List<GlobalSchoolPreview> schools) {
        int successCount = countSchools(schools, SCHOOL_STATUS_SUCCESS);
        int failedCount = countSchools(schools, SCHOOL_STATUS_FAILED);
        int skippedCount = countSchools(schools, SCHOOL_STATUS_SKIP);
        if (successCount == 0 && failedCount > 0) {
            return TASK_STATUS_FAILED;
        }
        if (failedCount > 0 || skippedCount > 0) {
            return TASK_STATUS_PARTIAL;
        }
        return TASK_STATUS_SUCCESS;
    }

    private String buildScopeSnapshot(StudentGlobalPromotionPreviewReqVO reqVO) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("scopeType", reqVO.getScopeType());
        snapshot.put("schoolIds", reqVO.getSchoolIds());
        snapshot.put("areaId", reqVO.getAreaId());
        return JsonUtils.toJsonString(snapshot);
    }

    private static class GlobalPreviewResult {

        private final List<GlobalSchoolPreview> schools;

        private GlobalPreviewResult(List<GlobalSchoolPreview> schools) {
            this.schools = schools;
        }

        public List<GlobalSchoolPreview> getSchools() {
            return schools;
        }
    }

    private static class GlobalSchoolPreview {

        private Long schoolId;
        private String schoolName;
        private Long fromSchoolYearId;
        private Long toSchoolYearId;
        private Long batchId;
        private String status;
        private String reason;
        private Integer totalCount;
        private Integer promotedCount;
        private Integer graduatedCount;
        private Integer repeatCount;
        private Integer skippedCount;
        private Integer missingTargetClassCount;
        private List<StudentPromotionItemRespVO> items;

        public Long getSchoolId() {
            return schoolId;
        }

        public void setSchoolId(Long schoolId) {
            this.schoolId = schoolId;
        }

        public String getSchoolName() {
            return schoolName;
        }

        public void setSchoolName(String schoolName) {
            this.schoolName = schoolName;
        }

        public Long getFromSchoolYearId() {
            return fromSchoolYearId;
        }

        public void setFromSchoolYearId(Long fromSchoolYearId) {
            this.fromSchoolYearId = fromSchoolYearId;
        }

        public Long getToSchoolYearId() {
            return toSchoolYearId;
        }

        public void setToSchoolYearId(Long toSchoolYearId) {
            this.toSchoolYearId = toSchoolYearId;
        }

        public Long getBatchId() {
            return batchId;
        }

        public void setBatchId(Long batchId) {
            this.batchId = batchId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public Integer getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }

        public Integer getPromotedCount() {
            return promotedCount;
        }

        public void setPromotedCount(Integer promotedCount) {
            this.promotedCount = promotedCount;
        }

        public Integer getGraduatedCount() {
            return graduatedCount;
        }

        public void setGraduatedCount(Integer graduatedCount) {
            this.graduatedCount = graduatedCount;
        }

        public Integer getRepeatCount() {
            return repeatCount;
        }

        public void setRepeatCount(Integer repeatCount) {
            this.repeatCount = repeatCount;
        }

        public Integer getSkippedCount() {
            return skippedCount;
        }

        public void setSkippedCount(Integer skippedCount) {
            this.skippedCount = skippedCount;
        }

        public Integer getMissingTargetClassCount() {
            return missingTargetClassCount;
        }

        public void setMissingTargetClassCount(Integer missingTargetClassCount) {
            this.missingTargetClassCount = missingTargetClassCount;
        }

        public List<StudentPromotionItemRespVO> getItems() {
            return items;
        }

        public void setItems(List<StudentPromotionItemRespVO> items) {
            this.items = items;
        }
    }

}
