package cn.iocoder.yudao.module.edu.dal.mysql.studentclass;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
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
        LocalDate today = LocalDate.now();
        return selectList(new LambdaQueryWrapperX<StudentClassDO>()
                .in(StudentClassDO::getClassId, classIds)
                .le(StudentClassDO::getStartDate, today)
                .and(wrapper -> wrapper.isNull(StudentClassDO::getEndDate)
                        .or()
                        .ge(StudentClassDO::getEndDate, today))
                .orderByAsc(StudentClassDO::getStartDate)
                .orderByAsc(StudentClassDO::getId));
    }

    default List<StudentClassDO> selectCurrentListByStudentIds(Collection<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate today = LocalDate.now();
        return selectList(new LambdaQueryWrapperX<StudentClassDO>()
                .in(StudentClassDO::getStudentId, studentIds)
                .le(StudentClassDO::getStartDate, today)
                .and(wrapper -> wrapper.isNull(StudentClassDO::getEndDate)
                        .or()
                        .ge(StudentClassDO::getEndDate, today))
                .orderByAsc(StudentClassDO::getStartDate)
                .orderByAsc(StudentClassDO::getId));
    }

    default List<StudentClassDO> selectCurrentListByStudentIdRange(Long startExclusiveStudentId,
                                                                   Long endInclusiveStudentId) {
        if (endInclusiveStudentId == null) {
            return Collections.emptyList();
        }
        LocalDate today = LocalDate.now();
        return selectList(new LambdaQueryWrapperX<StudentClassDO>()
                .gtIfPresent(StudentClassDO::getStudentId, startExclusiveStudentId)
                .le(StudentClassDO::getStudentId, endInclusiveStudentId)
                .le(StudentClassDO::getStartDate, today)
                .and(wrapper -> wrapper.isNull(StudentClassDO::getEndDate)
                        .or()
                        .ge(StudentClassDO::getEndDate, today))
                .orderByAsc(StudentClassDO::getStudentId)
                .orderByAsc(StudentClassDO::getStartDate)
                .orderByAsc(StudentClassDO::getId));
    }

    default List<StudentClassDO> selectCurrentListByStudentId(Long studentId) {
        LocalDate today = LocalDate.now();
        return selectList(new LambdaQueryWrapperX<StudentClassDO>()
                .eq(StudentClassDO::getStudentId, studentId)
                .le(StudentClassDO::getStartDate, today)
                .and(wrapper -> wrapper.isNull(StudentClassDO::getEndDate)
                        .or()
                        .ge(StudentClassDO::getEndDate, today))
                .orderByAsc(StudentClassDO::getStartDate)
                .orderByAsc(StudentClassDO::getId));
    }

    default List<StudentClassDO> selectListByStudentIdsAndTargetYear(Collection<Long> studentIds,
                                                                     Integer yearStart,
                                                                     Integer yearEnd) {
        if (studentIds == null || studentIds.isEmpty() || yearStart == null || yearEnd == null) {
            return Collections.emptyList();
        }
        return selectListByStudentIdsAndTargetYearInternal(studentIds, yearStart, yearEnd);
    }

    default List<StudentClassDO> selectListByStudentIdRangeAndTargetYear(Long startExclusiveStudentId,
                                                                         Long endInclusiveStudentId,
                                                                         Integer yearStart,
                                                                         Integer yearEnd) {
        if (endInclusiveStudentId == null || yearStart == null || yearEnd == null) {
            return Collections.emptyList();
        }
        return selectListByStudentIdRangeAndTargetYearInternal(startExclusiveStudentId, endInclusiveStudentId,
                yearStart, yearEnd);
    }

    @Select({
            "<script>",
            "SELECT sc.*",
            "FROM edu_student_class sc",
            "INNER JOIN edu_school_class c ON c.id = sc.class_id AND c.deleted = b'0'",
            "INNER JOIN edu_school_year y ON y.id = c.school_year_id AND y.deleted = b'0'",
            "WHERE sc.deleted = b'0'",
            "  AND sc.student_id IN",
            "  <foreach collection='studentIds' item='studentId' open='(' separator=',' close=')'>",
            "    #{studentId}",
            "  </foreach>",
            "  AND y.year_start = #{yearStart}",
            "  AND y.year_end = #{yearEnd}",
            "ORDER BY sc.student_id ASC, sc.start_date ASC, sc.id ASC",
            "</script>"
    })
    List<StudentClassDO> selectListByStudentIdsAndTargetYearInternal(@Param("studentIds") Collection<Long> studentIds,
                                                                     @Param("yearStart") Integer yearStart,
                                                                     @Param("yearEnd") Integer yearEnd);

    @Select({
            "<script>",
            "SELECT sc.*",
            "FROM edu_student_class sc",
            "INNER JOIN edu_school_class c ON c.id = sc.class_id AND c.deleted = b'0'",
            "INNER JOIN edu_school_year y ON y.id = c.school_year_id AND y.deleted = b'0'",
            "WHERE sc.deleted = b'0'",
            "  AND sc.student_id &gt; #{startExclusiveStudentId}",
            "  AND sc.student_id &lt;= #{endInclusiveStudentId}",
            "  AND y.year_start = #{yearStart}",
            "  AND y.year_end = #{yearEnd}",
            "ORDER BY sc.student_id ASC, sc.start_date ASC, sc.id ASC",
            "</script>"
    })
    List<StudentClassDO> selectListByStudentIdRangeAndTargetYearInternal(
            @Param("startExclusiveStudentId") Long startExclusiveStudentId,
            @Param("endInclusiveStudentId") Long endInclusiveStudentId,
            @Param("yearStart") Integer yearStart,
            @Param("yearEnd") Integer yearEnd);

    default StudentClassDO selectCurrentByStudentIdAndClassIdAndStartDate(Long studentId, Long classId,
                                                                           java.time.LocalDate startDate) {
        LocalDate today = LocalDate.now();
        return selectOne(new LambdaQueryWrapperX<StudentClassDO>()
                .eq(StudentClassDO::getStudentId, studentId)
                .eq(StudentClassDO::getClassId, classId)
                .eq(StudentClassDO::getStartDate, startDate)
                .le(StudentClassDO::getStartDate, today)
                .and(wrapper -> wrapper.isNull(StudentClassDO::getEndDate)
                        .or()
                        .ge(StudentClassDO::getEndDate, today))
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

    @Delete("DELETE FROM edu_student_class WHERE id = #{id}")
    int deletePhysicallyById(Long id);

    @Update("UPDATE edu_student_class SET end_date = NULL, update_time = NOW() WHERE id = #{id} AND deleted = b'0'")
    int restoreEndDateById(@Param("id") Long id);

}
