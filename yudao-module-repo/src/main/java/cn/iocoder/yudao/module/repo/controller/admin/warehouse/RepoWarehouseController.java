package cn.iocoder.yudao.module.repo.controller.admin.warehouse;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.repo.controller.admin.warehouse.vo.RepoWarehousePageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.warehouse.vo.RepoWarehouseRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.warehouse.vo.RepoWarehouseSaveReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.warehouse.RepoWarehouseDO;
import cn.iocoder.yudao.module.repo.service.warehouse.RepoWarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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

@Tag(name = "管理后台 - 仓库")
@RestController
@RequestMapping("/repo/warehouse")
@Validated
public class RepoWarehouseController {

    @Resource
    private RepoWarehouseService warehouseService;

    @PostMapping("/create")
    @Operation(summary = "创建仓库")
    @PreAuthorize("@ss.hasPermission('repo:warehouse:create')")
    public CommonResult<Long> createWarehouse(@Valid @RequestBody RepoWarehouseSaveReqVO createReqVO) {
        return success(warehouseService.createWarehouse(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新仓库")
    @PreAuthorize("@ss.hasPermission('repo:warehouse:update')")
    public CommonResult<Boolean> updateWarehouse(@Valid @RequestBody RepoWarehouseSaveReqVO updateReqVO) {
        warehouseService.updateWarehouse(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-default-status")
    @Operation(summary = "更新仓库默认状态")
    @Parameters({
            @Parameter(name = "id", description = "编号", required = true),
            @Parameter(name = "defaultStatus", description = "是否默认", required = true)
    })
    @PreAuthorize("@ss.hasPermission('repo:warehouse:update')")
    public CommonResult<Boolean> updateWarehouseDefaultStatus(@RequestParam("id") Long id,
                                                              @RequestParam("defaultStatus") Boolean defaultStatus) {
        warehouseService.updateWarehouseDefaultStatus(id, defaultStatus);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除仓库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('repo:warehouse:delete')")
    public CommonResult<Boolean> deleteWarehouse(@RequestParam("id") Long id) {
        warehouseService.deleteWarehouse(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得仓库")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('repo:warehouse:query')")
    public CommonResult<RepoWarehouseRespVO> getWarehouse(@RequestParam("id") Long id) {
        RepoWarehouseDO warehouse = warehouseService.getWarehouse(id);
        return success(BeanUtils.toBean(warehouse, RepoWarehouseRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得仓库分页")
    @PreAuthorize("@ss.hasPermission('repo:warehouse:query')")
    public CommonResult<PageResult<RepoWarehouseRespVO>> getWarehousePage(@Valid RepoWarehousePageReqVO pageReqVO) {
        PageResult<RepoWarehouseDO> pageResult = warehouseService.getWarehousePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RepoWarehouseRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得仓库精简列表", description = "只包含被开启的仓库，主要用于学校配送仓库下拉选项")
    public CommonResult<List<RepoWarehouseRespVO>> getWarehouseSimpleList() {
        List<RepoWarehouseDO> list = warehouseService.getWarehouseListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, warehouse -> new RepoWarehouseRespVO()
                .setId(warehouse.getId())
                .setName(warehouse.getName())
                .setAddress(warehouse.getAddress())
                .setPrincipal(warehouse.getPrincipal())
                .setDefaultStatus(warehouse.getDefaultStatus())));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出仓库 Excel")
    @PreAuthorize("@ss.hasPermission('repo:warehouse:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWarehouseExcel(@Valid RepoWarehousePageReqVO pageReqVO,
                                     HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<RepoWarehouseDO> list = warehouseService.getWarehousePage(pageReqVO).getList();
        ExcelUtils.write(response, "仓库.xls", "数据", RepoWarehouseRespVO.class,
                BeanUtils.toBean(list, RepoWarehouseRespVO.class));
    }

}
