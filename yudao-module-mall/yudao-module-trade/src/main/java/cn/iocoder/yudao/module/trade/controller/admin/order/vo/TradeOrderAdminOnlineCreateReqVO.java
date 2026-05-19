package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 在线订刊下单创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TradeOrderAdminOnlineCreateReqVO extends TradeOrderAdminOnlineSettlementReqVO {

    @Schema(description = "商家备注", example = "后台代家长在线下单")
    private String remark;

}
