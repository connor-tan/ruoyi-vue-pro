package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - 在线订刊下单家长地址 Response VO")
@Data
@Accessors(chain = true)
public class TradeOrderAdminOnlineAddressRespVO {

    @Schema(description = "地址编号", example = "1")
    private Long id;

    @Schema(description = "收件人名称", example = "张三")
    private String name;

    @Schema(description = "手机号", example = "13800000000")
    private String mobile;

    @Schema(description = "地区编号", example = "110101")
    private Integer areaId;

    @Schema(description = "地区名称", example = "北京市 北京市 东城区")
    private String areaName;

    @Schema(description = "详细地址", example = "示例路 1 号")
    private String detailAddress;

    @Schema(description = "是否默认地址", example = "true")
    private Boolean defaultStatus;

}
