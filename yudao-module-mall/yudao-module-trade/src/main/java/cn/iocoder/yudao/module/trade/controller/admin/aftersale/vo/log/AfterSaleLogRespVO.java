package cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 交易售后日志 Response VO")
@Data
public class AfterSaleLogRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20669")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22634")
    private Long userId;

    @Schema(description = "用户类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer userType;

    @Schema(description = "售后编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3023")
    private Long afterSaleId;

    @Schema(description = "售后状态（之前）", example = "2")
    private Integer beforeStatus;

    @Schema(description = "售后状态（之后）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer afterStatus;

    @Schema(description = "操作明细", requiredMode = Schema.RequiredMode.REQUIRED, example = "维权完成，退款金额：¥37776.00")
    private String content;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

}
