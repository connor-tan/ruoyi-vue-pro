package cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 窗口刊物更新 Request VO")
@Data
public class SubscriptionWindowSpuSaveReqVO {

    @NotNull(message = "窗口刊物不能为空")
    private Long id;

    @NotNull(message = "推荐配置不能为空")
    private Boolean recommendFlag;

    @NotNull(message = "排序不能为空")
    @Min(value = 0, message = "排序不能小于 0")
    private Integer sort;

    @NotEmpty(message = "基础可见年级不能为空")
    private List<Long> gradeCatalogIds;

    private String remark;
}
