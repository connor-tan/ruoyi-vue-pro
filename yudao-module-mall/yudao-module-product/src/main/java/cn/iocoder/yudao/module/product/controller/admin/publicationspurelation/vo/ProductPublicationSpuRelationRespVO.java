package cn.iocoder.yudao.module.product.controller.admin.publicationspurelation.vo;

import lombok.Data;

@Data
public class ProductPublicationSpuRelationRespVO {

    private Long productSpuId;

    private Long publicationTitleId;

    private String publicationTitleName;

    private String remark;
}
