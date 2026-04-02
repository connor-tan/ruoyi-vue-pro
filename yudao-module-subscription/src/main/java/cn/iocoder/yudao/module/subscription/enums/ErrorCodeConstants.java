package cn.iocoder.yudao.module.subscription.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {

    ErrorCode WINDOW_NOT_EXISTS = new ErrorCode(1_018_001_001, "订刊窗口不存在");
    ErrorCode WINDOW_ENABLE_CONFLICT = new ErrorCode(1_018_001_002, "同一时刻只能存在一个启用中的订刊窗口");
    ErrorCode WINDOW_TIME_INVALID = new ErrorCode(1_018_001_003, "订刊窗口结束时间必须晚于开始时间");
    ErrorCode WINDOW_CURRENT_NOT_EXISTS = new ErrorCode(1_018_001_004, "当前没有开放中的订刊窗口");

    ErrorCode PUBLICATION_PRODUCT_NOT_EXISTS = new ErrorCode(1_018_002_003, "刊物商品不存在");
    ErrorCode PUBLICATION_PRODUCT_DISABLED = new ErrorCode(1_018_002_004, "刊物商品已下架或停用");
    ErrorCode PUBLICATION_CATEGORY_NOT_EXISTS = new ErrorCode(1_018_002_005, "刊物商品分类不存在");
    ErrorCode PUBLICATION_TYPE_ROOT_CATEGORY_NOT_CONFIGURED = new ErrorCode(1_018_002_006, "未配置刊物类型根分类");
    ErrorCode PUBLICATION_TYPE_ROOT_CATEGORY_INVALID = new ErrorCode(1_018_002_007, "刊物类型根分类不存在或已停用");
    ErrorCode PUBLICATION_TYPE_CATEGORY_INVALID = new ErrorCode(1_018_002_008, "商品分类未纳入刊物类型树");
    ErrorCode PUBLICATION_PRODUCT_SPEC_INVALID = new ErrorCode(1_018_002_009, "订刊商品当前只支持单规格");

    ErrorCode WINDOW_PUBLICATION_NOT_EXISTS = new ErrorCode(1_018_003_001, "窗口刊物关系不存在");
    ErrorCode WINDOW_PUBLICATION_DUPLICATE = new ErrorCode(1_018_003_002, "该刊物已在当前窗口中配置");
    ErrorCode WINDOW_PUBLICATION_RULE_NOT_EXISTS = new ErrorCode(1_018_003_003, "窗口刊物特殊规则不存在");
    ErrorCode WINDOW_PUBLICATION_RULE_SCOPE_INVALID = new ErrorCode(1_018_003_004, "窗口刊物特殊规则范围配置不正确");
    ErrorCode WINDOW_PUBLICATION_GRADE_EMPTY = new ErrorCode(1_018_003_005, "窗口刊物至少需要配置一个基础可见年级");

    ErrorCode SUPPORT_PROPERTY_NOT_EXISTS = new ErrorCode(1_018_004_001, "刊物属性项不存在");
    ErrorCode SUPPORT_PROPERTY_VALUE_NOT_EXISTS = new ErrorCode(1_018_004_002, "刊物属性值不存在");
    ErrorCode SUPPORT_PROPERTY_VALUE_NOT_MATCH = new ErrorCode(1_018_004_003, "刊物属性值与属性项不匹配");
    ErrorCode SUPPORT_GRADE_CATALOG_NOT_EXISTS = new ErrorCode(1_018_004_004, "年级目录不存在");
    ErrorCode SUPPORT_SCHOOL_NOT_EXISTS = new ErrorCode(1_018_004_005, "学校不存在");
    ErrorCode SUPPORT_SCHOOL_YEAR_NOT_EXISTS = new ErrorCode(1_018_004_006, "学年不存在");

    ErrorCode APP_STUDENT_NOT_BELONG_TO_PARENT = new ErrorCode(1_018_005_001, "当前学生不属于当前家长");
    ErrorCode APP_SUBSCRIPTION_STUDENT_BLOCKED = new ErrorCode(1_018_005_002, "当前学生暂不具备订刊资格：{}");
    ErrorCode APP_PUBLICATION_NOT_VISIBLE = new ErrorCode(1_018_005_003, "当前刊物对该学生不可见");
}
