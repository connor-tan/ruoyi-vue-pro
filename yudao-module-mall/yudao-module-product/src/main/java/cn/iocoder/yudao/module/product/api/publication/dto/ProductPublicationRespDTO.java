package cn.iocoder.yudao.module.product.api.publication.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductPublicationRespDTO {

    private Long id;

    private String name;

    private String bizScene;

    private List<Long> categoryIds;

    private List<Category> categories;

    private String picUrl;

    private Integer status;

    private Boolean specType;

    private Integer price;

    private Integer marketPrice;

    private Integer costPrice;

    private PublicationSpuExtDTO publicationExt;

    private List<PublicationSkuDTO> skus;

    @Data
    public static class Category {

        private Long id;

        private String name;

        private String bizScene;

    }

    @Data
    public static class PublicationSpuExtDTO {

        private Long publisherId;

        private String publisherName;

        private Long publicationTypeId;

        private String publicationTypeName;

        private String publicationTypeIdentifierRule;

        private String issueMode;

        private String issueCycle;

        private String issn;

        private String cnCode;

        private String postDistributionCode;

    }

    @Data
    public static class PublicationSkuDTO {

        private Long id;

        private String name;

        private Integer status;

        private Integer price;

        private Integer marketPrice;

        private Integer costPrice;

        private String barCode;

        private String picUrl;

        private PublicationSkuExtDTO publicationExt;

        private Integer issueTemplateCount;

        private List<PublicationSkuIssueTemplateDTO> issueTemplates;

        private List<Long> applicableGradeCatalogIds;

        private List<String> applicableGradeNames;

    }

    @Data
    public static class PublicationSkuExtDTO {

        private String isbn;

        private String remark;

    }

    @Data
    public static class PublicationSkuIssueTemplateDTO {

        private Long id;

        private Long skuId;

        private Integer issueNo;

        private String issueName;

        private Integer publishOffsetDays;

        private Integer deliveryOffsetDays;

        private Integer sort;

        private Integer status;

        private String remark;

    }

}
