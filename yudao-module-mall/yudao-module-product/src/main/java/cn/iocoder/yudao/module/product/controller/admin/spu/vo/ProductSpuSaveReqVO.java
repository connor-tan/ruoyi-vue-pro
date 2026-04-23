package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 商品 SPU 新增/更新 Request VO")
@Data
public class ProductSpuSaveReqVO {

    @Schema(description = "商品编号", example = "1")
    private Long id;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉小短袖")
    @NotEmpty(message = "商品名称不能为空")
    private String name;

    @Schema(description = "关键字", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉丝滑不出汗")
    @NotEmpty(message = "商品关键字不能为空")
    private String keyword;

    @Schema(description = "商品简介", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉小短袖简介")
    @NotEmpty(message = "商品简介不能为空")
    private String introduction;

    @Schema(description = "商品详情", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉小短袖详情")
    @NotEmpty(message = "商品详情不能为空")
    private String description;

    @Schema(description = "商品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    @Schema(description = "商品品牌编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long brandId;

    @Schema(description = "商品封面图", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xx.png")
    @NotEmpty(message = "商品封面图不能为空")
    private String picUrl;

    @Schema(description = "商品轮播图", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "[https://www.iocoder.cn/xx.png, https://www.iocoder.cn/xxx.png]")
    private List<String> sliderPicUrls;

    @Schema(description = "排序字段", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商品排序字段不能为空")
    private Integer sort;

    // ========== SKU 相关字段 =========

    @Schema(description = "规格类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "商品规格类型不能为空")
    private Boolean specType;

    // ========== 物流相关字段 =========

    @Schema(description = "配送方式数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<Integer> deliveryTypes;

    @Schema(description = "物流配置模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "111")
    private Long deliveryTemplateId;

    // ========== 营销相关字段 =========

    @Schema(description = "赠送积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "111")
    private Integer giveIntegral;

    @Schema(description = "分销类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean subCommissionType;

    // ========== 统计相关字段 =========

    @Schema(description = "虚拟销量", example = "66")
    private Integer virtualSalesCount;

    @Schema(description = "商品销量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1999")
    private Integer salesCount;

    @Schema(description = "浏览量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1999")
    private Integer browseCount;

    // ========== SKU 相关字段 =========

    @Schema(description = "SKU 数组")
    @Valid
    private List<ProductSkuSaveReqVO> skus;

    @Schema(description = "刊物扩展")
    @Valid
    private PublicationSpuExtSaveReqVO publicationExt;

    @Schema(description = "业务场景，仅用于回显", example = "PUBLICATION")
    private String bizScene;

    @Schema(description = "刊物扩展")
    @Data
    public static class PublicationSpuExtSaveReqVO {

        @Schema(description = "出版社编号", example = "1")
        private Long publisherId;

        @Schema(description = "刊物类型编号", example = "1")
        private Long publicationTypeId;

        @Schema(description = "出刊周期", example = "MONTHLY")
        private String issueCycle;

        @Schema(description = "ISSN", example = "1674-1234")
        private String issn;

        @Schema(description = "CN 刊号", example = "CN11-1234/G4")
        private String cnCode;

        @Schema(description = "邮发代号", example = "80-123")
        private String postDistributionCode;

        @Schema(description = "履约方式", example = "SCHOOL_STATION")
        private String fulfillmentMode;

    }

}
