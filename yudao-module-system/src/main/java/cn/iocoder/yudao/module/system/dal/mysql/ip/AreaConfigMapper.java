package cn.iocoder.yudao.module.system.dal.mysql.ip;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.ip.AreaConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AreaConfigMapper extends BaseMapperX<AreaConfigDO> {

    default AreaConfigDO selectByAreaId(Integer areaId) {
        return selectOne(AreaConfigDO::getAreaId, areaId);
    }

}
