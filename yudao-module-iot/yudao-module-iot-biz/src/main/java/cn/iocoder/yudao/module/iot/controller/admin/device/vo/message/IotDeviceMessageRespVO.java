package cn.iocoder.yudao.module.iot.controller.admin.device.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - IoT 设备消息 Response VO")
@Data
public class IotDeviceMessageRespVO {

    @Schema(description = "消息编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String id;

    @Schema(description = "上报时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime reportTime;

    @Schema(description = "记录时间戳", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime ts;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Long deviceId;

    @Schema(description = "服务编号", example = "server_123")
    private String serverId;

    @Schema(description = "是否上行消息", example = "true", examples = "false")
    private Boolean upstream;

    @Schema(description = "是否回复消息", example = "false", examples = "true")
    private Boolean reply;

    @Schema(description = "标识符", example = "temperature")
    private String identifier;

    // ========== codec（编解码）字段 ==========

    @Schema(description = "请求编号", example = "req_123")
    private String requestId;

    @Schema(description = "请求方法", requiredMode = Schema.RequiredMode.REQUIRED, example = "thing.property.post")
    private String method;

    @Schema(description = "请求参数")
    private Object params;

    @Schema(description = "响应结果")
    private Object data;

    @Schema(description = "响应错误码", example = "200")
    private Integer code;

    @Schema(description = "响应提示", example = "操作成功")
    private String msg;

}
