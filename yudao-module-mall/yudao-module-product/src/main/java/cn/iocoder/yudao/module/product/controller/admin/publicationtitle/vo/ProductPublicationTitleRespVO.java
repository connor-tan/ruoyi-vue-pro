package cn.iocoder.yudao.module.product.controller.admin.publicationtitle.vo;

import lombok.Data;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Data
public class ProductPublicationTitleRespVO {

    private Long id;

    private String code;

    private String name;

    private Long typeId;

    private String typeName;

    private String typeCode;

    private String typeIdentifierRule;

    private Long publisherId;

    private String publisherName;

    private String issueCycle;

    private Integer status;

    private String issn;

    private String cnCode;

    private String postDistributionCode;

    private String remark;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;
}
