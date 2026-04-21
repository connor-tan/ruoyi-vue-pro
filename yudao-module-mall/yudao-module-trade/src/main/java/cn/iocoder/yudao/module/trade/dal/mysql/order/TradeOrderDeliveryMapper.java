package cn.iocoder.yudao.module.trade.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface TradeOrderDeliveryMapper extends BaseMapperX<TradeOrderDeliveryDO> {

    default List<TradeOrderDeliveryDO> selectListByOrderId(Long orderId) {
        return selectList(TradeOrderDeliveryDO::getOrderId, orderId);
    }

    default List<TradeOrderDeliveryDO> selectListByOrderId(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(TradeOrderDeliveryDO::getOrderId, orderIds);
    }

    default int updateByIdAndStatus(Long id, Integer status, TradeOrderDeliveryDO update) {
        return update(update, new LambdaUpdateWrapper<TradeOrderDeliveryDO>()
                .eq(TradeOrderDeliveryDO::getId, id)
                .eq(TradeOrderDeliveryDO::getStatus, status));
    }

    default TradeOrderDeliveryDO selectByIdAndOrderId(Long id, Long orderId) {
        return selectOne(new LambdaQueryWrapperX<TradeOrderDeliveryDO>()
                .eq(TradeOrderDeliveryDO::getId, id)
                .eq(TradeOrderDeliveryDO::getOrderId, orderId));
    }
}
