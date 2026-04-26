package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 订单站点配送 Request VO")
@Data
public class TradeOrderStationDeliveryReqVO {

    @Schema(description = "配送组编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "配送组编号不能为空")
    private Long deliveryId;
}
