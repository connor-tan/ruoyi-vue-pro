package cn.iocoder.yudao.module.product.service.category;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategorySaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.mysql.category.ProductCategoryMapper;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO.PARENT_ID_NULL;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@Import(ProductCategoryServiceImpl.class)
class ProductCategoryMultiCategoryTest extends BaseDbUnitTest {

    @Resource
    private ProductCategoryServiceImpl productCategoryService;
    @Resource
    private ProductCategoryMapper productCategoryMapper;
    @MockitoBean
    private ProductSpuService productSpuService;

    @BeforeEach
    void setUp() {
        when(productSpuService.getSpuCountByCategoryIds(anyCollection())).thenReturn(0L);
        when(productSpuService.getSpuCountByCategoryId(anyLong())).thenReturn(0L);
    }

    @Test
    void validateLeafCategoryList_rejectsParentCategory() {
        ProductCategoryDO parent = createCategory(PARENT_ID_NULL, BizSceneEnum.NORMAL.getCode(), CommonStatusEnum.ENABLE.getStatus());

        assertServiceException(() -> productCategoryService.validateLeafCategoryList(
                BizSceneEnum.NORMAL.getCode(), List.of(parent.getId())), SPU_SAVE_FAIL_CATEGORY_LEVEL_ERROR);
    }

    @Test
    void validateCategoryScopeList_allowsParentCategory() {
        ProductCategoryDO parent = createCategory(PARENT_ID_NULL, BizSceneEnum.NORMAL.getCode(), CommonStatusEnum.ENABLE.getStatus());

        productCategoryService.validateCategoryScopeList(List.of(parent.getId()));
    }

    @Test
    void getSelfAndAncestorCategoryIds_returnsLeafAndParent() {
        ProductCategoryDO parent = createCategory(PARENT_ID_NULL, BizSceneEnum.NORMAL.getCode(), CommonStatusEnum.ENABLE.getStatus());
        ProductCategoryDO leaf = createCategory(parent.getId(), BizSceneEnum.NORMAL.getCode(), CommonStatusEnum.ENABLE.getStatus());

        assertEquals(Set.of(leaf.getId(), parent.getId()),
                productCategoryService.getSelfAndAncestorCategoryIds(List.of(leaf.getId())));
    }

    @Test
    void updateCategory_rejectsDisableWhenDescendantReferenced() {
        ProductCategoryDO parent = createCategory(PARENT_ID_NULL, BizSceneEnum.NORMAL.getCode(), CommonStatusEnum.ENABLE.getStatus());
        createCategory(parent.getId(), BizSceneEnum.NORMAL.getCode(), CommonStatusEnum.ENABLE.getStatus());
        when(productSpuService.getSpuCountByCategoryIds(anyCollection())).thenReturn(1L);

        ProductCategorySaveReqVO reqVO = new ProductCategorySaveReqVO();
        reqVO.setId(parent.getId());
        reqVO.setParentId(PARENT_ID_NULL);
        reqVO.setName(parent.getName());
        reqVO.setPicUrl(parent.getPicUrl());
        reqVO.setSort(parent.getSort());
        reqVO.setStatus(CommonStatusEnum.DISABLE.getStatus());
        reqVO.setBizScene(parent.getBizScene());

        assertServiceException(() -> productCategoryService.updateCategory(reqVO), CATEGORY_UPDATE_FAIL_REFERENCED_STATUS);
    }

    @Test
    void createCategory_rejectsSecondPublicationRoot() {
        createCategory(PARENT_ID_NULL, BizSceneEnum.PUBLICATION.getCode(), CommonStatusEnum.ENABLE.getStatus());
        ProductCategorySaveReqVO reqVO = createSaveReq(null, PARENT_ID_NULL,
                BizSceneEnum.PUBLICATION.getCode(), CommonStatusEnum.ENABLE.getStatus());

        assertServiceException(() -> productCategoryService.createCategory(reqVO), CATEGORY_PUBLICATION_ROOT_EXISTS);
    }

    @Test
    void createCategory_allowsPublicationChildUnderRoot() {
        ProductCategoryDO root = createCategory(PARENT_ID_NULL, BizSceneEnum.PUBLICATION.getCode(),
                CommonStatusEnum.ENABLE.getStatus());
        ProductCategorySaveReqVO reqVO = createSaveReq(null, root.getId(),
                BizSceneEnum.PUBLICATION.getCode(), CommonStatusEnum.ENABLE.getStatus());

        Long categoryId = productCategoryService.createCategory(reqVO);

        ProductCategoryDO category = productCategoryMapper.selectById(categoryId);
        assertNotNull(category);
        assertEquals(root.getId(), category.getParentId());
        assertEquals(BizSceneEnum.PUBLICATION.getCode(), category.getBizScene());
    }

    @Test
    void createCategory_allowsNormalRoot() {
        ProductCategorySaveReqVO reqVO = createSaveReq(null, PARENT_ID_NULL,
                BizSceneEnum.NORMAL.getCode(), CommonStatusEnum.ENABLE.getStatus());

        Long categoryId = productCategoryService.createCategory(reqVO);

        ProductCategoryDO category = productCategoryMapper.selectById(categoryId);
        assertNotNull(category);
        assertEquals(PARENT_ID_NULL, category.getParentId());
        assertEquals(BizSceneEnum.NORMAL.getCode(), category.getBizScene());
    }

