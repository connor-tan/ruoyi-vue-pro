package cn.iocoder.yudao.module.product.dal.mysql.publication;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProductSkuPublicationMapper extends BaseMapperX<ProductSkuPublicationDO> {

    default ProductSkuPublicationDO selectByProductSkuId(Long productSkuId) {
        return selectById(productSkuId);
    }

    default List<ProductSkuPublicationDO> selectListByProductSkuIds(Collection<Long> productSkuIds) {
        return selectList(new LambdaQueryWrapperX<ProductSkuPublicationDO>()
                .inIfPresent(ProductSkuPublicationDO::getProductSkuId, productSkuIds));
    }

    @Delete({
            "<script>",
            "DELETE FROM product_sku_publication",
            "WHERE product_sku_id IN",
            "<foreach collection='productSkuIds' item='productSkuId' open='(' separator=',' close=')'>",
            "#{productSkuId}",
            "</foreach>",
            "</script>"
    })
    void deleteByProductSkuIds(@Param("productSkuIds") Collection<Long> productSkuIds);
}
