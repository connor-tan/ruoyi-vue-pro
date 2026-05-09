package cn.iocoder.yudao.module.product.service.category;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategoryListReqVO;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategorySaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.mysql.category.ProductCategoryMapper;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO.CATEGORY_LEVEL;
import static cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO.PARENT_ID_NULL;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 商品分类 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductCategoryServiceImpl implements ProductCategoryService {

    @Resource
    private ProductCategoryMapper productCategoryMapper;
    @Resource
    @Lazy // 循环依赖，避免报错
    private ProductSpuService productSpuService;

    @Override
    public Long createCategory(ProductCategorySaveReqVO createReqVO) {
        validateBizScene(createReqVO.getBizScene());
        validatePublicationCategoryCreate(createReqVO);
        // 校验父分类存在
        validateParentProductCategory(createReqVO.getParentId(), createReqVO.getBizScene());

        // 插入
        ProductCategoryDO category = BeanUtils.toBean(createReqVO, ProductCategoryDO.class);
        productCategoryMapper.insert(category);
        // 返回
        return category.getId();
    }

    @Override
    public void updateCategory(ProductCategorySaveReqVO updateReqVO) {
        validateBizScene(updateReqVO.getBizScene());
        // 校验分类是否存在
        ProductCategoryDO oldCategory = validateProductCategoryExists(updateReqVO.getId());
        // 校验刊物分类根节点约束
        validatePublicationCategoryUpdate(oldCategory, updateReqVO);
        // 校验父分类存在
        validateParentProductCategory(updateReqVO.getParentId(), updateReqVO.getBizScene());
        // 校验更新不会破坏已绑定商品的分类不变量
        validateReferencedCategoryUpdate(oldCategory, updateReqVO);

        // 更新
        ProductCategoryDO updateObj = BeanUtils.toBean(updateReqVO, ProductCategoryDO.class);
        productCategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteCategory(Long id) {
        // 校验分类是否存在
        ProductCategoryDO category = validateProductCategoryExists(id);
        if (isPublicationRootCategory(category)) {
            throw exception(CATEGORY_PUBLICATION_ROOT_DELETE_FORBIDDEN);
        }
        // 校验是否还有子分类
        if (productCategoryMapper.selectCountByParentId(id) > 0) {
            throw exception(CATEGORY_EXISTS_CHILDREN);
        }
        // 校验分类是否绑定了 SPU
        Long spuCount = productSpuService.getSpuCountByCategoryId(id);
        if (spuCount > 0) {
            throw exception(CATEGORY_HAVE_BIND_SPU);
        }
        // 删除
        productCategoryMapper.deleteById(id);
    }

    private void validatePublicationCategoryCreate(ProductCategorySaveReqVO createReqVO) {
        if (!BizSceneEnum.isPublication(createReqVO.getBizScene())) {
            return;
        }
        ProductCategoryDO publicationRoot = getUniquePublicationRootCategory();
        if (publicationRoot == null) {
            if (!Objects.equals(createReqVO.getParentId(), PARENT_ID_NULL)) {
                throw exception(CATEGORY_PUBLICATION_MUST_UNDER_ROOT);
            }
            return;
        }
        if (Objects.equals(createReqVO.getParentId(), PARENT_ID_NULL)) {
            throw exception(CATEGORY_PUBLICATION_ROOT_EXISTS);
        }
        if (!Objects.equals(createReqVO.getParentId(), publicationRoot.getId())) {
            throw exception(CATEGORY_PUBLICATION_MUST_UNDER_ROOT);
        }
    }

    private void validatePublicationCategoryUpdate(ProductCategoryDO oldCategory, ProductCategorySaveReqVO updateReqVO) {
        boolean oldPublication = BizSceneEnum.isPublication(oldCategory.getBizScene());
        boolean targetPublication = BizSceneEnum.isPublication(updateReqVO.getBizScene());
        if (!oldPublication && !targetPublication) {
            return;
        }

        ProductCategoryDO publicationRoot = getUniquePublicationRootCategory();
        if (isPublicationRootCategory(oldCategory)) {
            if (!targetPublication || !Objects.equals(updateReqVO.getParentId(), PARENT_ID_NULL)) {
                throw exception(CATEGORY_PUBLICATION_ROOT_UPDATE_FORBIDDEN);
            }
            if (Objects.equals(updateReqVO.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
                throw exception(CATEGORY_PUBLICATION_ROOT_DISABLE_FORBIDDEN);
            }
            return;
        }

        if (oldPublication && Objects.equals(updateReqVO.getParentId(), PARENT_ID_NULL)) {
            throw exception(CATEGORY_PUBLICATION_MUST_UNDER_ROOT);
        }
        if (!targetPublication) {
            return;
        }
        if (publicationRoot == null
                || Objects.equals(updateReqVO.getParentId(), PARENT_ID_NULL)
                || !Objects.equals(updateReqVO.getParentId(), publicationRoot.getId())) {
            throw exception(CATEGORY_PUBLICATION_MUST_UNDER_ROOT);
        }
    }

    private ProductCategoryDO getUniquePublicationRootCategory() {
        List<ProductCategoryDO> roots = productCategoryMapper.selectListByBizSceneAndParentId(
                BizSceneEnum.PUBLICATION.getCode(), PARENT_ID_NULL);
        if (roots.size() > 1) {
            throw exception(CATEGORY_PUBLICATION_ROOT_DUPLICATED);
        }
        return roots.isEmpty() ? null : roots.get(0);
    }

    private boolean isPublicationRootCategory(ProductCategoryDO category) {
        return category != null
                && BizSceneEnum.isPublication(category.getBizScene())
                && Objects.equals(category.getParentId(), PARENT_ID_NULL);
    }

    private void validateParentProductCategory(Long id, String bizScene) {
        // 如果是根分类，无需验证
        if (Objects.equals(id, PARENT_ID_NULL)) {
            return;
        }
        // 父分类不存在
        ProductCategoryDO category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw exception(CATEGORY_PARENT_NOT_EXISTS);
        }
        // 父分类不能是二级分类
        if (!Objects.equals(category.getParentId(), PARENT_ID_NULL)) {
            throw exception(CATEGORY_PARENT_NOT_FIRST_LEVEL);
        }
        if (!Objects.equals(category.getBizScene(), bizScene)) {
            throw exception(CATEGORY_PARENT_BIZ_SCENE_INCONSISTENT);
        }
    }

    private ProductCategoryDO validateProductCategoryExists(Long id) {
        ProductCategoryDO category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw exception(CATEGORY_NOT_EXISTS);
        }
        return category;
    }

    private void validateReferencedCategoryUpdate(ProductCategoryDO oldCategory, ProductCategorySaveReqVO updateReqVO) {
        boolean parentChanged = !Objects.equals(oldCategory.getParentId(), updateReqVO.getParentId());
        if (parentChanged && productCategoryMapper.selectCountByParentId(oldCategory.getId()) > 0) {
            throw exception(CATEGORY_UPDATE_FAIL_CHILDREN_PARENT);
        }

        boolean referenced = isCategoryReferencedBySpu(oldCategory.getId());
        if (!referenced) {
            return;
        }
        if (!Objects.equals(oldCategory.getBizScene(), updateReqVO.getBizScene())) {
            throw exception(CATEGORY_UPDATE_FAIL_REFERENCED_BIZ_SCENE);
        }
        if (!Objects.equals(oldCategory.getStatus(), updateReqVO.getStatus())
                && Objects.equals(updateReqVO.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(CATEGORY_UPDATE_FAIL_REFERENCED_STATUS);
        }
        if (parentChanged) {
            throw exception(CATEGORY_UPDATE_FAIL_REFERENCED_PARENT);
        }
    }

    @Override
    public void validateCategoryList(Collection<Long> ids) {
        validateCategoryList0(null, ids, true, false);
    }

    @Override
    public List<ProductCategoryDO> validateLeafCategoryList(String bizScene, Collection<Long> ids) {
        validateBizScene(bizScene);
        return validateCategoryList0(bizScene, ids, true, true);
    }

    @Override
    public void validateCategoryScopeList(Collection<Long> ids) {
        validateCategoryList0(null, ids, false, false);
    }

    private List<ProductCategoryDO> validateCategoryList0(String bizScene, Collection<Long> ids,
                                                          boolean leafOnly, boolean required) {
        if (CollUtil.isEmpty(ids)) {
            if (required) {
                throw exception(SPU_SAVE_FAIL_CATEGORY_REQUIRED);
            }
            return Collections.emptyList();
        }
        List<Long> normalizedIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollUtil.isEmpty(normalizedIds)) {
            if (required) {
                throw exception(SPU_SAVE_FAIL_CATEGORY_REQUIRED);
            }
            return Collections.emptyList();
        }

        // 获得商品分类信息
        List<ProductCategoryDO> list = productCategoryMapper.selectByIds(normalizedIds);
        Map<Long, ProductCategoryDO> categoryMap = CollectionUtils.convertMap(list, ProductCategoryDO::getId);
        // 校验
        List<ProductCategoryDO> categories = new ArrayList<>(normalizedIds.size());
        normalizedIds.forEach(id -> {
            // 校验分类是否存在
            ProductCategoryDO category = categoryMap.get(id);
            if (category == null) {
                throw exception(CATEGORY_NOT_EXISTS);
            }
            // 校验分类是否启用
            if (!CommonStatusEnum.ENABLE.getStatus().equals(category.getStatus())) {
                throw exception(CATEGORY_DISABLED, category.getName());
            }
            // 商品绑定分类必须使用叶子分类；营销范围允许父分类
            if (leafOnly && getCategoryLevel(id) < CATEGORY_LEVEL) {
                throw exception(SPU_SAVE_FAIL_CATEGORY_LEVEL_ERROR);
            }
            if (bizScene != null && !Objects.equals(category.getBizScene(), bizScene)) {
                throw exception(SPU_SAVE_FAIL_CATEGORY_BIZ_SCENE_INCONSISTENT);
            }
            categories.add(category);
        });
        return categories;
    }

    @Override
    public ProductCategoryDO getCategory(Long id) {
        return productCategoryMapper.selectById(id);
    }

    @Override
    public List<ProductCategoryDO> getCategoryList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return productCategoryMapper.selectByIds(ids);
    }

    @Override
    public void validateCategory(Long id) {
        ProductCategoryDO category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw exception(CATEGORY_NOT_EXISTS);
        }
        if (Objects.equals(category.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(CATEGORY_DISABLED, category.getName());
        }
    }

    @Override
    public void validateBizScene(String bizScene) {
        if (bizScene == null) {
            throw exception(CATEGORY_BIZ_SCENE_REQUIRED);
        }
        if (BizSceneEnum.valueOfCode(bizScene) == null) {
            throw exception(CATEGORY_BIZ_SCENE_INVALID);
        }
    }

    @Override
    public Integer getCategoryLevel(Long id) {
        if (Objects.equals(id, PARENT_ID_NULL)) {
            return 0;
        }
        int level = 1;
        // for 的原因，是因为避免脏数据，导致可能的死循环。一般不会超过 100 层哈
        for (int i = 0; i < Byte.MAX_VALUE; i++) {
            // 如果没有父节点，break 结束
            ProductCategoryDO category = productCategoryMapper.selectById(id);
            if (category == null
                    || Objects.equals(category.getParentId(), PARENT_ID_NULL)) {
                break;
            }
            // 继续递归父节点
            level++;
            id = category.getParentId();
        }
        return level;
    }

    @Override
    public List<ProductCategoryDO> getCategoryList(ProductCategoryListReqVO listReqVO) {
        return productCategoryMapper.selectList(listReqVO);
    }

    @Override
    public List<ProductCategoryDO> getEnableCategoryList() {
        return productCategoryMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public List<ProductCategoryDO> getEnableCategoryList(List<Long> ids) {
        return productCategoryMapper.selectListByIdAndStatus(ids, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public Set<Long> getSelfAndDescendantCategoryIds(Collection<Long> ids) {
        Set<Long> result = normalizeCategoryIds(ids);
        if (CollUtil.isEmpty(result)) {
            return Collections.emptySet();
        }
        Set<Long> parentIds = new LinkedHashSet<>(result);
        for (int i = 0; i < Byte.MAX_VALUE && CollUtil.isNotEmpty(parentIds); i++) {
            List<ProductCategoryDO> children = productCategoryMapper.selectList(new ProductCategoryListReqVO()
                    .setParentIds(parentIds));
            Set<Long> childIds = CollectionUtils.convertSet(children, ProductCategoryDO::getId);
            childIds.removeAll(result);
            if (CollUtil.isEmpty(childIds)) {
                break;
            }
            result.addAll(childIds);
            parentIds = childIds;
        }
        return result;
    }

    @Override
    public Set<Long> getSelfAndAncestorCategoryIds(Collection<Long> ids) {
        Set<Long> result = normalizeCategoryIds(ids);
        if (CollUtil.isEmpty(result)) {
            return Collections.emptySet();
        }
        Set<Long> currentIds = new LinkedHashSet<>(result);
        for (int i = 0; i < Byte.MAX_VALUE && CollUtil.isNotEmpty(currentIds); i++) {
            List<ProductCategoryDO> categories = getCategoryList(currentIds);
            Set<Long> parentIds = categories.stream()
                    .map(ProductCategoryDO::getParentId)
                    .filter(Objects::nonNull)
                    .filter(parentId -> !Objects.equals(parentId, PARENT_ID_NULL))
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            parentIds.removeAll(result);
            if (CollUtil.isEmpty(parentIds)) {
                break;
            }
            result.addAll(parentIds);
            currentIds = parentIds;
        }
        return result;
    }

    @Override
    public boolean isCategoryReferencedBySpu(Long id) {
        if (id == null) {
            return false;
        }
        return productSpuService.getSpuCountByCategoryIds(getSelfAndDescendantCategoryIds(List.of(id))) > 0;
    }

    private Set<Long> normalizeCategoryIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return new LinkedHashSet<>();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

}
