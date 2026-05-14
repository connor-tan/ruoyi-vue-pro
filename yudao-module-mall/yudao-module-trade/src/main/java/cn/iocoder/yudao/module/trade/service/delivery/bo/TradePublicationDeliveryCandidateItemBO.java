package cn.iocoder.yudao.module.trade.service.delivery.bo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 刊物期次批次发货候选订单期次。
 */
@Data
public class TradePublicationDeliveryCandidateItemBO {

    private Long orderIssueId;

    private Long orderId;

    private String orderNo;

    private Long orderItemId;

    private Long deliveryId;

    private Long userId;

    private Integer deliveryType;

    private Integer count;

    private Long schoolId;

    private String schoolNameSnapshot;

    private Long warehouseId;

    private String warehouseNameSnapshot;

    private Long windowId;

    private String windowNameSnapshot;

    private Long offerId;

    private Long offerSkuId;

    private Long skuId;

    private String productNameSnapshot;

    private Long studentId;

    private String studentNameSnapshot;

    private Long classId;

    private String classNameSnapshot;

    private Long issueId;

    private Integer issueNo;

    private String issueName;

    private LocalDate plannedDeliveryDate;

}
