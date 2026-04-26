package cn.iocoder.yudao.module.edu.controller.admin.publicationtype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 刊物类型新增/更新 Request VO")
@Data
public class ProductPublicationTypeSaveReqVO {

    @Schema(description = "类型编号", example = "1")
    private Long id;

    @Schema(description = "类型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "刊物类型名称不能为空")
    private String name;

    @Schema(description = "标识规则", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标识规则不能为空")
    private String identifierRule;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
