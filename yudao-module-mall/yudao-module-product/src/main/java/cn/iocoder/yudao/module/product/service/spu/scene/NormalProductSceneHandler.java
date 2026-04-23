package cn.iocoder.yudao.module.product.service.spu.scene;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.service.brand.ProductBrandService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import cn.iocoder.yudao.module.product.service.publication.ProductPublicationService;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.NORMAL_PRODUCT_BRAND_REQUIRED;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.NORMAL_PRODUCT_DELIVERY_REQUIRED;

@Component
public class NormalProductSceneHandler implements ProductSceneHandler {

    @Resource
    private ProductBrandService brandService;
    @Resource
    private ProductSkuService productSkuService;
    @Resource
    private ProductPublicationService productPublicationService;

    @Override
    public String getBizScene() {
        return BizSceneEnum.NORMAL.getCode();
    }

    @Override
    public void validateForSave(ProductCategoryDO category, ProductSpuSaveReqVO reqVO) {
        if (reqVO.getBrandId() == null) {
            throw exception(NORMAL_PRODUCT_BRAND_REQUIRED);
        }
        if (CollUtil.isEmpty(reqVO.getDeliveryTypes())) {
            throw exception(NORMAL_PRODUCT_DELIVERY_REQUIRED);
        }
        brandService.validateProductBrand(reqVO.getBrandId());
        List<ProductSkuSaveReqVO> skuSaveReqList = reqVO.getSkus();
        productSkuService.validateSkuList(skuSaveReqList, reqVO.getSpecType());
        reqVO.setPublicationExt(null);
    }

    @Override
    public void afterSave(Long spuId, ProductSpuSaveReqVO reqVO, List<ProductSkuDO> savedSkus, java.util.Collection<Long> cleanupSkuIds) {
        productPublicationService.clearPublication(spuId, cleanupSkuIds);
    }
}
