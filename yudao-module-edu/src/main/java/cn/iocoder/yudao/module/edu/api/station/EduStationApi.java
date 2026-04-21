package cn.iocoder.yudao.module.edu.api.station;

import cn.iocoder.yudao.module.edu.api.station.dto.EduSchoolStationRespDTO;

import java.util.Collection;
import java.util.Map;

public interface EduStationApi {

    Map<Long, EduSchoolStationRespDTO> getSchoolStationMap(Collection<Long> schoolIds);
}
