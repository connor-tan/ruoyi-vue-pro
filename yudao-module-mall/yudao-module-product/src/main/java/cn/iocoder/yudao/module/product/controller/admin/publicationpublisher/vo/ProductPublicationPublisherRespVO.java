package cn.iocoder.yudao.module.product.controller.admin.publicationpublisher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 出版社 Response VO")
@Data
public class ProductPublicationPublisherRespVO {

    private Long id;

    private String name;

    private Integer sort;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;
}
