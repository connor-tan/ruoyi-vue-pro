package cn.iocoder.yudao.module.edu.dal.mysql.school;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo.YearCatalogPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.YearCatalogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface YearCatalogMapper extends BaseMapperX<YearCatalogDO> {

    default PageResult<YearCatalogDO> selectPage(YearCatalogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<YearCatalogDO>()
                .eqIfPresent(YearCatalogDO::getYearStart, reqVO.getYearStart())
                .eqIfPresent(YearCatalogDO::getYearEnd, reqVO.getYearEnd())
                .betweenIfPresent(YearCatalogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(YearCatalogDO::getYearStart)
                .orderByDesc(YearCatalogDO::getYearEnd)
                .orderByDesc(YearCatalogDO::getId));
    }

    default List<YearCatalogDO> selectAllList() {
        return selectList(new LambdaQueryWrapperX<YearCatalogDO>()
                .orderByDesc(YearCatalogDO::getYearStart)
                .orderByDesc(YearCatalogDO::getYearEnd)
                .orderByDesc(YearCatalogDO::getId));
    }

    default YearCatalogDO selectByYearRange(Integer yearStart, Integer yearEnd) {
        return selectOne(new LambdaQueryWrapperX<YearCatalogDO>()
                .eq(YearCatalogDO::getYearStart, yearStart)
                .eq(YearCatalogDO::getYearEnd, yearEnd));
    }

    int deletePhysicallyById(@Param("id") Long id);
}
