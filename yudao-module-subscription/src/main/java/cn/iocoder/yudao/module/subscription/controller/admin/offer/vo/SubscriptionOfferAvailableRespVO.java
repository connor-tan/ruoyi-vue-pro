package cn.iocoder.yudao.module.subscription.controller.admin.offer.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class SubscriptionOfferAvailableRespVO {

    private Long productSpuId;

    private String productName;

    private List<Long> categoryIds;

    private List<String> categoryNames;

    private String picUrl;

    private Integer price;

    private Integer stock;

    private Long publisherId;

    private String publisherName;

    private Long publicationTypeId;

    private String publicationTypeName;

    private String issueCycle;

    private Boolean added;

    private Integer matchedSkuCount;

    private Integer totalSkuCount;

    private List<Long> matchedGradeCatalogIds;

    private List<String> matchedGradeNames;

    private String candidateStatus;

    private String disabledReason;

    @JsonIgnore
    private Integer productStatus;

    @JsonIgnore
    private Integer enabledSkuCount;

    @JsonIgnore
    private String matchedGradeCatalogIdText;

    @JsonIgnore
    private String categoryIdText;

    @JsonIgnore
    private String categoryNameText;

}
