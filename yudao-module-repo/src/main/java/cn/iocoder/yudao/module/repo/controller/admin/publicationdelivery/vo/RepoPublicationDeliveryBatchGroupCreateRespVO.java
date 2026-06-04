package cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 仓库刊物候选主表批量创建并发货 Response VO")
@Data
public class RepoPublicationDeliveryBatchGroupCreateRespVO {

    @Schema(description = "创建批次数量", example = "3")
    private Integer batchCount;

    @Schema(description = "批次编号列表")
    private List<Long> batchIds;

    @Schema(description = "本次总发货数量", example = "120")
    private Integer totalCount;

}
