package cn.iocoder.yudao.module.edu.dal.mysql.student;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentPromotionBatchDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 学生一键升班批次 Mapper
 */
@Mapper
public interface StudentPromotionBatchMapper extends BaseMapperX<StudentPromotionBatchDO> {

    default List<StudentPromotionBatchDO> selectListByTaskId(Long taskId) {
        return selectList(new LambdaQueryWrapperX<StudentPromotionBatchDO>()
                .eq(StudentPromotionBatchDO::getTaskId, taskId)
                .orderByAsc(StudentPromotionBatchDO::getId));
    }

    default List<StudentPromotionBatchDO> selectListByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<StudentPromotionBatchDO>()
                .in(StudentPromotionBatchDO::getId, ids));
    }

    default Long countBySchoolYearIdsAndStatuses(Long schoolId, Long fromSchoolYearId, Long toSchoolYearId,
                                                  Collection<Integer> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return 0L;
        }
        return selectCount(new LambdaQueryWrapperX<StudentPromotionBatchDO>()
                .eq(StudentPromotionBatchDO::getSchoolId, schoolId)
                .eq(StudentPromotionBatchDO::getFromSchoolYearId, fromSchoolYearId)
                .eq(StudentPromotionBatchDO::getToSchoolYearId, toSchoolYearId)
                .in(StudentPromotionBatchDO::getStatus, statuses));
    }
}
