package cn.iocoder.yudao.module.trade.dal.dataobject.order;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryStatusEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationReceiveStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单刊物期次履约事实。
 */
@TableName("trade_order_publication_issue")
@KeySequence("trade_order_publication_issue_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderPublicationIssueDO extends BaseDO {

    @TableId
    private Long id;

    private Long orderId;
    private String orderNo;
    private Long orderItemId;
    private Long deliveryId;
    private Long userId;

    /**
     * 配送方式
     *
     * 枚举 {@link DeliveryTypeEnum}
     */
    private Integer deliveryType;

    private Long spuId;
    private Long skuId;
    private String productNameSnapshot;
    private Integer count;

    private Long studentId;
    private String studentNameSnapshot;
    private Long schoolId;
    private String schoolNameSnapshot;
    private Long stationId;
    private String stationNameSnapshot;
    private Long classId;
    private String classNameSnapshot;
    private Long warehouseId;
    private String warehouseNameSnapshot;

    private Long windowId;
    private String windowNameSnapshot;
    private Long offerId;
    private Long offerSkuId;

    /**
     * 订刊域期次编号；独立刊物合成期次为空。
     */
    private Long issueId;
    private Integer issueNo;
    private String issueName;
    private LocalDate plannedPublishDate;
    private LocalDate plannedDeliveryDate;

    /**
     * 枚举 {@link PublicationDeliveryStatusEnum}
     */
    private Integer deliveryStatus;
    /**
     * 枚举 {@link PublicationReceiveStatusEnum}
     */
    private Integer receiveStatus;
    private Boolean canceled;

    private Long deliveryBatchId;
    private LocalDateTime deliveryTime;
    private Long logisticsId;
    private String logisticsNo;
    private LocalDateTime receiveTime;
    private Long receiverUserId;

}
