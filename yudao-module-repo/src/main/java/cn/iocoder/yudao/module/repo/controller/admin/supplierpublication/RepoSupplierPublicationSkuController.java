package cn.iocoder.yudao.module.repo.controller.admin.supplierpublication;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo.RepoPublicationSkuPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo.RepoPublicationSkuRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo.RepoSupplierPublicationSkuPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo.RepoSupplierPublicationSkuRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo.RepoSupplierPublicationSkuSaveReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.supplier.RepoSupplierDO;
import cn.iocoder.yudao.module.repo.dal.dataobject.supplierpublication.RepoSupplierPublicationSkuDO;
import cn.iocoder.yudao.module.repo.service.supplier.RepoSupplierService;
import cn.iocoder.yudao.module.repo.service.supplierpublication.RepoSupplierPublicationSkuService;
import cn.iocoder.yudao.module.repo.service.supplierpublication.bo.RepoPublicationSkuBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 仓库刊物供应商")
@RestController
@RequestMapping("/repo/supplier-publication-sku")
@Validated
public class RepoSupplierPublicationSkuController {

    @Resource
    private RepoSupplierPublicationSkuService supplierPublicationSkuService;
    @Resource
    private RepoSupplierService supplierService;

    @PostMapping("/create")
    @Operation(summary = "创建刊物供应商关系")
    @PreAuthorize("@ss.hasPermission('repo:supplier-publication-sku:create')")
    public CommonResult<Long> createSupplierPublicationSku(
            @Valid @RequestBody RepoSupplierPublicationSkuSaveReqVO createReqVO) {
        return success(supplierPublicationSkuService.createSupplierPublicationSku(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新刊物供应商关系")
    @PreAuthorize("@ss.hasPermission('repo:supplier-publication-sku:update')")
    public CommonResult<Boolean> updateSupplierPublicationSku(
            @Valid @RequestBody RepoSupplierPublicationSkuSaveReqVO updateReqVO) {
        supplierPublicationSkuService.updateSupplierPublicationSku(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除刊物供应商关系")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('repo:supplier-publication-sku:delete')")
    public CommonResult<Boolean> deleteSupplierPublicationSku(@RequestParam("id") Long id) {
        supplierPublicationSkuService.deleteSupplierPublicationSku(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得刊物供应商关系")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('repo:supplier-publication-sku:query')")
    public CommonResult<RepoSupplierPublicationSkuRespVO> getSupplierPublicationSku(@RequestParam("id") Long id) {
        RepoSupplierPublicationSkuDO relation = supplierPublicationSkuService.getSupplierPublicationSku(id);
        return success(fillSupplierName(BeanUtils.toBean(relation, RepoSupplierPublicationSkuRespVO.class)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得刊物供应商关系分页")
    @PreAuthorize("@ss.hasPermission('repo:supplier-publication-sku:query')")
    public CommonResult<PageResult<RepoSupplierPublicationSkuRespVO>> getSupplierPublicationSkuPage(
            @Valid RepoSupplierPublicationSkuPageReqVO pageReqVO) {
        PageResult<RepoSupplierPublicationSkuDO> pageResult =
                supplierPublicationSkuService.getSupplierPublicationSkuPage(pageReqVO);
        PageResult<RepoSupplierPublicationSkuRespVO> respPage =
                BeanUtils.toBean(pageResult, RepoSupplierPublicationSkuRespVO.class);
        Map<Long, RepoSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(pageResult.getList(), RepoSupplierPublicationSkuDO::getSupplierId));
        respPage.getList().forEach(item -> {
            RepoSupplierDO supplier = supplierMap.get(item.getSupplierId());
            if (supplier != null) {
                item.setSupplierName(supplier.getName());
            }
        });
        return success(respPage);
    }

    @GetMapping("/publication-sku-page")
    @Operation(summary = "获得商品中心刊物 SKU 分页")
    @PreAuthorize("@ss.hasPermission('repo:supplier-publication-sku:query')")
    public CommonResult<PageResult<RepoPublicationSkuRespVO>> getPublicationSkuPage(
            @Valid RepoPublicationSkuPageReqVO pageReqVO) {
        PageResult<RepoPublicationSkuBO> pageResult = supplierPublicationSkuService.getPublicationSkuPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RepoPublicationSkuRespVO.class));
    }

    private RepoSupplierPublicationSkuRespVO fillSupplierName(RepoSupplierPublicationSkuRespVO respVO) {
        if (respVO == null || respVO.getSupplierId() == null) {
            return respVO;
        }
        RepoSupplierDO supplier = supplierService.getSupplier(respVO.getSupplierId());
        if (supplier != null) {
            respVO.setSupplierName(supplier.getName());
        }
        return respVO;
    }

}
