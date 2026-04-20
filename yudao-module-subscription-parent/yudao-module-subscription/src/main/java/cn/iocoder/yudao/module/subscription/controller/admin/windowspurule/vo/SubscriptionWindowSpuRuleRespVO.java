package cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Data
public class SubscriptionWindowSpuRuleRespVO {

    private Long id;

    private Long windowSpuId;

    private String effectType;

    private String scopeType;

    private Long schoolId;

    private String schoolName;

    private Long gradeCatalogId;

    private String gradeName;

    private String gradeAliasName;

    private Integer sort;

    private String remark;

    private Boolean gradeApplicabilityOverride;

    private String warningReason;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;
}
