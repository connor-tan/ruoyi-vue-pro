package cn.iocoder.yudao.module.subscription.service.visibility;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import cn.iocoder.yudao.module.edu.dal.mysql.studentclass.StudentClassMapper;
import cn.iocoder.yudao.module.edu.enums.StudentStatusEnum;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionBlockedReasonEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeCalcRuleEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeResolveModeEnum;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionGradeResolveRespBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubscriptionGradeResolveServiceImplTest {

    private SubscriptionGradeResolveServiceImpl service;
    private SubscriptionSupportService subscriptionSupportService;
    private StudentClassMapper studentClassMapper;

    @BeforeEach
    void setUp() {
        service = new SubscriptionGradeResolveServiceImpl();
        subscriptionSupportService = mock(SubscriptionSupportService.class);
        studentClassMapper = mock(StudentClassMapper.class);
        ReflectionTestUtils.setField(service, "subscriptionSupportService", subscriptionSupportService);
        ReflectionTestUtils.setField(service, "studentClassMapper", studentClassMapper);
    }

    @Test
    void resolveShouldPromoteToDirectNextGradeWhenSchoolEnabledContinuousGrade() {
        mockCurrentChainData(List.of(schoolGrade(10L, 1L, 1L), schoolGrade(20L, 1L, 2L)),
                schoolClass(100L, 1L, 10L), List.of(grade(1L, "P1", "一年级", 1),
                        grade(2L, "P2", "二年级", 2)));

        SubscriptionGradeResolveRespBO respBO = service.resolve(1L, promotedWindow());

        assertEquals(2L, respBO.getEffectiveGradeCatalogId());
        assertEquals("二年级", respBO.getEffectiveGradeName());
    }

    @Test
    void resolveShouldBlockWhenSchoolNextGradeSkipsGlobalCatalog() {
        mockCurrentChainData(List.of(schoolGrade(10L, 1L, 1L), schoolGrade(30L, 1L, 3L)),
                schoolClass(100L, 1L, 10L), List.of(grade(1L, "P1", "一年级", 1),
                        grade(2L, "P2", "二年级", 2), grade(3L, "P3", "三年级", 3)));

        SubscriptionGradeResolveRespBO respBO = service.resolve(1L, promotedWindow());

        assertEquals(SubscriptionBlockedReasonEnum.NEXT_GRADE_NOT_ENABLED.getReason(), respBO.getBlockedReason());
    }

    private void mockCurrentChainData(List<SchoolGradeDO> schoolGrades, SchoolClassDO currentClass,
                                      List<GradeCatalogDO> enabledGradeCatalogs) {
        StudentDO student = StudentDO.builder()
                .id(1L)
                .studentName("张三")
                .currentSchoolId(1L)
                .status(StudentStatusEnum.READING.getStatus())
                .build();
        when(subscriptionSupportService.getStudent(1L)).thenReturn(student);
        when(studentClassMapper.selectCurrentListByStudentIdRange(0L, 1L)).thenReturn(List.of(StudentClassDO.builder()
                .studentId(1L)
                .classId(currentClass.getId())
                .build()));
        when(subscriptionSupportService.getSchoolClassMap(any())).thenReturn(Map.of(currentClass.getId(), currentClass));
        when(subscriptionSupportService.getSchoolGradeListBySchoolIds(any())).thenReturn(schoolGrades);
        when(subscriptionSupportService.getSchoolMap(any())).thenReturn(Map.of(1L, SchoolDO.builder()
                .id(1L)
                .schoolName("测试学校")
                .build()));
        when(subscriptionSupportService.getSchoolGradeMap(any())).thenReturn(schoolGrades.stream()
                .collect(Collectors.toMap(SchoolGradeDO::getId, Function.identity())));
        Map<Long, GradeCatalogDO> gradeCatalogMap = enabledGradeCatalogs.stream()
                .collect(Collectors.toMap(GradeCatalogDO::getId, Function.identity()));
        when(subscriptionSupportService.getGradeCatalogMap(any(Collection.class))).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return ids == null ? Collections.emptyMap() : ids.stream()
                    .filter(gradeCatalogMap::containsKey)
                    .collect(Collectors.toMap(Function.identity(), gradeCatalogMap::get));
        });
        when(subscriptionSupportService.getEnabledGradeCatalogList()).thenReturn(enabledGradeCatalogs);
    }

    private SubscriptionWindowDO promotedWindow() {
        return SubscriptionWindowDO.builder()
                .gradeResolveMode(SubscriptionGradeResolveModeEnum.CURRENT_CHAIN.getMode())
                .gradeCalcRule(SubscriptionGradeCalcRuleEnum.PROMOTED_GRADE.getRule())
                .build();
    }

    private SchoolClassDO schoolClass(Long id, Long schoolId, Long schoolGradeId) {
        return SchoolClassDO.builder()
                .id(id)
                .schoolId(schoolId)
                .schoolGradeId(schoolGradeId)
                .build();
    }

    private SchoolGradeDO schoolGrade(Long id, Long schoolId, Long gradeCatalogId) {
        return SchoolGradeDO.builder()
                .id(id)
                .schoolId(schoolId)
                .gradeCatalogId(gradeCatalogId)
                .build();
    }

    private GradeCatalogDO grade(Long id, String gradeNo, String gradeName, Integer sort) {
        return GradeCatalogDO.builder()
                .id(id)
                .gradeNo(gradeNo)
                .gradeName(gradeName)
                .sort(sort)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build();
    }
}
