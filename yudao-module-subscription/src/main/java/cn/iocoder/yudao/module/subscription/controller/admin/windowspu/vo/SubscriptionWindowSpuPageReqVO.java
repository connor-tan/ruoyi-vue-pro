package cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 窗口刊物分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SubscriptionWindowSpuPageReqVO extends PageParam {

    @Schema(description = "窗口编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long windowId;

    @Schema(description = "刊物名称")
    private String productName;

    @Schema(description = "商品分类编号")
    private Long categoryId;

    @Schema(description = "刊物类型编号")
    private Long publicationTypeId;

    @Schema(description = "出版社编号")
    private Long publisherId;

    @Schema(description = "推荐标记")
    private Boolean recommendFlag;
}
