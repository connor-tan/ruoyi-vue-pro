package cn.iocoder.yudao.module.product.controller.admin.publicationproduct.vo;

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuSaveReqVO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProductPublicationProductSkuSaveReqVO {

    private Long skuId;

    private String name;

    @NotNull(message = "销售价格不能为空")
    private Integer price;

    private Integer marketPrice;

    private Integer costPrice;

    private String barCode;

    @NotNull(message = "SKU 图片不能为空")
    private String picUrl;

    @NotNull(message = "SKU 库存不能为空")
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
