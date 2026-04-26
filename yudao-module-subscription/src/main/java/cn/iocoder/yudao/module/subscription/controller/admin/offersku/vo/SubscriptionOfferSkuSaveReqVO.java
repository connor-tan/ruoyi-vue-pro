package cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionOfferSkuSaveReqVO {

    private Long id;

    @NotNull(message = "窗口刊物编号不能为空")
    private Long offerId;

    @NotNull(message = "商品 SKU 编号不能为空")
    private Long productSkuId;

    private Integer sort;

    private Integer status;

    private Integer maxQuantityPerStudent;

    private String remark;

}
