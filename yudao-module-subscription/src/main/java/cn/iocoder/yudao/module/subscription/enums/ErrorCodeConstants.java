package cn.iocoder.yudao.module.subscription.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {

    ErrorCode WINDOW_NOT_EXISTS = new ErrorCode(1_018_001_001, "订刊窗口不存在");
    ErrorCode WINDOW_ENABLE_CONFLICT = new ErrorCode(1_018_001_002, "同一时刻只能存在一个启用中的订刊窗口");
    ErrorCode WINDOW_TIME_INVALID = new ErrorCode(1_018_001_003, "订刊窗口结束时间必须晚于开始时间");
    ErrorCode SUPPORT_SCHOOL_YEAR_NOT_EXISTS = new ErrorCode(1_018_004_006, "学年不存在");
}
