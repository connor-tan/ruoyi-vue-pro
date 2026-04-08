package cn.iocoder.yudao.module.product.controller.admin.publicationproduct.vo;

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuPageReqVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPublicationProductPageReqVO extends ProductSpuPageReqVO {

    private Long publicationTitleId;

    private Long publicationTypeId;

    private Long publisherId;

    private Long gradeCatalogId;
}
