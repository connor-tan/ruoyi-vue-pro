package cn.iocoder.yudao.module.mes.dal.mysql.wm.productreceipt;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productreceipt.MesWmProductReceiptDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 产品收货单明细 Mapper
 */
@Mapper
public interface MesWmProductReceiptDetailMapper extends BaseMapperX<MesWmProductReceiptDetailDO> {

    default List<MesWmProductReceiptDetailDO> selectListByRecptId(Long receiptId) {
        return selectList(MesWmProductReceiptDetailDO::getReceiptId, receiptId);
    }

    default List<MesWmProductReceiptDetailDO> selectListByLineId(Long lineId) {
        return selectList(MesWmProductReceiptDetailDO::getLineId, lineId);
    }

    default int deleteByRecptId(Long receiptId) {
        return delete(MesWmProductReceiptDetailDO::getReceiptId, receiptId);
    }

    default int deleteByLineId(Long lineId) {
        return delete(MesWmProductReceiptDetailDO::getLineId, lineId);
    }

}
