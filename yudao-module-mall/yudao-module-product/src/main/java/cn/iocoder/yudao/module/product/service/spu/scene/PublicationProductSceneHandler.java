package cn.iocoder.yudao.module.product.service.spu.scene;

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.service.publication.ProductPublicationService;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PublicationProductSceneHandler implements ProductSceneHandler {

    @Resource
    private ProductPublicationService productPublicationService;

    @Override
    public String getBizScene() {
        return BizSceneEnum.PUBLICATION.getCode();
    }

    @Override
    public void validateForSave(ProductCategoryDO category, ProductSpuSaveReqVO reqVO) {
        productPublicationService.validatePublicationSaveReq(reqVO);
    }

    @Override
    public void afterSave(Long spuId, ProductSpuSaveReqVO reqVO, List<ProductSkuDO> savedSkus, java.util.Collection<Long> cleanupSkuIds) {
        productPublicationService.savePublication(spuId, reqVO, savedSkus, cleanupSkuIds);
    }
}
