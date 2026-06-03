package cn.iocoder.yudao.module.product.service.spu.scene;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.service.brand.ProductBrandService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import cn.iocoder.yudao.module.product.service.publication.ProductPublicationService;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.NORMAL_PRODUCT_BRAND_REQUIRED;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.NORMAL_PRODUCT_DELIVERY_REQUIRED;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.NORMAL_PRODUCT_DELIVERY_TEMPLATE_REQUIRED;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.NORMAL_PRODUCT_DELIVERY_TYPE_INVALID;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_STOCK_INVALID;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_STOCK_REQUIRED;

@Component
public class NormalProductSceneHandler implements ProductSceneHandler {

    private static final Set<Integer> NORMAL_DELIVERY_TYPES = Set.of(
            DeliveryTypeEnum.EXPRESS.getType(), DeliveryTypeEnum.PICK_UP.getType());

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
    public void validateForSave(ProductSpuSaveReqVO reqVO) {
        if (reqVO.getBrandId() == null) {
            throw exception(NORMAL_PRODUCT_BRAND_REQUIRED);
        }
        if (CollUtil.isEmpty(reqVO.getDeliveryTypes())) {
            throw exception(NORMAL_PRODUCT_DELIVERY_REQUIRED);
        }
        Set<Integer> deliveryTypes = new LinkedHashSet<>(reqVO.getDeliveryTypes());
        if (deliveryTypes.stream().anyMatch(deliveryType -> deliveryType == null
                || !NORMAL_DELIVERY_TYPES.contains(deliveryType))) {
            throw exception(NORMAL_PRODUCT_DELIVERY_TYPE_INVALID);
        }
        if (deliveryTypes.contains(DeliveryTypeEnum.EXPRESS.getType()) && reqVO.getDeliveryTemplateId() == null) {
            throw exception(NORMAL_PRODUCT_DELIVERY_TEMPLATE_REQUIRED);
        }
        brandService.validateProductBrand(reqVO.getBrandId());
        List<ProductSkuSaveReqVO> skuSaveReqList = reqVO.getSkus();
        productSkuService.validateSkuList(skuSaveReqList, reqVO.getSpecType());
        validateNormalSkuStock(skuSaveReqList);
        reqVO.setPublicationExt(null);
    }

    private void validateNormalSkuStock(List<ProductSkuSaveReqVO> skus) {
        for (ProductSkuSaveReqVO sku : skus) {
            if (sku.getStock() == null) {
                throw exception(SKU_STOCK_REQUIRED);
            }
            if (sku.getStock() < 0) {
                throw exception(SKU_STOCK_INVALID);
            }
        }
    }

    @Override
    public void afterSave(Long spuId, ProductSpuSaveReqVO reqVO, List<ProductSkuDO> savedSkus, java.util.Collection<Long> cleanupSkuIds) {
        productPublicationService.clearPublication(spuId, cleanupSkuIds);
    }
}
