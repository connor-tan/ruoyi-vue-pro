package cn.iocoder.yudao.module.product.controller.admin.publicationtype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 刊物类型精简 Response VO")
@Data
public class ProductPublicationTypeSimpleRespVO {

    private Long id;

    private String code;

    private String name;
}
