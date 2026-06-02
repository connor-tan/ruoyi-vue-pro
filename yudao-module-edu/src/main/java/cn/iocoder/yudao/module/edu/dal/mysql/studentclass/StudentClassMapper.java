package cn.iocoder.yudao.module.edu.dal.mysql.studentclass;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
        return selectCurrentListByClassIds(classIds, LocalDate.now());
    }

    default List<StudentClassDO> selectCurrentListByClassIds(Collection<Long> classIds, LocalDate businessDate) {
        if (classIds == null || classIds.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate today = normalizeBusinessDate(businessDate);
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
        return selectCurrentListByStudentIds(studentIds, LocalDate.now());
    }

    default List<StudentClassDO> selectCurrentListByStudentIds(Collection<Long> studentIds, LocalDate businessDate) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate today = normalizeBusinessDate(businessDate);
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
        return selectCurrentListByStudentIdRange(startExclusiveStudentId, endInclusiveStudentId, LocalDate.now());
    }

    default List<StudentClassDO> selectCurrentListByStudentIdRange(Long startExclusiveStudentId,
                                                                   Long endInclusiveStudentId,
                                                                   LocalDate businessDate) {
        if (endInclusiveStudentId == null) {
            return Collections.emptyList();
        }
        LocalDate today = normalizeBusinessDate(businessDate);
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
        return selectCurrentListByStudentId(studentId, LocalDate.now());
    }

    default List<StudentClassDO> selectCurrentListByStudentId(Long studentId, LocalDate businessDate) {
        LocalDate today = normalizeBusinessDate(businessDate);
        return selectList(new LambdaQueryWrapperX<StudentClassDO>()
                .eq(StudentClassDO::getStudentId, studentId)
                .le(StudentClassDO::getStartDate, today)
                .and(wrapper -> wrapper.isNull(StudentClassDO::getEndDate)
                        .or()
                        .ge(StudentClassDO::getEndDate, today))
                .orderByAsc(StudentClassDO::getStartDate)
                .orderByAsc(StudentClassDO::getId));
    }

    default List<StudentClassDO> selectFutureListByStudentIds(Collection<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate today = LocalDate.now();
        return selectList(new LambdaQueryWrapperX<StudentClassDO>()
                .in(StudentClassDO::getStudentId, studentIds)
                .gt(StudentClassDO::getStartDate, today)
                .orderByAsc(StudentClassDO::getStudentId)
                .orderByAsc(StudentClassDO::getStartDate)
                .orderByAsc(StudentClassDO::getId));
    }

    default List<StudentClassDO> selectFutureListByStudentId(Long studentId) {
        LocalDate today = LocalDate.now();
        return selectList(new LambdaQueryWrapperX<StudentClassDO>()
                .eq(StudentClassDO::getStudentId, studentId)
                .gt(StudentClassDO::getStartDate, today)
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

    default List<StudentClassDO> selectListByStudentIdsAndTargetYearCatalogId(Collection<Long> studentIds,
                                                                              Long yearCatalogId) {
        if (studentIds == null || studentIds.isEmpty() || yearCatalogId == null) {
            return Collections.emptyList();
        }
        return selectListByStudentIdsAndTargetYearCatalogIdInternal(studentIds, yearCatalogId);
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

    List<StudentClassDO> selectListByStudentIdsAndTargetYearInternal(@Param("studentIds") Collection<Long> studentIds,
                                                                     @Param("yearStart") Integer yearStart,
                                                                     @Param("yearEnd") Integer yearEnd);

    List<StudentClassDO> selectListByStudentIdsAndTargetYearCatalogIdInternal(
            @Param("studentIds") Collection<Long> studentIds,
            @Param("yearCatalogId") Long yearCatalogId);

    List<StudentClassDO> selectListByStudentIdRangeAndTargetYearInternal(
            @Param("startExclusiveStudentId") Long startExclusiveStudentId,
            @Param("endInclusiveStudentId") Long endInclusiveStudentId,
            @Param("yearStart") Integer yearStart,
            @Param("yearEnd") Integer yearEnd);

    default StudentClassDO selectCurrentByStudentIdAndClassIdAndStartDate(Long studentId, Long classId,
                                                                           java.time.LocalDate startDate) {
        return selectCurrentByStudentIdAndClassIdAndStartDate(studentId, classId, startDate, LocalDate.now());
    }

    default StudentClassDO selectCurrentByStudentIdAndClassIdAndStartDate(Long studentId, Long classId,
                                                                           java.time.LocalDate startDate,
                                                                           LocalDate businessDate) {
        LocalDate today = normalizeBusinessDate(businessDate);
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

    private static LocalDate normalizeBusinessDate(LocalDate businessDate) {
        return businessDate == null ? LocalDate.now() : businessDate;
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

    int deletePhysicallyById(@Param("id") Long id);

    int deletePhysicallyByIds(@Param("ids") Collection<Long> ids);

    int deletePhysicallyByStudentId(@Param("studentId") Long studentId);

    int deletePhysicallyByStudentIds(@Param("studentIds") Collection<Long> studentIds);

    int restoreEndDateById(@Param("id") Long id);

}
