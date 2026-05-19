package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - 在线订刊下单创建 Response VO")
@Data
@Accessors(chain = true)
public class TradeOrderAdminOnlineCreateRespVO {

    @Schema(description = "交易订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "支付订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long payOrderId;

}
