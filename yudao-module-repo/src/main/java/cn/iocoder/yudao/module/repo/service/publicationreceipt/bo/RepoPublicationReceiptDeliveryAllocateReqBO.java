package cn.iocoder.yudao.module.repo.service.publicationreceipt.bo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 刊物发货占用收货余额 Request BO。
 */
@Data
public class RepoPublicationReceiptDeliveryAllocateReqBO {

    private Long deliveryBatchId;

    private Long operatorUserId;

    private LocalDateTime deliveryTime;

    private RepoPublicationReceiptBalanceKey key;

    private Integer count;

    private String remark;

}
