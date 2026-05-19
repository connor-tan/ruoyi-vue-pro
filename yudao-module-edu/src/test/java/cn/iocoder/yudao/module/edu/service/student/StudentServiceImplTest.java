package cn.iocoder.yudao.module.edu.service.student;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.edu.controller.app.student.vo.AppStudentBindReqVO;
import cn.iocoder.yudao.module.edu.controller.app.student.vo.AppStudentBindRespVO;
import cn.iocoder.yudao.module.edu.controller.app.student.vo.AppStudentSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentClassSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentSubscriptionContextRespDTO;
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
import cn.iocoder.yudao.module.edu.enums.StudentStatusEnum;
import cn.iocoder.yudao.module.edu.service.station.StationService;
import cn.iocoder.yudao.module.edu.service.student.bo.StudentWaitingEntryActivateRespBO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
class StudentServiceImplTest {

    private StudentServiceImpl service;
    private StudentMapper studentMapper;
    private StudentClassMapper studentClassMapper;
    private StudentFlowMapper studentFlowMapper;
    private SchoolMapper schoolMapper;
    private SchoolClassMapper schoolClassMapper;
    private SchoolGradeMapper schoolGradeMapper;
    private GradeCatalogMapper gradeCatalogMapper;
    private SchoolYearMapper schoolYearMapper;
    private StationService stationService;
    private MemberUserApi memberUserApi;

    @BeforeEach
    void setUp() {
        service = new StudentServiceImpl();
        studentMapper = mock(StudentMapper.class);
        studentClassMapper = mock(StudentClassMapper.class);
        studentFlowMapper = mock(StudentFlowMapper.class);
        schoolMapper = mock(SchoolMapper.class);
        schoolClassMapper = mock(SchoolClassMapper.class);
        schoolGradeMapper = mock(SchoolGradeMapper.class);
        gradeCatalogMapper = mock(GradeCatalogMapper.class);
        schoolYearMapper = mock(SchoolYearMapper.class);
        stationService = mock(StationService.class);
        memberUserApi = mock(MemberUserApi.class);
        ReflectionTestUtils.setField(service, "studentMapper", studentMapper);
        ReflectionTestUtils.setField(service, "studentClassMapper", studentClassMapper);
        ReflectionTestUtils.setField(service, "studentFlowMapper", studentFlowMapper);
        ReflectionTestUtils.setField(service, "schoolMapper", schoolMapper);
        ReflectionTestUtils.setField(service, "schoolClassMapper", schoolClassMapper);
        ReflectionTestUtils.setField(service, "schoolGradeMapper", schoolGradeMapper);
        ReflectionTestUtils.setField(service, "gradeCatalogMapper", gradeCatalogMapper);
        ReflectionTestUtils.setField(service, "schoolYearMapper", schoolYearMapper);
        ReflectionTestUtils.setField(service, "stationService", stationService);
        ReflectionTestUtils.setField(service, "memberUserApi", memberUserApi);
    }

    @Test
    void createStudent_shouldRejectNullStatus() {
        StudentSaveReqVO reqVO = studentSaveReq();
        reqVO.setStatus(null);

        assertThrows(ServiceException.class, () -> service.createStudent(reqVO));

        verify(studentMapper, never()).insert(any(StudentDO.class));
        verify(studentClassMapper, never()).insertBatch(any());
    }

