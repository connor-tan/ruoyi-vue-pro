package cn.iocoder.yudao.module.repo.dal.dataobject.publicationdelivery;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryBatchStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 仓库刊物出库发货批次 DO。
 */
@TableName("repo_publication_delivery_batch")
@KeySequence("repo_publication_delivery_batch_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RepoPublicationDeliveryBatchDO extends BaseDO {

    @TableId
    private Long id;

    private String batchNo;

    private Integer deliveryType;

    private Long schoolId;

    private String schoolNameSnapshot;

    private Long stationId;

    private String stationNameSnapshot;

    private Long warehouseId;

    private String warehouseNameSnapshot;

    private Long windowId;

    private String windowNameSnapshot;

    private Long offerId;

    private Long offerSkuId;

    private Long skuId;

    private String productNameSnapshot;

    private Long issueId;

    private Integer issueNo;

    private String issueName;

    private Integer totalCount;

    private Integer orderCount;

    private Integer studentCount;

    /**
     * 枚举 {@link PublicationDeliveryBatchStatusEnum}
     */
    private Integer status;

    private LocalDateTime deliveryTime;

    private Long operatorUserId;

    private String remark;

}
