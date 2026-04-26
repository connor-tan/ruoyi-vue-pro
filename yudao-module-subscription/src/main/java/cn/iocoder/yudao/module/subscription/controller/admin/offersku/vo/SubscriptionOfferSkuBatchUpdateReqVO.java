package cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubscriptionOfferSkuBatchUpdateReqVO {

    @NotNull(message = "窗口刊物编号不能为空")
    private Long offerId;

    @Valid
    @NotEmpty(message = "SKU 列表不能为空")
    private List<SubscriptionOfferSkuSaveReqVO> skus;

}
