package cn.iocoder.yudao.module.edu.dal.mysql.station;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.station.vo.StationPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.station.StationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StationMapper extends BaseMapperX<StationDO> {

    default PageResult<StationDO> selectPage(StationPageReqVO reqVO, List<Long> areaIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StationDO>()
                .likeIfPresent(StationDO::getStationName, reqVO.getStationName())
                .inIfPresent(StationDO::getAreaId, areaIds)
                .eqIfPresent(StationDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(StationDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(StationDO::getSort)
                .orderByDesc(StationDO::getId));
    }

    default StationDO selectByAreaIdAndStationName(Long areaId, String stationName) {
        return selectOne(new LambdaQueryWrapperX<StationDO>()
                .eq(StationDO::getAreaId, areaId)
                .eq(StationDO::getStationName, stationName)
                .last("LIMIT 1"));
    }

    default List<StationDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<StationDO>()
                .eqIfPresent(StationDO::getStatus, status)
                .orderByAsc(StationDO::getSort)
                .orderByDesc(StationDO::getId));
    }
}
