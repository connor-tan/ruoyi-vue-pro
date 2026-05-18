package cn.iocoder.yudao.module.edu.controller.admin.station.vo;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 站点新增/修改 Request VO")
@Data
public class StationSaveReqVO {

    @Schema(description = "站点编号", example = "1")
    private Long id;

    @Schema(description = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "梁溪站")
    @NotEmpty(message = "站点名称不能为空")
    private String stationName;

    @Schema(description = "区域编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "320205")
    @NotNull(message = "区域编号不能为空")
    private Long areaId;

    @Schema(description = "联系人", example = "张三")
    private String contactName;

    @Schema(description = "联系电话", example = "13800138000")
    @Mobile
    private String contactMobile;

    @Schema(description = "站点地址", example = "无锡市梁溪区站前路 1 号")
    private String stationAddress;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "排序不能为空")
    @Min(value = 0, message = "排序必须大于等于 0")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "备注", example = "负责梁溪区小学配送")
    private String remark;
}
