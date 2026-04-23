package cn.iocoder.yudao.module.edu.api.station;

import cn.iocoder.yudao.module.edu.api.station.dto.EduSchoolStationRespDTO;
import cn.iocoder.yudao.module.edu.service.station.StationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Map;

@Service
@Validated
public class EduStationApiImpl implements EduStationApi {

    @Resource
    private StationService stationService;

    @Override
    public Map<Long, EduSchoolStationRespDTO> getSchoolStationMap(Collection<Long> schoolIds) {
        return stationService.getSchoolStationMap(schoolIds);
    }
}
