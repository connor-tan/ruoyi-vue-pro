package cn.iocoder.yudao.module.edu.service.school;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolGradeSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolYearSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolStageDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.YearCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.station.StationDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolGradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolStageMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.YearCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.studentclass.StudentClassMapper;
import cn.iocoder.yudao.module.edu.service.station.StationService;
import cn.iocoder.yudao.module.system.api.ip.AreaApi;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.time.LocalDate;
import java.util.List;

import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_GRADE_STAGE_NOT_ALLOWED;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_STAGE_IN_USE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
class SchoolServiceImplTest {

    private SchoolServiceImpl service;
    private SchoolMapper schoolMapper;
    private SchoolStageMapper schoolStageMapper;
    private GradeCatalogMapper gradeCatalogMapper;
    private SchoolGradeMapper schoolGradeMapper;
    private SchoolClassMapper schoolClassMapper;
    private SchoolYearMapper schoolYearMapper;
    private YearCatalogMapper yearCatalogMapper;
    private StudentMapper studentMapper;
    private StudentClassMapper studentClassMapper;
    private AreaApi areaApi;
    private StationService stationService;

    @BeforeEach
    void setUp() {
        service = new SchoolServiceImpl();
        schoolMapper = mock(SchoolMapper.class);
        schoolStageMapper = mock(SchoolStageMapper.class);
        gradeCatalogMapper = mock(GradeCatalogMapper.class);
        schoolGradeMapper = mock(SchoolGradeMapper.class);
        schoolClassMapper = mock(SchoolClassMapper.class);
        schoolYearMapper = mock(SchoolYearMapper.class);
        yearCatalogMapper = mock(YearCatalogMapper.class);
        studentMapper = mock(StudentMapper.class);
        studentClassMapper = mock(StudentClassMapper.class);
        areaApi = mock(AreaApi.class);
        stationService = mock(StationService.class);
        ReflectionTestUtils.setField(service, "schoolMapper", schoolMapper);
        ReflectionTestUtils.setField(service, "schoolStageMapper", schoolStageMapper);
        ReflectionTestUtils.setField(service, "gradeCatalogMapper", gradeCatalogMapper);
        ReflectionTestUtils.setField(service, "schoolGradeMapper", schoolGradeMapper);
        ReflectionTestUtils.setField(service, "schoolClassMapper", schoolClassMapper);
        ReflectionTestUtils.setField(service, "schoolYearMapper", schoolYearMapper);
        ReflectionTestUtils.setField(service, "yearCatalogMapper", yearCatalogMapper);
        ReflectionTestUtils.setField(service, "studentMapper", studentMapper);
        ReflectionTestUtils.setField(service, "studentClassMapper", studentClassMapper);
        ReflectionTestUtils.setField(service, "areaApi", areaApi);
        ReflectionTestUtils.setField(service, "stationService", stationService);
    }

    @Test
    void createSchoolShouldSaveDistinctStagesAndStation() {
        when(gradeCatalogMapper.selectListByStatus(0)).thenReturn(List.of(
                gradeCatalog(4L, "primary", "P1", "一年级"),
                gradeCatalog(10L, "middle", "M1", "初一")));
        when(stationService.validateStationBindable(9L, 1L)).thenReturn(station(9L, 1L, 0));
        doAnswer(invocation -> {
            SchoolDO school = invocation.getArgument(0);
            school.setId(1L);
            return 1;
        }).when(schoolMapper).insert(any(SchoolDO.class));
        SchoolSaveReqVO reqVO = new SchoolSaveReqVO();
        reqVO.setSchoolName("测试九年一贯制学校");
        reqVO.setAreaId(1L);
        reqVO.setSchoolAddress("测试地址");
        reqVO.setStationId(9L);
        reqVO.setStageCodes(List.of("primary", "middle", "primary"));

        Long schoolId = service.createSchool(reqVO);

        assertEquals(1L, schoolId);
        ArgumentCaptor<SchoolDO> schoolCaptor = ArgumentCaptor.forClass(SchoolDO.class);
        verify(schoolMapper).insert(schoolCaptor.capture());
        assertEquals(9L, schoolCaptor.getValue().getStationId());
        ArgumentCaptor<Collection<SchoolStageDO>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(schoolStageMapper).insertBatch(captor.capture());
        List<String> stages = captor.getValue().stream().map(SchoolStageDO::getStage).toList();
        assertEquals(List.of("primary", "middle"), stages);
    }

