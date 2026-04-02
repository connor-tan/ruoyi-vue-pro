package cn.iocoder.yudao.module.subscription.controller.admin.windowpublication.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 窗口刊物新增/修改 Request VO")
@Data
public class SubscriptionWindowPublicationSaveReqVO {

    private Long id;

    @NotNull(message = "窗口不能为空")
    private Long windowId;

    @NotNull(message = "刊物不能为空")
    private Long productSpuId;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    @NotNull(message = "推荐配置不能为空")
    private Boolean recommendFlag;

    @NotNull(message = "每个学生最大订购数量不能为空")
    @Min(value = 1, message = "每个学生最大订购数量必须大于等于 1")
    private Integer maxQuantityPerStudent;

    @NotEmpty(message = "基础可见年级不能为空")
    private List<Long> gradeCatalogIds;

    private String remark;
}
