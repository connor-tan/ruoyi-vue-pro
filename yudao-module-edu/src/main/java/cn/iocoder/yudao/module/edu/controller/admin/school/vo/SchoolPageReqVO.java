package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import lombok.Data;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 学校信息分页 Request VO")
@Data
public class SchoolPageReqVO extends PageParam {

    @Schema(description = "学校名称", example = "李四")
    private String schoolName;

    @Schema(description = "区域ID", example = "68")
    private Long areaId;

    @Schema(description = "学校地址")
    private String schoolAddress;

    @Schema(description = "归属站点编号", example = "1")
    private Long stationId;

    @Schema(description = "学校配送仓库编号", example = "1")
    private Long warehouseId;

    @Schema(description = "是否已绑定站点", example = "true")
    private Boolean stationBound;

    @Schema(description = "学校代码")
    private String code;

    @Schema(description = "办学学段编码", example = "primary")
    private String stageCode;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