    @Test
    void createSchoolGradeShouldRejectStageOutsideSchoolStages() {
        when(schoolMapper.selectById(1L)).thenReturn(school(1L));
        when(gradeCatalogMapper.selectById(10L)).thenReturn(gradeCatalog(10L, "middle", "M1", "初一"));
        when(schoolStageMapper.selectListBySchoolId(1L)).thenReturn(List.of(schoolStage(1L, "primary")));
        SchoolGradeSaveReqVO reqVO = new SchoolGradeSaveReqVO();
        reqVO.setSchoolId(1L);
        reqVO.setGradeCatalogId(10L);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.createSchoolGrade(reqVO));

        assertEquals(SCHOOL_GRADE_STAGE_NOT_ALLOWED.getCode(), exception.getCode());
    }

    @Test
    void updateSchoolShouldRejectRemovingStageWithExistingGrade() {
        when(schoolMapper.selectById(1L)).thenReturn(school(1L));
        when(gradeCatalogMapper.selectListByStatus(0)).thenReturn(List.of(
                gradeCatalog(4L, "primary", "P1", "一年级"),
                gradeCatalog(10L, "middle", "M1", "初一")));
        when(schoolGradeMapper.selectListBySchoolId(1L)).thenReturn(List.of(schoolGrade(100L, 1L, 10L)));
        when(gradeCatalogMapper.selectList(any(SFunction.class), any(Collection.class)))
                .thenReturn(List.of(gradeCatalog(10L, "middle", "M1", "初一")));
        SchoolSaveReqVO reqVO = new SchoolSaveReqVO();
        reqVO.setId(1L);
        reqVO.setSchoolName("测试小学");
        reqVO.setAreaId(1L);
        reqVO.setSchoolAddress("测试地址");
        reqVO.setStationId(9L);
        reqVO.setStageCodes(List.of("primary"));

        ServiceException exception = assertThrows(ServiceException.class, () -> service.updateSchool(reqVO));

        assertEquals(SCHOOL_STAGE_IN_USE.getCode(), exception.getCode());
    }

    @Test
    void createSchoolYearShouldSyncYearCatalogRange() {
        when(schoolMapper.selectById(1L)).thenReturn(school(1L));
        when(yearCatalogMapper.selectById(99L)).thenReturn(yearCatalog(99L, 2026, 2027));
        when(schoolYearMapper.selectBySchoolIdAndYearCatalogId(1L, 99L)).thenReturn(null);
        doAnswer(invocation -> {
            SchoolYearDO schoolYear = invocation.getArgument(0);
            schoolYear.setId(11L);
            return 1;
        }).when(schoolYearMapper).insert(any(SchoolYearDO.class));
        SchoolYearSaveReqVO reqVO = new SchoolYearSaveReqVO();
        reqVO.setSchoolId(1L);
        reqVO.setYearCatalogId(99L);
        reqVO.setStartDate(LocalDate.of(2026, 9, 1));
        reqVO.setEndDate(LocalDate.of(2027, 6, 30));

        Long schoolYearId = service.createSchoolYear(reqVO);

        assertEquals(11L, schoolYearId);
        ArgumentCaptor<SchoolYearDO> captor = ArgumentCaptor.forClass(SchoolYearDO.class);
        verify(schoolYearMapper).insert(captor.capture());
        assertEquals(99L, captor.getValue().getYearCatalogId());
        assertEquals(2026, captor.getValue().getYearStart());
        assertEquals(2027, captor.getValue().getYearEnd());
    }

    private SchoolDO school(Long id) {
        SchoolDO school = new SchoolDO();
        school.setId(id);
        school.setAreaId(1L);
        return school;
    }

    private StationDO station(Long id, Long areaId, Integer status) {
        StationDO station = new StationDO();
        station.setId(id);
        station.setAreaId(areaId);
        station.setStatus(status);
        station.setStationName("测试站点");
        return station;
    }

    private SchoolStageDO schoolStage(Long schoolId, String stage) {
        return SchoolStageDO.builder()
                .schoolId(schoolId)
                .stage(stage)
                .build();
    }

    private SchoolGradeDO schoolGrade(Long id, Long schoolId, Long gradeCatalogId) {
        return SchoolGradeDO.builder()
                .id(id)
                .schoolId(schoolId)
                .gradeCatalogId(gradeCatalogId)
                .build();
    }

    private GradeCatalogDO gradeCatalog(Long id, String stage, String gradeNo, String gradeName) {
        return GradeCatalogDO.builder()
                .id(id)
                .stage(stage)
                .gradeNo(gradeNo)
                .gradeName(gradeName)
                .status(0)
                .build();
    }

    private YearCatalogDO yearCatalog(Long id, Integer yearStart, Integer yearEnd) {
        return YearCatalogDO.builder()
                .id(id)
                .yearStart(yearStart)
                .yearEnd(yearEnd)
                .build();
    }

}
