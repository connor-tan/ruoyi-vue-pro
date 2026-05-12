package cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Data
public class SubscriptionOfferSkuIssueGenerateReqVO {

    @NotNull(message = "窗口 SKU 不能为空")
    private Long offerSkuId;

    @NotNull(message = "起始期号不能为空")
    @Min(value = 1, message = "起始期号必须大于等于 1")
    private Integer startIssueNo;

    @NotNull(message = "生成期数不能为空")
    @Min(value = 1, message = "生成期数必须大于等于 1")
    private Integer issueCount;

    private String issueNamePrefix;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate firstPublishDate;

    @Min(value = 1, message = "出刊间隔天数必须大于等于 1")
    private Integer publishIntervalDays;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate firstDeliveryDate;

    @Min(value = 1, message = "配送间隔天数必须大于等于 1")
    private Integer deliveryIntervalDays;

}
