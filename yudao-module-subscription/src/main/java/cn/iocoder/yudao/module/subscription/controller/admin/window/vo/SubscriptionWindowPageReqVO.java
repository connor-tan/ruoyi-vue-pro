package cn.iocoder.yudao.module.subscription.controller.admin.window.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 订刊窗口分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SubscriptionWindowPageReqVO extends PageParam {

    private String name;

    private Long targetYearCatalogId;

    private Integer status;

}
