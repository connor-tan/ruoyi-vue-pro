package cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 刊物收货单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RepoPublicationReceiptPageReqVO extends PageParam {

    @Schema(description = "收货单号", example = "pr123")
    private String receiptNo;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "仓库编号", example = "100")
    private Long warehouseId;

    @Schema(description = "状态", example = "20")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
