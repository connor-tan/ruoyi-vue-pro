package cn.iocoder.yudao.module.product.api.category;

import java.util.Collection;
import java.util.Set;

/**
 * 商品分类 API 接口
 *
 * @author owen
 */
public interface ProductCategoryApi {

    /**
     * 校验商品分类是否有效。如下情况，视为无效：
     * 1. 商品分类编号不存在
     * 2. 商品分类被禁用
     *
     * @param ids 商品分类编号数组
     */
    void validateCategoryList(Collection<Long> ids);

    /**
     * 校验商品分类范围是否有效。允许父分类和叶子分类，用于营销范围。
     *
     * @param ids 商品分类编号数组
     */
    void validateCategoryScopeList(Collection<Long> ids);

    /**
     * 获得分类自身及所有子孙分类编号。
     *
     * @param ids 分类编号数组
     * @return 分类自身及子孙分类编号
     */
    Set<Long> getSelfAndDescendantCategoryIds(Collection<Long> ids);

    /**
     * 获得分类自身及所有祖先分类编号。
     *
     * @param ids 分类编号数组
     * @return 分类自身及祖先分类编号
     */
    Set<Long> getSelfAndAncestorCategoryIds(Collection<Long> ids);
}
