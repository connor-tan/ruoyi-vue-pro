package cn.iocoder.yudao.module.product.service.category;

import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategoryListReqVO;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategorySaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 商品分类 Service 接口
 *
 * @author 芋道源码
 */
public interface ProductCategoryService {

    /**
     * 创建商品分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCategory(@Valid ProductCategorySaveReqVO createReqVO);

    /**
     * 更新商品分类
     *
     * @param updateReqVO 更新信息
     */
    void updateCategory(@Valid ProductCategorySaveReqVO updateReqVO);

    /**
     * 删除商品分类
     *
     * @param id 编号
     */
    void deleteCategory(Long id);

    /**
     * 获得商品分类
     *
     * @param id 编号
     * @return 商品分类
     */
    ProductCategoryDO getCategory(Long id);

    /**
     * 批量获得商品分类。
     *
     * @param ids 编号数组
     * @return 商品分类列表
     */
    List<ProductCategoryDO> getCategoryList(Collection<Long> ids);

    /**
     * 校验商品分类
     *
     * @param id 分类编号
     */
    void validateCategory(Long id);

    /**
     * 校验商品分类场景是否合法
     *
     * @param bizScene 业务场景
     */
    void validateBizScene(String bizScene);

    /**
     * 获得商品分类的层级
     *
     * @param id 编号
     * @return 商品分类的层级
     */
    Integer getCategoryLevel(Long id);

    /**
     * 获得商品分类列表
     *
     * @param listReqVO 查询条件
     * @return 商品分类列表
     */
    List<ProductCategoryDO> getCategoryList(ProductCategoryListReqVO listReqVO);

    /**
     * 获得开启状态的商品分类列表
     *
     * @return 商品分类列表
     */
    List<ProductCategoryDO> getEnableCategoryList();

    /**
     * 获得开启状态的商品分类列表，指定编号
     *
     * @param ids 商品分类编号数组
     * @return 商品分类列表
     */
    List<ProductCategoryDO> getEnableCategoryList(List<Long> ids);

    /**
     * 校验商品分类是否有效。如下情况，视为无效：
     * 1. 商品分类编号不存在
     * 2. 商品分类被禁用
     * 3. 商品分类层级校验，必须使用第二级的商品分类及以下
     *
     * @param ids 商品分类编号数组
     */
    void validateCategoryList(Collection<Long> ids);

    /**
     * 校验商品绑定分类列表，只允许启用的叶子分类。
     *
     * @param bizScene 商品业务场景
     * @param ids 商品分类编号数组
     * @return 商品分类列表，按入参去重后的顺序返回
     */
    List<ProductCategoryDO> validateLeafCategoryList(String bizScene, Collection<Long> ids);

    /**
     * 校验营销范围分类列表，允许父分类和叶子分类。
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

    /**
     * 判断分类自身或子孙分类是否已被商品引用。
     *
     * @param id 分类编号
     * @return 是否被商品引用
     */
    boolean isCategoryReferencedBySpu(Long id);

}
