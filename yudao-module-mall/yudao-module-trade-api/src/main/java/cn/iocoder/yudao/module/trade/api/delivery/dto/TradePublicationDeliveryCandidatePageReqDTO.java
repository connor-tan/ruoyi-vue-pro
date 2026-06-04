package cn.iocoder.yudao.module.trade.api.delivery.dto;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class TradePublicationDeliveryCandidatePageReqDTO extends PageParam {

    private Integer deliveryType;

    private Long schoolId;

    private Long warehouseId;

    private Long windowId;

    private Long offerId;

    private Long offerSkuId;

    private Long skuId;

    private Long issueId;

    private Integer issueNo;

}
