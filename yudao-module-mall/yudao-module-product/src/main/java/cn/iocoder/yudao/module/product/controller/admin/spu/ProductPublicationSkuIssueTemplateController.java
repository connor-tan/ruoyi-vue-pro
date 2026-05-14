package cn.iocoder.yudao.module.product.controller.admin.spu;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductPublicationSkuIssueTemplateGenerateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductPublicationSkuIssueTemplateRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductPublicationSkuIssueTemplateSaveReqVO;
import cn.iocoder.yudao.module.product.service.publication.ProductPublicationSkuIssueTemplateService;
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

@Tag(name = "管理后台 - 刊物 SKU 默认期次模板")
@RestController
@RequestMapping("/product/publication-sku-issue-template")
@Validated
public class ProductPublicationSkuIssueTemplateController {

    @Resource
    private ProductPublicationSkuIssueTemplateService issueTemplateService;

    @GetMapping("/list")
    @Operation(summary = "获得刊物 SKU 默认期次模板")
    @Parameter(name = "skuId", required = true)
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<List<ProductPublicationSkuIssueTemplateRespVO>> list(@RequestParam("skuId") Long skuId) {
        return success(issueTemplateService.getTemplateList(skuId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建刊物 SKU 默认期次模板")
    @PreAuthorize("@ss.hasPermission('product:spu:update')")
    public CommonResult<Long> create(@Valid @RequestBody ProductPublicationSkuIssueTemplateSaveReqVO reqVO) {
        return success(issueTemplateService.saveTemplate(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新刊物 SKU 默认期次模板")
    @PreAuthorize("@ss.hasPermission('product:spu:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody ProductPublicationSkuIssueTemplateSaveReqVO reqVO) {
        issueTemplateService.saveTemplate(reqVO);
        return success(true);
    }

    @PostMapping("/generate")
    @Operation(summary = "批量生成刊物 SKU 默认期次模板")
    @PreAuthorize("@ss.hasPermission('product:spu:update')")
    public CommonResult<Integer> generate(@Valid @RequestBody ProductPublicationSkuIssueTemplateGenerateReqVO reqVO) {
        return success(issueTemplateService.generateTemplates(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除刊物 SKU 默认期次模板")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('product:spu:update')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        issueTemplateService.deleteTemplate(id);
        return success(true);
    }

}
