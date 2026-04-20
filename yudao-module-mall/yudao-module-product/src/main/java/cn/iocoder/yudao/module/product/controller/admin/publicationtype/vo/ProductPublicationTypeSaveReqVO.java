package cn.iocoder.yudao.module.product.controller.admin.publicationtype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.product.enums.publication.ProductPublicationTypeIdentifierRuleEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 刊物类型新增/更新 Request VO")
@Data
public class ProductPublicationTypeSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "BOOK")
    @NotBlank(message = "刊物类型编码不能为空")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "书本")
    @NotBlank(message = "刊物类型名称不能为空")
    private String name;

    @Schema(description = "标识规则", example = "NONE")
    @InEnum(value = ProductPublicationTypeIdentifierRuleEnum.class, message = "标识规则必须是 {value}")
    private String identifierRule;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "刊物类型排序不能为空")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "刊物类型状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
