package cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - 仓库刊物发货候选分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RepoPublicationDeliveryCandidatePageReqVO extends PageParam {

    @Schema(description = "配送方式", example = "3")
    private Integer deliveryType;

    @Schema(description = "学校编号", example = "100")
    private Long schoolId;

    @Schema(description = "学校配送仓库编号", example = "200")
    private Long warehouseId;

    @Schema(description = "订刊窗口编号", example = "1")
    private Long windowId;

    @Schema(description = "订刊窗口刊物编号", example = "10")
    private Long offerId;

    @Schema(description = "订刊窗口 SKU 编号", example = "100")
    private Long offerSkuId;

    @Schema(description = "商品 SKU 编号", example = "1000")
    private Long skuId;

    @Schema(description = "订刊期次编号", example = "10000")
    private Long issueId;

    @Schema(description = "期号", example = "1")
    private Integer issueNo;

}
