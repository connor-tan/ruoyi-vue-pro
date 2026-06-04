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
 * 刊物出库占用流水 DO。
 */
@TableName("repo_publication_receipt_allocation")
@KeySequence("repo_publication_receipt_allocation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoPublicationReceiptAllocationDO extends BaseDO {

    @TableId
    private Long id;

    private Long receiptItemId;

    private Long deliveryBatchId;

    private Integer allocatedCount;

    private Long operatorUserId;

    private LocalDateTime deliveryTime;

    private String remark;

}
