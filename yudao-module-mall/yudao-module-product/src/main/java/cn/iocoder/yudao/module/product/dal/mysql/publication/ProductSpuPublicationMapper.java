package cn.iocoder.yudao.module.product.dal.mysql.publication;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuPublicationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProductSpuPublicationMapper extends BaseMapperX<ProductSpuPublicationDO> {

    default ProductSpuPublicationDO selectByProductSpuId(Long productSpuId) {
        return selectById(productSpuId);
    }

    default List<ProductSpuPublicationDO> selectListByProductSpuIds(Collection<Long> productSpuIds) {
        return selectList(new LambdaQueryWrapperX<ProductSpuPublicationDO>()
                .inIfPresent(ProductSpuPublicationDO::getProductSpuId, productSpuIds));
    }

    default List<ProductSpuPublicationDO> selectListByPublicationTitleIds(Collection<Long> publicationTitleIds) {
        return selectList(new LambdaQueryWrapperX<ProductSpuPublicationDO>()
                .inIfPresent(ProductSpuPublicationDO::getPublicationTitleId, publicationTitleIds));
    }

    default Long countByPublicationTitleId(Long publicationTitleId) {
        return selectCount(ProductSpuPublicationDO::getPublicationTitleId, publicationTitleId);
    }
}
