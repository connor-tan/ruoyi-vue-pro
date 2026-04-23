package cn.iocoder.yudao.module.edu.dal.mysql.school;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 年级目录 Mapper
 */
@Mapper
public interface GradeCatalogMapper extends BaseMapperX<GradeCatalogDO> {

    default List<GradeCatalogDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<GradeCatalogDO>()
                .eqIfPresent(GradeCatalogDO::getStatus, status)
                .orderByAsc(GradeCatalogDO::getSort)
                .orderByAsc(GradeCatalogDO::getId));
    }

}
