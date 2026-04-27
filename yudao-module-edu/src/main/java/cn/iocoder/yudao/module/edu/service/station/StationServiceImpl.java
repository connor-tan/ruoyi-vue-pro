package cn.iocoder.yudao.module.edu.service.station;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.api.station.dto.EduSchoolStationRespDTO;
import cn.iocoder.yudao.module.edu.controller.admin.station.vo.StationPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.station.vo.StationSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.station.vo.StationSimpleRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.station.StationDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.station.StationMapper;
import cn.iocoder.yudao.module.system.api.ip.AreaApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_STATION_AREA_NOT_MATCHED;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STATION_DISABLED;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STATION_IN_USE_BY_SCHOOL;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STATION_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STATION_NOT_EXISTS;

@Service
@Validated
public class StationServiceImpl implements StationService {

    @Resource
    private StationMapper stationMapper;
    @Resource
    private SchoolMapper schoolMapper;
    @Resource
    private AreaApi areaApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStation(StationSaveReqVO createReqVO) {
        validateAreaSelectable(createReqVO.getAreaId());
        validateStationNameUnique(null, createReqVO.getAreaId(), createReqVO.getStationName());
        StationDO station = BeanUtils.toBean(createReqVO, StationDO.class);
        stationMapper.insert(station);
        return station.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStation(StationSaveReqVO updateReqVO) {
        StationDO oldStation = validateStationExists(updateReqVO.getId());
        if (!Objects.equals(oldStation.getAreaId(), updateReqVO.getAreaId())) {
            validateAreaSelectable(updateReqVO.getAreaId());
        }
        validateStationNameUnique(updateReqVO.getId(), updateReqVO.getAreaId(), updateReqVO.getStationName());
        validateStationChangeable(oldStation, updateReqVO);
        StationDO updateObj = BeanUtils.toBean(updateReqVO, StationDO.class);
        stationMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStation(Long id) {
        validateStationExists(id);
        validateStationUnused(id);
        stationMapper.deletePhysicallyById(id);
    }

    @Override
    public StationDO getStation(Long id) {
        StationDO station = stationMapper.selectById(id);
        fillSchoolCount(station);
        return station;
    }

    @Override
    public PageResult<StationDO> getStationPage(StationPageReqVO pageReqVO) {
        List<Long> areaIds = pageReqVO.getAreaId() == null ? null
                : convertList(areaApi.getSelectableAreaIds(Math.toIntExact(pageReqVO.getAreaId())), Long::valueOf);
        PageResult<StationDO> pageResult = stationMapper.selectPage(pageReqVO, areaIds);
        fillSchoolCount(pageResult.getList());
        return pageResult;
    }

    @Override
    public List<StationSimpleRespVO> getStationSimpleList() {
        return BeanUtils.toBean(stationMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()), StationSimpleRespVO.class);
    }

    @Override
    public StationDO validateStationBindable(Long stationId, Long schoolAreaId) {
        StationDO station = validateStationExists(stationId);
        if (CommonStatusEnum.isDisable(station.getStatus())) {
            throw exception(STATION_DISABLED);
        }
        if (!Objects.equals(station.getAreaId(), schoolAreaId)) {
            throw exception(SCHOOL_STATION_AREA_NOT_MATCHED);
        }
        return station;
    }

    @Override
    public Map<Long, StationDO> getStationMap(Collection<Long> stationIds) {
        if (CollUtil.isEmpty(stationIds)) {
            return Collections.emptyMap();
        }
        List<Long> filteredStationIds = stationIds.stream().filter(Objects::nonNull).distinct().toList();
        if (CollUtil.isEmpty(filteredStationIds)) {
            return Collections.emptyMap();
        }
        return convertMap(stationMapper.selectList(StationDO::getId, filteredStationIds), StationDO::getId);
    }

    @Override
    public Map<Long, EduSchoolStationRespDTO> getSchoolStationMap(Collection<Long> schoolIds) {
        if (CollUtil.isEmpty(schoolIds)) {
            return Collections.emptyMap();
        }
        List<SchoolDO> schools = schoolMapper.selectList(SchoolDO::getId, schoolIds);
        if (CollUtil.isEmpty(schools)) {
            return Collections.emptyMap();
        }
        Map<Long, StationDO> stationMap = getStationMap(convertList(schools, SchoolDO::getStationId));
        return schools.stream().collect(Collectors.toMap(SchoolDO::getId, school -> {
            StationDO station = stationMap.get(school.getStationId());
            EduSchoolStationRespDTO respDTO = new EduSchoolStationRespDTO();
            respDTO.setSchoolId(school.getId());
            respDTO.setSchoolName(school.getSchoolName());
            respDTO.setSchoolAreaId(school.getAreaId());
            respDTO.setStationId(school.getStationId());
            if (station != null) {
                respDTO.setStationName(station.getStationName());
                respDTO.setStationAreaId(station.getAreaId());
                respDTO.setStationAddress(station.getStationAddress());
                respDTO.setContactName(station.getContactName());
                respDTO.setContactMobile(station.getContactMobile());
                respDTO.setStatus(station.getStatus());
            }
            return respDTO;
        }));
    }

    public StationDO validateStationExists(Long id) {
        StationDO station = stationMapper.selectById(id);
        if (station == null) {
            throw exception(STATION_NOT_EXISTS);
        }
        return station;
    }

    private void validateAreaSelectable(Long areaId) {
        areaApi.validateAreaSelectable(Math.toIntExact(areaId));
    }

    private void validateStationNameUnique(Long id, Long areaId, String stationName) {
        StationDO existed = stationMapper.selectByAreaIdAndStationName(areaId, stationName);
        if (existed == null) {
            return;
        }
        if (id != null && Objects.equals(existed.getId(), id)) {
            return;
        }
        throw exception(STATION_NAME_DUPLICATE);
    }

    private void validateStationUnused(Long stationId) {
        if (schoolMapper.countByStationId(stationId) > 0) {
            throw exception(STATION_IN_USE_BY_SCHOOL);
        }
    }

    private void validateStationChangeable(StationDO oldStation, StationSaveReqVO updateReqVO) {
        long boundSchoolCount = schoolMapper.countByStationId(oldStation.getId());
        if (boundSchoolCount <= 0) {
            return;
        }
        if (!Objects.equals(oldStation.getAreaId(), updateReqVO.getAreaId())
                || CommonStatusEnum.isDisable(updateReqVO.getStatus())) {
            throw exception(STATION_IN_USE_BY_SCHOOL);
        }
    }

    private void fillSchoolCount(StationDO station) {
        if (station == null) {
            return;
        }
        station.setSchoolCount(schoolMapper.countByStationId(station.getId()));
    }

    private void fillSchoolCount(List<StationDO> stations) {
        if (CollUtil.isEmpty(stations)) {
            return;
        }
        stations.forEach(this::fillSchoolCount);
    }
}
