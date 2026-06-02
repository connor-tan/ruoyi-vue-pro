package cn.iocoder.yudao.module.edu.service.school;

import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.YearCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolGradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.YearCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.studentclass.StudentClassMapper;
import cn.iocoder.yudao.module.edu.service.school.bo.SchoolYearClassGenerateReqBO;
import cn.iocoder.yudao.module.edu.service.school.bo.SchoolYearClassGenerateRespBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class SchoolYearClassGenerateServiceImplTest {

    private SchoolYearClassGenerateServiceImpl service;
    private SchoolMapper schoolMapper;
    private SchoolYearMapper schoolYearMapper;
    private YearCatalogMapper yearCatalogMapper;
    private SchoolClassMapper schoolClassMapper;
    private SchoolGradeMapper schoolGradeMapper;
    private GradeCatalogMapper gradeCatalogMapper;
    private StudentClassMapper studentClassMapper;

    @BeforeEach
    void setUp() {
        service = new SchoolYearClassGenerateServiceImpl();
        schoolMapper = mock(SchoolMapper.class);
        schoolYearMapper = mock(SchoolYearMapper.class);
        yearCatalogMapper = mock(YearCatalogMapper.class);
        schoolClassMapper = mock(SchoolClassMapper.class);
        schoolGradeMapper = mock(SchoolGradeMapper.class);
        gradeCatalogMapper = mock(GradeCatalogMapper.class);
        studentClassMapper = mock(StudentClassMapper.class);
        ReflectionTestUtils.setField(service, "schoolMapper", schoolMapper);
        ReflectionTestUtils.setField(service, "schoolYearMapper", schoolYearMapper);
        ReflectionTestUtils.setField(service, "yearCatalogMapper", yearCatalogMapper);
        ReflectionTestUtils.setField(service, "schoolClassMapper", schoolClassMapper);
        ReflectionTestUtils.setField(service, "schoolGradeMapper", schoolGradeMapper);
        ReflectionTestUtils.setField(service, "gradeCatalogMapper", gradeCatalogMapper);
        ReflectionTestUtils.setField(service, "studentClassMapper", studentClassMapper);
    }

    @Test
    void generateShouldCreateTargetYearPromotedClassAndNewEntryClass() {
        SchoolYearDO sourceYear = schoolYear(11L, 1L, 2025);
        SchoolGradeDO gradeP1 = schoolGrade(101L, 1L, 4L);
        SchoolGradeDO gradeP2 = schoolGrade(102L, 1L, 5L);
        SchoolClassDO sourceClass = schoolClass(1001L, 1L, 2025, 11L, 101L, 1);
        mockBaseData(List.of(school(1L)), primaryGradeCatalogs(), List.of(sourceYear),
                List.of(gradeP1, gradeP2), List.of(sourceClass), Collections.emptyList());
        doAnswer(invocation -> {
            SchoolYearDO schoolYear = invocation.getArgument(0);
            schoolYear.setId(12L);
            return 1;
        }).when(schoolYearMapper).insert(any(SchoolYearDO.class));
        when(schoolClassMapper.insertBatch(any(), eq(500))).thenReturn(true);

        SchoolYearClassGenerateRespBO respBO = service.generate(req(2026, false));

        assertEquals(1, respBO.getCreatedYearCount());
        assertEquals(2, respBO.getCreatedClassCount());
        assertEquals(0, respBO.getSkippedClassCount());
        ArgumentCaptor<SchoolYearDO> yearCaptor = ArgumentCaptor.forClass(SchoolYearDO.class);
        verify(schoolYearMapper).insert(yearCaptor.capture());
        SchoolYearDO createdYear = yearCaptor.getValue();
        assertEquals(9001L, createdYear.getYearCatalogId());
        assertEquals(2026, createdYear.getYearStart());
        assertEquals(2027, createdYear.getYearEnd());
        assertEquals(LocalDate.of(2026, 9, 1), createdYear.getStartDate());
        assertEquals(LocalDate.of(2027, 6, 30), createdYear.getEndDate());
        ArgumentCaptor<Collection<SchoolClassDO>> classCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(schoolClassMapper).insertBatch(classCaptor.capture(), eq(500));
        Collection<SchoolClassDO> createdClasses = classCaptor.getValue();
        assertTrue(createdClasses.stream().anyMatch(item -> item.getEntryYear().equals(2025)
                && item.getSchoolYearId().equals(12L)
                && item.getSchoolGradeId().equals(102L)
                && item.getClassNo().equals(1)
                && item.getClassName().equals("2025级二年级1班")));
        assertTrue(createdClasses.stream().anyMatch(item -> item.getEntryYear().equals(2026)
                && item.getSchoolYearId().equals(12L)
                && item.getSchoolGradeId().equals(101L)
                && item.getClassNo().equals(1)
                && item.getClassName().equals("2026级一年级1班")));
    }

    @Test
    void generateShouldNotWriteWhenDryRun() {
        SchoolYearDO sourceYear = schoolYear(11L, 1L, 2025);
        SchoolGradeDO gradeP1 = schoolGrade(101L, 1L, 4L);
        SchoolGradeDO gradeP2 = schoolGrade(102L, 1L, 5L);
        mockBaseData(List.of(school(1L)), primaryGradeCatalogs(), List.of(sourceYear),
                List.of(gradeP1, gradeP2), List.of(schoolClass(1001L, 1L, 2025, 11L, 101L, 1)),
                Collections.emptyList());

        SchoolYearClassGenerateRespBO respBO = service.generate(req(2026, true));

        assertEquals(1, respBO.getCreatedYearCount());
        assertEquals(2, respBO.getCreatedClassCount());
        verify(schoolYearMapper, never()).insert(any(SchoolYearDO.class));
        verify(schoolClassMapper, never()).insertBatch(any(), eq(500));
    }

    @Test
    void generateShouldOnlySkipExistingDataOnRepeatRun() {
        SchoolYearDO sourceYear = schoolYear(11L, 1L, 2025);
        SchoolYearDO targetYear = schoolYear(12L, 1L, 2026);
        SchoolGradeDO gradeP1 = schoolGrade(101L, 1L, 4L);
        SchoolGradeDO gradeP2 = schoolGrade(102L, 1L, 5L);
        SchoolClassDO sourceClass = schoolClass(1001L, 1L, 2025, 11L, 101L, 1);
        List<SchoolClassDO> targetClasses = List.of(
                schoolClass(2001L, 1L, 2025, 12L, 102L, 1),
                schoolClass(2002L, 1L, 2026, 12L, 101L, 1));
        mockBaseData(List.of(school(1L)), primaryGradeCatalogs(), List.of(sourceYear, targetYear),
                List.of(gradeP1, gradeP2), List.of(sourceClass), targetClasses);

        SchoolYearClassGenerateRespBO respBO = service.generate(req(2026, false));

        assertEquals(0, respBO.getCreatedYearCount());
        assertEquals(0, respBO.getCreatedClassCount());
        assertEquals(2, respBO.getSkippedClassCount());
        assertEquals(2, respBO.getSkipReasonCounts().get("TARGET_CLASS_EXISTS"));
        verify(schoolYearMapper, never()).insert(any(SchoolYearDO.class));
        verify(schoolClassMapper, never()).insertBatch(any(), eq(500));
    }

    @Test
    void generateShouldFillMissingClassesWhenTargetPartiallyExists() {
        SchoolYearDO sourceYear = schoolYear(11L, 1L, 2025);
        SchoolYearDO targetYear = schoolYear(12L, 1L, 2026);
        SchoolGradeDO gradeP1 = schoolGrade(101L, 1L, 4L);
        SchoolGradeDO gradeP2 = schoolGrade(102L, 1L, 5L);
        SchoolClassDO sourceClass = schoolClass(1001L, 1L, 2025, 11L, 101L, 1);
        SchoolClassDO existingPromotedClass = schoolClass(2001L, 1L, 2025, 12L, 102L, 1);
        mockBaseData(List.of(school(1L)), primaryGradeCatalogs(), List.of(sourceYear, targetYear),
                List.of(gradeP1, gradeP2), List.of(sourceClass), List.of(existingPromotedClass));
        when(schoolClassMapper.insertBatch(any(), eq(500))).thenReturn(true);

        SchoolYearClassGenerateRespBO respBO = service.generate(req(2026, false));

        assertEquals(0, respBO.getCreatedYearCount());
        assertEquals(1, respBO.getCreatedClassCount());
        assertEquals(1, respBO.getSkippedClassCount());
        assertEquals(1, respBO.getSkipReasonCounts().get("TARGET_CLASS_EXISTS"));
        verify(schoolYearMapper, never()).insert(any(SchoolYearDO.class));
        ArgumentCaptor<Collection<SchoolClassDO>> classCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(schoolClassMapper).insertBatch(classCaptor.capture(), eq(500));
        SchoolClassDO createdClass = classCaptor.getValue().iterator().next();
        assertEquals(2026, createdClass.getEntryYear());
        assertEquals(101L, createdClass.getSchoolGradeId());
        assertEquals("2026级一年级1班", createdClass.getClassName());
    }

    @Test
    void generateShouldNotCopyEmptyPreseededClass() {
        SchoolYearDO sourceYear = schoolYear(11L, 1L, 2025);
        SchoolYearDO targetYear = schoolYear(12L, 1L, 2026);
        SchoolGradeDO gradeP1 = schoolGrade(101L, 1L, 4L);
        SchoolGradeDO gradeP2 = schoolGrade(102L, 1L, 5L);
        SchoolClassDO occupiedClass = schoolClass(1001L, 1L, 2025, 11L, 101L, 1);
        SchoolClassDO emptyPreseededClass = schoolClass(1002L, 1L, 2025, 11L, 101L, 25);
        mockBaseData(List.of(school(1L)), primaryGradeCatalogs(), List.of(sourceYear, targetYear),
                List.of(gradeP1, gradeP2), List.of(occupiedClass, emptyPreseededClass),
                Collections.emptyList(), List.of(occupiedClass.getId()));
        when(schoolClassMapper.insertBatch(any(), eq(500))).thenReturn(true);

        SchoolYearClassGenerateRespBO respBO = service.generate(req(2026, false));

        assertEquals(2, respBO.getCreatedClassCount());
        ArgumentCaptor<Collection<SchoolClassDO>> classCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(schoolClassMapper).insertBatch(classCaptor.capture(), eq(500));
        assertTrue(classCaptor.getValue().stream().noneMatch(item -> item.getClassNo().equals(25)));
    }

    @Test
    void generateShouldOnlyPromoteTerminalGradeWhenSchoolHasNextGrade() {
        SchoolYearDO s1SourceYear = schoolYear(11L, 1L, 2025);
        SchoolYearDO s1TargetYear = schoolYear(12L, 1L, 2026);
        SchoolYearDO s2SourceYear = schoolYear(21L, 2L, 2025);
        SchoolYearDO s2TargetYear = schoolYear(22L, 2L, 2026);
        SchoolGradeDO s1P6 = schoolGrade(109L, 1L, 9L);
        SchoolGradeDO s2P6 = schoolGrade(209L, 2L, 9L);
        SchoolGradeDO s2M1 = schoolGrade(210L, 2L, 10L);
        mockBaseData(List.of(school(1L), school(2L)), primaryAndMiddleGradeCatalogs(),
                List.of(s1SourceYear, s1TargetYear, s2SourceYear, s2TargetYear),
                List.of(s1P6, s2P6, s2M1),
                List.of(schoolClass(1001L, 1L, 2020, 11L, 109L, 1),
                        schoolClass(2001L, 2L, 2020, 21L, 209L, 1)),
                Collections.emptyList());
        when(schoolClassMapper.insertBatch(any(), eq(500))).thenReturn(true);

        SchoolYearClassGenerateRespBO respBO = service.generate(req(2026, false));

        assertEquals(1, respBO.getCreatedClassCount());
        assertEquals(1, respBO.getSkippedClassCount());
        assertEquals(1, respBO.getSkipReasonCounts().get("TARGET_GRADE_NOT_FOUND"));
        ArgumentCaptor<Collection<SchoolClassDO>> classCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(schoolClassMapper).insertBatch(classCaptor.capture(), eq(500));
        SchoolClassDO createdClass = classCaptor.getValue().iterator().next();
        assertEquals(2L, createdClass.getSchoolId());
        assertEquals(210L, createdClass.getSchoolGradeId());
        assertEquals("2020级初一1班", createdClass.getClassName());
    }

    private void mockBaseData(List<SchoolDO> schools, List<GradeCatalogDO> gradeCatalogs, List<SchoolYearDO> schoolYears,
                              List<SchoolGradeDO> schoolGrades, List<SchoolClassDO> sourceClasses,
                              List<SchoolClassDO> targetClasses) {
        mockBaseData(schools, gradeCatalogs, schoolYears, schoolGrades, sourceClasses, targetClasses,
                sourceClasses.stream().map(SchoolClassDO::getId).toList());
    }

    private void mockBaseData(List<SchoolDO> schools, List<GradeCatalogDO> gradeCatalogs, List<SchoolYearDO> schoolYears,
                              List<SchoolGradeDO> schoolGrades, List<SchoolClassDO> sourceClasses,
                              List<SchoolClassDO> targetClasses, Collection<Long> occupiedSourceClassIds) {
        when(schoolMapper.selectList()).thenReturn(schools);
        when(yearCatalogMapper.selectByYearRange(2026, 2027)).thenReturn(yearCatalog(9001L, 2026, 2027));
        when(gradeCatalogMapper.selectListByStatus(0)).thenReturn(gradeCatalogs);
        when(schoolYearMapper.selectListBySchoolIdsAndYearStarts(any(), any())).thenReturn(schoolYears);
        when(schoolGradeMapper.selectListBySchoolIds(any())).thenReturn(schoolGrades);
        when(schoolClassMapper.selectListBySchoolYearIds(any())).thenAnswer(invocation -> {
            Collection<Long> schoolYearIds = invocation.getArgument(0);
            boolean sourceQuery = sourceClasses.stream().anyMatch(item -> schoolYearIds.contains(item.getSchoolYearId()));
            return sourceQuery ? sourceClasses : targetClasses;
        });
        when(studentClassMapper.selectCurrentListByClassIds(any(), any(LocalDate.class))).thenAnswer(invocation -> {
            Collection<Long> classIds = invocation.getArgument(0);
            return classIds.stream()
                    .filter(occupiedSourceClassIds::contains)
                    .map(classId -> studentClass(classId, classId))
                    .toList();
        });
    }

    private SchoolYearClassGenerateReqBO req(Integer targetYearStart, boolean dryRun) {
        SchoolYearClassGenerateReqBO reqBO = new SchoolYearClassGenerateReqBO();
        reqBO.setTargetYearStart(targetYearStart);
        reqBO.setDryRun(dryRun);
        return reqBO;
    }

    private SchoolDO school(Long id) {
        SchoolDO school = new SchoolDO();
        school.setId(id);
        return school;
    }

    private SchoolYearDO schoolYear(Long id, Long schoolId, Integer yearStart) {
        return SchoolYearDO.builder()
                .id(id)
                .schoolId(schoolId)
                .yearStart(yearStart)
                .yearEnd(yearStart + 1)
                .startDate(LocalDate.of(yearStart, 9, 1))
                .endDate(LocalDate.of(yearStart + 1, 6, 30))
                .build();
    }

    private SchoolGradeDO schoolGrade(Long id, Long schoolId, Long gradeCatalogId) {
        return SchoolGradeDO.builder()
                .id(id)
                .schoolId(schoolId)
                .gradeCatalogId(gradeCatalogId)
                .maxClassNo(25)
                .build();
    }

    private YearCatalogDO yearCatalog(Long id, Integer yearStart, Integer yearEnd) {
        return YearCatalogDO.builder()
                .id(id)
                .yearStart(yearStart)
                .yearEnd(yearEnd)
                .build();
    }

    private SchoolClassDO schoolClass(Long id, Long schoolId, Integer entryYear, Long schoolYearId,
                                      Long schoolGradeId, Integer classNo) {
        return SchoolClassDO.builder()
                .id(id)
                .schoolId(schoolId)
                .entryYear(entryYear)
                .schoolYearId(schoolYearId)
                .schoolGradeId(schoolGradeId)
                .classNo(classNo)
                .build();
    }

    private StudentClassDO studentClass(Long studentId, Long classId) {
        return StudentClassDO.builder()
                .studentId(studentId)
                .classId(classId)
                .build();
    }

    private List<GradeCatalogDO> primaryGradeCatalogs() {
        return List.of(
                gradeCatalog(4L, "primary", "P1", "一年级", 40),
                gradeCatalog(5L, "primary", "P2", "二年级", 50));
    }

    private List<GradeCatalogDO> primaryAndMiddleGradeCatalogs() {
        return List.of(
                gradeCatalog(4L, "primary", "P1", "一年级", 40),
                gradeCatalog(5L, "primary", "P2", "二年级", 50),
                gradeCatalog(6L, "primary", "P3", "三年级", 60),
                gradeCatalog(7L, "primary", "P4", "四年级", 70),
                gradeCatalog(8L, "primary", "P5", "五年级", 80),
                gradeCatalog(9L, "primary", "P6", "六年级", 90),
                gradeCatalog(10L, "middle", "M1", "初一", 100));
    }

    private GradeCatalogDO gradeCatalog(Long id, String stage, String gradeNo, String gradeName, Integer sort) {
        return GradeCatalogDO.builder()
                .id(id)
                .stage(stage)
                .gradeNo(gradeNo)
                .gradeName(gradeName)
                .sort(sort)
                .build();
    }

}
