package cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubscriptionOfferSkuRespVO {

    private Long id;
    private Long offerId;
    private Long productSkuId;
    private String productSkuName;
    private Integer price;
    private Integer stock;
    private String issueMode;
    private Integer issueCount;
    private Integer issueTemplateCount;
    private String isbn;
    private List<Long> applicableGradeCatalogIds;
    private List<String> applicableGradeNames;
    private Integer sort;
    private Integer status;
    private Integer maxQuantityPerStudent;
    private String remark;
    private LocalDateTime createTime;

}
