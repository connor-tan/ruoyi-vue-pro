package cn.iocoder.yudao.module.edu.controller.admin.station;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.edu.controller.admin.station.vo.StationPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.station.vo.StationRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.station.vo.StationSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.station.vo.StationSimpleRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.station.StationDO;
import cn.iocoder.yudao.module.edu.service.station.StationService;
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

@Tag(name = "管理后台 - 站点管理")
@RestController
@RequestMapping("/edu/station")
@Validated
public class StationController {

    @Resource
    private StationService stationService;

    @PostMapping("/create")
    @Operation(summary = "创建站点")
    @PreAuthorize("@ss.hasPermission('edu:station:create')")
    public CommonResult<Long> createStation(@Valid @RequestBody StationSaveReqVO createReqVO) {
        return success(stationService.createStation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新站点")
    @PreAuthorize("@ss.hasPermission('edu:station:update')")
    public CommonResult<Boolean> updateStation(@Valid @RequestBody StationSaveReqVO updateReqVO) {
        stationService.updateStation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除站点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:station:delete')")
    public CommonResult<Boolean> deleteStation(@RequestParam("id") Long id) {
        stationService.deleteStation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得站点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:station:query')")
    public CommonResult<StationRespVO> getStation(@RequestParam("id") Long id) {
        return success(buildStationResp(stationService.getStation(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得站点分页")
    @PreAuthorize("@ss.hasPermission('edu:station:query')")
    public CommonResult<PageResult<StationRespVO>> getStationPage(@Valid StationPageReqVO pageReqVO) {
        PageResult<StationDO> pageResult = stationService.getStationPage(pageReqVO);
        return success(new PageResult<>(buildStationRespList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得站点精简列表")
    @PreAuthorize("@ss.hasPermission('edu:station:query')")
    public CommonResult<List<StationSimpleRespVO>> getStationSimpleList() {
        return success(buildStationSimpleRespList(stationService.getStationSimpleList()));
    }

    private List<StationRespVO> buildStationRespList(List<StationDO> stations) {
        return BeanUtils.toBean(stations, StationRespVO.class, this::fillAreaName);
    }

    private StationRespVO buildStationResp(StationDO station) {
        if (station == null) {
            return null;
        }
        StationRespVO respVO = BeanUtils.toBean(station, StationRespVO.class);
        fillAreaName(respVO);
        return respVO;
    }

    private List<StationSimpleRespVO> buildStationSimpleRespList(List<StationSimpleRespVO> stations) {
        stations.forEach(this::fillAreaName);
        return stations;
    }

    private void fillAreaName(StationRespVO respVO) {
        if (respVO.getAreaId() != null) {
            respVO.setAreaName(AreaUtils.format(respVO.getAreaId().intValue()));
        }
    }

    private void fillAreaName(StationSimpleRespVO respVO) {
        if (respVO.getAreaId() != null) {
            respVO.setAreaName(AreaUtils.format(respVO.getAreaId().intValue()));
        }
    }
}
