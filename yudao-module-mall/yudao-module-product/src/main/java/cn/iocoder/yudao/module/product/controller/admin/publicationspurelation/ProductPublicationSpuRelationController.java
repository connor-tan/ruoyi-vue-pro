package cn.iocoder.yudao.module.product.controller.admin.publicationspurelation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.product.controller.admin.publicationspurelation.vo.ProductPublicationSpuRelationRespVO;
import cn.iocoder.yudao.module.product.controller.admin.publicationspurelation.vo.ProductPublicationSpuRelationSaveReqVO;
import cn.iocoder.yudao.module.product.service.publication.ProductPublicationSpuRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 刊物 SPU 主档关系")
@RestController
@RequestMapping("/product/publication-spu")
@Validated
public class ProductPublicationSpuRelationController {

    @Resource
    private ProductPublicationSpuRelationService publicationSpuRelationService;

    @GetMapping("/get-by-spu-id")
    @Operation(summary = "根据 SPU 获得刊物主档关系")
    @PreAuthorize("@ss.hasPermission('product:publication-product:query')")
    public CommonResult<ProductPublicationSpuRelationRespVO> getBySpuId(@RequestParam("productSpuId") Long productSpuId) {
        return success(publicationSpuRelationService.getBySpuId(productSpuId));
    }

    @PostMapping("/create-or-update")
    @Operation(summary = "创建或更新刊物主档关系")
    @PreAuthorize("@ss.hasPermission('product:publication-product:update')")
    public CommonResult<Boolean> createOrUpdate(@Valid @RequestBody ProductPublicationSpuRelationSaveReqVO reqVO) {
        publicationSpuRelationService.createOrUpdate(reqVO);
        return success(true);
    }
}
