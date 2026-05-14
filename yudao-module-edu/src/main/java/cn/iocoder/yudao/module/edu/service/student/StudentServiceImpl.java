package cn.iocoder.yudao.module.edu.service.student;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentOrderContextRespDTO;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentSubscriptionContextRespDTO;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.app.student.vo.AppStudentBindReqVO;
import cn.iocoder.yudao.module.edu.controller.app.student.vo.AppStudentBindRespVO;
import cn.iocoder.yudao.module.edu.controller.app.student.vo.AppStudentSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentClassRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentClassSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolGradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentFlowMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.studentclass.StudentClassMapper;
import cn.iocoder.yudao.module.edu.service.station.StationService;
import cn.iocoder.yudao.module.edu.service.school.SchoolGradeSequenceUtils;
import cn.iocoder.yudao.module.edu.dal.dataobject.station.StationDO;
import cn.iocoder.yudao.module.edu.enums.StudentStatusEnum;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.repo.dal.dataobject.warehouse.RepoWarehouseDO;
import cn.iocoder.yudao.module.repo.service.warehouse.RepoWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.GRADE_CATALOG_DISABLED;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.GRADE_CATALOG_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_CLASS_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_GRADE_NOT_BELONG_TO_SCHOOL;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_GRADE_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_BIND_BOUND_TO_OTHER_PARENT;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_BIND_CLASS_NOT_CURRENT;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_BIND_CURRENT_CLASS_MULTI;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_BIND_CURRENT_YEAR_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_BIND_DUPLICATE_STUDENT;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_DATE_OVERLAP;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_DUPLICATE_START_DATE;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_END_DATE_INVALID;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_ENTRY_YEAR_NOT_MATCH;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_MULTI_CURRENT;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_NOT_BELONG_TO_SCHOOL;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_IN_USE_BY_FLOW;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PARENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_STATUS_CURRENT_CLASS_FORBIDDEN;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_STATUS_READING_CURRENT_CLASS_REQUIRED;

/**
 * 学生 Service 实现类
 *
 * @author connor
 */
@Service
@Validated
public class StudentServiceImpl implements StudentService {

    private static final Integer STUDENT_STATUS_READING = StudentStatusEnum.READING.getStatus();
    private static final String GRADE_RESOLVE_SOURCE_TARGET_YEAR_CLASS = "TARGET_YEAR_CLASS";
    private static final String GRADE_RESOLVE_SOURCE_PROMOTED_FROM_CURRENT = "PROMOTED_FROM_CURRENT";
    private static final String STUDENT_BIND_RESULT_CREATED = "CREATED";
    private static final String STUDENT_BIND_RESULT_BOUND = "BOUND";
    private static final String STUDENT_BIND_RESULT_CONFIRM_REQUIRED = "CONFIRM_REQUIRED";
    private static final String STUDENT_BIND_CONFLICT_GRADE_MISMATCH = "GRADE_MISMATCH";
    private static final String STUDENT_BIND_CONFLICT_CLASS_MISMATCH = "CLASS_MISMATCH";
    private static final String STUDENT_BIND_CONFLICT_CLASS_MISSING = "CLASS_MISSING";

