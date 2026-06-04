package cn.iocoder.yudao.module.repo.dal.dataobject.publicationdelivery;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 仓库刊物出库发货批次明细 DO。
 */
@TableName("repo_publication_delivery_batch_item")
@KeySequence("repo_publication_delivery_batch_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RepoPublicationDeliveryBatchItemDO extends BaseDO {

    @TableId
    private Long id;

    private Long batchId;

    private Long orderId;

    private String orderNo;

    private Long orderItemId;

    private Long orderIssueId;

    private Long deliveryId;

    private Long userId;

    private Integer count;

    private Integer issueNo;

    private String issueName;

    private Long logisticsId;

    private String logisticsNo;

    private Long studentId;

    private String studentNameSnapshot;

    private Long classId;

    private String classNameSnapshot;

}
