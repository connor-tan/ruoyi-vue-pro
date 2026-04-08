package cn.iocoder.yudao.module.product.controller.admin.publicationspugrade;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.product.controller.admin.publicationspugrade.vo.ProductPublicationGradeSimpleRespVO;
import cn.iocoder.yudao.module.product.controller.admin.publicationspugrade.vo.ProductPublicationSpuGradeRespVO;
import cn.iocoder.yudao.module.product.controller.admin.publicationspugrade.vo.ProductPublicationSpuGradeSaveReqVO;
import cn.iocoder.yudao.module.product.service.publication.ProductPublicationSpuGradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 刊物 SPU 适用年级")
@RestController
@RequestMapping("/product/publication-spu-grade")
@Validated
public class ProductPublicationSpuGradeController {

    @Resource
    private ProductPublicationSpuGradeService publicationSpuGradeService;

    @GetMapping("/get-by-spu-id")
    @Operation(summary = "根据 SPU 获得适用年级")
    @PreAuthorize("@ss.hasPermission('product:publication-product:query')")
    public CommonResult<ProductPublicationSpuGradeRespVO> getBySpuId(@RequestParam("productSpuId") Long productSpuId) {
        return success(publicationSpuGradeService.getBySpuId(productSpuId));
    }

    @PostMapping("/create-or-update")
    @Operation(summary = "创建或更新刊物 SPU 适用年级")
    @PreAuthorize("@ss.hasPermission('product:publication-product:update')")
    public CommonResult<Boolean> createOrUpdate(@Valid @RequestBody ProductPublicationSpuGradeSaveReqVO reqVO) {
        publicationSpuGradeService.createOrUpdate(reqVO);
        return success(true);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得标准年级精简列表")
    @PreAuthorize("@ss.hasPermission('product:publication-product:query')")
    public CommonResult<List<ProductPublicationGradeSimpleRespVO>> getSimpleList() {
        return success(publicationSpuGradeService.getSimpleList());
    }
}
