package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 快递查询的轨迹 Resp DTO
 *
 * @author jason
 */
@Schema(description = "用户 App - 快递查询的轨迹 Response VO")
@Data
public class AppOrderExpressTrackRespDTO {

    @Schema(description = "发生时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime time;

    @Schema(description = "快递状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "已签收")
    private String content;

}
