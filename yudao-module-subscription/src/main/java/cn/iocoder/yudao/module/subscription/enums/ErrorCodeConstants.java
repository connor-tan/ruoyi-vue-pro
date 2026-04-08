package cn.iocoder.yudao.module.subscription.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {

    ErrorCode WINDOW_NOT_EXISTS = new ErrorCode(1_018_001_001, "订刊窗口不存在");
    ErrorCode WINDOW_ENABLE_CONFLICT = new ErrorCode(1_018_001_002, "同一时刻只能存在一个启用中的订刊窗口");
    ErrorCode WINDOW_TIME_INVALID = new ErrorCode(1_018_001_003, "订刊窗口结束时间必须晚于开始时间");
    ErrorCode SUPPORT_WINDOW_YEAR_NOT_EXISTS = new ErrorCode(1_018_001_004, "目标学年不存在");
    ErrorCode WINDOW_CURRENT_NOT_EXISTS = new ErrorCode(1_018_001_005, "当前没有开放中的订刊窗口");

    ErrorCode WINDOW_SPU_NOT_EXISTS = new ErrorCode(1_018_002_001, "窗口刊物不存在");
    ErrorCode WINDOW_SPU_GRADE_DUPLICATE = new ErrorCode(1_018_002_002, "该年级已存在于当前窗口刊物");
    ErrorCode WINDOW_SPU_GRADE_NOT_MATCH = new ErrorCode(1_018_002_003, "刊物商品未配置所选基础可见年级");

    ErrorCode WINDOW_SKU_NOT_EXISTS = new ErrorCode(1_018_003_001, "窗口刊物 SKU 不存在");

    ErrorCode SUPPORT_SCHOOL_NOT_EXISTS = new ErrorCode(1_018_004_001, "学校不存在");
    ErrorCode SUPPORT_GRADE_CATALOG_NOT_EXISTS = new ErrorCode(1_018_004_002, "年级不存在");
    ErrorCode SUPPORT_STUDENT_NOT_EXISTS = new ErrorCode(1_018_004_003, "学生不存在");
    ErrorCode SUPPORT_SCHOOL_GRADE_NOT_EXISTS = new ErrorCode(1_018_004_004, "学校年级不存在");
    ErrorCode SUPPORT_SCHOOL_CLASS_NOT_EXISTS = new ErrorCode(1_018_004_005, "班级不存在");

    ErrorCode WINDOW_SPU_RULE_NOT_EXISTS = new ErrorCode(1_018_005_001, "窗口刊物特殊规则不存在");
    ErrorCode WINDOW_SPU_RULE_SCOPE_INVALID = new ErrorCode(1_018_005_002, "特殊规则范围配置不正确");

    ErrorCode PREVIEW_STUDENT_BLOCKED = new ErrorCode(1_018_006_001, "当前学生不满足订刊条件：{}");
    ErrorCode APP_STUDENT_NOT_BELONG_TO_PARENT = new ErrorCode(1_018_006_002, "学生不属于当前家长");
    ErrorCode APP_PUBLICATION_NOT_VISIBLE = new ErrorCode(1_018_006_003, "当前刊物不可见");
}
