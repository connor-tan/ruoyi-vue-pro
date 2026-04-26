package cn.iocoder.yudao.module.subscription.controller.admin.offer.vo;

import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubscriptionOfferRespVO {

    private Long id;
    private Long windowId;
    private Long productSpuId;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private String picUrl;
    private Integer price;
    private Long publisherId;
    private String publisherName;
    private Long publicationTypeId;
    private String publicationTypeName;
    private Boolean recommendFlag;
    private Integer sort;
    private Integer status;
    private String remark;
    private List<Long> gradeCatalogIds;
    private List<String> gradeNames;
    private Integer enabledSkuCount;
    private Integer totalSkuCount;
    private ProductPublicationRespDTO publication;
    private LocalDateTime createTime;

}
