package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 刊物期次批次发货候选主表分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TradePublicationDeliveryCandidateGroupPageReqVO extends TradePublicationDeliveryCandidatePageReqVO {
}
