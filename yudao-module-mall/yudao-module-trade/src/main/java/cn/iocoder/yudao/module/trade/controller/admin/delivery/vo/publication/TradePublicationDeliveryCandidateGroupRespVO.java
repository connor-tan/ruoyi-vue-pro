package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 刊物期次批次发货候选主表 Response VO")
@Data
public class TradePublicationDeliveryCandidateGroupRespVO {

    @Schema(description = "配送方式", example = "3")
    private Integer deliveryType;

    @Schema(description = "学校编号", example = "100")
    private Long schoolId;

    @Schema(description = "学校名称快照", example = "实验小学")
    private String schoolNameSnapshot;

    @Schema(description = "学校配送仓库编号", example = "200")
    private Long warehouseId;

    @Schema(description = "学校配送仓库名称快照", example = "梁溪站")
    private String warehouseNameSnapshot;

    @Schema(description = "订刊窗口编号", example = "1")
    private Long windowId;

    @Schema(description = "订刊窗口名称快照", example = "2026 春季订刊")
    private String windowNameSnapshot;

    @Schema(description = "待发货数量", example = "120")
    private Integer totalCount;

    @Schema(description = "涉及订单数", example = "80")
    private Integer orderCount;

    @Schema(description = "涉及学生数", example = "75")
    private Integer studentCount;

    @Schema(description = "刊物聚合数", example = "6")
    private Integer publicationGroupCount;

    @Schema(description = "期次聚合数", example = "10")
    private Integer issueGroupCount;

}
