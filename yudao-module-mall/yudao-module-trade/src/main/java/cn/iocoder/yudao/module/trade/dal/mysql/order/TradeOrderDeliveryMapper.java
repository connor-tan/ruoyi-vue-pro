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

    default List<TradeOrderDeliveryDO> selectListByOrderIdAndDeliveryType(Long orderId, Integer deliveryType) {
        return selectList(new LambdaQueryWrapperX<TradeOrderDeliveryDO>()
                .eq(TradeOrderDeliveryDO::getOrderId, orderId)
                .eq(TradeOrderDeliveryDO::getDeliveryType, deliveryType));
    }

    default TradeOrderDeliveryDO selectOneByPickUpVerifyCode(String pickUpVerifyCode) {
        return selectOne(new LambdaQueryWrapperX<TradeOrderDeliveryDO>()
                .eq(TradeOrderDeliveryDO::getPickUpVerifyCode, pickUpVerifyCode));
    }

    default List<TradeOrderDeliveryDO> selectListByAdminFilter(Integer deliveryType, Long logisticsId,
                                                               Collection<Long> pickUpStoreIds,
                                                               String pickUpVerifyCode) {
        return selectList(new LambdaQueryWrapperX<TradeOrderDeliveryDO>()
                .eqIfPresent(TradeOrderDeliveryDO::getDeliveryType, deliveryType)
                .eqIfPresent(TradeOrderDeliveryDO::getLogisticsId, logisticsId)
                .inIfPresent(TradeOrderDeliveryDO::getPickUpStoreId, pickUpStoreIds)
                .likeIfPresent(TradeOrderDeliveryDO::getPickUpVerifyCode, pickUpVerifyCode));
    }

}
