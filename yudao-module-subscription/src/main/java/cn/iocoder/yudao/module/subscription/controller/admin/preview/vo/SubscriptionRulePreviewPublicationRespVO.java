package cn.iocoder.yudao.module.subscription.controller.admin.preview.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 订刊规则预览刊物 Response VO")
@Data
public class SubscriptionRulePreviewPublicationRespVO {

    private Long productSpuId;

    private String productName;

    private Long categoryId;

    private String categoryName;

    private Long typeCategoryId;

    private String typeCategoryName;

    private Boolean supportsGift;

    private String picUrl;

    private Integer price;

    private Boolean recommendFlag;

    private Integer maxQuantityPerStudent;
}
