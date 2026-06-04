package cn.iocoder.yudao.module.trade.api.delivery.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TradePublicationDeliveryCreateReqDTO {

    private Integer deliveryType;

    private Long schoolId;

    private Long warehouseId;

    private Long windowId;

    private Long offerId;

    private Long offerSkuId;

    private Long skuId;

    private Long issueId;

    private Integer issueNo;

    private List<ExpressItem> expressItems;

    @Data
    @Accessors(chain = true)
    public static class ExpressItem {

        private Long orderIssueId;

        private Long logisticsId;

        private String logisticsNo;

    }

}
