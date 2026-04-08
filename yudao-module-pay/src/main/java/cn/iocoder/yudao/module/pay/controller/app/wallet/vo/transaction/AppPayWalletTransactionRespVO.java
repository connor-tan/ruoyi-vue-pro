package cn.iocoder.yudao.module.pay.controller.app.wallet.vo.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;
import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "用户 APP - 钱包流水分页 Response VO")
@Data
public class AppPayWalletTransactionRespVO {

    @Schema(description = "业务分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer bizType;

    @Schema(description = "交易金额，单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long price;

    @Schema(description = "流水标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "土豆土豆")
    private String title;

    @Schema(description = "交易时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

}
