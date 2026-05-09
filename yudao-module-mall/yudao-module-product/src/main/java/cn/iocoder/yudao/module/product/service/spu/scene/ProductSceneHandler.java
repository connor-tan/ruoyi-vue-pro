package cn.iocoder.yudao.module.product.service.spu.scene;

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;

import java.util.Collection;
import java.util.List;

public interface ProductSceneHandler {

    String getBizScene();

    void validateForSave(ProductSpuSaveReqVO reqVO);

    void afterSave(Long spuId, ProductSpuSaveReqVO reqVO, List<ProductSkuDO> savedSkus, Collection<Long> cleanupSkuIds);
}