    @Resource
    private StudentMapper studentMapper;
    @Resource
    private StudentClassMapper studentClassMapper;
    @Resource
    private StudentFlowMapper studentFlowMapper;
    @Resource
    private SchoolMapper schoolMapper;
    @Resource
    private SchoolClassMapper schoolClassMapper;
    @Resource
    private SchoolGradeMapper schoolGradeMapper;
    @Resource
    private GradeCatalogMapper gradeCatalogMapper;
    @Resource
    private SchoolYearMapper schoolYearMapper;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private StationService stationService;
    @Resource
    private RepoWarehouseService warehouseService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStudent(StudentSaveReqVO createReqVO) {
        validateStudentSaveReqVO(createReqVO);

        StudentDO student = BeanUtils.toBean(createReqVO, StudentDO.class);
        studentMapper.insert(student);
        createStudentClassList(student.getId(), createReqVO.getStudentClasses());
        return student.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudent(StudentSaveReqVO updateReqVO) {
        validateStudentExists(updateReqVO.getId());
        validateStudentSaveReqVO(updateReqVO);

        StudentDO updateObj = BeanUtils.toBean(updateReqVO, StudentDO.class);
        studentMapper.updateById(updateObj);
        updateStudentClassList(updateReqVO.getId(), updateReqVO.getStudentClasses());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStudent(Long id) {
        validateStudentExists(id);
        validateStudentUnused(id);
        studentMapper.deleteById(id);
        deleteStudentClassByStudentId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStudentListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<Long> existedStudentIds = convertList(studentMapper.selectList(StudentDO::getId, ids), StudentDO::getId);
        if (CollUtil.isEmpty(existedStudentIds)) {
            return;
        }
        validateStudentUnused(existedStudentIds);
        studentMapper.deleteByIds(existedStudentIds);
        deleteStudentClassByStudentIds(existedStudentIds);
    }

    @Override
    public StudentRespVO getStudent(Long id) {
        StudentDO student = validateStudentExists(id);
        return buildStudentResp(student);
    }

    @Override
    public PageResult<StudentRespVO> getStudentPage(StudentPageReqVO pageReqVO) {
        PageResult<StudentDO> pageResult = studentMapper.selectPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return new PageResult<>(Collections.emptyList(), pageResult.getTotal());
        }
        return new PageResult<>(buildStudentRespList(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public List<AppStudentSimpleRespVO> getAppStudentSimpleList(Long belongTo) {
        List<StudentDO> students = studentMapper.selectListByBelongTo(belongTo);
        if (CollUtil.isEmpty(students)) {
            return Collections.emptyList();
        }
        Map<Long, SchoolDO> schoolMap = schoolMapper.selectList(SchoolDO::getId,
                        convertSet(students, StudentDO::getCurrentSchoolId))
                .stream()
                .collect(Collectors.toMap(SchoolDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, List<StudentClassDO>> currentStudentClassMap = studentClassMapper.selectCurrentListByStudentIds(
                        convertSet(students, StudentDO::getId))
                .stream()
                .collect(Collectors.groupingBy(StudentClassDO::getStudentId));
        Map<Long, SchoolClassDO> schoolClassMap = getSchoolClassMap(convertSet(currentStudentClassMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()), StudentClassDO::getClassId));
        Map<Long, SchoolGradeDO> schoolGradeMap = schoolGradeMapper.selectList(SchoolGradeDO::getId,
                        convertSet(schoolClassMap.values(), SchoolClassDO::getSchoolGradeId))
                .stream()
                .collect(Collectors.toMap(SchoolGradeDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, GradeCatalogDO> gradeCatalogMap = gradeCatalogMapper.selectList(GradeCatalogDO::getId,
                        convertSet(schoolGradeMap.values(), SchoolGradeDO::getGradeCatalogId))
                .stream()
                .collect(Collectors.toMap(GradeCatalogDO::getId, Function.identity(), (item1, item2) -> item1));
        return students.stream().map(student -> buildAppStudentSimpleResp(student, schoolMap, currentStudentClassMap,
                schoolClassMap, schoolGradeMap, gradeCatalogMap)).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppStudentBindRespVO bindAppStudent(Long belongTo, AppStudentBindReqVO reqVO) {
        validateParentExists(belongTo);
        LocalDate today = LocalDate.now();
        String studentName = StrUtil.trim(reqVO.getStudentName());
        StudentBindClassContext selectedContext = resolveStudentBindSelectedContext(reqVO, today);

        List<StudentDO> matchedStudents = studentMapper.selectSimpleListByExactStudentNameAndSchoolId(
                studentName, selectedContext.schoolId);
        if (CollUtil.isEmpty(matchedStudents)) {
            StudentDO student = StudentDO.builder()
                    .studentName(studentName)
                    .belongTo(belongTo)
                    .currentSchoolId(selectedContext.schoolId)
                    .entryYear(selectedContext.entryYear)
                    .status(STUDENT_STATUS_READING)
                    .build();
            student.clean();
            studentMapper.insert(student);
            createCurrentStudentClass(student.getId(), selectedContext, today);
            return buildStudentBindSuccessResp(STUDENT_BIND_RESULT_CREATED, student);
        }
        if (matchedStudents.size() > 1) {
            throw exception(STUDENT_BIND_DUPLICATE_STUDENT);
        }

        StudentDO student = matchedStudents.get(0);
        if (student.getBelongTo() != null && !Objects.equals(student.getBelongTo(), belongTo)) {
            throw exception(STUDENT_BIND_BOUND_TO_OTHER_PARENT);
        }

        List<StudentClassDO> currentStudentClasses = studentClassMapper.selectCurrentListByStudentId(student.getId());
        if (currentStudentClasses.size() > 1) {
            throw exception(STUDENT_BIND_CURRENT_CLASS_MULTI);
        }
        StudentBindClassContext currentContext = CollUtil.isEmpty(currentStudentClasses)
                ? null : resolveStudentBindCurrentContext(currentStudentClasses.get(0));
        String conflictType = getStudentBindConflictType(currentContext, selectedContext);
        if (conflictType != null && !Boolean.TRUE.equals(reqVO.getForceUpdate())) {
            return buildStudentBindConfirmResp(student, conflictType, currentContext, selectedContext);
        }
        if (conflictType != null) {
            replaceCurrentStudentClass(student.getId(), currentStudentClasses, selectedContext, today);
        }
        bindStudentToParent(student, belongTo, selectedContext);
        return buildStudentBindSuccessResp(STUDENT_BIND_RESULT_BOUND, student);
    }

    @Override
    public Map<Long, EduStudentOrderContextRespDTO> getOrderStudentContextMap(Long belongTo, Collection<Long> studentIds) {
        if (CollUtil.isEmpty(studentIds)) {
            return Collections.emptyMap();
        }
        List<StudentDO> students = studentMapper.selectList(StudentDO::getId, studentIds).stream()
                .filter(student -> Objects.equals(student.getBelongTo(), belongTo))
                .toList();
        if (CollUtil.isEmpty(students)) {
            return Collections.emptyMap();
        }
        Map<Long, SchoolDO> schoolMap = schoolMapper.selectList(SchoolDO::getId,
                        convertSet(students, StudentDO::getCurrentSchoolId))
                .stream()
                .collect(Collectors.toMap(SchoolDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, List<StudentClassDO>> currentStudentClassMap = studentClassMapper.selectCurrentListByStudentIds(
                        convertSet(students, StudentDO::getId))
                .stream()
                .collect(Collectors.groupingBy(StudentClassDO::getStudentId));
        Map<Long, SchoolClassDO> schoolClassMap = getSchoolClassMap(convertSet(currentStudentClassMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()), StudentClassDO::getClassId));
        Map<Long, SchoolGradeDO> schoolGradeMap = schoolGradeMapper.selectList(SchoolGradeDO::getId,
                        convertSet(schoolClassMap.values(), SchoolClassDO::getSchoolGradeId))
                .stream()
                .collect(Collectors.toMap(SchoolGradeDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, GradeCatalogDO> gradeCatalogMap = gradeCatalogMapper.selectList(GradeCatalogDO::getId,
                        convertSet(schoolGradeMap.values(), SchoolGradeDO::getGradeCatalogId))
                .stream()
                .collect(Collectors.toMap(GradeCatalogDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, StationDO> stationMap = stationService.getStationMap(
                convertSet(schoolMap.values(), SchoolDO::getStationId));
        Map<Long, RepoWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(schoolMap.values(), SchoolDO::getWarehouseId));
        return students.stream()
                .map(student -> buildOrderStudentContextResp(student, schoolMap, currentStudentClassMap,
                        schoolClassMap, schoolGradeMap, gradeCatalogMap, stationMap, warehouseMap))
                .collect(Collectors.toMap(EduStudentOrderContextRespDTO::getStudentId,
                        Function.identity(), (item1, item2) -> item1));
    }

    @Override
    public Map<Long, EduStudentSubscriptionContextRespDTO> getSubscriptionStudentContextMap(
            Long belongTo,
            Collection<Long> studentIds,
            Integer targetYearStart,
            Integer targetYearEnd,
            Long targetYearCatalogId) {
        if (CollUtil.isEmpty(studentIds)) {
            return Collections.emptyMap();
        }
        List<StudentDO> students = studentMapper.selectList(StudentDO::getId, studentIds).stream()
                .filter(student -> Objects.equals(student.getBelongTo(), belongTo))
                .toList();
        return buildSubscriptionStudentContextMap(students, targetYearStart, targetYearEnd, targetYearCatalogId);
    }

    @Override
    public Map<Long, EduStudentSubscriptionContextRespDTO> getAdminSubscriptionStudentContextMap(
            Collection<Long> studentIds,
            Integer targetYearStart,
            Integer targetYearEnd,
            Long targetYearCatalogId) {
        if (CollUtil.isEmpty(studentIds)) {
            return Collections.emptyMap();
        }
        List<StudentDO> students = studentMapper.selectList(StudentDO::getId, studentIds);
        return buildSubscriptionStudentContextMap(students, targetYearStart, targetYearEnd, targetYearCatalogId);
    }

    private Map<Long, EduStudentSubscriptionContextRespDTO> buildSubscriptionStudentContextMap(
            List<StudentDO> students,
            Integer targetYearStart,
            Integer targetYearEnd,
            Long targetYearCatalogId) {
        if (CollUtil.isEmpty(students)) {
            return Collections.emptyMap();
        }
        return students.stream()
                .map(student -> resolveSubscriptionStudentContext(student, targetYearStart, targetYearEnd,
                        targetYearCatalogId))
                .collect(Collectors.toMap(EduStudentSubscriptionContextRespDTO::getStudentId,
                        Function.identity(), (item1, item2) -> item1));
    }

    @Override
    public List<StudentClassRespVO> getStudentClassListByStudentId(Long studentId) {
        validateStudentExists(studentId);
        return buildStudentClassRespList(studentClassMapper.selectListByStudentId(studentId));
    }

    private EduStudentSubscriptionContextRespDTO resolveSubscriptionStudentContext(
            StudentDO student,
            Integer targetYearStart,
            Integer targetYearEnd,
            Long targetYearCatalogId) {
        EduStudentSubscriptionContextRespDTO respDTO = buildSubscriptionBaseContext(student, student.getCurrentSchoolId());
        List<StudentClassDO> targetClasses = targetYearCatalogId == null
                ? studentClassMapper.selectListByStudentIdsAndTargetYear(
                Collections.singleton(student.getId()), targetYearStart, targetYearEnd)
                : studentClassMapper.selectListByStudentIdsAndTargetYearCatalogId(
                Collections.singleton(student.getId()), targetYearCatalogId);
        if (targetClasses.size() == 1) {
            EduStudentSubscriptionContextRespDTO filled = fillSubscriptionClassAndGrade(respDTO, targetClasses.get(0));
            if (filled.getBlockedReason() == null) {
                filled.setGradeResolveSource(GRADE_RESOLVE_SOURCE_TARGET_YEAR_CLASS);
            }
            return filled;
        }
        if (targetClasses.size() > 1) {
            return blockSubscription(respDTO, "MULTI_TARGET_YEAR_CLASS", "目标学年存在多个班级关系");
        }
        return resolveSubscriptionPromotedFromCurrent(respDTO, student, targetYearCatalogId);
    }

    private EduStudentSubscriptionContextRespDTO resolveSubscriptionPromotedFromCurrent(
            EduStudentSubscriptionContextRespDTO respDTO, StudentDO student, Long targetYearCatalogId) {
        SchoolDO currentSchool = schoolMapper.selectById(student.getCurrentSchoolId());
        if (currentSchool == null) {
            return blockSubscription(respDTO, "SCHOOL_GRADE_NOT_EXISTS", "学校或年级不存在");
        }
        SchoolYearDO targetSchoolYear = targetYearCatalogId == null ? null
                : schoolYearMapper.selectBySchoolIdAndYearCatalogId(currentSchool.getId(), targetYearCatalogId);
        if (targetSchoolYear == null) {
            return blockSubscription(respDTO, "TARGET_SCHOOL_YEAR_NOT_CONFIGURED", "学校未配置目标学年");
        }
        if (targetSchoolYear.getStartDate() != null && !LocalDate.now().isBefore(targetSchoolYear.getStartDate())) {
            return blockSubscription(respDTO, "TARGET_YEAR_CLASS_NOT_READY", "目标学年班级未生成或学生未升班");
        }
        if (Objects.equals(student.getStatus(), StudentStatusEnum.PENDING_ADVANCE.getStatus())) {
            return blockSubscription(respDTO, "TARGET_YEAR_CLASS_REQUIRED", "待升学学生必须绑定目标学年班级");
        }
        if (!Objects.equals(student.getStatus(), StudentStatusEnum.READING.getStatus())) {
            return blockSubscription(respDTO, "STUDENT_STATUS_UNSUPPORTED", "学生状态不支持订刊");
        }
        List<StudentClassDO> currentClasses = studentClassMapper.selectCurrentListByStudentId(student.getId());
        if (currentClasses.size() != 1) {
            return blockSubscription(respDTO, "NO_CURRENT_CLASS", "学生未解析出唯一当前班级");
        }
        SchoolClassDO currentClass = schoolClassMapper.selectById(currentClasses.get(0).getClassId());
        if (currentClass == null || currentClass.getSchoolYearId() == null) {
            return blockSubscription(respDTO, "SCHOOL_GRADE_NOT_EXISTS", "学校或年级不存在");
        }
        SchoolYearDO currentSchoolYear = schoolYearMapper.selectById(currentClass.getSchoolYearId());
        if (currentSchoolYear == null || !isNextSchoolYear(currentSchoolYear, targetSchoolYear)) {
            return blockSubscription(respDTO, "TARGET_YEAR_NOT_NEXT", "目标学年不是当前学年的下一学年");
        }
        EduStudentSubscriptionContextRespDTO filled = fillSubscriptionClassAndGrade(respDTO, currentClasses.get(0));
        if (filled.getBlockedReason() != null) {
            return filled;
        }
        EduStudentSubscriptionContextRespDTO promoted = fillPromotedGrade(filled, currentSchool.getId());
        if (promoted.getBlockedReason() == null) {
            promoted.setGradeResolveSource(GRADE_RESOLVE_SOURCE_PROMOTED_FROM_CURRENT);
        }
        return promoted;
    }

    private boolean isNextSchoolYear(SchoolYearDO currentSchoolYear, SchoolYearDO targetSchoolYear) {
        return currentSchoolYear.getYearStart() != null && currentSchoolYear.getYearEnd() != null
                && targetSchoolYear.getYearStart() != null && targetSchoolYear.getYearEnd() != null
                && targetSchoolYear.getYearStart() == currentSchoolYear.getYearStart() + 1
                && targetSchoolYear.getYearEnd() == currentSchoolYear.getYearEnd() + 1;
    }

    private EduStudentSubscriptionContextRespDTO buildSubscriptionBaseContext(StudentDO student, Long schoolId) {
        EduStudentSubscriptionContextRespDTO respDTO = new EduStudentSubscriptionContextRespDTO();
        respDTO.setStudentId(student.getId());
        respDTO.setStudentName(student.getStudentName());
        respDTO.setStatus(student.getStatus());
        fillSubscriptionSchool(respDTO, schoolId);
        return respDTO;
    }

    private void fillSubscriptionSchool(EduStudentSubscriptionContextRespDTO respDTO, Long schoolId) {
        SchoolDO school = schoolId == null ? null : schoolMapper.selectById(schoolId);
        if (school == null) {
            return;
        }
        respDTO.setSchoolId(school.getId());
        respDTO.setSchoolName(school.getSchoolName());
        respDTO.setSchoolAddress(school.getSchoolAddress());
        fillSubscriptionWarehouse(respDTO, school);
        if (school.getStationId() == null) {
            return;
        }
        Map<Long, StationDO> stationMap = stationService.getStationMap(Collections.singleton(school.getStationId()));
        StationDO station = stationMap.get(school.getStationId());
        if (station == null) {
            return;
        }
        respDTO.setStationId(station.getId());
        respDTO.setStationName(station.getStationName());
        respDTO.setStationAddress(station.getStationAddress());
        respDTO.setContactName(station.getContactName());
        respDTO.setContactMobile(station.getContactMobile());
    }

    private void fillSubscriptionWarehouse(EduStudentSubscriptionContextRespDTO respDTO, SchoolDO school) {
        if (school.getWarehouseId() == null) {
            return;
        }
        RepoWarehouseDO warehouse = warehouseService.getWarehouseMap(Collections.singleton(school.getWarehouseId()))
                .get(school.getWarehouseId());
        if (warehouse == null) {
            return;
        }
        respDTO.setWarehouseId(warehouse.getId());
        respDTO.setWarehouseName(warehouse.getName());
        respDTO.setWarehouseAddress(warehouse.getAddress());
        respDTO.setWarehousePrincipal(warehouse.getPrincipal());
    }

    private EduStudentSubscriptionContextRespDTO fillSubscriptionClassAndGrade(
            EduStudentSubscriptionContextRespDTO respDTO, StudentClassDO studentClass) {
        SchoolClassDO schoolClass = schoolClassMapper.selectById(studentClass.getClassId());
        if (schoolClass == null) {
            return blockSubscription(respDTO, "SCHOOL_GRADE_NOT_EXISTS", "学校或年级不存在");
        }
        fillSubscriptionSchool(respDTO, schoolClass.getSchoolId());
        respDTO.setClassId(schoolClass.getId());
        respDTO.setClassName(schoolClass.getClassName());
        SchoolGradeDO schoolGrade = schoolGradeMapper.selectById(schoolClass.getSchoolGradeId());
        if (schoolGrade == null) {
            return blockSubscription(respDTO, "SCHOOL_GRADE_NOT_EXISTS", "学校或年级不存在");
        }
        GradeCatalogDO gradeCatalog = gradeCatalogMapper.selectById(schoolGrade.getGradeCatalogId());
        if (gradeCatalog == null || !CommonStatusEnum.isEnable(gradeCatalog.getStatus())) {
            return blockSubscription(respDTO, "SCHOOL_GRADE_NOT_EXISTS", "学校或年级不存在");
        }
        return fillSubscriptionGrade(respDTO, gradeCatalog);
    }

    private EduStudentSubscriptionContextRespDTO fillPromotedGrade(EduStudentSubscriptionContextRespDTO respDTO,
                                                                  Long schoolId) {
        if (respDTO.getGradeCatalogId() == null || schoolId == null) {
            return blockSubscription(respDTO, "SCHOOL_GRADE_NOT_EXISTS", "学校或年级不存在");
        }
        List<GradeCatalogDO> enabledGrades = gradeCatalogMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        Map<Long, Long> nextGlobalGradeMap = SchoolGradeSequenceUtils.buildNextGradeCatalogIdMap(enabledGrades);
        Long expectedNextGradeCatalogId = nextGlobalGradeMap.get(respDTO.getGradeCatalogId());
        if (expectedNextGradeCatalogId == null) {
            return blockSubscription(respDTO, "TERMINAL_GRADE_PROMOTION_UNSUPPORTED", "终端年级不支持升学订刊");
        }
        List<SchoolGradeDO> schoolGrades = schoolGradeMapper.selectListBySchoolId(schoolId);
        Map<Long, GradeCatalogDO> gradeCatalogMap = enabledGrades.stream()
                .collect(Collectors.toMap(GradeCatalogDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, SchoolGradeDO> nextSchoolGradeMap =
                SchoolGradeSequenceUtils.buildNextSchoolGradeMap(schoolGrades, gradeCatalogMap);
        SchoolGradeDO currentSchoolGrade = schoolGradeMapper.selectBySchoolIdAndGradeCatalogId(schoolId,
                respDTO.getGradeCatalogId());
        SchoolGradeDO nextSchoolGrade = currentSchoolGrade == null ? null : nextSchoolGradeMap.get(currentSchoolGrade.getId());
        if (nextSchoolGrade == null || !Objects.equals(nextSchoolGrade.getGradeCatalogId(), expectedNextGradeCatalogId)) {
            return blockSubscription(respDTO, "NEXT_GRADE_NOT_ENABLED", "学校未启用连续下一年级");
        }
        GradeCatalogDO nextGradeCatalog = gradeCatalogMap.get(expectedNextGradeCatalogId);
        if (nextGradeCatalog == null) {
            return blockSubscription(respDTO, "SCHOOL_GRADE_NOT_EXISTS", "学校或年级不存在");
        }
        return fillSubscriptionGrade(respDTO, nextGradeCatalog);
    }

    private EduStudentSubscriptionContextRespDTO fillSubscriptionGrade(
            EduStudentSubscriptionContextRespDTO respDTO, GradeCatalogDO gradeCatalog) {
        respDTO.setGradeCatalogId(gradeCatalog.getId());
        respDTO.setGradeNo(gradeCatalog.getGradeNo());
        respDTO.setGradeName(gradeCatalog.getGradeName());
        respDTO.setGradeAliasName(gradeCatalog.getAliasName());
        respDTO.setGradeSort(gradeCatalog.getSort());
        return respDTO;
    }

    private EduStudentSubscriptionContextRespDTO blockSubscription(
            EduStudentSubscriptionContextRespDTO respDTO, String reason, String reasonDesc) {
        respDTO.setBlockedReason(reason);
        respDTO.setBlockedReasonDesc(reasonDesc);
        return respDTO;
    }

    private void validateStudentSaveReqVO(StudentSaveReqVO reqVO) {
        validateParentExists(reqVO.getBelongTo());
        validateSchoolExists(reqVO.getCurrentSchoolId());
        validateStudentClassList(reqVO.getCurrentSchoolId(), reqVO.getEntryYear(), reqVO.getStudentClasses());
        validateStudentStatusMatchesStudentClassList(reqVO.getStatus(), reqVO.getStudentClasses());
    }

    private void validateParentExists(Long parentId) {
        if (memberUserApi.getUser(parentId) == null) {
            throw exception(STUDENT_PARENT_NOT_EXISTS);
        }
    }

    private void validateSchoolExists(Long schoolId) {
        if (schoolMapper.selectById(schoolId) == null) {
            throw exception(SCHOOL_NOT_EXISTS);
        }
    }

    private StudentDO validateStudentExists(Long id) {
        StudentDO student = studentMapper.selectById(id);
        if (student == null) {
            throw exception(STUDENT_NOT_EXISTS);
        }
        return student;
    }

    private StudentBindClassContext resolveStudentBindSelectedContext(AppStudentBindReqVO reqVO, LocalDate today) {
        SchoolDO school = schoolMapper.selectById(reqVO.getSchoolId());
        if (school == null) {
            throw exception(SCHOOL_NOT_EXISTS);
        }
        SchoolGradeDO schoolGrade = schoolGradeMapper.selectById(reqVO.getSchoolGradeId());
        if (schoolGrade == null) {
            throw exception(SCHOOL_GRADE_NOT_EXISTS);
        }
        if (!Objects.equals(schoolGrade.getSchoolId(), school.getId())) {
            throw exception(SCHOOL_GRADE_NOT_BELONG_TO_SCHOOL);
        }
        GradeCatalogDO gradeCatalog = validateGradeCatalogEnabled(schoolGrade.getGradeCatalogId());
        SchoolYearDO currentSchoolYear = schoolYearMapper.selectCurrentBySchoolId(school.getId(), today);
        if (currentSchoolYear == null) {
            throw exception(STUDENT_BIND_CURRENT_YEAR_NOT_EXISTS);
        }
        SchoolClassDO schoolClass = schoolClassMapper.selectById(reqVO.getClassId());
        if (schoolClass == null) {
            throw exception(SCHOOL_CLASS_NOT_EXISTS);
        }
        if (!Objects.equals(schoolClass.getSchoolId(), school.getId())
                || !Objects.equals(schoolClass.getSchoolGradeId(), schoolGrade.getId())
                || !Objects.equals(schoolClass.getSchoolYearId(), currentSchoolYear.getId())) {
            throw exception(STUDENT_BIND_CLASS_NOT_CURRENT);
        }
        return new StudentBindClassContext(school, schoolGrade, gradeCatalog, schoolClass);
    }

    private StudentBindClassContext resolveStudentBindCurrentContext(StudentClassDO studentClass) {
        SchoolClassDO schoolClass = schoolClassMapper.selectById(studentClass.getClassId());
        if (schoolClass == null) {
            throw exception(SCHOOL_CLASS_NOT_EXISTS);
        }
        SchoolDO school = schoolMapper.selectById(schoolClass.getSchoolId());
        if (school == null) {
            throw exception(SCHOOL_NOT_EXISTS);
        }
        SchoolGradeDO schoolGrade = schoolGradeMapper.selectById(schoolClass.getSchoolGradeId());
        if (schoolGrade == null) {
            throw exception(SCHOOL_GRADE_NOT_EXISTS);
        }
        GradeCatalogDO gradeCatalog = validateGradeCatalogEnabled(schoolGrade.getGradeCatalogId());
        return new StudentBindClassContext(school, schoolGrade, gradeCatalog, schoolClass);
    }

    private GradeCatalogDO validateGradeCatalogEnabled(Long id) {
        GradeCatalogDO gradeCatalog = gradeCatalogMapper.selectById(id);
        if (gradeCatalog == null) {
            throw exception(GRADE_CATALOG_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(gradeCatalog.getStatus())) {
            throw exception(GRADE_CATALOG_DISABLED);
        }
        return gradeCatalog;
    }

    private String getStudentBindConflictType(StudentBindClassContext currentContext,
                                              StudentBindClassContext selectedContext) {
        if (currentContext == null) {
            return STUDENT_BIND_CONFLICT_CLASS_MISSING;
        }
        if (!Objects.equals(currentContext.gradeCatalogId, selectedContext.gradeCatalogId)) {
            return STUDENT_BIND_CONFLICT_GRADE_MISMATCH;
        }
        if (!Objects.equals(currentContext.classId, selectedContext.classId)) {
            return STUDENT_BIND_CONFLICT_CLASS_MISMATCH;
        }
        return null;
    }

    private AppStudentBindRespVO buildStudentBindConfirmResp(StudentDO student,
                                                             String conflictType,
                                                             StudentBindClassContext currentContext,
                                                             StudentBindClassContext selectedContext) {
        AppStudentBindRespVO respVO = new AppStudentBindRespVO();
        respVO.setResult(STUDENT_BIND_RESULT_CONFIRM_REQUIRED);
        respVO.setStudentId(student.getId());
        respVO.setStudentName(student.getStudentName());
        respVO.setConflictType(conflictType);
        respVO.setMessage(buildStudentBindConfirmMessage(conflictType, currentContext, selectedContext));
        fillStudentBindCurrentContext(respVO, currentContext);
        fillStudentBindSelectedContext(respVO, selectedContext);
        return respVO;
    }

    private String buildStudentBindConfirmMessage(String conflictType,
                                                  StudentBindClassContext currentContext,
                                                  StudentBindClassContext selectedContext) {
        if (Objects.equals(conflictType, STUDENT_BIND_CONFLICT_GRADE_MISMATCH)) {
            return String.format("系统中该学生当前为%s，您选择的是%s。确认后将按所选年级和班级更新学生当前班级。",
                    currentContext.gradeName, selectedContext.gradeName);
        }
        if (Objects.equals(conflictType, STUDENT_BIND_CONFLICT_CLASS_MISMATCH)) {
            return String.format("系统中该学生当前班级为%s，您选择的是%s。确认后将更新学生当前班级。",
                    currentContext.className, selectedContext.className);
        }
        return "系统中该学生暂无当前班级。确认后将按所选班级建立当前班级记录。";
    }

    private void fillStudentBindCurrentContext(AppStudentBindRespVO respVO, StudentBindClassContext currentContext) {
        if (currentContext == null) {
            return;
        }
        respVO.setCurrentSchoolGradeId(currentContext.schoolGradeId);
        respVO.setCurrentGradeCatalogId(currentContext.gradeCatalogId);
        respVO.setCurrentGradeName(currentContext.gradeName);
        respVO.setCurrentClassId(currentContext.classId);
        respVO.setCurrentClassName(currentContext.className);
    }

    private void fillStudentBindSelectedContext(AppStudentBindRespVO respVO, StudentBindClassContext selectedContext) {
        respVO.setSelectedSchoolGradeId(selectedContext.schoolGradeId);
        respVO.setSelectedGradeCatalogId(selectedContext.gradeCatalogId);
        respVO.setSelectedGradeName(selectedContext.gradeName);
        respVO.setSelectedClassId(selectedContext.classId);
        respVO.setSelectedClassName(selectedContext.className);
    }

    private void replaceCurrentStudentClass(Long studentId,
                                            List<StudentClassDO> currentStudentClasses,
                                            StudentBindClassContext selectedContext,
                                            LocalDate today) {
        for (StudentClassDO currentStudentClass : currentStudentClasses) {
            if (currentStudentClass.getStartDate() != null && currentStudentClass.getStartDate().isBefore(today)) {
                StudentClassDO updateObj = StudentClassDO.builder()
                        .id(currentStudentClass.getId())
                        .endDate(today.minusDays(1))
                        .build();
                studentClassMapper.updateById(updateObj);
            } else {
                studentClassMapper.deletePhysicallyById(currentStudentClass.getId());
            }
        }
        createCurrentStudentClass(studentId, selectedContext, today);
    }

    private void createCurrentStudentClass(Long studentId, StudentBindClassContext selectedContext, LocalDate today) {
        StudentClassDO studentClass = StudentClassDO.builder()
                .studentId(studentId)
                .classId(selectedContext.classId)
                .startDate(today)
                .endDate(null)
                .build();
        studentClass.clean();
        studentClassMapper.insert(studentClass);
    }

    private void bindStudentToParent(StudentDO student, Long belongTo, StudentBindClassContext selectedContext) {
        StudentDO updateObj = StudentDO.builder()
                .id(student.getId())
                .belongTo(belongTo)
                .currentSchoolId(selectedContext.schoolId)
                .entryYear(selectedContext.entryYear)
                .status(STUDENT_STATUS_READING)
                .build();
        studentMapper.updateById(updateObj);
        student.setBelongTo(belongTo);
        student.setCurrentSchoolId(selectedContext.schoolId);
        student.setEntryYear(selectedContext.entryYear);
        student.setStatus(STUDENT_STATUS_READING);
    }

    private AppStudentBindRespVO buildStudentBindSuccessResp(String result, StudentDO student) {
        AppStudentBindRespVO respVO = new AppStudentBindRespVO();
        respVO.setResult(result);
        respVO.setStudentId(student.getId());
        respVO.setStudentName(student.getStudentName());
        return respVO;
    }

    private void validateStudentUnused(Long studentId) {
        if (studentFlowMapper.countByStudentId(studentId) > 0) {
            throw exception(STUDENT_IN_USE_BY_FLOW);
        }
    }

    private void validateStudentUnused(List<Long> studentIds) {
        if (studentFlowMapper.countByStudentIds(studentIds) > 0) {
            throw exception(STUDENT_IN_USE_BY_FLOW);
        }
    }

    private void validateStudentClassList(Long schoolId, Integer entryYear, List<StudentClassSaveReqVO> studentClasses) {
        if (CollUtil.isEmpty(studentClasses)) {
            return;
        }

        LocalDate today = LocalDate.now();
        Set<LocalDate> startDates = new HashSet<>();
        long currentClassCount = 0L;
        Set<Long> classIds = convertSet(studentClasses, StudentClassSaveReqVO::getClassId);
        Map<Long, SchoolClassDO> schoolClassMap = getSchoolClassMap(classIds);
        if (schoolClassMap.size() != classIds.size()) {
            throw exception(SCHOOL_CLASS_NOT_EXISTS);
        }
        for (StudentClassSaveReqVO studentClass : studentClasses) {
            if (!startDates.add(studentClass.getStartDate())) {
                throw exception(STUDENT_CLASS_DUPLICATE_START_DATE);
            }
            if (isCurrentStudentClass(studentClass, today)) {
                currentClassCount++;
            } else if (studentClass.getEndDate() != null
                    && studentClass.getEndDate().isBefore(studentClass.getStartDate())) {
                throw exception(STUDENT_CLASS_END_DATE_INVALID);
            }

            SchoolClassDO schoolClass = schoolClassMap.get(studentClass.getClassId());
            if (!Objects.equals(schoolClass.getSchoolId(), schoolId)) {
                throw exception(STUDENT_CLASS_NOT_BELONG_TO_SCHOOL);
            }
            if (!Objects.equals(schoolClass.getEntryYear(), entryYear)) {
                throw exception(STUDENT_CLASS_ENTRY_YEAR_NOT_MATCH);
            }
        }
        if (currentClassCount > 1) {
            throw exception(STUDENT_CLASS_MULTI_CURRENT);
        }
        validateStudentClassDateRange(studentClasses);
    }

    private void validateStudentStatusMatchesStudentClassList(Integer status, List<StudentClassSaveReqVO> studentClasses) {
        LocalDate today = LocalDate.now();
        long currentClassCount = studentClasses == null ? 0L
                : studentClasses.stream().filter(item -> isCurrentStudentClass(item, today)).count();
        if (Objects.equals(status, STUDENT_STATUS_READING) && currentClassCount == 0) {
            throw exception(STUDENT_STATUS_READING_CURRENT_CLASS_REQUIRED);
        }
        if (!Objects.equals(status, STUDENT_STATUS_READING) && currentClassCount > 0) {
            throw exception(STUDENT_STATUS_CURRENT_CLASS_FORBIDDEN);
        }
    }

    private boolean isCurrentStudentClass(StudentClassSaveReqVO studentClass, LocalDate today) {
        if (studentClass.getStartDate() == null || studentClass.getStartDate().isAfter(today)) {
            return false;
        }
        return studentClass.getEndDate() == null || !studentClass.getEndDate().isBefore(today);
    }

    private void validateStudentClassDateRange(List<StudentClassSaveReqVO> studentClasses) {
        List<StudentClassSaveReqVO> sortedStudentClasses = new ArrayList<>(studentClasses);
        sortedStudentClasses.sort(Comparator.comparing(StudentClassSaveReqVO::getStartDate));
        for (int i = 1; i < sortedStudentClasses.size(); i++) {
            StudentClassSaveReqVO previous = sortedStudentClasses.get(i - 1);
            StudentClassSaveReqVO current = sortedStudentClasses.get(i);
            if (previous.getEndDate() == null || !current.getStartDate().isAfter(previous.getEndDate())) {
                throw exception(STUDENT_CLASS_DATE_OVERLAP);
            }
        }
    }

    private Map<Long, SchoolClassDO> getSchoolClassMap(Set<Long> classIds) {
        if (CollUtil.isEmpty(classIds)) {
            return Collections.emptyMap();
        }
        return schoolClassMapper.selectList(SchoolClassDO::getId, classIds).stream()
                .collect(Collectors.toMap(SchoolClassDO::getId,
                        Function.identity(), (item1, item2) -> item1));
    }

    private List<StudentRespVO> buildStudentRespList(List<StudentDO> students) {
        Map<Long, MemberUserRespDTO> parentMap = memberUserApi.getUserMap(convertSet(students, StudentDO::getBelongTo));
        Map<Long, SchoolDO> schoolMap = schoolMapper.selectList(SchoolDO::getId, convertSet(students, StudentDO::getCurrentSchoolId))
                .stream()
                .collect(Collectors.toMap(SchoolDO::getId, Function.identity(), (item1, item2) -> item1));
        return students.stream()
                .map(student -> buildStudentResp(student, parentMap.get(student.getBelongTo()), schoolMap.get(student.getCurrentSchoolId())))
                .collect(Collectors.toList());
    }

    private StudentRespVO buildStudentResp(StudentDO student) {
        MemberUserRespDTO parent = memberUserApi.getUser(student.getBelongTo());
        SchoolDO school = schoolMapper.selectById(student.getCurrentSchoolId());
        return buildStudentResp(student, parent, school);
    }

    private StudentRespVO buildStudentResp(StudentDO student, MemberUserRespDTO parent, SchoolDO school) {
        StudentRespVO respVO = BeanUtils.toBean(student, StudentRespVO.class);
        if (parent != null) {
            respVO.setParentNickname(parent.getNickname());
            respVO.setParentMobile(parent.getMobile());
        }
        if (school != null) {
            respVO.setCurrentSchoolName(school.getSchoolName());
        }
        return respVO;
    }

    private AppStudentSimpleRespVO buildAppStudentSimpleResp(StudentDO student,
                                                             Map<Long, SchoolDO> schoolMap,
                                                             Map<Long, List<StudentClassDO>> currentStudentClassMap,
                                                             Map<Long, SchoolClassDO> schoolClassMap,
                                                             Map<Long, SchoolGradeDO> schoolGradeMap,
                                                             Map<Long, GradeCatalogDO> gradeCatalogMap) {
        AppStudentSimpleRespVO respVO = new AppStudentSimpleRespVO();
        respVO.setId(student.getId());
        respVO.setStudentName(student.getStudentName());
        respVO.setCurrentSchoolId(student.getCurrentSchoolId());
        respVO.setStatus(student.getStatus());
        SchoolDO school = schoolMap.get(student.getCurrentSchoolId());
        respVO.setCurrentSchoolName(school == null ? null : school.getSchoolName());
        List<StudentClassDO> currentStudentClasses = currentStudentClassMap.get(student.getId());
        if (CollUtil.size(currentStudentClasses) != 1) {
            return respVO;
        }
        SchoolClassDO schoolClass = schoolClassMap.get(currentStudentClasses.get(0).getClassId());
        if (schoolClass == null) {
            return respVO;
        }
        respVO.setClassName(schoolClass.getClassName());
        SchoolGradeDO schoolGrade = schoolGradeMap.get(schoolClass.getSchoolGradeId());
        if (schoolGrade == null) {
            return respVO;
        }
        GradeCatalogDO gradeCatalog = gradeCatalogMap.get(schoolGrade.getGradeCatalogId());
        respVO.setGradeName(gradeCatalog == null ? null : gradeCatalog.getGradeName());
        return respVO;
    }

    private EduStudentOrderContextRespDTO buildOrderStudentContextResp(StudentDO student,
                                                                       Map<Long, SchoolDO> schoolMap,
                                                                       Map<Long, List<StudentClassDO>> currentStudentClassMap,
                                                                       Map<Long, SchoolClassDO> schoolClassMap,
                                                                       Map<Long, SchoolGradeDO> schoolGradeMap,
                                                                       Map<Long, GradeCatalogDO> gradeCatalogMap,
                                                                       Map<Long, StationDO> stationMap,
                                                                       Map<Long, RepoWarehouseDO> warehouseMap) {
        EduStudentOrderContextRespDTO respDTO = new EduStudentOrderContextRespDTO();
        respDTO.setStudentId(student.getId());
        respDTO.setStudentName(student.getStudentName());
        respDTO.setStatus(student.getStatus());
        SchoolDO school = schoolMap.get(student.getCurrentSchoolId());
        if (school != null) {
            respDTO.setSchoolId(school.getId());
            respDTO.setSchoolName(school.getSchoolName());
            respDTO.setSchoolAddress(school.getSchoolAddress());
            StationDO station = stationMap.get(school.getStationId());
            if (station != null) {
                respDTO.setStationId(station.getId());
                respDTO.setStationName(station.getStationName());
                respDTO.setStationAddress(station.getStationAddress());
                respDTO.setContactName(station.getContactName());
                respDTO.setContactMobile(station.getContactMobile());
            }
            RepoWarehouseDO warehouse = warehouseMap.get(school.getWarehouseId());
            if (warehouse != null) {
                respDTO.setWarehouseId(warehouse.getId());
                respDTO.setWarehouseName(warehouse.getName());
                respDTO.setWarehouseAddress(warehouse.getAddress());
                respDTO.setWarehousePrincipal(warehouse.getPrincipal());
            }
        }
        List<StudentClassDO> currentStudentClasses = currentStudentClassMap.get(student.getId());
        if (CollUtil.size(currentStudentClasses) != 1) {
            return respDTO;
        }
        SchoolClassDO schoolClass = schoolClassMap.get(currentStudentClasses.get(0).getClassId());
        if (schoolClass == null) {
            return respDTO;
        }
        respDTO.setClassId(schoolClass.getId());
        respDTO.setClassName(schoolClass.getClassName());
        SchoolGradeDO schoolGrade = schoolGradeMap.get(schoolClass.getSchoolGradeId());
        if (schoolGrade == null) {
            return respDTO;
        }
        GradeCatalogDO gradeCatalog = gradeCatalogMap.get(schoolGrade.getGradeCatalogId());
        if (gradeCatalog == null) {
            return respDTO;
        }
        respDTO.setGradeCatalogId(gradeCatalog.getId());
        respDTO.setGradeName(gradeCatalog.getGradeName());
        return respDTO;
    }

    private List<StudentClassRespVO> buildStudentClassRespList(List<StudentClassDO> studentClasses) {
        if (CollUtil.isEmpty(studentClasses)) {
            return Collections.emptyList();
        }
        Map<Long, SchoolClassDO> schoolClassMap =
                getSchoolClassMap(convertSet(studentClasses, StudentClassDO::getClassId));
        return studentClasses.stream().map(studentClass -> {
            StudentClassRespVO respVO = BeanUtils.toBean(studentClass, StudentClassRespVO.class);
            SchoolClassDO schoolClass = schoolClassMap.get(studentClass.getClassId());
            if (schoolClass != null) {
                respVO.setClassName(schoolClass.getClassName());
            }
            return respVO;
        }).collect(Collectors.toList());
    }

    // ==================== 子表（学生班级记录） ====================

    private void createStudentClassList(Long studentId, List<StudentClassSaveReqVO> list) {
        List<StudentClassDO> studentClasses = buildStudentClassDOList(studentId, list);
        if (CollUtil.isEmpty(studentClasses)) {
            return;
        }
        studentClassMapper.insertBatch(studentClasses);
    }

    private void updateStudentClassList(Long studentId, List<StudentClassSaveReqVO> list) {
        List<StudentClassDO> oldList = studentClassMapper.selectListByStudentId(studentId);
        Map<Long, StudentClassDO> oldMap = oldList.stream()
                .collect(Collectors.toMap(StudentClassDO::getId, Function.identity(), (item1, item2) -> item1));
        List<StudentClassDO> newList = buildStudentClassDOList(studentId, list);
        for (StudentClassDO studentClass : newList) {
            if (studentClass.getId() == null) {
                continue;
            }
            if (!oldMap.containsKey(studentClass.getId())) {
                throw exception(STUDENT_CLASS_RECORD_NOT_EXISTS);
            }
        }

        List<List<StudentClassDO>> classDiffList = diffList(oldList, newList, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getId(), newVal.getId());
            if (same) {
                newVal.setId(oldVal.getId()).clean();
            }
            return same;
        });
        if (CollUtil.isNotEmpty(classDiffList.get(0))) {
            studentClassMapper.insertBatch(classDiffList.get(0));
        }
        if (CollUtil.isNotEmpty(classDiffList.get(1))) {
            studentClassMapper.updateBatch(classDiffList.get(1));
        }
        if (CollUtil.isNotEmpty(classDiffList.get(2))) {
            studentClassMapper.deletePhysicallyByIds(convertList(classDiffList.get(2), StudentClassDO::getId));
        }
    }

    private List<StudentClassDO> buildStudentClassDOList(Long studentId, List<StudentClassSaveReqVO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(studentClass -> {
            StudentClassDO studentClassDO = BeanUtils.toBean(studentClass, StudentClassDO.class);
            studentClassDO.setStudentId(studentId);
            studentClassDO.clean();
            return studentClassDO;
        }).collect(Collectors.toList());
    }

    private void deleteStudentClassByStudentId(Long studentId) {
        studentClassMapper.deletePhysicallyByStudentId(studentId);
    }

    private void deleteStudentClassByStudentIds(List<Long> studentIds) {
        studentClassMapper.deletePhysicallyByStudentIds(studentIds);
    }

    private static class StudentBindClassContext {

        private final Long schoolId;
        private final Long schoolGradeId;
        private final Long gradeCatalogId;
        private final Long classId;
        private final Integer entryYear;
        private final String gradeName;
        private final String className;

        private StudentBindClassContext(SchoolDO school, SchoolGradeDO schoolGrade,
                                        GradeCatalogDO gradeCatalog, SchoolClassDO schoolClass) {
            this.schoolId = school.getId();
            this.schoolGradeId = schoolGrade.getId();
            this.gradeCatalogId = gradeCatalog.getId();
            this.classId = schoolClass.getId();
            this.entryYear = schoolClass.getEntryYear();
            this.gradeName = gradeCatalog.getGradeName();
            this.className = schoolClass.getClassName();
        }

    }

}
