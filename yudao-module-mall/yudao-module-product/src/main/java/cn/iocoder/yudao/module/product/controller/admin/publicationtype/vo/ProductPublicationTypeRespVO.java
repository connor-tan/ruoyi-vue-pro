package cn.iocoder.yudao.module.product.controller.admin.publicationtype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 刊物类型 Response VO")
@Data
public class ProductPublicationTypeRespVO {

    private Long id;

    private String name;

    private String identifierRule;

    private Integer sort;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;
}
