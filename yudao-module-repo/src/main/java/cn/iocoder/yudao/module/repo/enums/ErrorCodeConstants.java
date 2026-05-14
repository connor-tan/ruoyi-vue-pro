package cn.iocoder.yudao.module.repo.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Repo 错误码枚举类
 * repo 系统，使用 1-017-000-000 段。
 */
public interface ErrorCodeConstants {

    ErrorCode WAREHOUSE_NOT_EXISTS = new ErrorCode(1_017_000_001, "仓库不存在");
    ErrorCode WAREHOUSE_NAME_DUPLICATE = new ErrorCode(1_017_000_002, "仓库名称已存在");
    ErrorCode WAREHOUSE_DISABLED = new ErrorCode(1_017_000_003, "仓库已停用，不能绑定到学校");
    ErrorCode WAREHOUSE_IN_USE_BY_SCHOOL = new ErrorCode(1_017_000_004, "仓库已绑定学校，无法执行当前操作");

}
