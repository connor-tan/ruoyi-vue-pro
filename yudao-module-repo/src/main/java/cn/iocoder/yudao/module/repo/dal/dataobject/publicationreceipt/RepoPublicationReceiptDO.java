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

import java.time.LocalDateTime;

/**
 * 刊物收货单 DO。
 */
@TableName("repo_publication_receipt")
@KeySequence("repo_publication_receipt_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoPublicationReceiptDO extends BaseDO {

    @TableId
    private Long id;

    private String receiptNo;

    private Long supplierId;

    private String supplierNameSnapshot;

    private Long warehouseId;

    private String warehouseNameSnapshot;

    /**
     * 枚举 {@link cn.iocoder.yudao.module.repo.enums.receipt.RepoPublicationReceiptStatusEnum}
     */
    private Integer status;

    private Integer expectedCount;

    private Integer receivedCount;

    private Integer allocatedCount;

    private LocalDateTime submitTime;

    private LocalDateTime closeTime;

    private String closeReason;

    private String remark;

}
