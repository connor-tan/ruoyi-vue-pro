package cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 仓库刊物发货候选明细 Response VO")
@Data
public class RepoPublicationDeliveryCandidateItemRespVO {

    private Long orderIssueId;

    private Long orderId;

    private String orderNo;

    private Long orderItemId;

    private Long deliveryId;

    private Long userId;

    private Integer deliveryType;

    private Integer count;

    private Long schoolId;

    private String schoolNameSnapshot;

    private Long warehouseId;

    private String warehouseNameSnapshot;

    private Long windowId;

    private String windowNameSnapshot;

    private Long offerId;

    private Long offerSkuId;

    private Long skuId;

    private String productNameSnapshot;

    private Long studentId;

    private String studentNameSnapshot;

    private Long classId;

    private String classNameSnapshot;

    private Long issueId;

    private Integer issueNo;

    private String issueName;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY, timezone = TIME_ZONE_DEFAULT)
    private LocalDate plannedDeliveryDate;

    private Long logisticsId;

    private String logisticsNo;

}
