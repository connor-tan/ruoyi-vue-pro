package cn.iocoder.yudao.module.subscription.controller.admin.support.vo;

import lombok.Data;

@Data
public class SubscriptionSupportProductSpuSimpleRespVO {

    private Long id;

    private String name;

    private Long categoryId;

    private String categoryName;

    private Boolean supportsGift;

    private String picUrl;

    private Integer price;
}
