package cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 按年级批量新增窗口刊物 Request VO")
@Data
public class SubscriptionWindowSpuBatchCreateReqVO {

    @NotNull(message = "窗口不能为空")
    private Long windowId;

    @NotEmpty(message = "基础可见年级不能为空")
    private List<@NotNull(message = "基础可见年级不能为空") Long> baseGradeCatalogIds;

    @NotEmpty(message = "刊物商品不能为空")
    private List<@NotNull(message = "刊物商品不能为空") Long> productSpuIds;
}
