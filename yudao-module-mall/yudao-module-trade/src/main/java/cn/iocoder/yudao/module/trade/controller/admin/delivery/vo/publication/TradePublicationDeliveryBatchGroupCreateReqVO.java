package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 刊物学校配送候选主表批量创建并发货 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TradePublicationDeliveryBatchGroupCreateReqVO extends TradePublicationDeliveryCandidatePageReqVO {

    @Schema(description = "备注", example = "学校本批次统一发货")
    private String remark;

}
