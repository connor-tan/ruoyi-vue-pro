package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 刊物 SKU 默认期次模板创建/更新 Request VO")
@Data
public class ProductPublicationSkuIssueTemplateSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "商品 SKU 不能为空")
    private Long skuId;

    @Schema(description = "期号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "期号不能为空")
    @Min(value = 1, message = "期号必须大于等于 1")
    private Integer issueNo;

    @Schema(description = "期次名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "第1期")
    @NotBlank(message = "期次名称不能为空")
    private String issueName;

    @Schema(description = "计划出刊日期相对订刊窗口开始日期的偏移天数", example = "0")
    @Min(value = 0, message = "出刊偏移天数必须大于等于 0")
    private Integer publishOffsetDays;

    @Schema(description = "计划配送日期相对订刊窗口开始日期的偏移天数", example = "7")
    @Min(value = 0, message = "配送偏移天数必须大于等于 0")
    private Integer deliveryOffsetDays;

    @Schema(description = "排序", example = "1")
    @Min(value = 0, message = "排序必须大于等于 0")
    private Integer sort;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "首期")
    private String remark;

}
