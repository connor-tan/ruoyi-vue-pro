package cn.iocoder.yudao.module.subscription.controller.app.publication.vo;

import lombok.Data;

import java.util.List;

@Data
public class AppSubscriptionPublicationRespVO {

    private Long productSpuId;

    private String productName;

    private Long categoryId;

    private String categoryName;

    private String picUrl;

    private Integer price;

    private String keyword;

    private String introduction;

    private String description;

    private String publicationTitleName;

    private Boolean recommendFlag;

    private List<Sku> skus;

    @Data
    public static class Sku {

        private Long windowSkuId;

        private Long productSkuId;

        private String volumeLabel;

        private String editionLabel;

        private String isbn;

        private Integer price;

        private Integer maxQuantityPerStudent;
    }
}
