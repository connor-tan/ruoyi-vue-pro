package cn.iocoder.yudao.module.product.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Product 错误码枚举类
 *
 * product 系统，使用 1-008-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 商品分类相关 1-008-001-000 ============
    ErrorCode CATEGORY_NOT_EXISTS = new ErrorCode(1_008_001_000, "商品分类不存在");
    ErrorCode CATEGORY_PARENT_NOT_EXISTS = new ErrorCode(1_008_001_001, "父分类不存在");
    ErrorCode CATEGORY_PARENT_NOT_FIRST_LEVEL = new ErrorCode(1_008_001_002, "父分类不能是二级分类");
    ErrorCode CATEGORY_EXISTS_CHILDREN = new ErrorCode(1_008_001_003, "存在子分类，无法删除");
    ErrorCode CATEGORY_DISABLED = new ErrorCode(1_008_001_004, "商品分类({})已禁用，无法使用");
    ErrorCode CATEGORY_HAVE_BIND_SPU = new ErrorCode(1_008_001_005, "类别下存在商品，无法删除");
    ErrorCode CATEGORY_BIZ_SCENE_REQUIRED = new ErrorCode(1_008_001_006, "商品分类业务场景不能为空");
    ErrorCode CATEGORY_BIZ_SCENE_INVALID = new ErrorCode(1_008_001_007, "商品分类业务场景不合法");
    ErrorCode CATEGORY_PARENT_BIZ_SCENE_INCONSISTENT = new ErrorCode(1_008_001_008, "父子分类业务场景必须一致");
    ErrorCode CATEGORY_UPDATE_FAIL_REFERENCED_BIZ_SCENE = new ErrorCode(1_008_001_009, "商品分类已被商品引用，不能修改业务场景");
    ErrorCode CATEGORY_UPDATE_FAIL_REFERENCED_STATUS = new ErrorCode(1_008_001_010, "商品分类已被商品引用，不能禁用");
    ErrorCode CATEGORY_UPDATE_FAIL_REFERENCED_PARENT = new ErrorCode(1_008_001_011, "商品分类已被商品引用，不能修改上级分类");
    ErrorCode CATEGORY_UPDATE_FAIL_CHILDREN_PARENT = new ErrorCode(1_008_001_012, "商品分类存在子分类，不能修改上级分类");
    ErrorCode CATEGORY_PUBLICATION_ROOT_EXISTS = new ErrorCode(1_008_001_013, "刊物顶级分类已存在，新增刊物分类必须挂在刊物根分类下");
    ErrorCode CATEGORY_PUBLICATION_MUST_UNDER_ROOT = new ErrorCode(1_008_001_014, "刊物分类必须挂在刊物根分类下");
    ErrorCode CATEGORY_PUBLICATION_ROOT_DUPLICATED = new ErrorCode(1_008_001_015, "刊物顶级分类数据异常，请保留唯一刊物根分类");
    ErrorCode CATEGORY_PUBLICATION_ROOT_UPDATE_FORBIDDEN = new ErrorCode(1_008_001_016, "刊物根分类不能修改业务场景或上级分类");
    ErrorCode CATEGORY_PUBLICATION_ROOT_DISABLE_FORBIDDEN = new ErrorCode(1_008_001_017, "刊物根分类不能禁用");
    ErrorCode CATEGORY_PUBLICATION_ROOT_DELETE_FORBIDDEN = new ErrorCode(1_008_001_018, "刊物根分类不能删除");

    // ========== 商品品牌相关编号 1-008-002-000 ==========
    ErrorCode BRAND_NOT_EXISTS = new ErrorCode(1_008_002_000, "品牌不存在");
    ErrorCode BRAND_DISABLED = new ErrorCode(1_008_002_001, "品牌已禁用");
    ErrorCode BRAND_NAME_EXISTS = new ErrorCode(1_008_002_002, "品牌名称已存在");

    // ========== 商品属性项 1-008-003-000 ==========
    ErrorCode PROPERTY_NOT_EXISTS = new ErrorCode(1_008_003_000, "属性项不存在");
    ErrorCode PROPERTY_EXISTS = new ErrorCode(1_008_003_001, "属性项的名称已存在");
    ErrorCode PROPERTY_DELETE_FAIL_VALUE_EXISTS = new ErrorCode(1_008_003_002, "属性项下存在属性值，无法删除");

    // ========== 商品属性值 1-008-004-000 ==========
    ErrorCode PROPERTY_VALUE_NOT_EXISTS = new ErrorCode(1_008_004_000, "属性值不存在");
    ErrorCode PROPERTY_VALUE_EXISTS = new ErrorCode(1_008_004_001, "属性值的名称已存在");

    // ========== 商品 SPU 1-008-005-000 ==========
    ErrorCode SPU_NOT_EXISTS = new ErrorCode(1_008_005_000, "商品 SPU 不存在");
    ErrorCode SPU_SAVE_FAIL_CATEGORY_LEVEL_ERROR = new ErrorCode(1_008_005_001, "商品分类不正确，原因：必须使用第二级的商品分类及以下");
    ErrorCode SPU_SAVE_FAIL_COUPON_TEMPLATE_NOT_EXISTS = new ErrorCode(1_008_005_002, "商品 SPU 保存失败，原因：优惠劵不存在");
    ErrorCode SPU_NOT_ENABLE = new ErrorCode(1_008_005_003, "商品 SPU【{}】不处于上架状态");
    ErrorCode SPU_NOT_RECYCLE = new ErrorCode(1_008_005_004, "商品 SPU 不处于回收站状态");
    ErrorCode NORMAL_PRODUCT_BRAND_REQUIRED = new ErrorCode(1_008_005_005, "普通商品品牌不能为空");
    ErrorCode NORMAL_PRODUCT_DELIVERY_REQUIRED = new ErrorCode(1_008_005_006, "普通商品配送方式不能为空");
    ErrorCode NORMAL_PRODUCT_DELIVERY_TYPE_INVALID = new ErrorCode(1_008_005_007, "普通商品配送方式只允许快递发货或用户自提");
    ErrorCode NORMAL_PRODUCT_DELIVERY_TEMPLATE_REQUIRED = new ErrorCode(1_008_005_008, "普通商品选择快递发货时，运费模板不能为空");
    ErrorCode SPU_SAVE_FAIL_CATEGORY_REQUIRED = new ErrorCode(1_008_005_009, "商品分类不能为空");
    ErrorCode SPU_SAVE_FAIL_CATEGORY_BIZ_SCENE_INCONSISTENT = new ErrorCode(1_008_005_010, "商品分类业务场景必须与商品业务场景一致");

    // ========== 商品 SKU 1-008-006-000 ==========
    ErrorCode SKU_NOT_EXISTS = new ErrorCode(1_008_006_000, "商品 SKU 不存在");
    ErrorCode SKU_PROPERTIES_DUPLICATED = new ErrorCode(1_008_006_001, "商品 SKU 的属性组合存在重复");
    ErrorCode SPU_ATTR_NUMBERS_MUST_BE_EQUALS = new ErrorCode(1_008_006_002, "一个 SPU 下的每个 SKU，其属性项必须一致");
    ErrorCode SPU_SKU_NOT_DUPLICATE = new ErrorCode(1_008_006_003, "一个 SPU 下的每个 SKU，必须不重复");
    ErrorCode SKU_STOCK_NOT_ENOUGH = new ErrorCode(1_008_006_004, "商品 SKU 库存不足");

    // ========== 商品 评价 1-008-007-000 ==========
    ErrorCode COMMENT_NOT_EXISTS = new ErrorCode(1_008_007_000, "商品评价不存在");
    ErrorCode COMMENT_ORDER_EXISTS = new ErrorCode(1_008_007_001, "订单的商品评价已存在");

    // ========== 商品 收藏 1-008-008-000 ==========
    ErrorCode FAVORITE_EXISTS = new ErrorCode(1_008_008_000, "该商品已经被收藏");
    ErrorCode FAVORITE_NOT_EXISTS = new ErrorCode(1_008_008_001, "商品收藏不存在");

    // ========== 刊物主数据 1-008-009-000 ==========
    ErrorCode PUBLICATION_PUBLISHER_NOT_EXISTS = new ErrorCode(1_008_009_000, "出版社不存在");
    ErrorCode PUBLICATION_PUBLISHER_NAME_EXISTS = new ErrorCode(1_008_009_001, "出版社名称已存在");
    ErrorCode PUBLICATION_TYPE_NOT_EXISTS = new ErrorCode(1_008_009_002, "刊物类型不存在");
    ErrorCode PUBLICATION_TYPE_NAME_EXISTS = new ErrorCode(1_008_009_003, "刊物类型名称已存在");

    // ========== 刊物商品 1-008-010-000 ==========
    ErrorCode PUBLICATION_EXT_REQUIRED = new ErrorCode(1_008_010_000, "刊物扩展信息不能为空");
    ErrorCode PUBLICATION_PUBLISHER_REQUIRED = new ErrorCode(1_008_010_001, "刊物出版社不能为空");
    ErrorCode PUBLICATION_TYPE_REQUIRED = new ErrorCode(1_008_010_002, "刊物类型不能为空");
    ErrorCode PUBLICATION_ISSUE_CYCLE_REQUIRED = new ErrorCode(1_008_010_003, "出刊周期不能为空");
    ErrorCode PUBLICATION_SKU_REQUIRED = new ErrorCode(1_008_010_004, "刊物至少需要一个 SKU");
    ErrorCode PUBLICATION_SKU_EXT_REQUIRED = new ErrorCode(1_008_010_005, "刊物 SKU 扩展信息不能为空");
    ErrorCode PUBLICATION_SKU_GRADE_REQUIRED = new ErrorCode(1_008_010_006, "刊物 SKU 适用年级不能为空");
    ErrorCode PUBLICATION_TITLE_IDENTIFIER_REQUIRED = new ErrorCode(1_008_010_007, "当前刊物类型要求至少填写 ISSN、CN 刊号、邮发代号之一");
    ErrorCode PUBLICATION_SKU_ISBN_REQUIRED = new ErrorCode(1_008_010_008, "当前刊物类型要求每个 SKU 都填写 ISBN");
    ErrorCode PUBLICATION_GRADE_CATALOG_NOT_EXISTS = new ErrorCode(1_008_010_009, "适用年级不存在或已禁用");
    ErrorCode PUBLICATION_DELIVERY_REQUIRED = new ErrorCode(1_008_010_010, "刊物商品配送方式不能为空");
    ErrorCode PUBLICATION_DELIVERY_TYPE_INVALID = new ErrorCode(1_008_010_011, "刊物商品配送方式只允许快递发货或站点配送");
    ErrorCode PUBLICATION_DELIVERY_TEMPLATE_REQUIRED = new ErrorCode(1_008_010_012, "刊物商品选择快递发货时，运费模板不能为空");

}
