package cn.iocoder.yudao.module.repo.controller.admin.supplier;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.repo.controller.admin.supplier.vo.RepoSupplierPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.supplier.vo.RepoSupplierRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.supplier.vo.RepoSupplierSaveReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.supplier.RepoSupplierDO;
import cn.iocoder.yudao.module.repo.service.supplier.RepoSupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 仓库供应商")
@RestController
@RequestMapping("/repo/supplier")
@Validated
public class RepoSupplierController {

    @Resource
    private RepoSupplierService supplierService;

    @PostMapping("/create")
    @Operation(summary = "创建仓库供应商")
    @PreAuthorize("@ss.hasPermission('repo:supplier:create')")
    public CommonResult<Long> createSupplier(@Valid @RequestBody RepoSupplierSaveReqVO createReqVO) {
        return success(supplierService.createSupplier(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新仓库供应商")
    @PreAuthorize("@ss.hasPermission('repo:supplier:update')")
    public CommonResult<Boolean> updateSupplier(@Valid @RequestBody RepoSupplierSaveReqVO updateReqVO) {
        supplierService.updateSupplier(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除仓库供应商")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('repo:supplier:delete')")
    public CommonResult<Boolean> deleteSupplier(@RequestParam("id") Long id) {
        supplierService.deleteSupplier(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得仓库供应商")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('repo:supplier:query')")
    public CommonResult<RepoSupplierRespVO> getSupplier(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(supplierService.getSupplier(id), RepoSupplierRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得仓库供应商分页")
    @PreAuthorize("@ss.hasPermission('repo:supplier:query')")
    public CommonResult<PageResult<RepoSupplierRespVO>> getSupplierPage(@Valid RepoSupplierPageReqVO pageReqVO) {
        PageResult<RepoSupplierDO> pageResult = supplierService.getSupplierPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RepoSupplierRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得仓库供应商精简列表")
    public CommonResult<List<RepoSupplierRespVO>> getSupplierSimpleList() {
        List<RepoSupplierDO> list = supplierService.getSupplierListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, supplier -> new RepoSupplierRespVO()
                .setId(supplier.getId())
                .setName(supplier.getName())
                .setCode(supplier.getCode())
                .setContactName(supplier.getContactName())
                .setContactMobile(supplier.getContactMobile())));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出仓库供应商 Excel")
    @PreAuthorize("@ss.hasPermission('repo:supplier:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSupplierExcel(@Valid RepoSupplierPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<RepoSupplierDO> list = supplierService.getSupplierPage(pageReqVO).getList();
        ExcelUtils.write(response, "仓库供应商.xls", "数据", RepoSupplierRespVO.class,
                BeanUtils.toBean(list, RepoSupplierRespVO.class));
    }

}
