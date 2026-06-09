package cn.iocoder.yudao.module.trade.api.delivery.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class TradePublicationDeliveryCandidateRespDTO {

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

    private String productSkuName;

    private String isbn;

    private Long issueId;

    private Integer issueNo;

    private String issueName;

    private LocalDate plannedDeliveryDate;

    private Integer totalCount;

    private Integer orderCount;

    private Integer studentCount;

}
