package cn.iocoder.yudao.module.trade.controller.admin.delivery;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchPageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidateItemRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidatePageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidateRespVO;
import cn.iocoder.yudao.module.trade.service.delivery.TradePublicationDeliveryBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 刊物学校配送发货批次")
@RestController
@RequestMapping("/trade/publication-delivery-batch")
@Validated
public class TradePublicationDeliveryBatchController {

    @Resource
    private TradePublicationDeliveryBatchService publicationDeliveryBatchService;

    @GetMapping("/candidate-page")
    @Operation(summary = "获得刊物学校配送发货候选分页")
    @PreAuthorize("@ss.hasPermission('trade:publication-delivery-batch:query')")
    public CommonResult<PageResult<TradePublicationDeliveryCandidateRespVO>> getCandidatePage(
            @Valid TradePublicationDeliveryCandidatePageReqVO reqVO) {
        return success(publicationDeliveryBatchService.getCandidatePage(reqVO));
    }

    @GetMapping("/candidate-item-list")
    @Operation(summary = "获得刊物期次发货候选明细")
    @PreAuthorize("@ss.hasPermission('trade:publication-delivery-batch:query')")
    public CommonResult<List<TradePublicationDeliveryCandidateItemRespVO>> getCandidateItemList(
            @Valid TradePublicationDeliveryCandidatePageReqVO reqVO) {
        return success(publicationDeliveryBatchService.getCandidateItemList(reqVO));
    }

    @PostMapping("/create-and-deliver")
    @Operation(summary = "创建刊物学校配送发货批次并发货")
    @PreAuthorize("@ss.hasPermission('trade:publication-delivery-batch:create')")
    public CommonResult<Long> createAndDeliver(@Valid @RequestBody TradePublicationDeliveryBatchCreateReqVO reqVO) {
        return success(publicationDeliveryBatchService.createAndDeliver(reqVO, getLoginUserId()));
    }

    @GetMapping("/page")
    @Operation(summary = "获得刊物学校配送发货批次分页")
    @PreAuthorize("@ss.hasPermission('trade:publication-delivery-batch:query')")
    public CommonResult<PageResult<TradePublicationDeliveryBatchRespVO>> getBatchPage(
            @Valid TradePublicationDeliveryBatchPageReqVO reqVO) {
        return success(publicationDeliveryBatchService.getBatchPage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得刊物学校配送发货批次详情")
    @Parameter(name = "id", description = "批次编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('trade:publication-delivery-batch:query')")
    public CommonResult<TradePublicationDeliveryBatchRespVO> getBatch(@RequestParam("id") Long id) {
        return success(publicationDeliveryBatchService.getBatch(id));
    }

}
