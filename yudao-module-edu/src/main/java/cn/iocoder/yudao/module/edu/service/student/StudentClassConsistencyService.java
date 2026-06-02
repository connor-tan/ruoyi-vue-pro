package cn.iocoder.yudao.module.edu.service.student;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolGradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.studentclass.StudentClassMapper;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_CLASS_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_GRADE_NOT_BELONG_TO_SCHOOL;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_GRADE_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_YEAR_NOT_BELONG_TO_SCHOOL;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_YEAR_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_MULTI_CURRENT;

/**
 * 学生班级链路一致性服务。
 */
@Service
@Validated
public class StudentClassConsistencyService {

    @Resource
    private StudentClassMapper studentClassMapper;
    @Resource
    private SchoolClassMapper schoolClassMapper;
    @Resource
    private SchoolGradeMapper schoolGradeMapper;
    @Resource
    private SchoolYearMapper schoolYearMapper;

    public StudentClassChain validateClassChain(Long classId) {
        SchoolClassDO schoolClass = schoolClassMapper.selectById(classId);
        if (schoolClass == null) {
            throw exception(SCHOOL_CLASS_NOT_EXISTS);
        }
        SchoolYearDO schoolYear = schoolYearMapper.selectById(schoolClass.getSchoolYearId());
        if (schoolYear == null) {
            throw exception(SCHOOL_YEAR_NOT_EXISTS);
        }
        SchoolGradeDO schoolGrade = schoolGradeMapper.selectById(schoolClass.getSchoolGradeId());
        if (schoolGrade == null) {
            throw exception(SCHOOL_GRADE_NOT_EXISTS);
        }
        validateClassChain(schoolClass, schoolYear, schoolGrade);
        return new StudentClassChain(schoolClass, schoolYear, schoolGrade);
    }

    public void validateClassChain(SchoolClassDO schoolClass, SchoolYearDO schoolYear, SchoolGradeDO schoolGrade) {
        if (!Objects.equals(schoolYear.getSchoolId(), schoolClass.getSchoolId())) {
            throw exception(SCHOOL_YEAR_NOT_BELONG_TO_SCHOOL);
        }
        if (!Objects.equals(schoolGrade.getSchoolId(), schoolClass.getSchoolId())) {
            throw exception(SCHOOL_GRADE_NOT_BELONG_TO_SCHOOL);
        }
    }

    public void validateClassChain(SchoolClassDO schoolClass, SchoolYearDO schoolYear, SchoolGradeDO schoolGrade,
                                   Long expectedSchoolId, Long expectedSchoolYearId, Long expectedSchoolGradeId) {
        validateClassChain(schoolClass, schoolYear, schoolGrade);
        if (!Objects.equals(schoolClass.getSchoolId(), expectedSchoolId)
                || !Objects.equals(schoolClass.getSchoolYearId(), expectedSchoolYearId)
                || !Objects.equals(schoolClass.getSchoolGradeId(), expectedSchoolGradeId)) {
            throw exception(SCHOOL_CLASS_NOT_EXISTS);
        }
    }

    public StudentClassSnapshot resolveCurrentClassSnapshot(Long studentId, LocalDate businessDate) {
        List<StudentClassDO> currentStudentClasses = studentClassMapper.selectCurrentListByStudentId(studentId, businessDate);
        if (CollUtil.isEmpty(currentStudentClasses)) {
            return StudentClassSnapshot.empty();
        }
        if (currentStudentClasses.size() > 1) {
            throw exception(STUDENT_CLASS_MULTI_CURRENT);
        }
        StudentClassDO currentStudentClass = currentStudentClasses.get(0);
        StudentClassChain chain = validateClassChain(currentStudentClass.getClassId());
        return StudentClassSnapshot.of(chain.getSchoolClass().getSchoolId(), currentStudentClass.getClassId());
    }

    public List<StudentClassDO> selectOccupiedStudentClasses(Collection<Long> classIds, LocalDate businessDate) {
        return studentClassMapper.selectCurrentListByClassIds(classIds, businessDate);
    }

    @Getter
    @AllArgsConstructor
    public static class StudentClassChain {

        private final SchoolClassDO schoolClass;
        private final SchoolYearDO schoolYear;
        private final SchoolGradeDO schoolGrade;
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class StudentClassSnapshot {

        private final Long currentSchoolId;
        private final Long currentClassId;

        public static StudentClassSnapshot empty() {
            return new StudentClassSnapshot(null, null);
        }
    }

}
