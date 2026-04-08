package cn.iocoder.yudao.module.product.controller.admin.publicationspugrade.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProductPublicationSpuGradeSaveReqVO {

    @NotNull(message = "商品 SPU 不能为空")
    private Long productSpuId;

    @NotEmpty(message = "适用年级不能为空")
    private List<Long> gradeCatalogIds;
}
