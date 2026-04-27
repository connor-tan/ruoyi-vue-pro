package cn.iocoder.yudao.module.product.dal.mysql.publication;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationQueryReqDTO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSpuExtDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductPublicationSpuExtMapper extends BaseMapperX<ProductPublicationSpuExtDO> {

    List<Long> selectSpuIdsByQuery(@Param("req") ProductPublicationQueryReqDTO reqDTO);

    int upsert(@Param("ext") ProductPublicationSpuExtDO ext);

    int deleteBySpuIdPhysically(@Param("spuId") Long spuId);
}
