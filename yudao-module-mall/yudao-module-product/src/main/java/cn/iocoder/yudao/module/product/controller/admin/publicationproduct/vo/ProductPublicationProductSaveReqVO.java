package cn.iocoder.yudao.module.product.controller.admin.publicationproduct.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 刊物商品新增/更新 Request VO")
@Data
public class ProductPublicationProductSaveReqVO {

    private Long id;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    @NotBlank(message = "商品关键字不能为空")
    private String keyword;

    @NotBlank(message = "商品简介不能为空")
    private String introduction;

    @NotBlank(message = "商品详情不能为空")
    private String description;

    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    private Long brandId;

    @NotBlank(message = "商品封面图不能为空")
    private String picUrl;

    private List<String> sliderPicUrls;

    @NotNull(message = "商品排序不能为空")
    private Integer sort;

    @NotNull(message = "商品规格类型不能为空")
    private Boolean specType;

    @NotEmpty(message = "配送方式不能为空")
    private List<Integer> deliveryTypes;

    private Long deliveryTemplateId;

    @NotNull(message = "赠送积分不能为空")
    private Integer giveIntegral;

    @NotNull(message = "分销类型不能为空")
    private Boolean subCommissionType;

    private Integer virtualSalesCount;

    private Integer salesCount;

    private Integer browseCount;

    @NotNull(message = "刊物主档不能为空")
    private Long publicationTitleId;

    @NotEmpty(message = "适用年级不能为空")
    private List<Long> applicableGradeCatalogIds;

    @Valid
    @NotEmpty(message = "SKU 数组不能为空")
    private List<ProductPublicationProductSkuSaveReqVO> skus;
}
