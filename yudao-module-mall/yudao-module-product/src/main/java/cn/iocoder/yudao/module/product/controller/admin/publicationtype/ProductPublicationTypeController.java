package cn.iocoder.yudao.module.product.controller.admin.publicationtype;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.publicationtype.vo.*;
import cn.iocoder.yudao.module.product.service.publication.ProductPublicationTypeService;
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

@Tag(name = "管理后台 - 刊物类型")
@RestController
@RequestMapping("/product/publication-type")
@Validated
public class ProductPublicationTypeController {

    @Resource
    private ProductPublicationTypeService publicationTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建刊物类型")
    @PreAuthorize("@ss.hasPermission('product:publication-type:create')")
    public CommonResult<Long> create(@Valid @RequestBody ProductPublicationTypeSaveReqVO reqVO) {
        return success(publicationTypeService.create(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新刊物类型")
    @PreAuthorize("@ss.hasPermission('product:publication-type:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody ProductPublicationTypeSaveReqVO reqVO) {
        publicationTypeService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除刊物类型")
    @PreAuthorize("@ss.hasPermission('product:publication-type:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        publicationTypeService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得刊物类型")
    @PreAuthorize("@ss.hasPermission('product:publication-type:query')")
    public CommonResult<ProductPublicationTypeRespVO> get(@RequestParam("id") Long id) {
        return success(publicationTypeService.get(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得刊物类型分页")
    @PreAuthorize("@ss.hasPermission('product:publication-type:query')")
    public CommonResult<PageResult<ProductPublicationTypeRespVO>> getPage(@Valid ProductPublicationTypePageReqVO reqVO) {
        return success(publicationTypeService.getPage(reqVO));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得刊物类型精简列表")
    @PreAuthorize("@ss.hasPermission('product:publication-type:query')")
    public CommonResult<List<ProductPublicationTypeSimpleRespVO>> getSimpleList() {
        return success(publicationTypeService.getSimpleList());
    }
}
