package cn.iocoder.yudao.module.product.api.publication.dto;

import lombok.Data;

import java.util.List;

/**
 * 刊物商品查询请求 DTO
 */
@Data
public class ProductPublicationQueryReqDTO {

    /**
     * 刊物名称
     */
    private String productName;

    /**
     * 商品分类编号数组
     */
    private List<Long> categoryIds;

    /**
     * 出版社编号
     */
    private Long publisherId;

    /**
     * 刊物类型编号
     */
    private Long publicationTypeId;

    /**
     * 出刊周期
     */
    private String issueCycle;

    /**
     * 适用年级目录编号
     */
    private Long gradeCatalogId;

}
