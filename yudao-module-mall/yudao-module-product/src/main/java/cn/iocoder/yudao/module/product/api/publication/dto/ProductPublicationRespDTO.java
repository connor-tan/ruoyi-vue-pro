package cn.iocoder.yudao.module.product.api.publication.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductPublicationRespDTO {

    private Long id;

    private String name;

    private Long categoryId;

    private String categoryName;

    private String bizScene;

    private String picUrl;

    private Integer status;

    private Boolean specType;

    private Integer price;

    private Integer marketPrice;

    private Integer costPrice;

    private Integer stock;

    private PublicationSpuExtDTO publicationExt;

    private List<PublicationSkuDTO> skus;

    @Data
    public static class PublicationSpuExtDTO {

        private Long publisherId;

        private String publisherName;

        private Long publicationTypeId;

        private String publicationTypeName;

        private String publicationTypeIdentifierRule;

        private String issueCycle;

        private String issn;

        private String cnCode;

        private String postDistributionCode;

        private String fulfillmentMode;

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

        private Integer stock;

        private PublicationSkuExtDTO publicationExt;

        private List<Long> applicableGradeCatalogIds;

        private List<String> applicableGradeNames;

    }

    @Data
    public static class PublicationSkuExtDTO {

        private String targetPeriod;

        private String volumeLabel;

        private String editionLabel;

        private String isbn;

        private String remark;

    }

}
