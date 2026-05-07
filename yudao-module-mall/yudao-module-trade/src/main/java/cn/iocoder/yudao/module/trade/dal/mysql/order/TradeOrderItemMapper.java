package cn.iocoder.yudao.module.trade.dal.mysql.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidatePageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidateRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.service.delivery.bo.TradePublicationDeliveryCandidateItemBO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface TradeOrderItemMapper extends BaseMapperX<TradeOrderItemDO> {

    default int updateAfterSaleStatus(Long id, Integer oldAfterSaleStatus, Integer newAfterSaleStatus,
                                      Long afterSaleId) {
        return update(new TradeOrderItemDO().setAfterSaleStatus(newAfterSaleStatus).setAfterSaleId(afterSaleId),
                new LambdaUpdateWrapper<>(new TradeOrderItemDO().setId(id).setAfterSaleStatus(oldAfterSaleStatus)));
    }

    default List<TradeOrderItemDO> selectListByOrderId(Long orderId) {
        return selectList(TradeOrderItemDO::getOrderId, orderId);
    }

    default List<TradeOrderItemDO> selectListByOrderId(Collection<Long> orderIds) {
        return selectList(TradeOrderItemDO::getOrderId, orderIds);
    }

    default TradeOrderItemDO selectByIdAndUserId(Long orderItemId, Long loginUserId) {
        return selectOne(new LambdaQueryWrapperX<TradeOrderItemDO>()
                .eq(TradeOrderItemDO::getId, orderItemId)
                .eq(TradeOrderItemDO::getUserId, loginUserId));
    }

    default List<TradeOrderItemDO> selectListByOrderIdAndCommentStatus(Long orderId, Boolean commentStatus) {
        return selectList(new LambdaQueryWrapperX<TradeOrderItemDO>()
                .eq(TradeOrderItemDO::getOrderId, orderId)
                .eq(TradeOrderItemDO::getCommentStatus, commentStatus));
    }

    default int selectProductSumByOrderId(@Param("orderIds") Set<Long> orderIds) {
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<TradeOrderItemDO>()
                .select("SUM(count) AS sumCount")
                .in("order_id", orderIds)); // 只计算选中的
        // 获得数量
        return CollUtil.getFirst(result) != null ? MapUtil.getInt(result.get(0), "sumCount") : 0;
    }

    Integer selectEffectiveSubscriptionOrderItemQuantity(@Param("userId") Long userId,
                                                         @Param("studentId") Long studentId,
                                                         @Param("offerSkuId") Long offerSkuId,
                                                         @Param("canceledStatus") Integer canceledStatus);

    IPage<TradePublicationDeliveryCandidateRespVO> selectPublicationDeliveryCandidatePage(
            IPage<?> page,
            @Param("reqVO") TradePublicationDeliveryCandidatePageReqVO reqVO,
            @Param("bizScene") String bizScene,
            @Param("deliveryType") Integer deliveryType,
            @Param("orderStatus") Integer orderStatus,
            @Param("refundStatus") Integer refundStatus,
            @Param("deliveryStatus") Integer deliveryStatus,
            @Param("publicationDeliveryStatus") Integer publicationDeliveryStatus);

    List<TradePublicationDeliveryCandidateItemBO> selectPublicationDeliveryCandidateItemList(
            @Param("reqVO") TradePublicationDeliveryCandidatePageReqVO reqVO,
            @Param("bizScene") String bizScene,
            @Param("deliveryType") Integer deliveryType,
            @Param("orderStatus") Integer orderStatus,
            @Param("refundStatus") Integer refundStatus,
            @Param("deliveryStatus") Integer deliveryStatus,
            @Param("publicationDeliveryStatus") Integer publicationDeliveryStatus);

    Long selectUndeliveredCountByDeliveryId(@Param("deliveryId") Long deliveryId,
                                            @Param("publicationDeliveryStatus") Integer publicationDeliveryStatus);

    default int updatePublicationDeliveryByIds(Collection<Long> ids, Integer oldStatus, Integer newStatus,
                                               Long batchId, LocalDateTime deliveryTime) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        return update(new TradeOrderItemDO().setPublicationDeliveryStatus(newStatus)
                        .setPublicationDeliveryBatchId(batchId).setPublicationDeliveryTime(deliveryTime),
                new LambdaUpdateWrapper<TradeOrderItemDO>()
                        .in(TradeOrderItemDO::getId, ids)
                        .and(wrapper -> wrapper.eq(TradeOrderItemDO::getPublicationDeliveryStatus, oldStatus)
                                .or().isNull(TradeOrderItemDO::getPublicationDeliveryStatus)));
    }

}
