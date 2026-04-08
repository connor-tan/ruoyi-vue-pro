package cn.iocoder.yudao.module.subscription.controller.admin.windowsku.vo;

import lombok.Data;

@Data
public class SubscriptionWindowSkuRespVO {

    private Long id;

    private Long windowSpuId;

    private Long productSkuId;

    private Integer status;

    private Integer sort;

    private Integer maxQuantityPerStudent;

    private String remark;

    private Integer price;

    private Integer stock;

    private String volumeLabel;

    private String editionLabel;

    private String isbn;
}
