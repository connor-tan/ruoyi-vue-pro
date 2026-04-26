package cn.iocoder.yudao.module.product.dal.mysql.publication;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSkuExtDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProductPublicationSkuExtMapper extends BaseMapperX<ProductPublicationSkuExtDO> {

    int upsert(@Param("ext") ProductPublicationSkuExtDO ext);

    int deleteBySkuIdsPhysically(@Param("skuIds") Collection<Long> skuIds);

    default List<ProductPublicationSkuExtDO> selectListBySkuIds(Collection<Long> skuIds) {
        return selectList(new LambdaQueryWrapperX<ProductPublicationSkuExtDO>()
                .inIfPresent(ProductPublicationSkuExtDO::getSkuId, skuIds));
    }
}
