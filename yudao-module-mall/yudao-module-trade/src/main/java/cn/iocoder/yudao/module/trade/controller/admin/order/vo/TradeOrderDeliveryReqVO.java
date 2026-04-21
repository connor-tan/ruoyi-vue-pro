package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Schema(description = "管理后台 - 订单发货 Request VO")
@Data
public class TradeOrderDeliveryReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "配送单编号", example = "2048")
    private Long deliveryId;

    @Schema(description = "发货物流公司编号", example = "1")
    @NotNull(message = "发货物流公司不能为空")
    private Long logisticsId;

    @Schema(description = "发货物流单号", example = "SF123456789")
    private String logisticsNo;

    @AssertTrue(message = "订单编号或配送单编号不能为空")
    @JsonIgnore
    public boolean isValidTarget() {
        return id != null || deliveryId != null;
    }

}
