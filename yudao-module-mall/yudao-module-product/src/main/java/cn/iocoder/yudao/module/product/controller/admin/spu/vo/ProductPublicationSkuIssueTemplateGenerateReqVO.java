package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 刊物 SKU 默认期次模板批量生成 Request VO")
@Data
public class ProductPublicationSkuIssueTemplateGenerateReqVO {

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "商品 SKU 不能为空")
    private Long skuId;

    @Schema(description = "起始期号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "起始期号不能为空")
    @Min(value = 1, message = "起始期号必须大于等于 1")
    private Integer startIssueNo;

    @Schema(description = "生成期数", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    @NotNull(message = "生成期数不能为空")
    @Min(value = 1, message = "生成期数必须大于等于 1")
    private Integer issueCount;

    @Schema(description = "期次名称前缀", example = "第")
    private String issueNamePrefix;

    @Schema(description = "首期出刊偏移天数", example = "0")
    @Min(value = 0, message = "首期出刊偏移天数必须大于等于 0")
    private Integer firstPublishOffsetDays;

    @Schema(description = "出刊间隔天数", example = "30")
    @Min(value = 1, message = "出刊间隔天数必须大于等于 1")
    private Integer publishIntervalDays;

    @Schema(description = "首期配送偏移天数", example = "7")
    @Min(value = 0, message = "首期配送偏移天数必须大于等于 0")
    private Integer firstDeliveryOffsetDays;

    @Schema(description = "配送间隔天数", example = "30")
    @Min(value = 1, message = "配送间隔天数必须大于等于 1")
    private Integer deliveryIntervalDays;

}
