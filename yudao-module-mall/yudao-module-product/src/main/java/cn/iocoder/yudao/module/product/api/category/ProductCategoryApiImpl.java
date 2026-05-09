package cn.iocoder.yudao.module.product.api.category;

import cn.iocoder.yudao.module.product.service.category.ProductCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Set;

/**
 * 商品分类 API 接口实现类
 *
 * @author owen
 */
@Service
@Validated
public class ProductCategoryApiImpl implements ProductCategoryApi {

    @Resource
    private ProductCategoryService productCategoryService;

    @Override
    public void validateCategoryList(Collection<Long> ids) {
        productCategoryService.validateCategoryList(ids);
    }

    @Override
    public void validateCategoryScopeList(Collection<Long> ids) {
        productCategoryService.validateCategoryScopeList(ids);
    }

    @Override
    public Set<Long> getSelfAndDescendantCategoryIds(Collection<Long> ids) {
        return productCategoryService.getSelfAndDescendantCategoryIds(ids);
    }

    @Override
    public Set<Long> getSelfAndAncestorCategoryIds(Collection<Long> ids) {
        return productCategoryService.getSelfAndAncestorCategoryIds(ids);
    }

}
