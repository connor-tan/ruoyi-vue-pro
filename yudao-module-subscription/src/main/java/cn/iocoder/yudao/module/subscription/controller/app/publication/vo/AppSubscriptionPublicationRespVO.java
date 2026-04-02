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

    private Long windowId;

    private Integer targetSemester;

    private Long effectiveGradeCatalogId;

    private String effectiveGradeNo;

    private String effectiveGradeName;

    private String effectiveGradeAliasName;

    private AppSubscriptionPublicationProfileRespVO publicationProfile;

    private List<AppSubscriptionPublicationAttrRespVO> attrs;
}
