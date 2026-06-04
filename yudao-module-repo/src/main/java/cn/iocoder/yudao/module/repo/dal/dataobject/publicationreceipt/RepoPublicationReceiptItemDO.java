package cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 刊物收货明细 DO。
 */
@TableName("repo_publication_receipt_item")
@KeySequence("repo_publication_receipt_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoPublicationReceiptItemDO extends BaseDO {

    @TableId
    private Long id;

    private Long receiptId;

    private Long supplierId;

    private Long warehouseId;

    private Long windowId;

    private String windowNameSnapshot;

    private Long offerId;

    private Long offerSkuId;

    private Long spuId;

    private Long skuId;

    private String productNameSnapshot;

    private String productSkuNameSnapshot;

    private String isbn;

    private Long issueId;

    private Integer issueNo;

    private String issueName;

    private Integer expectedCount;

    private Integer receivedCount;

    private Integer allocatedCount;

    private String remark;

}
