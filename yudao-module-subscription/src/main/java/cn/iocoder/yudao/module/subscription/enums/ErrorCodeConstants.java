package cn.iocoder.yudao.module.subscription.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Subscription 错误码，使用 1-028-000-000 段。
 */
public interface ErrorCodeConstants {

    ErrorCode WINDOW_NOT_EXISTS = new ErrorCode(1_028_000_000, "订刊窗口不存在");
    ErrorCode WINDOW_CURRENT_NOT_EXISTS = new ErrorCode(1_028_000_001, "当前没有开放的订刊窗口");
    ErrorCode WINDOW_TIME_INVALID = new ErrorCode(1_028_000_002, "订刊窗口结束时间必须晚于开始时间");
    ErrorCode WINDOW_MULTIPLE_OPEN = new ErrorCode(1_028_000_003, "当前只能存在一个开放订刊窗口");
    ErrorCode WINDOW_TIME_OVERLAP = new ErrorCode(1_028_000_004, "启用订刊窗口时间段不能重叠");

    ErrorCode OFFER_NOT_EXISTS = new ErrorCode(1_028_001_000, "窗口刊物不存在");
    ErrorCode OFFER_PRODUCT_NOT_PUBLICATION = new ErrorCode(1_028_001_001, "只能添加刊物商品");
    ErrorCode OFFER_PRODUCT_DUPLICATE = new ErrorCode(1_028_001_002, "窗口中已存在该刊物商品");
    ErrorCode OFFER_ANCHOR_IMMUTABLE = new ErrorCode(1_028_001_003, "窗口刊物的窗口和商品锚点不允许修改");
    ErrorCode OFFER_NO_MATCHED_SKU = new ErrorCode(1_028_001_004, "刊物没有启用且周期匹配的 SKU，不能加入窗口");
    ErrorCode OFFER_SKU_NOT_EXISTS = new ErrorCode(1_028_002_000, "窗口 SKU 不存在");
    ErrorCode OFFER_SKU_TARGET_PERIOD_NOT_MATCHED = new ErrorCode(1_028_002_001, "SKU 周期与窗口目标周期不匹配");
    ErrorCode OFFER_SKU_PRODUCT_MISMATCH = new ErrorCode(1_028_002_002, "SKU 不属于当前窗口刊物");
    ErrorCode OFFER_SKU_DUPLICATE = new ErrorCode(1_028_002_003, "窗口刊物中已存在该 SKU");
    ErrorCode OFFER_SKU_BELONG_ERROR = new ErrorCode(1_028_002_004, "窗口 SKU 不属于当前窗口刊物");
    ErrorCode OFFER_SKU_EFFECTIVE_REQUIRED = new ErrorCode(1_028_002_005, "启用窗口刊物必须至少保留一个启用、周期匹配且有库存的窗口 SKU");

    ErrorCode RULE_NOT_EXISTS = new ErrorCode(1_028_003_000, "订刊规则不存在");
    ErrorCode RULE_EFFECT_INVALID = new ErrorCode(1_028_003_001, "订刊规则作用类型不合法");
    ErrorCode RULE_CONDITION_REQUIRED = new ErrorCode(1_028_003_002, "订刊规则条件不能为空");
    ErrorCode RULE_OFFER_WINDOW_NOT_MATCHED = new ErrorCode(1_028_003_003, "订刊规则刊物不属于当前窗口");
    ErrorCode RULE_FACTOR_INVALID = new ErrorCode(1_028_003_004, "订刊规则因子不合法");
    ErrorCode RULE_OPERATOR_INVALID = new ErrorCode(1_028_003_005, "订刊规则操作符不合法");
    ErrorCode RULE_OFFER_SKU_SCOPE_INVALID = new ErrorCode(1_028_003_006, "窗口 SKU 条件只能用于刊物级规则");
    ErrorCode RULE_OFFER_SKU_NOT_MATCHED = new ErrorCode(1_028_003_007, "窗口 SKU 不属于当前规则刊物");
    ErrorCode RULE_SCOPE_INVALID = new ErrorCode(1_028_003_008, "订刊规则作用域不合法");
    ErrorCode RULE_CONDITION_VALUE_INVALID = new ErrorCode(1_028_003_009, "订刊规则条件值不合法");

    ErrorCode PREVIEW_STUDENT_BLOCKED = new ErrorCode(1_028_004_000, "学生无法参与订刊：{}");
    ErrorCode APP_PUBLICATION_NOT_VISIBLE = new ErrorCode(1_028_004_001, "当前刊物对该学生不可订");
    ErrorCode ORDER_ITEM_COUNT_INVALID = new ErrorCode(1_028_004_002, "订刊数量必须大于 0");
    ErrorCode ORDER_OFFER_SKU_NOT_AVAILABLE = new ErrorCode(1_028_004_003, "窗口 SKU 不可订");
    ErrorCode ORDER_OFFER_SKU_PRODUCT_SKU_MISMATCH = new ErrorCode(1_028_004_004, "窗口 SKU 与商品 SKU 不匹配");
    ErrorCode ORDER_MAX_QUANTITY_EXCEEDED = new ErrorCode(1_028_004_005, "超过该刊物 SKU 的限购数量");

}
