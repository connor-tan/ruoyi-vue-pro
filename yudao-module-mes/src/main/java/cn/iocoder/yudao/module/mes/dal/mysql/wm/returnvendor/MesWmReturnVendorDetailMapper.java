package cn.iocoder.yudao.module.mes.dal.mysql.wm.returnvendor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnvendor.MesWmReturnVendorDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 供应商退货明细 Mapper
 */
@Mapper
public interface MesWmReturnVendorDetailMapper extends BaseMapperX<MesWmReturnVendorDetailDO> {

    default List<MesWmReturnVendorDetailDO> selectListByLineId(Long lineId) {
        return selectList(MesWmReturnVendorDetailDO::getLineId, lineId);
    }

    default List<MesWmReturnVendorDetailDO> selectListByReturnId(Long returnId) {
        return selectList(MesWmReturnVendorDetailDO::getReturnId, returnId);
    }

    default int deleteByReturnId(Long returnId) {
        return delete(MesWmReturnVendorDetailDO::getReturnId, returnId);
    }

    default int deleteByLineId(Long lineId) {
        return delete(MesWmReturnVendorDetailDO::getLineId, lineId);
    }

}
