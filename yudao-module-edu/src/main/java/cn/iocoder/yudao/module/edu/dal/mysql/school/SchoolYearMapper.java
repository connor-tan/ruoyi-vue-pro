package cn.iocoder.yudao.module.edu.dal.mysql.school;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 学年 Mapper
 *
 * @author connor
 */
@Mapper
public interface SchoolYearMapper extends BaseMapperX<SchoolYearDO> {

    default PageResult<SchoolYearDO> selectPage(PageParam reqVO, Long schoolId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SchoolYearDO>()
            .eq(SchoolYearDO::getSchoolId, schoolId)
            .orderByDesc(SchoolYearDO::getId));
    }

    default List<SchoolYearDO> selectListBySchoolId(Long schoolId) {
        return selectList(new LambdaQueryWrapperX<SchoolYearDO>()
                .eq(SchoolYearDO::getSchoolId, schoolId)
                .orderByDesc(SchoolYearDO::getYearStart)
                .orderByDesc(SchoolYearDO::getId));
    }

    default SchoolYearDO selectBySchoolIdAndYearStart(Long schoolId, Integer yearStart) {
        return selectOne(new LambdaQueryWrapperX<SchoolYearDO>()
                .eq(SchoolYearDO::getSchoolId, schoolId)
                .eq(SchoolYearDO::getYearStart, yearStart));
    }

    default SchoolYearDO selectBySchoolIdAndYearCatalogId(Long schoolId, Long yearCatalogId) {
        return selectOne(new LambdaQueryWrapperX<SchoolYearDO>()
                .eq(SchoolYearDO::getSchoolId, schoolId)
                .eq(SchoolYearDO::getYearCatalogId, yearCatalogId));
    }

    default List<SchoolYearDO> selectListBySchoolIdsAndYearStarts(Collection<Long> schoolIds,
                                                                  Collection<Integer> yearStarts) {
        if (CollUtil.isEmpty(schoolIds) || CollUtil.isEmpty(yearStarts)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SchoolYearDO>()
                .in(SchoolYearDO::getSchoolId, schoolIds)
                .in(SchoolYearDO::getYearStart, yearStarts));
    }

    default List<SchoolYearDO> selectListBySchoolIdsAndYearCatalogIds(Collection<Long> schoolIds,
                                                                      Collection<Long> yearCatalogIds) {
        if (CollUtil.isEmpty(schoolIds) || CollUtil.isEmpty(yearCatalogIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SchoolYearDO>()
                .in(SchoolYearDO::getSchoolId, schoolIds)
                .in(SchoolYearDO::getYearCatalogId, yearCatalogIds));
    }

    default long countByYearCatalogId(Long yearCatalogId) {
        return selectCount(new LambdaQueryWrapperX<SchoolYearDO>()
                .eq(SchoolYearDO::getYearCatalogId, yearCatalogId));
    }

    default int deleteBySchoolId(Long schoolId) {
        return delete(SchoolYearDO::getSchoolId, schoolId);
    }

    default int deleteBySchoolIds(List<Long> schoolIds) {
        return deleteBatch(SchoolYearDO::getSchoolId, schoolIds);
    }

}
