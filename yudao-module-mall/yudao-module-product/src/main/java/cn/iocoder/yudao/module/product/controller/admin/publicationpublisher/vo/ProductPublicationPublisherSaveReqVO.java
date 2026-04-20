package cn.iocoder.yudao.module.product.controller.admin.publicationpublisher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 出版社新增/更新 Request VO")
@Data
public class ProductPublicationPublisherSaveReqVO {

    private Long id;

    @NotBlank(message = "出版社名称不能为空")
    private String name;

    @NotNull(message = "出版社排序不能为空")
    private Integer sort;

    @NotNull(message = "出版社状态不能为空")
    private Integer status;

    private String remark;
}
