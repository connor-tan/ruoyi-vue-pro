package cn.iocoder.yudao.module.edu.dal.mysql.student;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentFlowPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentFlowDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 学生流转日志 Mapper
 */
@Mapper
public interface StudentFlowMapper extends BaseMapperX<StudentFlowDO> {

    default List<StudentFlowDO> selectListByStudentId(Long studentId) {
        return selectList(new LambdaQueryWrapperX<StudentFlowDO>()
                .eq(StudentFlowDO::getStudentId, studentId)
                .orderByDesc(StudentFlowDO::getEffectiveDate)
                .orderByDesc(StudentFlowDO::getId));
    }

    default List<StudentFlowDO> selectListByBatchIds(Collection<Long> batchIds) {
        if (batchIds == null || batchIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<StudentFlowDO>()
                .in(StudentFlowDO::getBatchId, batchIds)
                .orderByDesc(StudentFlowDO::getEffectiveDate)
                .orderByDesc(StudentFlowDO::getId));
    }

    default int deleteByBatchIds(Collection<Long> batchIds) {
        if (batchIds == null || batchIds.isEmpty()) {
            return 0;
        }
        return delete(new LambdaQueryWrapperX<StudentFlowDO>()
                .in(StudentFlowDO::getBatchId, batchIds));
    }

    default int updateStatusByBatchIds(Collection<Long> batchIds, Integer status) {
        if (batchIds == null || batchIds.isEmpty()) {
            return 0;
        }
        return update(StudentFlowDO.builder().status(status).build(), new LambdaQueryWrapperX<StudentFlowDO>()
                .in(StudentFlowDO::getBatchId, batchIds));
    }

    default PageResult<StudentFlowDO> selectPage(StudentFlowPageReqVO reqVO, Collection<Long> studentIds,
                                                 Collection<Long> batchIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StudentFlowDO>()
                .inIfPresent(StudentFlowDO::getStudentId, studentIds)
                .inIfPresent(StudentFlowDO::getBatchId, batchIds)
                .eqIfPresent(StudentFlowDO::getBatchId, reqVO.getBatchId())
                .eqIfPresent(StudentFlowDO::getChangeType, reqVO.getChangeType())
                .betweenIfPresent(StudentFlowDO::getEffectiveDate, reqVO.getEffectiveDate())
                .orderByDesc(StudentFlowDO::getEffectiveDate)
                .orderByDesc(StudentFlowDO::getId));
    }

}
