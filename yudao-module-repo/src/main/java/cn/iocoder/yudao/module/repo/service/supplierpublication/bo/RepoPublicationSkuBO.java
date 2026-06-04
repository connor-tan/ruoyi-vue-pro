package cn.iocoder.yudao.module.repo.service.supplierpublication.bo;

import lombok.Data;

/**
 * 商品中心刊物 SKU 只读信息。
 */
@Data
public class RepoPublicationSkuBO {

    private Long spuId;

    private Long skuId;

    private String productName;

    private String productSkuName;

    private String isbn;

}
