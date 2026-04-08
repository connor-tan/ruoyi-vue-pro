
package cn.iocoder.yudao.module.pay.controller.admin.notify.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 回调通知的明细 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayNotifyTaskDetailRespVO extends PayNotifyTaskRespVO {

    @Schema(description = "回调日志列表")
    private List<Log> logs;

    @Schema(description = "管理后台 - 回调日志")
    @Data
    public static class Log {

        @Schema(description = "日志编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8848")
        private Long id;

        @Schema(description = "通知状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Byte status;

        @Schema(description = "当前通知次数", requiredMode = Schema.RequiredMode.REQUIRED)
        private Byte notifyTimes;

        @Schema(description = "HTTP 响应结果", requiredMode = Schema.RequiredMode.REQUIRED)
        private String response;

        @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
        private LocalDateTime createTime;

    }

}
