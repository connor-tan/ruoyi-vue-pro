package cn.iocoder.yudao.module.edu.dal.mysql.student;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 学生 Mapper
 *
 * @author connor
 */
@Mapper
public interface StudentMapper extends BaseMapperX<StudentDO> {

    default PageResult<StudentDO> selectPage(StudentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StudentDO>()
                .likeIfPresent(StudentDO::getStudentName, reqVO.getStudentName())
                .eqIfPresent(StudentDO::getBelongTo, reqVO.getBelongTo())
                .eqIfPresent(StudentDO::getCurrentSchoolId, reqVO.getCurrentSchoolId())
                .eqIfPresent(StudentDO::getEntryYear, reqVO.getEntryYear())
                .eqIfPresent(StudentDO::getStudentCode, reqVO.getStudentCode())
                .eqIfPresent(StudentDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(StudentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(StudentDO::getId));
    }

    default Long countByCurrentSchoolId(Long schoolId) {
        return selectCount(StudentDO::getCurrentSchoolId, schoolId);
    }

    default Long countByCurrentSchoolIds(Collection<Long> schoolIds) {
        if (schoolIds == null || schoolIds.isEmpty()) {
            return 0L;
        }
        return selectCount(new LambdaQueryWrapperX<StudentDO>()
                .in(StudentDO::getCurrentSchoolId, schoolIds));
    }

    default List<StudentDO> selectListByStudentName(String studentName) {
        return selectList(new LambdaQueryWrapperX<StudentDO>()
                .likeIfPresent(StudentDO::getStudentName, studentName));
    }

    default List<StudentDO> selectSimpleListByStudentName(String studentName) {
        return selectList(new LambdaQueryWrapperX<StudentDO>()
                .likeIfPresent(StudentDO::getStudentName, studentName)
                .orderByAsc(StudentDO::getId)
                .last("LIMIT 50"));
    }

    default List<StudentDO> selectSimpleListByExactStudentName(String studentName) {
        return selectList(new LambdaQueryWrapperX<StudentDO>()
                .eqIfPresent(StudentDO::getStudentName, studentName)
                .orderByAsc(StudentDO::getId)
                .last("LIMIT 500"));
    }

    default List<StudentDO> selectSimpleListByStudentNameAndSchoolId(String studentName, Long schoolId) {
        return selectList(new LambdaQueryWrapperX<StudentDO>()
                .likeIfPresent(StudentDO::getStudentName, studentName)
                .eqIfPresent(StudentDO::getCurrentSchoolId, schoolId)
                .orderByAsc(StudentDO::getId)
                .last("LIMIT 50"));
    }

    default List<StudentDO> selectSimpleListByExactStudentNameAndSchoolId(String studentName, Long schoolId) {
        return selectList(new LambdaQueryWrapperX<StudentDO>()
                .eqIfPresent(StudentDO::getStudentName, studentName)
                .eqIfPresent(StudentDO::getCurrentSchoolId, schoolId)
                .orderByAsc(StudentDO::getId)
                .last("LIMIT 500"));
    }

    default List<StudentDO> selectListByCurrentSchoolId(Long schoolId) {
        return selectList(new LambdaQueryWrapperX<StudentDO>()
                .eq(StudentDO::getCurrentSchoolId, schoolId)
                .orderByAsc(StudentDO::getId));
    }

    default List<StudentDO> selectListByBelongTo(Long belongTo) {
        return selectList(new LambdaQueryWrapperX<StudentDO>()
                .eq(StudentDO::getBelongTo, belongTo)
                .orderByAsc(StudentDO::getId));
    }

    default List<StudentDO> selectListByIdGreaterThan(Long lastId, Integer limit) {
        if (limit == null || limit <= 0) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<StudentDO>()
                .gtIfPresent(StudentDO::getId, lastId)
                .orderByAsc(StudentDO::getId)
                .last("LIMIT " + limit));
    }

    List<Long> selectDistinctCurrentSchoolIds();

    List<Long> selectDistinctCurrentSchoolIdsByStatuses(@Param("statuses") Collection<Integer> statuses);

    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);

}
