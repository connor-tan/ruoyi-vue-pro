package cn.iocoder.yudao.module.product.service.publication;

import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationQueryReqDTO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;

import java.util.Collection;
import java.util.List;

/**
 * 刊物商品 Service 接口
 */
public interface ProductPublicationService {

    ProductPublicationRespDTO getPublication(Long spuId);

    List<ProductPublicationRespDTO> getPublicationList(Collection<Long> spuIds);

    List<ProductPublicationRespDTO> getPublicationList(ProductPublicationQueryReqDTO reqDTO);

    void validatePublicationSaveReq(ProductSpuSaveReqVO reqVO);

    void savePublication(Long spuId, ProductSpuSaveReqVO reqVO, List<ProductSkuDO> savedSkus,
                         Collection<Long> cleanupSkuIds);

    void clearPublication(Long spuId, Collection<Long> skuIds);

    void fillAdminDetail(ProductSpuRespVO respVO, List<ProductSkuRespVO> skuRespVOList);

}
