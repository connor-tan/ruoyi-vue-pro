package cn.iocoder.yudao.module.edu.service.station;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.edu.api.station.dto.EduSchoolStationRespDTO;
import cn.iocoder.yudao.module.edu.controller.admin.station.vo.StationSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.station.StationDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.station.StationMapper;
import cn.iocoder.yudao.module.system.api.ip.AreaApi;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_STATION_AREA_NOT_MATCHED;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STATION_DISABLED;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STATION_IN_USE_BY_SCHOOL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

class StationServiceImplTest {

    private StationServiceImpl service;
    private StationMapper stationMapper;
    private SchoolMapper schoolMapper;
    private AreaApi areaApi;

    @BeforeEach
    void setUp() {
        service = new StationServiceImpl();
        stationMapper = mock(StationMapper.class);
        schoolMapper = mock(SchoolMapper.class);
        areaApi = mock(AreaApi.class);
        ReflectionTestUtils.setField(service, "stationMapper", stationMapper);
        ReflectionTestUtils.setField(service, "schoolMapper", schoolMapper);
        ReflectionTestUtils.setField(service, "areaApi", areaApi);
    }

    @Test
    void validateStationBindableShouldRejectDisabledStation() {
        when(stationMapper.selectById(1L)).thenReturn(station(1L, 100L, 1));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.validateStationBindable(1L, 100L));

        assertEquals(STATION_DISABLED.getCode(), exception.getCode());
    }

    @Test
    void validateStationBindableShouldRejectAreaMismatch() {
        when(stationMapper.selectById(1L)).thenReturn(station(1L, 100L, 0));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.validateStationBindable(1L, 200L));

        assertEquals(SCHOOL_STATION_AREA_NOT_MATCHED.getCode(), exception.getCode());
    }

    @Test
    void updateStationShouldRejectDisableWhenSchoolBound() {
        when(stationMapper.selectById(1L)).thenReturn(station(1L, 100L, 0));
        when(schoolMapper.countByStationId(1L)).thenReturn(2L);
        StationSaveReqVO reqVO = new StationSaveReqVO();
        reqVO.setId(1L);
        reqVO.setStationName("梁溪站");
        reqVO.setAreaId(100L);
        reqVO.setStatus(1);
        reqVO.setSort(0);
        reqVO.setStationAddress("测试地址");

        ServiceException exception = assertThrows(ServiceException.class, () -> service.updateStation(reqVO));

        assertEquals(STATION_IN_USE_BY_SCHOOL.getCode(), exception.getCode());
    }

    @Test
    void updateStationShouldAllowNonAreaChangesWhenSchoolBound() {
        when(stationMapper.selectById(1L)).thenReturn(station(1L, 100L, 0));
        when(schoolMapper.countByStationId(1L)).thenReturn(2L);
        StationSaveReqVO reqVO = new StationSaveReqVO();
        reqVO.setId(1L);
        reqVO.setStationName("梁溪站-新");
        reqVO.setAreaId(100L);
        reqVO.setStatus(0);
        reqVO.setSort(9);
        reqVO.setStationAddress("测试新地址");
        reqVO.setContactName("李四");
        reqVO.setContactMobile("13900000000");
        reqVO.setRemark("更新备注");

        service.updateStation(reqVO);

        verify(stationMapper).updateById(org.mockito.ArgumentMatchers.any(StationDO.class));
        verify(areaApi, never()).validateAreaSelectable(100);
    }

    @Test
    void deleteStationShouldRejectWhenSchoolBound() {
        when(stationMapper.selectById(1L)).thenReturn(station(1L, 100L, 0));
        when(schoolMapper.countByStationId(1L)).thenReturn(1L);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.deleteStation(1L));

        assertEquals(STATION_IN_USE_BY_SCHOOL.getCode(), exception.getCode());
    }

    @Test
    void getSchoolStationMapShouldReturnSnapshot() {
        SchoolDO school = new SchoolDO();
        school.setId(10L);
        school.setSchoolName("测试学校");
        school.setAreaId(100L);
        school.setStationId(1L);
        when(schoolMapper.selectList(org.mockito.ArgumentMatchers.<SFunction<SchoolDO, ?>>any(), eq(List.of(10L))))
                .thenReturn(List.of(school));
        when(stationMapper.selectList(org.mockito.ArgumentMatchers.<SFunction<StationDO, ?>>any(), eq(List.of(1L))))
                .thenReturn(List.of(station(1L, 100L, 0)));

        Map<Long, EduSchoolStationRespDTO> result = service.getSchoolStationMap(List.of(10L));

        assertEquals(1, result.size());
        EduSchoolStationRespDTO snapshot = result.get(10L);
        assertEquals("测试学校", snapshot.getSchoolName());
        assertEquals(1L, snapshot.getStationId());
        assertEquals("测试站点", snapshot.getStationName());
    }

    @Test
    void createStationShouldValidateAreaSelectable() {
        StationSaveReqVO reqVO = new StationSaveReqVO();
        reqVO.setStationName("梁溪站");
        reqVO.setAreaId(100L);
        reqVO.setStatus(0);
        reqVO.setSort(0);
        reqVO.setStationAddress("测试地址");

        service.createStation(reqVO);

        verify(areaApi).validateAreaSelectable(100);
    }

    private StationDO station(Long id, Long areaId, Integer status) {
        StationDO station = new StationDO();
        station.setId(id);
        station.setAreaId(areaId);
        station.setStatus(status);
        station.setStationName("测试站点");
        station.setStationAddress("测试地址");
        station.setContactName("张三");
        station.setContactMobile("13800138000");
        return station;
    }
}
