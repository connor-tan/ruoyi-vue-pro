package cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;
import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "用户 App - 下级分销统计 Response VO")
@Data
public class AppBrokerageUserChildSummaryRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long id;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "小王")
    private String nickname;

    @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xxx.jpg")
    private String avatar;

    @Schema(description = "佣金金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer brokeragePrice;

    @Schema(description = "分销订单数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer brokerageOrderCount;

    @Schema(description = "分销用户数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer brokerageUserCount;

    @Schema(description = "绑定推广员的时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime brokerageTime;

}
