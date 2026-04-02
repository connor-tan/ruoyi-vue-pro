package cn.iocoder.yudao.module.subscription.controller.app.publication.vo;

import lombok.Data;

@Data
public class AppSubscriptionPublicationProfileRespVO {

    private Long typeCategoryId;

    private String typeCategoryName;

    private Boolean supportsGift;

    private Boolean recommendFlag;

    private Integer maxQuantityPerStudent;
}
