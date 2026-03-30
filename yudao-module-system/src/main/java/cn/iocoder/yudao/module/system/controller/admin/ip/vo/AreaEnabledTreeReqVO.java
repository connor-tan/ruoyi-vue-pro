package cn.iocoder.yudao.module.system.controller.admin.ip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 启用地区树 Request VO")
@Data
public class AreaEnabledTreeReqVO {

    @Schema(description = "需要额外包含的地区编号列表", example = "[320505,320506]")
    private List<Integer> includeAreaIds;

}
