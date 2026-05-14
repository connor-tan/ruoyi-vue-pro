package cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo.SubscriptionOfferSkuIssueApplyDefaultTemplateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo.SubscriptionOfferSkuIssueGenerateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo.SubscriptionOfferSkuIssueRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo.SubscriptionOfferSkuIssueSaveReqVO;
import cn.iocoder.yudao.module.subscription.service.offerskuissue.SubscriptionOfferSkuIssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 订刊窗口 SKU 期次")
@RestController
@RequestMapping("/subscription/offer-sku-issue")
@Validated
public class SubscriptionOfferSkuIssueController {

    @Resource
    private SubscriptionOfferSkuIssueService offerSkuIssueService;

    @GetMapping("/list")
    @Operation(summary = "获得窗口 SKU 期次计划")
    @Parameter(name = "offerSkuId", required = true)
    @PreAuthorize("@ss.hasPermission('subscription:offer:query')")
    public CommonResult<List<SubscriptionOfferSkuIssueRespVO>> list(@RequestParam("offerSkuId") Long offerSkuId) {
        return success(offerSkuIssueService.getIssueList(offerSkuId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建窗口 SKU 期次")
    @PreAuthorize("@ss.hasPermission('subscription:offer:update')")
    public CommonResult<Long> create(@Valid @RequestBody SubscriptionOfferSkuIssueSaveReqVO reqVO) {
        return success(offerSkuIssueService.saveIssue(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新窗口 SKU 期次")
    @PreAuthorize("@ss.hasPermission('subscription:offer:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SubscriptionOfferSkuIssueSaveReqVO reqVO) {
        offerSkuIssueService.saveIssue(reqVO);
        return success(true);
    }

    @PostMapping("/generate")
    @Operation(summary = "批量生成窗口 SKU 期次")
    @PreAuthorize("@ss.hasPermission('subscription:offer:update')")
    public CommonResult<Integer> generate(@Valid @RequestBody SubscriptionOfferSkuIssueGenerateReqVO reqVO) {
        return success(offerSkuIssueService.generateIssues(reqVO));
    }

    @PostMapping("/apply-default-template")
    @Operation(summary = "应用商品 SKU 默认期次模板")
    @PreAuthorize("@ss.hasPermission('subscription:offer:update')")
    public CommonResult<Integer> applyDefaultTemplate(
            @Valid @RequestBody SubscriptionOfferSkuIssueApplyDefaultTemplateReqVO reqVO) {
        return success(offerSkuIssueService.applyDefaultTemplate(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除窗口 SKU 期次")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('subscription:offer:update')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        offerSkuIssueService.deleteIssue(id);
        return success(true);
    }

}
