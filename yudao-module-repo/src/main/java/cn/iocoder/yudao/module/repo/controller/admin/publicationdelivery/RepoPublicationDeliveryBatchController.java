package cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchCreateReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchGroupCreateReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchGroupCreateRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidateChildReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidateGroupRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidateItemRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidatePageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidateRespVO;
import cn.iocoder.yudao.module.repo.service.publicationdelivery.RepoPublicationDeliveryBatchService;
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

@Tag(name = "管理后台 - 仓库刊物发货批次")
@RestController
@RequestMapping("/repo/publication-delivery-batch")
@Validated
public class RepoPublicationDeliveryBatchController {

    @Resource
    private RepoPublicationDeliveryBatchService publicationDeliveryBatchService;

    @GetMapping("/candidate-page")
    @Operation(summary = "获得仓库刊物发货候选分页")
    @PreAuthorize("@ss.hasPermission('repo:publication-delivery-batch:query')")
    public CommonResult<PageResult<RepoPublicationDeliveryCandidateRespVO>> getCandidatePage(
            @Valid RepoPublicationDeliveryCandidatePageReqVO reqVO) {
        return success(publicationDeliveryBatchService.getCandidatePage(reqVO));
    }

    @GetMapping("/candidate-group-page")
    @Operation(summary = "获得仓库刊物发货候选主表分页")
    @PreAuthorize("@ss.hasPermission('repo:publication-delivery-batch:query')")
    public CommonResult<PageResult<RepoPublicationDeliveryCandidateGroupRespVO>> getCandidateGroupPage(
            @Valid RepoPublicationDeliveryCandidatePageReqVO reqVO) {
        return success(publicationDeliveryBatchService.getCandidateGroupPage(reqVO));
    }

    @GetMapping("/candidate-child-list")
    @Operation(summary = "获得仓库刊物发货候选子表列表")
    @PreAuthorize("@ss.hasPermission('repo:publication-delivery-batch:query')")
    public CommonResult<List<RepoPublicationDeliveryCandidateRespVO>> getCandidateChildList(
            @Valid RepoPublicationDeliveryCandidateChildReqVO reqVO) {
        return success(publicationDeliveryBatchService.getCandidateChildList(reqVO));
    }

    @GetMapping("/candidate-child-page")
    @Operation(summary = "获得仓库刊物发货候选子表分页")
    @PreAuthorize("@ss.hasPermission('repo:publication-delivery-batch:query')")
    public CommonResult<PageResult<RepoPublicationDeliveryCandidateRespVO>> getCandidateChildPage(
            @Valid RepoPublicationDeliveryCandidateChildReqVO reqVO) {
        return success(publicationDeliveryBatchService.getCandidateChildPage(reqVO));
    }

    @GetMapping("/candidate-item-list")
    @Operation(summary = "获得仓库刊物发货候选明细")
    @PreAuthorize("@ss.hasPermission('repo:publication-delivery-batch:query')")
    public CommonResult<List<RepoPublicationDeliveryCandidateItemRespVO>> getCandidateItemList(
            @Valid RepoPublicationDeliveryCandidatePageReqVO reqVO) {
        return success(publicationDeliveryBatchService.getCandidateItemList(reqVO));
    }

    @PostMapping("/create-and-deliver")
    @Operation(summary = "创建仓库刊物发货批次并发货")
    @PreAuthorize("@ss.hasPermission('repo:publication-delivery-batch:create')")
    public CommonResult<Long> createAndDeliver(@Valid @RequestBody RepoPublicationDeliveryBatchCreateReqVO reqVO) {
        return success(publicationDeliveryBatchService.createAndDeliver(reqVO, getLoginUserId()));
    }

    @PostMapping("/create-group-and-deliver")
    @Operation(summary = "创建仓库刊物候选主表下全部批次并发货")
    @PreAuthorize("@ss.hasPermission('repo:publication-delivery-batch:create')")
    public CommonResult<RepoPublicationDeliveryBatchGroupCreateRespVO> createGroupAndDeliver(
            @Valid @RequestBody RepoPublicationDeliveryBatchGroupCreateReqVO reqVO) {
        return success(publicationDeliveryBatchService.createGroupAndDeliver(reqVO, getLoginUserId()));
    }

    @GetMapping("/page")
    @Operation(summary = "获得仓库刊物发货批次分页")
    @PreAuthorize("@ss.hasPermission('repo:publication-delivery-batch:query')")
    public CommonResult<PageResult<RepoPublicationDeliveryBatchRespVO>> getBatchPage(
            @Valid RepoPublicationDeliveryBatchPageReqVO reqVO) {
        return success(publicationDeliveryBatchService.getBatchPage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得仓库刊物发货批次详情")
    @Parameter(name = "id", description = "批次编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('repo:publication-delivery-batch:query')")
    public CommonResult<RepoPublicationDeliveryBatchRespVO> getBatch(@RequestParam("id") Long id) {
        return success(publicationDeliveryBatchService.getBatch(id));
    }

}
