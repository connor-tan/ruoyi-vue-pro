package cn.iocoder.yudao.module.trade.api.delivery.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class TradePublicationDeliveryConfirmReqDTO {

    private Long deliveryBatchId;

    private Integer deliveryType;

    private LocalDateTime deliveryTime;

    private List<Item> items;

    @Data
    @Accessors(chain = true)
    public static class Item {

        private Long orderIssueId;

        private Long logisticsId;

        private String logisticsNo;

    }

}
