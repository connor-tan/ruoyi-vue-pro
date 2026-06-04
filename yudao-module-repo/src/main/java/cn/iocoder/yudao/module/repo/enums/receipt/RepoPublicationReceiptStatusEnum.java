package cn.iocoder.yudao.module.repo.enums.receipt;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 刊物收货单状态。
 */
@Getter
@AllArgsConstructor
public enum RepoPublicationReceiptStatusEnum {

    DRAFT(10, "草稿"),
    PENDING_RECEIVE(20, "待收货"),
    PARTIAL_RECEIVED(30, "部分收货"),
    RECEIVED(40, "已收齐"),
    CLOSED(50, "已关闭");

    private final Integer status;
    private final String name;

}
