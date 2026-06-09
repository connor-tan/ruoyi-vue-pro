package cn.iocoder.yudao.module.trade.api.delivery.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TradePublicationDeliveryCandidateGroupRespDTO {

    private Integer deliveryType;

    private Long schoolId;

    private String schoolNameSnapshot;

    private Long stationId;

    private String stationNameSnapshot;

    private Long warehouseId;

    private String warehouseNameSnapshot;

    private Long windowId;

    private String windowNameSnapshot;

    private Integer totalCount;

    private Integer orderCount;

    private Integer studentCount;

    private Integer publicationGroupCount;

    private Integer issueGroupCount;

}
