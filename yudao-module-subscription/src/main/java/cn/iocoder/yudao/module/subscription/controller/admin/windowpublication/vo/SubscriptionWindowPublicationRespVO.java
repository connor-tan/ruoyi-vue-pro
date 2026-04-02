package cn.iocoder.yudao.module.subscription.controller.admin.windowpublication.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 窗口刊物 Response VO")
@Data
public class SubscriptionWindowPublicationRespVO {

    private Long id;

    private Long windowId;

    private String windowName;

    private Long productSpuId;

    private String productName;

    private Long categoryId;

    private String categoryName;

    private Integer price;

    private String picUrl;

    private Integer status;

    private Integer sort;

    private Boolean recommendFlag;

    private Integer maxQuantityPerStudent;

    private String remark;

    private List<Long> gradeCatalogIds;

    private String gradeNames;

    private LocalDateTime createTime;
}
