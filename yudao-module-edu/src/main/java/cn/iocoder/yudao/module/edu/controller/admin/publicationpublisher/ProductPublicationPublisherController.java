package cn.iocoder.yudao.module.edu.controller.admin.publicationpublisher;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.publicationpublisher.vo.*;
import cn.iocoder.yudao.module.edu.service.publication.ProductPublicationPublisherService;
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

@Tag(name = "管理后台 - 出版社")
@RestController
@RequestMapping("/edu/publication-publisher")
@Validated
public class ProductPublicationPublisherController {

    @Resource
    private ProductPublicationPublisherService publicationPublisherService;

    @PostMapping("/create")
    @Operation(summary = "创建出版社")
    @PreAuthorize("@ss.hasPermission('edu:publication-publisher:create')")
    public CommonResult<Long> create(@Valid @RequestBody ProductPublicationPublisherSaveReqVO reqVO) {
        return success(publicationPublisherService.create(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新出版社")
    @PreAuthorize("@ss.hasPermission('edu:publication-publisher:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody ProductPublicationPublisherSaveReqVO reqVO) {
        publicationPublisherService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除出版社")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('edu:publication-publisher:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        publicationPublisherService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取出版社")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('edu:publication-publisher:query')")
    public CommonResult<ProductPublicationPublisherRespVO> get(@RequestParam("id") Long id) {
        return success(publicationPublisherService.get(id));
    }

    @GetMapping("/page")
    @Operation(summary = "出版社分页")
    @PreAuthorize("@ss.hasPermission('edu:publication-publisher:query')")
    public CommonResult<PageResult<ProductPublicationPublisherRespVO>> page(@Valid ProductPublicationPublisherPageReqVO reqVO) {
        return success(publicationPublisherService.getPage(reqVO));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "出版社精简列表")
    @PreAuthorize("@ss.hasPermission('edu:publication-publisher:query')")
    public CommonResult<List<ProductPublicationPublisherSimpleRespVO>> simpleList() {
        return success(publicationPublisherService.getSimpleList());
    }
}
