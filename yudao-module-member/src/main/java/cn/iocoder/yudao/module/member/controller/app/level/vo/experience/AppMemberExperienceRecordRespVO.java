package cn.iocoder.yudao.module.member.controller.app.level.vo.experience;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;
import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "用户 App - 会员经验记录 Response VO")
@Data
public class AppMemberExperienceRecordRespVO {

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "增加经验")
    private String title;

    @Schema(description = "经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer experience;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "下单增加 100 经验")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

}
