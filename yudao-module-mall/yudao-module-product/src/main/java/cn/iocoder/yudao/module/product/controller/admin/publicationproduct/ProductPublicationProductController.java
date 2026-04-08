package cn.iocoder.yudao.module.product.controller.admin.publicationproduct;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.publicationproduct.vo.*;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuUpdateStatusReqVO;
import cn.iocoder.yudao.module.product.service.publication.ProductPublicationProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 刊物商品")
@RestController
@RequestMapping("/product/publication-product")
@Validated
public class ProductPublicationProductController {

    @Resource
    private ProductPublicationProductService publicationProductService;

    @GetMapping("/page")
    @Operation(summary = "获得刊物商品分页")
    @PreAuthorize("@ss.hasPermission('product:publication-product:query')")
    public CommonResult<PageResult<ProductPublicationProductRespVO>> getPage(@Valid ProductPublicationProductPageReqVO reqVO) {
        return success(publicationProductService.getPage(reqVO));
    }

    @GetMapping("/get-count")
    @Operation(summary = "获得刊物商品分页 tab count")
    @PreAuthorize("@ss.hasPermission('product:publication-product:query')")
    public CommonResult<Map<Integer, Long>> getCount() {
        return success(publicationProductService.getTabsCount());
    }

    @GetMapping("/get")
    @Operation(summary = "获得刊物商品详情")
    @PreAuthorize("@ss.hasPermission('product:publication-product:query')")
    public CommonResult<ProductPublicationProductRespVO> get(@RequestParam("id") Long id) {
        return success(publicationProductService.get(id));
    }

    @PostMapping("/create")
    @Operation(summary = "创建刊物商品")
    @PreAuthorize("@ss.hasPermission('product:publication-product:create')")
    public CommonResult<Long> create(@Valid @RequestBody ProductPublicationProductSaveReqVO reqVO) {
        return success(publicationProductService.create(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新刊物商品")
    @PreAuthorize("@ss.hasPermission('product:publication-product:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody ProductPublicationProductSaveReqVO reqVO) {
        publicationProductService.update(reqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新刊物商品状态")
    @PreAuthorize("@ss.hasPermission('product:publication-product:update')")
    public CommonResult<Boolean> updateStatus(@Valid @RequestBody ProductSpuUpdateStatusReqVO reqVO) {
        publicationProductService.updateStatus(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除刊物商品")
    @PreAuthorize("@ss.hasPermission('product:publication-product:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        publicationProductService.delete(id);
        return success(true);
    }
}
