package cn.iocoder.yudao.module.product.api.publication;

import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 刊物商品只读 API
 */
public interface ProductPublicationApi {

    /**
     * 获得单个刊物商品
     *
     * @param spuId SPU 编号
     * @return 刊物商品
     */
    ProductPublicationRespDTO getPublication(Long spuId);

    /**
     * 批量获得刊物商品
     *
     * @param spuIds SPU 编号集合
     * @return 刊物商品列表
     */
    List<ProductPublicationRespDTO> getPublicationList(Collection<Long> spuIds);

}
