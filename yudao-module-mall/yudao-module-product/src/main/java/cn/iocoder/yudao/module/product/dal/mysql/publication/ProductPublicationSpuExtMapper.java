package cn.iocoder.yudao.module.product.dal.mysql.publication;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSpuExtDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductPublicationSpuExtMapper extends BaseMapperX<ProductPublicationSpuExtDO> {

    int upsert(@Param("ext") ProductPublicationSpuExtDO ext);

    int deleteBySpuIdPhysically(@Param("spuId") Long spuId);
}
