package cn.iocoder.yudao.module.product.controller.admin.publicationproduct.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Data
public class ProductPublicationProductRespVO {

    private Long id;

    private String domainType;

    private String name;

    private String keyword;

    private String introduction;

    private String description;

    private Long categoryId;

    private Long brandId;

    private String picUrl;

    private List<String> sliderPicUrls;

    private Integer sort;

    private Integer status;

    private Boolean specType;

    private Integer price;

    private Integer marketPrice;

    private Integer costPrice;

    private Integer stock;

    private List<Integer> deliveryTypes;

    private Long deliveryTemplateId;

    private Integer giveIntegral;

    private Boolean subCommissionType;

    private Integer salesCount;

    private Integer virtualSalesCount;

    private Integer browseCount;

    private Long publicationTitleId;

    private String publicationTitleName;

    private Long publicationTypeId;

    private String publicationTypeCode;

    private String publicationTypeName;

    private Long publisherId;

    private String publisherName;

    private String issueCycle;

    private String issn;

    private String cnCode;

    private String postDistributionCode;

    private List<Long> applicableGradeCatalogIds;

    private List<String> applicableGradeNames;

    private List<ProductPublicationProductSkuRespVO> skus;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;
}
