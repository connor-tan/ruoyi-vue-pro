package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 刊物站点批次发货候选分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TradePublicationDeliveryCandidatePageReqVO extends PageParam {

    @Schema(description = "学校编号", example = "100")
    private Long schoolId;

    @Schema(description = "站点编号", example = "200")
    private Long stationId;

    @Schema(description = "订刊窗口编号", example = "1")
    private Long windowId;

    @Schema(description = "订刊窗口刊物编号", example = "10")
    private Long offerId;

    @Schema(description = "订刊窗口 SKU 编号", example = "100")
    private Long offerSkuId;

    @Schema(description = "商品 SKU 编号", example = "1000")
    private Long skuId;

}
