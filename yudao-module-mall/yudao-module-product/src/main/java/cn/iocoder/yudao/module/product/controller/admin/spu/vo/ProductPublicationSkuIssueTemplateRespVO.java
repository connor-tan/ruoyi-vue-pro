package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 刊物 SKU 默认期次模板 Response VO")
@Data
public class ProductPublicationSkuIssueTemplateRespVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "商品 SKU 编号", example = "2048")
    private Long skuId;

    @Schema(description = "期号", example = "1")
    private Integer issueNo;

    @Schema(description = "期次名称", example = "第1期")
    private String issueName;

    @Schema(description = "计划出刊日期相对订刊窗口开始日期的偏移天数", example = "0")
    private Integer publishOffsetDays;

    @Schema(description = "计划配送日期相对订刊窗口开始日期的偏移天数", example = "7")
    private Integer deliveryOffsetDays;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "首期")
    private String remark;

}
