package cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptCloseReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptCreateReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptDemandPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptDemandRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptReceiveReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptRespVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt.RepoPublicationReceiptDO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt.RepoPublicationReceiptItemDO;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.RepoPublicationReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 刊物收货")
@RestController
@RequestMapping("/repo/publication-receipt")
@Validated
public class RepoPublicationReceiptController {

    @Resource
    private RepoPublicationReceiptService publicationReceiptService;

    @GetMapping("/demand-page")
    @Operation(summary = "获得刊物收货需求分页")
    @PreAuthorize("@ss.hasPermission('repo:publication-receipt:query')")
    public CommonResult<PageResult<RepoPublicationReceiptDemandRespVO>> getDemandPage(
            @Valid RepoPublicationReceiptDemandPageReqVO reqVO) {
        return success(publicationReceiptService.getDemandPage(reqVO));
    }

    @PostMapping("/create")
    @Operation(summary = "创建刊物收货单")
    @PreAuthorize("@ss.hasPermission('repo:publication-receipt:create')")
    public CommonResult<Long> createReceipt(@Valid @RequestBody RepoPublicationReceiptCreateReqVO createReqVO) {
        return success(publicationReceiptService.createReceipt(createReqVO));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交刊物收货单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('repo:publication-receipt:update')")
    public CommonResult<Boolean> submitReceipt(@RequestParam("id") Long id) {
        publicationReceiptService.submitReceipt(id);
        return success(true);
    }

    @PutMapping("/receive")
    @Operation(summary = "登记刊物到货")
    @PreAuthorize("@ss.hasPermission('repo:publication-receipt:receive')")
    public CommonResult<Boolean> receiveReceipt(@Valid @RequestBody RepoPublicationReceiptReceiveReqVO reqVO) {
        publicationReceiptService.receiveReceipt(reqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭刊物收货单")
    @PreAuthorize("@ss.hasPermission('repo:publication-receipt:update')")
    public CommonResult<Boolean> closeReceipt(@Valid @RequestBody RepoPublicationReceiptCloseReqVO reqVO) {
        publicationReceiptService.closeReceipt(reqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得刊物收货单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('repo:publication-receipt:query')")
    public CommonResult<RepoPublicationReceiptRespVO> getReceipt(@RequestParam("id") Long id) {
        RepoPublicationReceiptDO receipt = publicationReceiptService.getReceipt(id);
        RepoPublicationReceiptRespVO respVO = BeanUtils.toBean(receipt, RepoPublicationReceiptRespVO.class);
        List<RepoPublicationReceiptItemDO> items = publicationReceiptService.getReceiptItemList(id);
        respVO.setItems(BeanUtils.toBean(items, RepoPublicationReceiptRespVO.Item.class));
        respVO.getItems().forEach(item -> item.setAvailableCount(
                Math.max(defaultZero(item.getReceivedCount()) - defaultZero(item.getAllocatedCount()), 0)));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得刊物收货单分页")
    @PreAuthorize("@ss.hasPermission('repo:publication-receipt:query')")
    public CommonResult<PageResult<RepoPublicationReceiptRespVO>> getReceiptPage(
            @Valid RepoPublicationReceiptPageReqVO pageReqVO) {
        PageResult<RepoPublicationReceiptDO> pageResult = publicationReceiptService.getReceiptPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RepoPublicationReceiptRespVO.class));
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

}