    @Test
    void updateCategory_rejectsPublicationChildMovedToRoot() {
        ProductCategoryDO root = createCategory(PARENT_ID_NULL, BizSceneEnum.PUBLICATION.getCode(),
                CommonStatusEnum.ENABLE.getStatus());
        ProductCategoryDO child = createCategory(root.getId(), BizSceneEnum.PUBLICATION.getCode(),
                CommonStatusEnum.ENABLE.getStatus());
        ProductCategorySaveReqVO reqVO = createSaveReq(child.getId(), PARENT_ID_NULL,
                BizSceneEnum.PUBLICATION.getCode(), CommonStatusEnum.ENABLE.getStatus());

        assertServiceException(() -> productCategoryService.updateCategory(reqVO), CATEGORY_PUBLICATION_MUST_UNDER_ROOT);
    }

    @Test
    void updateCategory_rejectsNormalRootChangedToPublicationRoot() {
        createCategory(PARENT_ID_NULL, BizSceneEnum.PUBLICATION.getCode(), CommonStatusEnum.ENABLE.getStatus());
        ProductCategoryDO normalRoot = createCategory(PARENT_ID_NULL, BizSceneEnum.NORMAL.getCode(),
                CommonStatusEnum.ENABLE.getStatus());
        ProductCategorySaveReqVO reqVO = createSaveReq(normalRoot.getId(), PARENT_ID_NULL,
                BizSceneEnum.PUBLICATION.getCode(), CommonStatusEnum.ENABLE.getStatus());

        assertServiceException(() -> productCategoryService.updateCategory(reqVO), CATEGORY_PUBLICATION_MUST_UNDER_ROOT);
    }

    @Test
    void updateCategory_rejectsPublicationRootMoved() {
        ProductCategoryDO root = createCategory(PARENT_ID_NULL, BizSceneEnum.PUBLICATION.getCode(),
                CommonStatusEnum.ENABLE.getStatus());
        ProductCategoryDO normalRoot = createCategory(PARENT_ID_NULL, BizSceneEnum.NORMAL.getCode(),
                CommonStatusEnum.ENABLE.getStatus());
        ProductCategorySaveReqVO reqVO = createSaveReq(root.getId(), normalRoot.getId(),
                BizSceneEnum.PUBLICATION.getCode(), CommonStatusEnum.ENABLE.getStatus());

        assertServiceException(() -> productCategoryService.updateCategory(reqVO),
                CATEGORY_PUBLICATION_ROOT_UPDATE_FORBIDDEN);
    }

    @Test
    void updateCategory_rejectsPublicationRootDisabled() {
        ProductCategoryDO root = createCategory(PARENT_ID_NULL, BizSceneEnum.PUBLICATION.getCode(),
                CommonStatusEnum.ENABLE.getStatus());
        ProductCategorySaveReqVO reqVO = createSaveReq(root.getId(), PARENT_ID_NULL,
                BizSceneEnum.PUBLICATION.getCode(), CommonStatusEnum.DISABLE.getStatus());

        assertServiceException(() -> productCategoryService.updateCategory(reqVO),
                CATEGORY_PUBLICATION_ROOT_DISABLE_FORBIDDEN);
    }

    @Test
    void deleteCategory_rejectsPublicationRoot() {
        ProductCategoryDO root = createCategory(PARENT_ID_NULL, BizSceneEnum.PUBLICATION.getCode(),
                CommonStatusEnum.ENABLE.getStatus());

        assertServiceException(() -> productCategoryService.deleteCategory(root.getId()),
                CATEGORY_PUBLICATION_ROOT_DELETE_FORBIDDEN);
    }

    @Test
    void updateCategory_allowsPublicationRootDisplayFields() {
        ProductCategoryDO root = createCategory(PARENT_ID_NULL, BizSceneEnum.PUBLICATION.getCode(),
                CommonStatusEnum.ENABLE.getStatus());
        ProductCategorySaveReqVO reqVO = createSaveReq(root.getId(), PARENT_ID_NULL,
                BizSceneEnum.PUBLICATION.getCode(), CommonStatusEnum.ENABLE.getStatus());
        reqVO.setName("新刊物根");
        reqVO.setPicUrl("https://example.com/new-root.png");
        reqVO.setSort(99);

        productCategoryService.updateCategory(reqVO);

        ProductCategoryDO category = productCategoryMapper.selectById(root.getId());
        assertEquals("新刊物根", category.getName());
        assertEquals("https://example.com/new-root.png", category.getPicUrl());
        assertEquals(99, category.getSort());
        assertEquals(PARENT_ID_NULL, category.getParentId());
        assertEquals(BizSceneEnum.PUBLICATION.getCode(), category.getBizScene());
    }

    private ProductCategoryDO createCategory(Long parentId, String bizScene, Integer status) {
        ProductCategoryDO category = new ProductCategoryDO();
        category.setParentId(parentId);
        category.setName("category-" + System.nanoTime());
        category.setPicUrl("https://example.com/category.png");
        category.setSort(1);
        category.setStatus(status);
        category.setBizScene(bizScene);
        productCategoryMapper.insert(category);
        return category;
    }

    private ProductCategorySaveReqVO createSaveReq(Long id, Long parentId, String bizScene, Integer status) {
        ProductCategorySaveReqVO reqVO = new ProductCategorySaveReqVO();
        reqVO.setId(id);
        reqVO.setParentId(parentId);
        reqVO.setName("category-" + System.nanoTime());
        reqVO.setPicUrl("https://example.com/category.png");
        reqVO.setSort(1);
        reqVO.setStatus(status);
        reqVO.setBizScene(bizScene);
        return reqVO;
    }

}