    @Test
    void createStudent_shouldBackfillClassStartDateFromSchoolYear() {
        StudentSaveReqVO reqVO = studentSaveReq();
        reqVO.setStatus(StudentStatusEnum.WAITING_ENTRY.getStatus());
        StudentClassSaveReqVO classReqVO = studentClassSaveReq(21L, LocalDate.now().plusDays(1), null);
        reqVO.setStudentClasses(List.of(classReqVO));
        LocalDate schoolYearStartDate = LocalDate.now().plusDays(30);
        mockParent();
        when(schoolMapper.selectById(11L)).thenReturn(school(11L, "实验小学"));
        when(schoolClassMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(schoolClass(21L, 11L, 102L, 31L, 2027, "2027级一年级1班")));
        when(schoolYearMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(schoolYear(102L, 11L, 101L, 2027, 2028, schoolYearStartDate)));
        doAnswer(invocation -> {
            StudentDO student = invocation.getArgument(0);
            student.setId(100L);
            return 1;
        }).when(studentMapper).insert(any(StudentDO.class));

        service.createStudent(reqVO);

        ArgumentCaptor<List<StudentClassDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(studentClassMapper).insertBatch(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(100L, captor.getValue().get(0).getStudentId());
        assertEquals(21L, captor.getValue().get(0).getClassId());
        assertEquals(schoolYearStartDate, captor.getValue().get(0).getStartDate());
    }

    @Test
    void getAppStudentSimpleListShouldAssembleCurrentSchoolGradeAndClass() {
        when(studentMapper.selectListByBelongTo(9L)).thenReturn(List.of(student(1L, "小明", 11L, 1)));
        when(schoolMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(school(11L, "实验小学")));
        when(studentClassMapper.selectCurrentListByStudentIds(any(Collection.class)))
                .thenReturn(List.of(studentClass(1L, 21L), studentClass(2L, 22L)));
        when(schoolClassMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(
                        schoolClass(21L, 31L, "2026级一年级1班"),
                        schoolClass(22L, 32L, "2026级二年级1班")));
        when(schoolGradeMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(
                        schoolGrade(31L, 41L),
                        schoolGrade(32L, 42L)));
        when(gradeCatalogMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(
                        gradeCatalog(41L, "一年级"),
                        gradeCatalog(42L, "二年级")));

        List<AppStudentSimpleRespVO> result = service.getAppStudentSimpleList(9L);

        assertEquals(1, result.size());
        AppStudentSimpleRespVO respVO = result.get(0);
        assertEquals(1L, respVO.getId());
        assertEquals("小明", respVO.getStudentName());
        assertEquals(11L, respVO.getCurrentSchoolId());
        assertEquals("实验小学", respVO.getCurrentSchoolName());
        assertEquals("一年级", respVO.getGradeName());
        assertEquals("2026级一年级1班", respVO.getClassName());
        assertEquals(1, respVO.getStatus());
    }

    @Test
    void getAppStudentSimpleListShouldKeepSchoolOnlyWhenCurrentClassNotUnique() {
        when(studentMapper.selectListByBelongTo(9L)).thenReturn(List.of(student(1L, "小明", 11L, 1)));
        when(schoolMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(school(11L, "实验小学")));
        when(studentClassMapper.selectCurrentListByStudentIds(any(Collection.class)))
                .thenReturn(List.of(studentClass(1L, 21L), studentClass(1L, 22L)));
        when(schoolClassMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(
                        schoolClass(21L, 31L, "2026级一年级1班"),
                        schoolClass(22L, 32L, "2026级二年级1班")));
        when(schoolGradeMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(
                        schoolGrade(31L, 41L),
                        schoolGrade(32L, 42L)));
        when(gradeCatalogMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(
                        gradeCatalog(41L, "一年级"),
                        gradeCatalog(42L, "二年级")));

        List<AppStudentSimpleRespVO> result = service.getAppStudentSimpleList(9L);

        assertEquals(1, result.size());
        AppStudentSimpleRespVO respVO = result.get(0);
        assertEquals("实验小学", respVO.getCurrentSchoolName());
        assertNull(respVO.getGradeName());
        assertNull(respVO.getClassName());
    }

    @Test
    void bindAppStudent_shouldCreateStudentAndBindWhenNoSameNameStudent() {
        mockParent();
        mockSelectedBindContext(21L, 31L, 41L, "一年级", "2026级一年级1班");
        when(studentMapper.selectSimpleListByExactStudentNameAndSchoolId("小明", 11L)).thenReturn(List.of());
        doAnswer(invocation -> {
            StudentDO student = invocation.getArgument(0);
            student.setId(100L);
            return 1;
        }).when(studentMapper).insert(any(StudentDO.class));

        AppStudentBindRespVO result = service.bindAppStudent(9L, bindReq(false));

        assertEquals("CREATED", result.getResult());
        assertEquals(100L, result.getStudentId());
        ArgumentCaptor<StudentDO> studentCaptor = ArgumentCaptor.forClass(StudentDO.class);
        verify(studentMapper).insert(studentCaptor.capture());
        assertEquals("小明", studentCaptor.getValue().getStudentName());
        assertEquals(9L, studentCaptor.getValue().getBelongTo());
        assertEquals(11L, studentCaptor.getValue().getCurrentSchoolId());
        assertEquals(2026, studentCaptor.getValue().getEntryYear());
        ArgumentCaptor<StudentClassDO> studentClassCaptor = ArgumentCaptor.forClass(StudentClassDO.class);
        verify(studentClassMapper).insert(studentClassCaptor.capture());
        assertEquals(100L, studentClassCaptor.getValue().getStudentId());
        assertEquals(21L, studentClassCaptor.getValue().getClassId());
        assertEquals(LocalDate.now().minusDays(30), studentClassCaptor.getValue().getStartDate());
    }

    @Test
    void bindAppStudent_shouldCreateWaitingEntryStudentForFutureEntry() {
        mockParent();
        mockFutureSelectedBindContext();
        when(studentMapper.selectSimpleListByExactStudentNameAndSchoolId("小明", 11L)).thenReturn(List.of());
        doAnswer(invocation -> {
            StudentDO student = invocation.getArgument(0);
            student.setId(100L);
            return 1;
        }).when(studentMapper).insert(any(StudentDO.class));

        AppStudentBindRespVO result = service.bindAppStudent(9L, futureBindReq(false));

        assertEquals("CREATED", result.getResult());
        ArgumentCaptor<StudentDO> studentCaptor = ArgumentCaptor.forClass(StudentDO.class);
        verify(studentMapper).insert(studentCaptor.capture());
        assertEquals(StudentStatusEnum.WAITING_ENTRY.getStatus(), studentCaptor.getValue().getStatus());
        ArgumentCaptor<StudentClassDO> studentClassCaptor = ArgumentCaptor.forClass(StudentClassDO.class);
        verify(studentClassMapper).insert(studentClassCaptor.capture());
        assertEquals(21L, studentClassCaptor.getValue().getClassId());
        assertEquals(LocalDate.now().plusDays(30), studentClassCaptor.getValue().getStartDate());
    }

    @Test
    void bindAppStudent_shouldBindExistingStudentWhenCurrentClassMatched() {
        mockParent();
        mockSelectedBindContext(21L, 31L, 41L, "一年级", "2026级一年级1班");
        when(studentMapper.selectSimpleListByExactStudentNameAndSchoolId("小明", 11L))
                .thenReturn(List.of(student(1L, "小明", null, 11L, StudentStatusEnum.READING.getStatus())));
        when(studentClassMapper.selectCurrentListByStudentId(1L))
                .thenReturn(List.of(studentClassRecord(1001L, 1L, 21L, LocalDate.now().minusDays(10))));

        AppStudentBindRespVO result = service.bindAppStudent(9L, bindReq(false));

        assertEquals("BOUND", result.getResult());
        assertEquals(1L, result.getStudentId());
        ArgumentCaptor<StudentDO> studentCaptor = ArgumentCaptor.forClass(StudentDO.class);
        verify(studentMapper).updateById(studentCaptor.capture());
        assertEquals(1L, studentCaptor.getValue().getId());
        assertEquals(9L, studentCaptor.getValue().getBelongTo());
        verify(studentClassMapper, never()).insert(any(StudentClassDO.class));
        verify(studentClassMapper, never()).updateById(any(StudentClassDO.class));
        verify(studentClassMapper, never()).deletePhysicallyById(any(Long.class));
    }

    @Test
    void bindAppStudent_shouldBlockWhenExistingStudentBelongsToOtherParent() {
        mockParent();
        mockSelectedBindContext(21L, 31L, 41L, "一年级", "2026级一年级1班");
        when(studentMapper.selectSimpleListByExactStudentNameAndSchoolId("小明", 11L))
                .thenReturn(List.of(student(1L, "小明", 10L, 11L, StudentStatusEnum.READING.getStatus())));

        assertThrows(ServiceException.class, () -> service.bindAppStudent(9L, bindReq(false)));
        verify(studentMapper, never()).updateById(any(StudentDO.class));
        verify(studentClassMapper, never()).insert(any(StudentClassDO.class));
    }

    @Test
    void bindAppStudent_shouldBlockWhenSameNameStudentDuplicatedInSchool() {
        mockParent();
        mockSelectedBindContext(21L, 31L, 41L, "一年级", "2026级一年级1班");
        when(studentMapper.selectSimpleListByExactStudentNameAndSchoolId("小明", 11L))
                .thenReturn(List.of(
                        student(1L, "小明", null, 11L, StudentStatusEnum.READING.getStatus()),
                        student(2L, "小明", null, 11L, StudentStatusEnum.READING.getStatus())));

        assertThrows(ServiceException.class, () -> service.bindAppStudent(9L, bindReq(false)));
        verify(studentMapper, never()).updateById(any(StudentDO.class));
        verify(studentClassMapper, never()).insert(any(StudentClassDO.class));
    }

    @Test
    void bindAppStudent_shouldConfirmAndForceUpdateWhenGradeMismatched() {
        mockParent();
        mockSelectedBindContext(21L, 31L, 41L, "一年级", "2026级一年级1班");
        when(studentMapper.selectSimpleListByExactStudentNameAndSchoolId("小明", 11L))
                .thenReturn(List.of(student(1L, "小明", null, 11L, StudentStatusEnum.READING.getStatus())));
        when(studentClassMapper.selectCurrentListByStudentId(1L))
                .thenReturn(List.of(studentClassRecord(1001L, 1L, 22L, LocalDate.now().minusDays(40))));
        when(schoolClassMapper.selectById(22L))
                .thenReturn(schoolClass(22L, 11L, 101L, 32L, 2026, "2026级二年级1班"));
        when(schoolGradeMapper.selectById(32L)).thenReturn(schoolGrade(32L, 11L, 42L));
        when(gradeCatalogMapper.selectById(42L)).thenReturn(gradeCatalog(42L, "二年级", 2));

        AppStudentBindRespVO confirmResult = service.bindAppStudent(9L, bindReq(false));

        assertEquals("CONFIRM_REQUIRED", confirmResult.getResult());
        assertEquals("GRADE_MISMATCH", confirmResult.getConflictType());
        assertEquals("二年级", confirmResult.getCurrentGradeName());
        assertEquals("一年级", confirmResult.getSelectedGradeName());

        AppStudentBindRespVO forceResult = service.bindAppStudent(9L, bindReq(true));

        assertEquals("BOUND", forceResult.getResult());
        ArgumentCaptor<StudentClassDO> updateCaptor = ArgumentCaptor.forClass(StudentClassDO.class);
        verify(studentClassMapper).updateById(updateCaptor.capture());
        assertEquals(1001L, updateCaptor.getValue().getId());
        assertEquals(LocalDate.now().minusDays(31), updateCaptor.getValue().getEndDate());
        ArgumentCaptor<StudentClassDO> insertCaptor = ArgumentCaptor.forClass(StudentClassDO.class);
        verify(studentClassMapper).insert(insertCaptor.capture());
        assertEquals(1L, insertCaptor.getValue().getStudentId());
        assertEquals(21L, insertCaptor.getValue().getClassId());
    }

    @Test
    void bindAppStudent_shouldConfirmAndForceUpdateWhenClassMismatched() {
        mockParent();
        mockSelectedBindContext(21L, 31L, 41L, "一年级", "2026级一年级1班");
        when(studentMapper.selectSimpleListByExactStudentNameAndSchoolId("小明", 11L))
                .thenReturn(List.of(student(1L, "小明", null, 11L, StudentStatusEnum.READING.getStatus())));
        when(studentClassMapper.selectCurrentListByStudentId(1L))
                .thenReturn(List.of(studentClassRecord(1001L, 1L, 23L, LocalDate.now().minusDays(40))));
        when(schoolClassMapper.selectById(23L))
                .thenReturn(schoolClass(23L, 11L, 101L, 31L, 2026, "2026级一年级2班"));

        AppStudentBindRespVO confirmResult = service.bindAppStudent(9L, bindReq(false));

        assertEquals("CONFIRM_REQUIRED", confirmResult.getResult());
        assertEquals("CLASS_MISMATCH", confirmResult.getConflictType());
        assertEquals("2026级一年级2班", confirmResult.getCurrentClassName());

        AppStudentBindRespVO forceResult = service.bindAppStudent(9L, bindReq(true));

        assertEquals("BOUND", forceResult.getResult());
        verify(studentClassMapper).updateById(any(StudentClassDO.class));
        verify(studentClassMapper).insert(any(StudentClassDO.class));
    }

    @Test
    void bindAppStudent_shouldConfirmAndCreateCurrentClassWhenCurrentClassMissing() {
        mockParent();
        mockSelectedBindContext(21L, 31L, 41L, "一年级", "2026级一年级1班");
        when(studentMapper.selectSimpleListByExactStudentNameAndSchoolId("小明", 11L))
                .thenReturn(List.of(student(1L, "小明", null, 11L, StudentStatusEnum.READING.getStatus())));
        when(studentClassMapper.selectCurrentListByStudentId(1L)).thenReturn(List.of());

        AppStudentBindRespVO confirmResult = service.bindAppStudent(9L, bindReq(false));

        assertEquals("CONFIRM_REQUIRED", confirmResult.getResult());
        assertEquals("CLASS_MISSING", confirmResult.getConflictType());

        AppStudentBindRespVO forceResult = service.bindAppStudent(9L, bindReq(true));

        assertEquals("BOUND", forceResult.getResult());
        verify(studentClassMapper).insert(any(StudentClassDO.class));
        verify(studentClassMapper, never()).updateById(any(StudentClassDO.class));
        verify(studentClassMapper, never()).deletePhysicallyById(any(Long.class));
    }

    @Test
    void bindAppStudent_shouldRejectClassOutsideCurrentSchoolYearEvenForceUpdate() {
        mockParent();
        when(schoolMapper.selectById(11L)).thenReturn(school(11L, "实验小学"));
        when(schoolGradeMapper.selectById(31L)).thenReturn(schoolGrade(31L, 11L, 41L));
        when(gradeCatalogMapper.selectById(41L)).thenReturn(gradeCatalog(41L, "一年级", 1));
        when(schoolYearMapper.selectCurrentBySchoolId(eq(11L), any(LocalDate.class)))
                .thenReturn(schoolYear(101L, 11L, 100L, 2026, 2027, LocalDate.now().minusDays(30)));
        when(schoolYearMapper.selectById(101L))
                .thenReturn(schoolYear(101L, 11L, 100L, 2026, 2027, LocalDate.now().minusDays(30)));
        when(schoolClassMapper.selectById(21L))
                .thenReturn(schoolClass(21L, 11L, 102L, 31L, 2026, "历史学年一年级1班"));

        assertThrows(ServiceException.class, () -> service.bindAppStudent(9L, bindReq(true)));
        verify(studentMapper, never()).selectSimpleListByExactStudentNameAndSchoolId(any(), any());
        verify(studentMapper, never()).updateById(any(StudentDO.class));
        verify(studentClassMapper, never()).insert(any(StudentClassDO.class));
    }

    @Test
    void getSubscriptionStudentContextMap_shouldUseTargetYearClassWhenExists() {
        when(studentMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(student(1L, "小明", 11L, StudentStatusEnum.PENDING_ADVANCE.getStatus())));
        when(schoolMapper.selectById(11L)).thenReturn(school(11L, "实验小学"));
        when(studentClassMapper.selectListByStudentIdsAndTargetYearCatalogId(any(Collection.class), eq(100L)))
                .thenReturn(List.of(studentClass(1L, 21L)));
        when(schoolClassMapper.selectById(21L)).thenReturn(schoolClass(21L, 11L, 101L, 31L, "2026级二年级1班"));
        when(schoolGradeMapper.selectById(31L)).thenReturn(schoolGrade(31L, 42L));
        when(gradeCatalogMapper.selectById(42L)).thenReturn(gradeCatalog(42L, "二年级", 2));

        Map<Long, EduStudentSubscriptionContextRespDTO> result = service.getSubscriptionStudentContextMap(
                9L, List.of(1L), 2026, 2027, 100L);

        EduStudentSubscriptionContextRespDTO context = result.get(1L);
        assertEquals(42L, context.getGradeCatalogId());
        assertEquals("二年级", context.getGradeName());
        assertEquals("2026级二年级1班", context.getClassName());
        assertEquals("TARGET_YEAR_CLASS", context.getGradeResolveSource());
        assertNull(context.getBlockedReason());
    }

    @Test
    void getSubscriptionStudentContextMap_shouldPromoteFromCurrentBeforeTargetYearStarts() {
        when(studentMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(student(1L, "小明", 11L, StudentStatusEnum.READING.getStatus())));
        when(schoolMapper.selectById(11L)).thenReturn(school(11L, "实验小学"));
        when(studentClassMapper.selectListByStudentIdsAndTargetYearCatalogId(any(Collection.class), eq(100L)))
                .thenReturn(List.of());
        when(schoolYearMapper.selectBySchoolIdAndYearCatalogId(11L, 100L))
                .thenReturn(schoolYear(101L, 11L, 100L, 2026, 2027, LocalDate.now().plusDays(30)));
        when(studentClassMapper.selectCurrentListByStudentId(1L)).thenReturn(List.of(studentClass(1L, 21L)));
        when(schoolClassMapper.selectById(21L)).thenReturn(schoolClass(21L, 11L, 90L, 31L, "2025级一年级1班"));
        when(schoolYearMapper.selectById(90L)).thenReturn(schoolYear(90L, 11L, 99L, 2025, 2026, LocalDate.now().minusMonths(10)));
        when(schoolGradeMapper.selectById(31L)).thenReturn(schoolGrade(31L, 41L));
        when(gradeCatalogMapper.selectById(41L)).thenReturn(gradeCatalog(41L, "一年级", 1));
        when(gradeCatalogMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(List.of(gradeCatalog(41L, "一年级", 1), gradeCatalog(42L, "二年级", 2)));
        when(schoolGradeMapper.selectListBySchoolId(11L)).thenReturn(List.of(schoolGrade(31L, 41L), schoolGrade(32L, 42L)));
        when(schoolGradeMapper.selectBySchoolIdAndGradeCatalogId(11L, 41L)).thenReturn(schoolGrade(31L, 41L));

        Map<Long, EduStudentSubscriptionContextRespDTO> result = service.getSubscriptionStudentContextMap(
                9L, List.of(1L), 2026, 2027, 100L);

        EduStudentSubscriptionContextRespDTO context = result.get(1L);
        assertEquals(42L, context.getGradeCatalogId());
        assertEquals("二年级", context.getGradeName());
        assertEquals("PROMOTED_FROM_CURRENT", context.getGradeResolveSource());
        assertNull(context.getBlockedReason());
    }

    @Test
    void getSubscriptionStudentContextMap_shouldBlockWhenTargetYearAlreadyStartedWithoutTargetClass() {
        when(studentMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(student(1L, "小明", 11L, StudentStatusEnum.READING.getStatus())));
        when(schoolMapper.selectById(11L)).thenReturn(school(11L, "实验小学"));
        when(studentClassMapper.selectListByStudentIdsAndTargetYearCatalogId(any(Collection.class), eq(100L)))
                .thenReturn(List.of());
        when(schoolYearMapper.selectBySchoolIdAndYearCatalogId(11L, 100L))
                .thenReturn(schoolYear(101L, 11L, 100L, 2026, 2027, LocalDate.now().minusDays(1)));

        Map<Long, EduStudentSubscriptionContextRespDTO> result = service.getSubscriptionStudentContextMap(
                9L, List.of(1L), 2026, 2027, 100L);

        assertEquals("TARGET_YEAR_CLASS_NOT_READY", result.get(1L).getBlockedReason());
    }

    @Test
    void getSubscriptionStudentContextMap_shouldBlockPendingAdvanceWithoutTargetClass() {
        when(studentMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(student(1L, "小明", 11L, StudentStatusEnum.PENDING_ADVANCE.getStatus())));
        when(schoolMapper.selectById(11L)).thenReturn(school(11L, "实验小学"));
        when(studentClassMapper.selectListByStudentIdsAndTargetYearCatalogId(any(Collection.class), eq(100L)))
                .thenReturn(List.of());
        when(schoolYearMapper.selectBySchoolIdAndYearCatalogId(11L, 100L))
                .thenReturn(schoolYear(101L, 11L, 100L, 2026, 2027, LocalDate.now().plusDays(30)));

        Map<Long, EduStudentSubscriptionContextRespDTO> result = service.getSubscriptionStudentContextMap(
                9L, List.of(1L), 2026, 2027, 100L);

        assertEquals("TARGET_YEAR_CLASS_REQUIRED", result.get(1L).getBlockedReason());
    }

    @Test
    void getSubscriptionStudentContextMap_shouldBlockWaitingEntryWithoutTargetClass() {
        when(studentMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(student(1L, "小明", 11L, StudentStatusEnum.WAITING_ENTRY.getStatus())));
        when(schoolMapper.selectById(11L)).thenReturn(school(11L, "实验小学"));
        when(studentClassMapper.selectListByStudentIdsAndTargetYearCatalogId(any(Collection.class), eq(100L)))
                .thenReturn(List.of());
        when(schoolYearMapper.selectBySchoolIdAndYearCatalogId(11L, 100L))
                .thenReturn(schoolYear(101L, 11L, 100L, 2026, 2027, LocalDate.now().plusDays(30)));

        Map<Long, EduStudentSubscriptionContextRespDTO> result = service.getSubscriptionStudentContextMap(
                9L, List.of(1L), 2026, 2027, 100L);

        assertEquals("TARGET_YEAR_CLASS_REQUIRED", result.get(1L).getBlockedReason());
        assertEquals("待入学学生必须绑定目标学年班级", result.get(1L).getBlockedReasonDesc());
    }

    @Test
    void activateWaitingEntryStudents_shouldActivateWhenCurrentClassUnique() {
        when(studentMapper.selectListByStatusAndIdGreaterThan(StudentStatusEnum.WAITING_ENTRY.getStatus(), null, 500))
                .thenReturn(List.of(student(1L, "小明", 11L, StudentStatusEnum.WAITING_ENTRY.getStatus())));
        when(studentClassMapper.selectCurrentListByStudentIds(any(Collection.class)))
                .thenReturn(List.of(studentClass(1L, 21L)));

        StudentWaitingEntryActivateRespBO result = service.activateWaitingEntryStudents();

        assertEquals(1, result.getScannedCount());
        assertEquals(1, result.getActivatedCount());
        verify(studentMapper).updateStatusById(1L, StudentStatusEnum.READING.getStatus());
    }

    private StudentDO student(Long id, String studentName, Long currentSchoolId, Integer status) {
        return student(id, studentName, 9L, currentSchoolId, status);
    }

    private StudentDO student(Long id, String studentName, Long belongTo, Long currentSchoolId, Integer status) {
        return StudentDO.builder()
                .id(id)
                .studentName(studentName)
                .currentSchoolId(currentSchoolId)
                .belongTo(belongTo)
                .status(status)
                .build();
    }

    private AppStudentBindReqVO bindReq(Boolean forceUpdate) {
        AppStudentBindReqVO reqVO = new AppStudentBindReqVO();
        reqVO.setSchoolId(11L);
        reqVO.setBindMode("CURRENT_READING");
        reqVO.setSchoolYearId(101L);
        reqVO.setSchoolGradeId(31L);
        reqVO.setClassId(21L);
        reqVO.setStudentName(" 小明 ");
        reqVO.setForceUpdate(forceUpdate);
        return reqVO;
    }

    private AppStudentBindReqVO futureBindReq(Boolean forceUpdate) {
        AppStudentBindReqVO reqVO = bindReq(forceUpdate);
        reqVO.setBindMode("FUTURE_ENTRY");
        reqVO.setSchoolYearId(102L);
        return reqVO;
    }

    private StudentSaveReqVO studentSaveReq() {
        StudentSaveReqVO reqVO = new StudentSaveReqVO();
        reqVO.setStudentName("小明");
        reqVO.setBelongTo(9L);
        reqVO.setCurrentSchoolId(11L);
        reqVO.setEntryYear(2027);
        reqVO.setStatus(StudentStatusEnum.READING.getStatus());
        return reqVO;
    }

    private StudentClassSaveReqVO studentClassSaveReq(Long classId, LocalDate startDate, LocalDate endDate) {
        StudentClassSaveReqVO reqVO = new StudentClassSaveReqVO();
        reqVO.setClassId(classId);
        reqVO.setStartDate(startDate);
        reqVO.setEndDate(endDate);
        return reqVO;
    }

    private void mockParent() {
        MemberUserRespDTO parent = new MemberUserRespDTO();
        parent.setId(9L);
        when(memberUserApi.getUser(9L)).thenReturn(parent);
    }

    private void mockSelectedBindContext(Long classId, Long schoolGradeId, Long gradeCatalogId,
                                         String gradeName, String className) {
        when(schoolMapper.selectById(11L)).thenReturn(school(11L, "实验小学"));
        when(schoolGradeMapper.selectById(schoolGradeId)).thenReturn(schoolGrade(schoolGradeId, 11L, gradeCatalogId));
        when(gradeCatalogMapper.selectById(gradeCatalogId)).thenReturn(gradeCatalog(gradeCatalogId, gradeName, 1));
        when(schoolYearMapper.selectCurrentBySchoolId(eq(11L), any(LocalDate.class)))
                .thenReturn(schoolYear(101L, 11L, 100L, 2026, 2027, LocalDate.now().minusDays(30)));
        when(schoolYearMapper.selectById(101L))
                .thenReturn(schoolYear(101L, 11L, 100L, 2026, 2027, LocalDate.now().minusDays(30)));
        when(schoolClassMapper.selectById(classId))
                .thenReturn(schoolClass(classId, 11L, 101L, schoolGradeId, 2026, className));
    }

    private void mockFutureSelectedBindContext() {
        when(schoolMapper.selectById(11L)).thenReturn(school(11L, "实验小学"));
        when(schoolGradeMapper.selectById(31L)).thenReturn(schoolGrade(31L, 11L, 41L));
        when(gradeCatalogMapper.selectById(41L)).thenReturn(gradeCatalog(41L, "一年级", 1));
        when(schoolYearMapper.selectById(102L))
                .thenReturn(schoolYear(102L, 11L, 101L, 2027, 2028, LocalDate.now().plusDays(30)));
        when(schoolClassMapper.selectById(21L))
                .thenReturn(schoolClass(21L, 11L, 102L, 31L, 2027, "2027级一年级1班"));
    }

    private SchoolDO school(Long id, String schoolName) {
        SchoolDO school = new SchoolDO();
        school.setId(id);
        school.setSchoolName(schoolName);
        return school;
    }

    private StudentClassDO studentClass(Long studentId, Long classId) {
        return StudentClassDO.builder()
                .studentId(studentId)
                .classId(classId)
                .build();
    }

    private StudentClassDO studentClassRecord(Long id, Long studentId, Long classId, LocalDate startDate) {
        return StudentClassDO.builder()
                .id(id)
                .studentId(studentId)
                .classId(classId)
                .startDate(startDate)
                .build();
    }

    private SchoolClassDO schoolClass(Long id, Long schoolGradeId, String className) {
        return SchoolClassDO.builder()
                .id(id)
                .schoolGradeId(schoolGradeId)
                .className(className)
                .build();
    }

    private SchoolClassDO schoolClass(Long id, Long schoolId, Long schoolYearId, Long schoolGradeId, String className) {
        return schoolClass(id, schoolId, schoolYearId, schoolGradeId, null, className);
    }

    private SchoolClassDO schoolClass(Long id, Long schoolId, Long schoolYearId, Long schoolGradeId,
                                      Integer entryYear, String className) {
        return SchoolClassDO.builder()
                .id(id)
                .schoolId(schoolId)
                .schoolYearId(schoolYearId)
                .schoolGradeId(schoolGradeId)
                .entryYear(entryYear)
                .className(className)
                .build();
    }

    private SchoolGradeDO schoolGrade(Long id, Long gradeCatalogId) {
        return schoolGrade(id, null, gradeCatalogId);
    }

    private SchoolGradeDO schoolGrade(Long id, Long schoolId, Long gradeCatalogId) {
        return SchoolGradeDO.builder()
                .id(id)
                .schoolId(schoolId)
                .gradeCatalogId(gradeCatalogId)
                .build();
    }

    private GradeCatalogDO gradeCatalog(Long id, String gradeName) {
        return gradeCatalog(id, gradeName, null);
    }

    private GradeCatalogDO gradeCatalog(Long id, String gradeName, Integer sort) {
        return GradeCatalogDO.builder()
                .id(id)
                .gradeName(gradeName)
                .sort(sort)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build();
    }

    private SchoolYearDO schoolYear(Long id, Long schoolId, Long yearCatalogId, Integer yearStart, Integer yearEnd,
                                    LocalDate startDate) {
        return SchoolYearDO.builder()
                .id(id)
                .schoolId(schoolId)
                .yearCatalogId(yearCatalogId)
                .yearStart(yearStart)
                .yearEnd(yearEnd)
                .startDate(startDate)
                .build();
    }
}
