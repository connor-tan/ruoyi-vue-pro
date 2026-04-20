package cn.iocoder.yudao.module.subscription.controller.admin.windowsku.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 窗口刊物 SKU 批量更新 Request VO")
@Data
public class SubscriptionWindowSkuBatchUpdateReqVO {

    @NotNull(message = "窗口刊物不能为空")
    private Long windowSpuId;

    @Valid
    @NotEmpty(message = "SKU 配置不能为空")
    private List<Item> items;

    @Data
    public static class Item {

        @NotNull(message = "窗口 SKU 配置不能为空")
        private Long id;

        @NotNull(message = "状态不能为空")
        private Integer status;

        @NotNull(message = "排序不能为空")
        @Min(value = 0, message = "排序不能小于 0")
        private Integer sort;

        @NotNull(message = "每生限购不能为空")
        @Min(value = 1, message = "每生限购必须大于等于 1")
        private Integer maxQuantityPerStudent;

        private String remark;
    }
}
