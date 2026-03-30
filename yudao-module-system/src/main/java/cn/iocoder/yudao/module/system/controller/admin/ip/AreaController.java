package cn.iocoder.yudao.module.system.controller.admin.ip;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.framework.ip.core.utils.IPUtils;
import cn.iocoder.yudao.module.system.controller.admin.ip.vo.AreaEnabledTreeReqVO;
import cn.iocoder.yudao.module.system.controller.admin.ip.vo.AreaNodeRespVO;
import cn.iocoder.yudao.module.system.controller.admin.ip.vo.AreaUpdateStatusBatchReqVO;
import cn.iocoder.yudao.module.system.controller.admin.ip.vo.AreaUpdateStatusReqVO;
import cn.iocoder.yudao.module.system.service.ip.AreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.ArrayList;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 地区")
@RestController
@RequestMapping("/system/area")
@Validated
public class AreaController {

    @Resource
    private AreaService areaService;

    @GetMapping("/tree")
    @Operation(summary = "获得地区树")
    public CommonResult<List<AreaNodeRespVO>> getAreaTree() {
        return success(areaService.getAreaTree());
    }

    @GetMapping("/tree-enabled")
    @Operation(summary = "获得启用的地区树")
    @Parameter(name = "includeAreaId", description = "需要额外包含的地区编号", example = "320505")
    @Parameter(name = "includeAreaIds", description = "需要额外包含的地区编号列表，多个编号使用逗号分隔",
            example = "320505,320506")
    public CommonResult<List<AreaNodeRespVO>> getEnabledAreaTree(
            @RequestParam(value = "includeAreaId", required = false) Integer includeAreaId,
            @RequestParam(value = "includeAreaIds", required = false) List<Integer> includeAreaIds) {
        List<Integer> actualIncludeAreaIds = CollectionUtils.isEmpty(includeAreaIds)
                ? new ArrayList<>() : new ArrayList<>(includeAreaIds);
        if (includeAreaId != null) {
            actualIncludeAreaIds.add(includeAreaId);
        }
        return success(areaService.getEnabledAreaTree(actualIncludeAreaIds));
    }

    @PostMapping("/tree-enabled")
    @Operation(summary = "获得启用的地区树")
    public CommonResult<List<AreaNodeRespVO>> getEnabledAreaTree(
            @Valid @RequestBody AreaEnabledTreeReqVO reqVO) {
        return success(areaService.getEnabledAreaTree(reqVO.getIncludeAreaIds()));
    }

    @PutMapping("/update-status")
    @Operation(summary = "修改地区状态")
    public CommonResult<Boolean> updateAreaStatus(@Valid @RequestBody AreaUpdateStatusReqVO reqVO) {
        areaService.updateAreaStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @PutMapping("/update-status-batch")
    @Operation(summary = "批量修改地区状态")
    public CommonResult<Boolean> updateAreaStatusBatch(@Valid @RequestBody AreaUpdateStatusBatchReqVO reqVO) {
        areaService.updateAreaStatusBatch(reqVO.getIds(), reqVO.getStatus());
        return success(true);
    }

    @GetMapping("/get-by-ip")
    @Operation(summary = "获得 IP 对应的地区名")
    @Parameter(name = "ip", description = "IP", required = true)
    public CommonResult<String> getAreaByIp(@RequestParam("ip") String ip) {
        // 获得城市
        Area area = IPUtils.getArea(ip);
        if (area == null) {
            return success("未知");
        }
        // 格式化返回
        return success(AreaUtils.format(area.getId()));
    }

}
