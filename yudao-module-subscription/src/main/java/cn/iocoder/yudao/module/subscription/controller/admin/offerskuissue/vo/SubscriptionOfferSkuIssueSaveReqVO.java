package cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Data
public class SubscriptionOfferSkuIssueSaveReqVO {

    private Long id;

    @NotNull(message = "窗口 SKU 不能为空")
    private Long offerSkuId;

    @NotNull(message = "期号不能为空")
    @Min(value = 1, message = "期号必须大于等于 1")
    private Integer issueNo;

    @NotBlank(message = "期次名称不能为空")
    private String issueName;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate plannedPublishDate;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate plannedDeliveryDate;

    private Integer sort;

    private Integer status;

    private String remark;

}
