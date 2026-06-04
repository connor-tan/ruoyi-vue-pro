package cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 刊物收货需求分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RepoPublicationReceiptDemandPageReqVO extends PageParam {

    @Schema(description = "供应商编号，传入后只返回该供应商可供 SKU", example = "1")
    private Long supplierId;

    @Schema(description = "仓库编号", example = "100")
    private Long warehouseId;

    @Schema(description = "订刊窗口编号", example = "1")
    private Long windowId;

    @Schema(description = "订刊窗口刊物编号", example = "10")
    private Long offerId;

    @Schema(description = "订刊窗口 SKU 编号", example = "100")
    private Long offerSkuId;

    @Schema(description = "商品 SKU 编号", example = "1000")
    private Long skuId;

    @Schema(description = "期号", example = "1")
    private Integer issueNo;

    @Schema(description = "关键字，匹配刊物名称、SKU 名称、ISBN 或期次名称", example = "读者")
    private String keyword;

}
