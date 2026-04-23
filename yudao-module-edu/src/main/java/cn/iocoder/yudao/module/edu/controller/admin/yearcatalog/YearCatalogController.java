package cn.iocoder.yudao.module.edu.controller.admin.yearcatalog;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo.YearCatalogPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo.YearCatalogRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo.YearCatalogSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo.YearCatalogSimpleRespVO;
import cn.iocoder.yudao.module.edu.service.yearcatalog.YearCatalogService;
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

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 学年目录")
@RestController
@RequestMapping("/edu/year-catalog")
@Validated
public class YearCatalogController {

    @Resource
    private YearCatalogService yearCatalogService;

    @PostMapping("/create")
    @Operation(summary = "创建学年目录")
    @PreAuthorize("@ss.hasPermission('edu:year-catalog:create')")
    public CommonResult<Long> createYearCatalog(@Valid @RequestBody YearCatalogSaveReqVO createReqVO) {
        return success(yearCatalogService.createYearCatalog(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新学年目录")
    @PreAuthorize("@ss.hasPermission('edu:year-catalog:update')")
    public CommonResult<Boolean> updateYearCatalog(@Valid @RequestBody YearCatalogSaveReqVO updateReqVO) {
        yearCatalogService.updateYearCatalog(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除学年目录")
    @Parameter(name = "id", required = true, description = "编号")
    @PreAuthorize("@ss.hasPermission('edu:year-catalog:delete')")
    public CommonResult<Boolean> deleteYearCatalog(@RequestParam("id") Long id) {
        yearCatalogService.deleteYearCatalog(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得学年目录")
    @Parameter(name = "id", required = true, description = "编号")
    @PreAuthorize("@ss.hasPermission('edu:year-catalog:query')")
    public CommonResult<YearCatalogRespVO> getYearCatalog(@RequestParam("id") Long id) {
        return success(yearCatalogService.getYearCatalog(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学年目录分页")
    @PreAuthorize("@ss.hasPermission('edu:year-catalog:query')")
    public CommonResult<PageResult<YearCatalogRespVO>> getYearCatalogPage(@Valid YearCatalogPageReqVO pageReqVO) {
        return success(yearCatalogService.getYearCatalogPage(pageReqVO));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得学年目录精简列表")
    @PreAuthorize("@ss.hasPermission('edu:year-catalog:query')")
    public CommonResult<List<YearCatalogSimpleRespVO>> getYearCatalogSimpleList() {
        return success(yearCatalogService.getYearCatalogSimpleList());
    }
}
