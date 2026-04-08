package cn.iocoder.yudao.module.iot.controller.admin.alert.vo.recrod;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - IoT 告警记录 Response VO")
@Data
public class IotAlertRecordRespVO {

    @Schema(description = "记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19904")
    private Long id;

    @Schema(description = "告警配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29320")
    private Long configId;

    @Schema(description = "告警名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String configName;

    @Schema(description = "告警级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer configLevel;

    @Schema(description = "产品编号", example = "2050")
    private Long productId;

    @Schema(description = "设备编号", example = "21727")
    private Long deviceId;

    @Schema(description = "触发的设备消息")
    private IotDeviceMessage deviceMessage;

    @Schema(description = "是否处理", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Boolean processStatus;

    @Schema(description = "处理结果（备注）", example = "你说的对")
    private String processRemark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

}
