package cn.iocoder.yudao.module.product.dal.mysql.publication;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSkuGradeRelDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProductPublicationSkuGradeRelMapper extends BaseMapperX<ProductPublicationSkuGradeRelDO> {

    int deleteBySkuIdsPhysically(@Param("skuIds") Collection<Long> skuIds);

    default List<ProductPublicationSkuGradeRelDO> selectListBySkuIds(Collection<Long> skuIds) {
        return selectList(new LambdaQueryWrapperX<ProductPublicationSkuGradeRelDO>()
                .inIfPresent(ProductPublicationSkuGradeRelDO::getSkuId, skuIds));
    }
}
