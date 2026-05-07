package cn.iocoder.yudao.module.trade.dal.mysql.delivery;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.TradePublicationDeliveryBatchItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TradePublicationDeliveryBatchItemMapper extends BaseMapperX<TradePublicationDeliveryBatchItemDO> {

    default List<TradePublicationDeliveryBatchItemDO> selectListByBatchId(Long batchId) {
        return selectList(TradePublicationDeliveryBatchItemDO::getBatchId, batchId);
    }

}
