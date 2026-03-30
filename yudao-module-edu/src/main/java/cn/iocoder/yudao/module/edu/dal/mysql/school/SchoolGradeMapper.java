package cn.iocoder.yudao.module.edu.dal.mysql.school;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 年级定义 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SchoolGradeMapper extends BaseMapperX<SchoolGradeDO> {

    default PageResult<SchoolGradeDO> selectPage(PageParam reqVO, Long schoolId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SchoolGradeDO>()
            .eq(SchoolGradeDO::getSchoolId, schoolId)
            .orderByDesc(SchoolGradeDO::getId));
    }

    default List<SchoolGradeDO> selectListBySchoolId(Long schoolId) {
        return selectList(new LambdaQueryWrapperX<SchoolGradeDO>()
                .eq(SchoolGradeDO::getSchoolId, schoolId)
                .orderByAsc(SchoolGradeDO::getId));
    }

    default SchoolGradeDO selectBySchoolIdAndGradeCatalogId(Long schoolId, Long gradeCatalogId) {
        return selectOne(new LambdaQueryWrapperX<SchoolGradeDO>()
                .eq(SchoolGradeDO::getSchoolId, schoolId)
                .eq(SchoolGradeDO::getGradeCatalogId, gradeCatalogId));
    }

    default int deleteBySchoolId(Long schoolId) {
        return delete(SchoolGradeDO::getSchoolId, schoolId);
    }

    default int deleteBySchoolIds(List<Long> schoolIds) {
        return deleteBatch(SchoolGradeDO::getSchoolId, schoolIds);
    }

}
