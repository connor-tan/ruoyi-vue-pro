package cn.iocoder.yudao.module.repo.controller.admin.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 仓库新增/修改 Request VO")
@Data
public class RepoWarehouseSaveReqVO {

    @Schema(description = "仓库编号", example = "1")
    private Long id;

    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "滨湖履约仓")
    @NotBlank(message = "仓库名称不能为空")
    private String name;

    @Schema(description = "仓库地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "仓库地址不能为空")
    private String address;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "排序不能为空")
    private Long sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "负责人")
    private String principal;

    @Schema(description = "仓储费")
    private BigDecimal warehousePrice;

    @Schema(description = "搬运费")
    private BigDecimal truckagePrice;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "开启状态不能为空")
    private Integer status;

    @Schema(description = "是否默认")
    private Boolean defaultStatus;

}
