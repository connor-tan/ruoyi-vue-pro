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
 * 刊物收货记录 DO。
 */
@TableName("repo_publication_receipt_record")
@KeySequence("repo_publication_receipt_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoPublicationReceiptRecordDO extends BaseDO {

    @TableId
    private Long id;

    private Long receiptId;

    private Long receiptItemId;

    private LocalDateTime receivedTime;

    private Integer bundleCount;

    private Integer receivedCount;

    private Long operatorUserId;

    private String remark;

}
