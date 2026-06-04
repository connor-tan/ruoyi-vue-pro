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

    ErrorCode SUPPLIER_NOT_EXISTS = new ErrorCode(1_017_001_001, "供应商不存在");
    ErrorCode SUPPLIER_NAME_DUPLICATE = new ErrorCode(1_017_001_002, "供应商名称已存在");
    ErrorCode SUPPLIER_CODE_DUPLICATE = new ErrorCode(1_017_001_003, "供应商编码已存在");
    ErrorCode SUPPLIER_DISABLED = new ErrorCode(1_017_001_004, "供应商已停用，不能执行当前操作");
    ErrorCode SUPPLIER_IN_USE = new ErrorCode(1_017_001_005, "供应商已有供货关系或收货单，不能删除");

    ErrorCode SUPPLIER_PUBLICATION_SKU_NOT_EXISTS = new ErrorCode(1_017_002_001, "刊物供应关系不存在");
    ErrorCode SUPPLIER_PUBLICATION_SKU_DUPLICATE = new ErrorCode(1_017_002_002, "该供应商已维护当前刊物 SKU");
    ErrorCode SUPPLIER_PUBLICATION_SKU_DISABLED = new ErrorCode(1_017_002_003, "刊物供应关系已停用，不能收货");
    ErrorCode PUBLICATION_SKU_NOT_EXISTS = new ErrorCode(1_017_002_004, "刊物 SKU 不存在或不是刊物商品");

    ErrorCode PUBLICATION_RECEIPT_NOT_EXISTS = new ErrorCode(1_017_003_001, "刊物收货单不存在");
    ErrorCode PUBLICATION_RECEIPT_ITEM_NOT_EXISTS = new ErrorCode(1_017_003_002, "刊物收货明细不存在");
    ErrorCode PUBLICATION_RECEIPT_STATUS_INVALID = new ErrorCode(1_017_003_003, "当前收货单状态不能执行该操作");
    ErrorCode PUBLICATION_RECEIPT_ITEM_REQUIRED = new ErrorCode(1_017_003_004, "收货单明细不能为空");
    ErrorCode PUBLICATION_RECEIPT_ITEM_EXPECTED_COUNT_INVALID = new ErrorCode(1_017_003_005, "应收数量必须大于 0");
    ErrorCode PUBLICATION_RECEIPT_RECORD_COUNT_INVALID = new ErrorCode(1_017_003_006, "本次收货数量必须大于 0");
    ErrorCode PUBLICATION_RECEIPT_BALANCE_NOT_ENOUGH = new ErrorCode(1_017_003_007, "刊物到货余额不足，不能发货");
    ErrorCode PUBLICATION_RECEIPT_WAREHOUSE_REQUIRED = new ErrorCode(1_017_003_008, "刊物发货必须先确定出库仓库");
    ErrorCode PUBLICATION_RECEIPT_DEMAND_NOT_EXISTS = new ErrorCode(1_017_003_009, "未找到对应的待发刊物需求");

}
