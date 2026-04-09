package cn.iocoder.yudao.module.promotion.controller.app.reward.vo;

import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;

@Schema(description = "用户 App - 满减送活动 Response VO")
@Data
public class AppRewardActivityRespVO {

    @Schema(description = "活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer id;

    @Schema(description = "活动状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "活动标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "满啦满啦")
    private String name;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime endTime;

    @Schema(description = "条件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer conditionType;

    @Schema(description = "商品范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer productScope;

    @Schema(description = "商品 SPU 编号的数组", example = "1,2,3")
    private List<Long> productScopeValues;

    @Schema(description = "优惠规则的数组")
    private List<Rule> rules;

    @Schema(description = "优惠规则")
    @Data
    public static class Rule extends RewardActivityBaseVO.Rule {

        @Schema(description = "规则描述")
        private String description; // 通过 {@link #limit}、{@link #discountPrice} 等字段进行拼接

    }

}
