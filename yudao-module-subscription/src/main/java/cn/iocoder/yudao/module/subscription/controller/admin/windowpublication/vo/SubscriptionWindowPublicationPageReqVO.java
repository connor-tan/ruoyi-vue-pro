package cn.iocoder.yudao.module.subscription.controller.admin.windowpublication.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 窗口刊物分页 Request VO")
@Data
public class SubscriptionWindowPublicationPageReqVO extends PageParam {

    @Schema(description = "窗口ID")
    private Long windowId;

    @Schema(description = "刊物名称")
    private String productName;

    @Schema(description = "状态")
    private Integer status;
}
