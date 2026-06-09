package cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 仓库刊物发货批次 Response VO")
@Data
public class RepoPublicationDeliveryBatchRespVO {

    private Long id;

    private String batchNo;

    private Integer deliveryType;

    private Long schoolId;

    private String schoolNameSnapshot;

    private Long stationId;

    private String stationNameSnapshot;

    private Long warehouseId;

    private String warehouseNameSnapshot;

    private Long windowId;

    private String windowNameSnapshot;

    private Long offerId;

    private Long offerSkuId;

    private Long skuId;

    private String productNameSnapshot;

    private Long issueId;

    private Integer issueNo;

    private String issueName;

    private Integer totalCount;

    private Integer orderCount;

    private Integer studentCount;

    private Integer status;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime deliveryTime;

    private Long operatorUserId;

    private String remark;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

    private List<Item> items;

    @Data
    public static class Item {

        private Long id;

        private Long orderId;

        private String orderNo;

        private Long orderItemId;

        private Long orderIssueId;

        private Long deliveryId;

        private Long userId;

        private Integer count;

        private Integer issueNo;

        private String issueName;

        private Long logisticsId;

        private String logisticsNo;

        private Long studentId;

        private String studentNameSnapshot;

        private Long classId;

        private String classNameSnapshot;

    }

}
