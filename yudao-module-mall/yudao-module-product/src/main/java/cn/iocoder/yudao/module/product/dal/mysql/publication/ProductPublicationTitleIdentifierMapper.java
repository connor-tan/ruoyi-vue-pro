package cn.iocoder.yudao.module.product.dal.mysql.publication;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTitleIdentifierDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProductPublicationTitleIdentifierMapper extends BaseMapperX<ProductPublicationTitleIdentifierDO> {

    default ProductPublicationTitleIdentifierDO selectByPublicationTitleId(Long publicationTitleId) {
        return selectById(publicationTitleId);
    }

    default List<ProductPublicationTitleIdentifierDO> selectListByPublicationTitleIds(Collection<Long> publicationTitleIds) {
        return selectList(new LambdaQueryWrapperX<ProductPublicationTitleIdentifierDO>()
                .inIfPresent(ProductPublicationTitleIdentifierDO::getPublicationTitleId, publicationTitleIds));
    }
}
