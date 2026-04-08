package cn.iocoder.yudao.module.product.controller.admin.publicationproduct.vo;

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuSaveReqVO;
import lombok.Data;

import java.util.List;

@Data
public class ProductPublicationProductSkuRespVO {

    private Long skuId;

    private String name;

    private Integer price;

    private Integer marketPrice;

    private Integer costPrice;

    private String barCode;

    private String picUrl;

    private Integer stock;

    private Double weight;

    private Double volume;

    private Integer firstBrokeragePrice;

    private Integer secondBrokeragePrice;

    private List<ProductSkuSaveReqVO.Property> properties;

    private String volumeLabel;

    private String editionLabel;

    private String isbn;

    private String remark;
}
