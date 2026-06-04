package cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 刊物收货单关闭 Request VO")
@Data
public class RepoPublicationReceiptCloseReqVO {

    @Schema(description = "收货单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "收货单不能为空")
    private Long id;

    @Schema(description = "关闭原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "关闭原因不能为空")
    private String closeReason;

}
