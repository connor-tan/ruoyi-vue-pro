package cn.iocoder.yudao.module.product.controller.admin.publicationpublisher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 出版社精简 Response VO")
@Data
public class ProductPublicationPublisherSimpleRespVO {

    private Long id;

    private String name;
}
