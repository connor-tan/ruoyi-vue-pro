package cn.iocoder.yudao.module.edu.service.student;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.controller.app.student.vo.AppStudentSimpleRespVO;
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
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
                9L, List.of(1L), 2026, 2027, 100L, "CURRENT_GRADE", "CURRENT_CHAIN");

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
                9L, List.of(1L), 2026, 2027, 100L, null, null);

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
                9L, List.of(1L), 2026, 2027, 100L, null, null);

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
                9L, List.of(1L), 2026, 2027, 100L, null, null);

        assertEquals("TARGET_YEAR_CLASS_REQUIRED", result.get(1L).getBlockedReason());
    }

    private StudentDO student(Long id, String studentName, Long currentSchoolId, Integer status) {
        return StudentDO.builder()
                .id(id)
                .studentName(studentName)
                .currentSchoolId(currentSchoolId)
                .belongTo(9L)
                .status(status)
                .build();
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

    private SchoolClassDO schoolClass(Long id, Long schoolGradeId, String className) {
        return SchoolClassDO.builder()
                .id(id)
                .schoolGradeId(schoolGradeId)
                .className(className)
                .build();
    }

    private SchoolClassDO schoolClass(Long id, Long schoolId, Long schoolYearId, Long schoolGradeId, String className) {
        return SchoolClassDO.builder()
                .id(id)
                .schoolId(schoolId)
                .schoolYearId(schoolYearId)
                .schoolGradeId(schoolGradeId)
                .className(className)
                .build();
    }

    private SchoolGradeDO schoolGrade(Long id, Long gradeCatalogId) {
        return SchoolGradeDO.builder()
                .id(id)
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
