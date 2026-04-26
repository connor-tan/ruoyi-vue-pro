package cn.iocoder.yudao.module.product.api.spu;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.service.category.ProductCategoryService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 商品 SPU API 接口实现类
 *
 * @author LeeYan9
 * @since 2022-09-06
 */
@Service
@Validated
public class ProductSpuApiImpl implements ProductSpuApi {

    @Resource
    private ProductSpuService spuService;
    @Resource
    private ProductCategoryService categoryService;

    @Override
    public List<ProductSpuRespDTO> getSpuList(Collection<Long> ids) {
        List<ProductSpuDO> spus = spuService.getSpuList(ids);
        return fillBizScene(spus);
    }

    @Override
    public List<ProductSpuRespDTO> validateSpuList(Collection<Long> ids) {
        List<ProductSpuDO> spus = spuService.validateSpuList(ids);
        return fillBizScene(spus);
    }

    @Override
    public ProductSpuRespDTO getSpu(Long id) {
        ProductSpuDO spu = spuService.getSpu(id);
        if (spu == null) {
            return null;
        }
        return fillBizScene(List.of(spu)).get(0);
    }

    private List<ProductSpuRespDTO> fillBizScene(List<ProductSpuDO> spus) {
        List<ProductSpuRespDTO> respDTOs = BeanUtils.toBean(spus, ProductSpuRespDTO.class);
        Map<Long, ProductCategoryDO> categoryMap = convertMap(
                categoryService.getEnableCategoryList(List.copyOf(new LinkedHashSet<>(
                        convertSet(spus, ProductSpuDO::getCategoryId)))),
                ProductCategoryDO::getId);
        respDTOs.forEach(item -> {
            ProductCategoryDO category = categoryMap.get(item.getCategoryId());
            item.setBizScene(category == null ? null : category.getBizScene());
        });
        return respDTOs;
    }

}
