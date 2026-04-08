package cn.iocoder.yudao.module.product.controller.admin.publicationspurelation.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductPublicationSpuRelationSaveReqVO {

    @NotNull(message = "商品 SPU 不能为空")
    private Long productSpuId;

    @NotNull(message = "刊物主档不能为空")
    private Long publicationTitleId;

    private String remark;
}
