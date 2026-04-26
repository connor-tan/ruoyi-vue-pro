package cn.iocoder.yudao.module.subscription.controller.admin.offer.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubscriptionOfferBatchCreateReqVO {

    @NotNull(message = "窗口编号不能为空")
    private Long windowId;

    @NotEmpty(message = "商品 SPU 编号不能为空")
    private List<Long> productSpuIds;

}
