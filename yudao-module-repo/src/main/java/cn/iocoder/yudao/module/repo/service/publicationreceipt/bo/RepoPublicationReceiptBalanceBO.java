package cn.iocoder.yudao.module.repo.service.publicationreceipt.bo;

import lombok.Data;

/**
 * 刊物收货余额。
 */
@Data
public class RepoPublicationReceiptBalanceBO {

    private Long warehouseId;

    private Long windowId;

    private Long offerId;

    private Long offerSkuId;

    private Long skuId;

    private Long issueId;

    private Integer issueNo;

    private Integer receivedCount;

    private Integer allocatedCount;

    private Integer availableCount;

}
