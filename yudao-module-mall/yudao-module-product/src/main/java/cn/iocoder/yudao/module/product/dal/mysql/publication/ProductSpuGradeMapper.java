package cn.iocoder.yudao.module.product.dal.mysql.publication;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuGradeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProductSpuGradeMapper extends BaseMapperX<ProductSpuGradeDO> {

    default List<ProductSpuGradeDO> selectListByProductSpuId(Long productSpuId) {
        return selectList(new LambdaQueryWrapperX<ProductSpuGradeDO>()
                .eq(ProductSpuGradeDO::getProductSpuId, productSpuId));
    }

    default List<ProductSpuGradeDO> selectListByProductSpuIds(Collection<Long> productSpuIds) {
        return selectList(new LambdaQueryWrapperX<ProductSpuGradeDO>()
                .inIfPresent(ProductSpuGradeDO::getProductSpuId, productSpuIds));
    }

    default List<ProductSpuGradeDO> selectListByGradeCatalogIds(Collection<Long> gradeCatalogIds) {
        return selectList(new LambdaQueryWrapperX<ProductSpuGradeDO>()
                .inIfPresent(ProductSpuGradeDO::getGradeCatalogId, gradeCatalogIds));
    }

    default void deleteByProductSpuId(Long productSpuId) {
        delete(new LambdaQueryWrapperX<ProductSpuGradeDO>()
                .eq(ProductSpuGradeDO::getProductSpuId, productSpuId));
    }
}
