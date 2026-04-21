package cn.iocoder.yudao.module.edu.controller.admin.station.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 站点 Response VO")
@Data
public class StationRespVO {

    @Schema(description = "站点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "梁溪站")
    private String stationName;

    @Schema(description = "区域编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "320205")
    private Long areaId;

    @Schema(description = "区域名称", example = "江苏省 无锡市 梁溪区")
    private String areaName;

    @Schema(description = "联系人", example = "张三")
    private String contactName;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactMobile;

    @Schema(description = "站点地址", example = "无锡市梁溪区站前路 1 号")
    private String stationAddress;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "负责梁溪区小学配送")
    private String remark;

    @Schema(description = "已绑定学校数量", example = "5")
    private Long schoolCount;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;
}
