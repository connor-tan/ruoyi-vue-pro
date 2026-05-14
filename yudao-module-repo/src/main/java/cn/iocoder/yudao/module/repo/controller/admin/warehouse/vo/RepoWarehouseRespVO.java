package cn.iocoder.yudao.module.repo.controller.admin.warehouse.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 仓库 Response VO")
@Data
@ExcelIgnoreUnannotated
public class RepoWarehouseRespVO {

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("仓库编号")
    private Long id;

    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "滨湖履约仓")
    @ExcelProperty("仓库名称")
    private String name;

    @Schema(description = "仓库地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("仓库地址")
    private String address;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("排序")
    private Long sort;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "负责人")
    @ExcelProperty("负责人")
    private String principal;

    @Schema(description = "仓储费")
    @ExcelProperty("仓储费")
    private BigDecimal warehousePrice;

    @Schema(description = "搬运费")
    @ExcelProperty("搬运费")
    private BigDecimal truckagePrice;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("开启状态")
    private Integer status;

    @Schema(description = "是否默认")
    @ExcelProperty("是否默认")
    private Boolean defaultStatus;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

}
