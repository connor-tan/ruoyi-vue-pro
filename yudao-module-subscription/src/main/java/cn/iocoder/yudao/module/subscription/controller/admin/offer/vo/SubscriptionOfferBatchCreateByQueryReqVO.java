package cn.iocoder.yudao.module.subscription.controller.admin.offer.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionOfferBatchCreateByQueryReqVO {

    @NotNull(message = "窗口编号不能为空")
    private Long windowId;

    @Valid
    @NotNull(message = "查询条件不能为空")
    private SubscriptionOfferAvailablePageReqVO query;

}
