package cn.iocoder.yudao.module.trade.dal.dataobject.delivery;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryBatchStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 刊物站点发货批次 DO
 */
@TableName("trade_publication_delivery_batch")
@KeySequence("trade_publication_delivery_batch_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TradePublicationDeliveryBatchDO extends BaseDO {

    /**
     * 批次编号
     */
    private Long id;
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 学校编号
     */
    private Long schoolId;
    /**
     * 配送方式
     */
    private Integer deliveryType;
    /**
     * 学校名称快照
     */
    private String schoolNameSnapshot;
    /**
     * 站点编号
     */
    private Long stationId;
    /**
     * 站点名称快照
     */
    private String stationNameSnapshot;
    /**
     * 订刊窗口编号
     */
    private Long windowId;
    /**
     * 订刊窗口名称快照
     */
    private String windowNameSnapshot;
    /**
     * 订刊窗口刊物编号
     */
    private Long offerId;
    /**
     * 订刊窗口 SKU 编号
     */
    private Long offerSkuId;
    /**
     * 商品 SKU 编号
     */
    private Long skuId;
    /**
     * 刊物商品名称快照
     */
    private String productNameSnapshot;
    /**
     * 订刊期次编号；独立刊物可为空。
     */
    private Long issueId;
    /**
     * 期号
     */
    private Integer issueNo;
    /**
     * 期次名称
     */
    private String issueName;
    /**
     * 本批次数量
     */
    private Integer totalCount;
    /**
     * 涉及订单数
     */
    private Integer orderCount;
    /**
     * 涉及学生数
     */
    private Integer studentCount;
    /**
     * 批次状态
     *
     * 枚举 {@link PublicationDeliveryBatchStatusEnum}
     */
    private Integer status;
    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;
    /**
     * 操作人
     */
    private Long operatorUserId;
    /**
     * 备注
     */
    private String remark;

}
