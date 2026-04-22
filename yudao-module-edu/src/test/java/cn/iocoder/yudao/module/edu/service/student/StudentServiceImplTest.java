package cn.iocoder.yudao.module.edu.service.student;

import cn.iocoder.yudao.module.edu.controller.app.student.vo.AppStudentSimpleRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolGradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentFlowMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.studentclass.StudentClassMapper;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
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
        memberUserApi = mock(MemberUserApi.class);
        ReflectionTestUtils.setField(service, "studentMapper", studentMapper);
        ReflectionTestUtils.setField(service, "studentClassMapper", studentClassMapper);
        ReflectionTestUtils.setField(service, "studentFlowMapper", studentFlowMapper);
        ReflectionTestUtils.setField(service, "schoolMapper", schoolMapper);
        ReflectionTestUtils.setField(service, "schoolClassMapper", schoolClassMapper);
        ReflectionTestUtils.setField(service, "schoolGradeMapper", schoolGradeMapper);
        ReflectionTestUtils.setField(service, "gradeCatalogMapper", gradeCatalogMapper);
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

    private StudentDO student(Long id, String studentName, Long currentSchoolId, Integer status) {
        return StudentDO.builder()
                .id(id)
                .studentName(studentName)
                .currentSchoolId(currentSchoolId)
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

    private SchoolGradeDO schoolGrade(Long id, Long gradeCatalogId) {
        return SchoolGradeDO.builder()
                .id(id)
                .gradeCatalogId(gradeCatalogId)
                .build();
    }

    private GradeCatalogDO gradeCatalog(Long id, String gradeName) {
        return GradeCatalogDO.builder()
                .id(id)
                .gradeName(gradeName)
                .build();
    }
}
