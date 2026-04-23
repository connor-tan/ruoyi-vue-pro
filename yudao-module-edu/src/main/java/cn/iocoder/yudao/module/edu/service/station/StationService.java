package cn.iocoder.yudao.module.edu.service.station;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.api.station.dto.EduSchoolStationRespDTO;
import cn.iocoder.yudao.module.edu.controller.admin.station.vo.StationPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.station.vo.StationSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.station.vo.StationSimpleRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.station.StationDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface StationService {

    Long createStation(@Valid StationSaveReqVO createReqVO);

    void updateStation(@Valid StationSaveReqVO updateReqVO);

    void deleteStation(Long id);

    StationDO getStation(Long id);

    PageResult<StationDO> getStationPage(StationPageReqVO pageReqVO);

    List<StationSimpleRespVO> getStationSimpleList();

    StationDO validateStationBindable(Long stationId, Long schoolAreaId);

    Map<Long, StationDO> getStationMap(Collection<Long> stationIds);

    Map<Long, EduSchoolStationRespDTO> getSchoolStationMap(Collection<Long> schoolIds);
}
