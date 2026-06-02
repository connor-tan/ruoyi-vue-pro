package cn.iocoder.yudao.module.edu.service.school;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.controller.app.school.vo.AppSchoolClassSimpleRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolGradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SchoolClassEnsureServiceTest {

    private SchoolClassEnsureService service;
    private SchoolYearMapper schoolYearMapper;
    private SchoolGradeMapper schoolGradeMapper;
    private GradeCatalogMapper gradeCatalogMapper;
    private SchoolClassMapper schoolClassMapper;

    @BeforeEach
    void setUp() {
        service = new SchoolClassEnsureService();
        schoolYearMapper = mock(SchoolYearMapper.class);
        schoolGradeMapper = mock(SchoolGradeMapper.class);
        gradeCatalogMapper = mock(GradeCatalogMapper.class);
        schoolClassMapper = mock(SchoolClassMapper.class);
        ReflectionTestUtils.setField(service, "schoolYearMapper", schoolYearMapper);
        ReflectionTestUtils.setField(service, "schoolGradeMapper", schoolGradeMapper);
        ReflectionTestUtils.setField(service, "gradeCatalogMapper", gradeCatalogMapper);
        ReflectionTestUtils.setField(service, "schoolClassMapper", schoolClassMapper);
        mockBaseData();
    }

    @Test
    void buildClassOptions_shouldReturnConfiguredClassNoRangeWithExistingFlag() {
        when(schoolClassMapper.selectListBySchoolIdAndSchoolYearIdAndSchoolGradeId(11L, 101L, 31L))
                .thenReturn(List.of(schoolClass(21L, 1, "2026级一年级1班")));

        List<AppSchoolClassSimpleRespVO> result = service.buildClassOptions(11L, 101L, 31L);

        assertEquals(3, result.size());
        assertEquals(21L, result.get(0).getId());
        assertEquals(1, result.get(0).getClassNo());
        assertTrue(result.get(0).getExists());
        assertNull(result.get(1).getId());
        assertEquals(2, result.get(1).getClassNo());
        assertFalse(result.get(1).getExists());
        assertEquals("2026级一年级2班", result.get(1).getClassName());
    }

    @Test
    void ensureSchoolClass_shouldCreateMissingClassWithinCapacity() {
        when(schoolClassMapper.selectByUniqueKey(11L, 2026, 101L, 31L, 2)).thenReturn(null);
        doAnswer(invocation -> {
            SchoolClassDO schoolClass = invocation.getArgument(0);
            schoolClass.setId(22L);
            return 1;
        }).when(schoolClassMapper).insert(any(SchoolClassDO.class));

        SchoolClassDO result = service.ensureSchoolClass(11L, 101L, 31L, 2);

        assertEquals(22L, result.getId());
        ArgumentCaptor<SchoolClassDO> captor = ArgumentCaptor.forClass(SchoolClassDO.class);
        org.mockito.Mockito.verify(schoolClassMapper).insert(captor.capture());
        assertEquals(11L, captor.getValue().getSchoolId());
        assertEquals(2026, captor.getValue().getEntryYear());
        assertEquals(101L, captor.getValue().getSchoolYearId());
        assertEquals(31L, captor.getValue().getSchoolGradeId());
        assertEquals(2, captor.getValue().getClassNo());
        assertEquals("2026级一年级2班", captor.getValue().getClassName());
    }

    private void mockBaseData() {
        when(schoolYearMapper.selectById(101L)).thenReturn(SchoolYearDO.builder()
                .id(101L)
                .schoolId(11L)
                .yearStart(2026)
                .yearEnd(2027)
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 6, 30))
                .build());
        when(schoolGradeMapper.selectById(31L)).thenReturn(SchoolGradeDO.builder()
                .id(31L)
                .schoolId(11L)
                .gradeCatalogId(41L)
                .maxClassNo(3)
                .build());
        GradeCatalogDO gradeCatalog = GradeCatalogDO.builder()
                .id(41L)
                .stage("primary")
                .gradeName("一年级")
                .sort(1)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build();
        when(gradeCatalogMapper.selectById(41L)).thenReturn(gradeCatalog);
        when(gradeCatalogMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(List.of(gradeCatalog));
    }

    private SchoolClassDO schoolClass(Long id, Integer classNo, String className) {
        return SchoolClassDO.builder()
                .id(id)
                .schoolId(11L)
                .schoolYearId(101L)
                .schoolGradeId(31L)
                .entryYear(2026)
                .classNo(classNo)
                .className(className)
                .build();
    }

}
