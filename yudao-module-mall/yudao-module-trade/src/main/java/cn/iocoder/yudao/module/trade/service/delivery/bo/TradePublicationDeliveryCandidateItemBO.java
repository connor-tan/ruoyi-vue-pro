package cn.iocoder.yudao.module.trade.service.delivery.bo;

import lombok.Data;

/**
 * 刊物站点批次发货候选订单项
 */
@Data
public class TradePublicationDeliveryCandidateItemBO {

    private Long orderId;

    private String orderNo;

    private Long orderItemId;

    private Long deliveryId;

    private Long userId;

    private Integer count;

    private Long schoolId;

    private String schoolNameSnapshot;

    private Long stationId;

    private String stationNameSnapshot;

    private Long windowId;

    private String windowNameSnapshot;

    private Long offerId;

    private Long offerSkuId;

    private Long skuId;

    private String productNameSnapshot;

    private String targetPeriod;

    private Long studentId;

    private String studentNameSnapshot;

    private Long classId;

    private String classNameSnapshot;

}
