package cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo;

import lombok.Data;

@Data
public class SubscriptionWindowSpuAvailableRespVO {

    private Long productSpuId;

    private String productName;

    private Long categoryId;

    private String categoryName;

    private String picUrl;

    private Integer price;

    private Long publicationTitleId;

    private String publicationTitleName;

    private Long publicationTypeId;

    private String publicationTypeName;

    private Long publisherId;

    private String publisherName;

    private String applicableGradeNames;
}
