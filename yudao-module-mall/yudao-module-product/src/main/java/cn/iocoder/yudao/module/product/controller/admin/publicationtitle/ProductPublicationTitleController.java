package cn.iocoder.yudao.module.product.controller.admin.publicationtitle;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.publicationtitle.vo.*;
import cn.iocoder.yudao.module.product.service.publication.ProductPublicationTitleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 刊物主档")
@RestController
@RequestMapping("/product/publication-title")
@Validated
public class ProductPublicationTitleController {

    @Resource
    private ProductPublicationTitleService publicationTitleService;

    @PostMapping("/create")
    @Operation(summary = "创建刊物主档")
    @PreAuthorize("@ss.hasPermission('product:publication-title:create')")
    public CommonResult<Long> create(@Valid @RequestBody ProductPublicationTitleSaveReqVO reqVO) {
        return success(publicationTitleService.create(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新刊物主档")
    @PreAuthorize("@ss.hasPermission('product:publication-title:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody ProductPublicationTitleSaveReqVO reqVO) {
        publicationTitleService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除刊物主档")
    @PreAuthorize("@ss.hasPermission('product:publication-title:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        publicationTitleService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得刊物主档")
    @PreAuthorize("@ss.hasPermission('product:publication-title:query')")
    public CommonResult<ProductPublicationTitleRespVO> get(@RequestParam("id") Long id) {
        return success(publicationTitleService.get(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得刊物主档分页")
    @PreAuthorize("@ss.hasPermission('product:publication-title:query')")
    public CommonResult<PageResult<ProductPublicationTitleRespVO>> getPage(@Valid ProductPublicationTitlePageReqVO reqVO) {
        return success(publicationTitleService.getPage(reqVO));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得刊物主档精简列表")
    @PreAuthorize("@ss.hasPermission('product:publication-title:query')")
    public CommonResult<List<ProductPublicationTitleSimpleRespVO>> getSimpleList() {
        return success(publicationTitleService.getSimpleList());
    }
}
