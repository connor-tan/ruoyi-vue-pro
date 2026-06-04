package cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 仓库刊物候选主表批量创建并发货 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RepoPublicationDeliveryBatchGroupCreateReqVO extends RepoPublicationDeliveryCandidatePageReqVO {

    @Schema(description = "备注", example = "学校本批次统一发货")
    private String remark;

}
