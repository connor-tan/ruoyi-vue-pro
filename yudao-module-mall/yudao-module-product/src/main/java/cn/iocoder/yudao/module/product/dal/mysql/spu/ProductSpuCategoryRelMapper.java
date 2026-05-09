package cn.iocoder.yudao.module.product.dal.mysql.spu;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuCategoryRelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 商品 SPU 分类关系 Mapper
 */
@Mapper
public interface ProductSpuCategoryRelMapper extends BaseMapperX<ProductSpuCategoryRelDO> {

    default List<ProductSpuCategoryRelDO> selectListBySpuId(Long spuId) {
        return selectList(new LambdaQueryWrapperX<ProductSpuCategoryRelDO>()
                .eq(ProductSpuCategoryRelDO::getSpuId, spuId)
                .orderByAsc(ProductSpuCategoryRelDO::getSort)
                .orderByAsc(ProductSpuCategoryRelDO::getId));
    }

    default List<ProductSpuCategoryRelDO> selectListBySpuIds(Collection<Long> spuIds) {
        if (CollUtil.isEmpty(spuIds)) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<ProductSpuCategoryRelDO>()
                .in(ProductSpuCategoryRelDO::getSpuId, spuIds)
                .orderByAsc(ProductSpuCategoryRelDO::getSpuId)
                .orderByAsc(ProductSpuCategoryRelDO::getSort)
                .orderByAsc(ProductSpuCategoryRelDO::getId));
    }

    default int deleteBySpuId(Long spuId) {
        return delete(ProductSpuCategoryRelDO::getSpuId, spuId);
    }

    default Long selectCountByCategoryId(Long categoryId) {
        return selectCount(ProductSpuCategoryRelDO::getCategoryId, categoryId);
    }

    default Long selectCountByCategoryIds(Collection<Long> categoryIds) {
        if (CollUtil.isEmpty(categoryIds)) {
            return 0L;
        }
        return selectCount(new LambdaQueryWrapperX<ProductSpuCategoryRelDO>()
                .in(ProductSpuCategoryRelDO::getCategoryId, categoryIds));
    }

}
