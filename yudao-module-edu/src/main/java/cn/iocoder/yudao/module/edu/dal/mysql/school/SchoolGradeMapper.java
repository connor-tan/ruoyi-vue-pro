package cn.iocoder.yudao.module.edu.dal.mysql.school;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Collections;
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

    default List<SchoolGradeDO> selectListBySchoolIds(Collection<Long> schoolIds) {
        if (CollUtil.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SchoolGradeDO>()
                .in(SchoolGradeDO::getSchoolId, schoolIds)
                .orderByAsc(SchoolGradeDO::getSchoolId)
                .orderByAsc(SchoolGradeDO::getId));
    }

    default SchoolGradeDO selectBySchoolIdAndGradeCatalogId(Long schoolId, Long gradeCatalogId) {
        return selectOne(new LambdaQueryWrapperX<SchoolGradeDO>()
                .eq(SchoolGradeDO::getSchoolId, schoolId)
                .eq(SchoolGradeDO::getGradeCatalogId, gradeCatalogId));
    }

    int deletePhysicallyById(@Param("id") Long id);

    int deletePhysicallyBySchoolId(@Param("schoolId") Long schoolId);

    int deletePhysicallyBySchoolIds(@Param("schoolIds") Collection<Long> schoolIds);

}
