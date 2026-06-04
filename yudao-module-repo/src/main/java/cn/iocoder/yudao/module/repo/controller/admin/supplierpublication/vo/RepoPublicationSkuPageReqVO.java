package cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 刊物 SKU 选择分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RepoPublicationSkuPageReqVO extends PageParam {

    @Schema(description = "商品 SPU 编号", example = "100")
    private Long spuId;

    @Schema(description = "商品 SKU 编号", example = "1000")
    private Long skuId;

    @Schema(description = "关键字，匹配刊物名称、SKU 名称或 ISBN", example = "读者")
    private String keyword;

}
