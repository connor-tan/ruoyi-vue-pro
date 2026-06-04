package cn.iocoder.yudao.module.repo.controller.admin.supplier.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 仓库供应商新增/修改 Request VO")
@Data
public class RepoSupplierSaveReqVO {

    @Schema(description = "供应商编号", example = "1")
    private Long id;

    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "新华印务")
    @NotBlank(message = "供应商名称不能为空")
    private String name;

    @Schema(description = "供应商编码", example = "XH")
    private String code;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactMobile;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "排序不能为空")
    private Long sort;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "开启状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
