package cn.iocoder.yudao.module.edu.dal.mysql.studentclass;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 学生班级记录 Mapper
 *
 * @author connor
 */
@Mapper
public interface StudentClassMapper extends BaseMapperX<StudentClassDO> {

    default List<StudentClassDO> selectListByStudentId(Long studentId) {
        return selectList(new LambdaQueryWrapperX<StudentClassDO>()
                .eq(StudentClassDO::getStudentId, studentId)
                .orderByAsc(StudentClassDO::getStartDate)
                .orderByAsc(StudentClassDO::getId));
    }

    default List<StudentClassDO> selectCurrentListByClassIds(Collection<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<StudentClassDO>()
                .in(StudentClassDO::getClassId, classIds)
                .isNull(StudentClassDO::getEndDate)
                .orderByAsc(StudentClassDO::getStartDate)
                .orderByAsc(StudentClassDO::getId));
    }

    default List<StudentClassDO> selectCurrentListByStudentId(Long studentId) {
        return selectList(new LambdaQueryWrapperX<StudentClassDO>()
                .eq(StudentClassDO::getStudentId, studentId)
                .isNull(StudentClassDO::getEndDate)
                .orderByAsc(StudentClassDO::getStartDate)
                .orderByAsc(StudentClassDO::getId));
    }

    default StudentClassDO selectCurrentByStudentIdAndClassIdAndStartDate(Long studentId, Long classId,
                                                                           java.time.LocalDate startDate) {
        return selectOne(new LambdaQueryWrapperX<StudentClassDO>()
                .eq(StudentClassDO::getStudentId, studentId)
                .eq(StudentClassDO::getClassId, classId)
                .eq(StudentClassDO::getStartDate, startDate)
                .isNull(StudentClassDO::getEndDate)
                .last("LIMIT 1"));
    }

    default StudentClassDO selectLatestEndedByStudentIdAndClassIdAndEndDate(Long studentId, Long classId,
                                                                             java.time.LocalDate endDate) {
        return selectOne(new LambdaQueryWrapperX<StudentClassDO>()
                .eq(StudentClassDO::getStudentId, studentId)
                .eq(StudentClassDO::getClassId, classId)
                .eq(StudentClassDO::getEndDate, endDate)
                .orderByDesc(StudentClassDO::getStartDate)
                .orderByDesc(StudentClassDO::getId)
                .last("LIMIT 1"));
    }

    default Long countByClassId(Long classId) {
        return selectCount(StudentClassDO::getClassId, classId);
    }

    default Long countByClassIds(Collection<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return 0L;
        }
        return selectCount(new LambdaQueryWrapperX<StudentClassDO>()
                .in(StudentClassDO::getClassId, classIds));
    }

    default int deleteByStudentId(Long studentId) {
        return delete(StudentClassDO::getStudentId, studentId);
    }

    default int deleteByStudentIds(List<Long> studentIds) {
        return deleteBatch(StudentClassDO::getStudentId, studentIds);
    }

}
