package cn.iocoder.yudao.module.product.controller.admin.publicationtitle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 刊物主档新增/更新 Request VO")
@Data
public class ProductPublicationTitleSaveReqVO {

    private Long id;

    @NotBlank(message = "刊物主档编码不能为空")
    private String code;

    @NotBlank(message = "刊物主档名称不能为空")
    private String name;

    @NotNull(message = "刊物类型不能为空")
    private Long typeId;

    @NotNull(message = "出版社不能为空")
    private Long publisherId;

    @NotBlank(message = "刊物周期不能为空")
    private String issueCycle;

    @NotNull(message = "刊物主档状态不能为空")
    private Integer status;

    private String issn;

    private String cnCode;

    private String postDistributionCode;

    private String remark;
}
