package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 手工订单导入 Response VO")
@Data
public class TradeOrderManualImportRespVO {

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failureCount;

    @Schema(description = "导入结果")
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "导入订单号")
        private String importOrderNo;

        @Schema(description = "订单编号")
        private Long orderId;

        @Schema(description = "是否成功")
        private Boolean success;

        @Schema(description = "结果说明")
        private String message;

    }

}
