package cn.iocoder.yudao.module.trade.dal.dataobject.delivery;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 刊物学校配送发货批次明细 DO
 */
@TableName("trade_publication_delivery_batch_item")
@KeySequence("trade_publication_delivery_batch_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TradePublicationDeliveryBatchItemDO extends BaseDO {

    /**
     * 明细编号
     */
    private Long id;
    /**
     * 批次编号
     */
    private Long batchId;
    /**
     * 订单编号
     *
     * 关联 {@link TradeOrderDO#getId()}
     */
    private Long orderId;
    /**
     * 订单号快照
     */
    private String orderNo;
    /**
     * 订单项编号
     *
     * 关联 {@link TradeOrderItemDO#getId()}
     */
    private Long orderItemId;
    /**
     * 订单刊物期次编号
     */
    private Long orderIssueId;
    /**
     * 配送组编号
     *
     * 关联 {@link TradeOrderDeliveryDO#getId()}
     */
    private Long deliveryId;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 商品数量
     */
    private Integer count;
    /**
     * 期号
     */
    private Integer issueNo;
    /**
     * 期次名称
     */
    private String issueName;
    /**
     * 物流公司
     */
    private Long logisticsId;
    /**
     * 物流单号
     */
    private String logisticsNo;
    /**
     * 学生编号
     */
    private Long studentId;
    /**
     * 学生名称快照
     */
    private String studentNameSnapshot;
    /**
     * 班级编号
     */
    private Long classId;
    /**
     * 班级名称快照
     */
    private String classNameSnapshot;

}
