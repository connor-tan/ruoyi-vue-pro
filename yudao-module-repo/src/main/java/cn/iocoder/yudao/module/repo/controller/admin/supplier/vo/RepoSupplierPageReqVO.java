package cn.iocoder.yudao.module.repo.controller.admin.supplier.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 仓库供应商分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RepoSupplierPageReqVO extends PageParam {

    @Schema(description = "供应商名称", example = "新华印务")
    private String name;

    @Schema(description = "供应商编码", example = "XH")
    private String code;

    @Schema(description = "开启状态", example = "0")
    private Integer status;

}
