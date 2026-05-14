package cn.iocoder.yudao.module.repo.controller.admin.warehouse.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 仓库分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RepoWarehousePageReqVO extends PageParam {

    @Schema(description = "仓库名称", example = "滨湖履约仓")
    private String name;

    @Schema(description = "开启状态", example = "0")
    private Integer status;

}
