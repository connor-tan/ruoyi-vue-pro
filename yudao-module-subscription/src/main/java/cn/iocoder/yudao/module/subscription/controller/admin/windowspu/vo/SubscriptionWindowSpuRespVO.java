package cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Data
public class SubscriptionWindowSpuRespVO {

    private Long id;

    private Long windowId;

    private Long productSpuId;

    private String productName;

    private Long categoryId;

    private String categoryName;

    private String picUrl;

    private Integer price;

    private Long publicationTitleId;

    private String publicationTitleName;

    private Long publicationTypeId;

    private String publicationTypeName;

    private Long publisherId;

    private String publisherName;

    private Boolean recommendFlag;

    private Integer sort;

    private String remark;

    private List<Long> gradeCatalogIds;

    private String gradeNames;

    private Integer enabledSkuCount;

    private Integer totalSkuCount;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;
}
