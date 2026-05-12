package cn.iocoder.yudao.module.trade.dal.mysql.order;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidatePageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidateRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderPublicationIssueDO;
import cn.iocoder.yudao.module.trade.service.delivery.bo.TradePublicationDeliveryCandidateItemBO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface TradeOrderPublicationIssueMapper extends BaseMapperX<TradeOrderPublicationIssueDO> {

    default List<TradeOrderPublicationIssueDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<TradeOrderPublicationIssueDO>()
                .eq(TradeOrderPublicationIssueDO::getOrderId, orderId)
                .orderByAsc(TradeOrderPublicationIssueDO::getOrderItemId)
                .orderByAsc(TradeOrderPublicationIssueDO::getIssueNo));
    }

    default List<TradeOrderPublicationIssueDO> selectListByOrderIds(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<TradeOrderPublicationIssueDO>()
                .in(TradeOrderPublicationIssueDO::getOrderId, orderIds)
                .orderByAsc(TradeOrderPublicationIssueDO::getOrderItemId)
                .orderByAsc(TradeOrderPublicationIssueDO::getIssueNo));
    }

    default List<TradeOrderPublicationIssueDO> selectListByOrderItemIds(Collection<Long> orderItemIds) {
        if (CollUtil.isEmpty(orderItemIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<TradeOrderPublicationIssueDO>()
                .in(TradeOrderPublicationIssueDO::getOrderItemId, orderItemIds)
                .orderByAsc(TradeOrderPublicationIssueDO::getOrderItemId)
                .orderByAsc(TradeOrderPublicationIssueDO::getIssueNo));
    }

    default TradeOrderPublicationIssueDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(new LambdaQueryWrapperX<TradeOrderPublicationIssueDO>()
                .eq(TradeOrderPublicationIssueDO::getId, id)
                .eq(TradeOrderPublicationIssueDO::getUserId, userId));
    }

    IPage<TradePublicationDeliveryCandidateRespVO> selectPublicationDeliveryCandidatePage(
            IPage<?> page,
            @Param("reqVO") TradePublicationDeliveryCandidatePageReqVO reqVO,
            @Param("orderStatus") Integer orderStatus,
            @Param("publicationDeliveryStatus") Integer publicationDeliveryStatus);

    List<TradePublicationDeliveryCandidateItemBO> selectPublicationDeliveryCandidateItemList(
            @Param("reqVO") TradePublicationDeliveryCandidatePageReqVO reqVO,
            @Param("orderStatus") Integer orderStatus,
            @Param("publicationDeliveryStatus") Integer publicationDeliveryStatus,
            @Param("limit") Integer limit);

    Long selectNotDeliveredCountByDeliveryId(@Param("deliveryId") Long deliveryId,
                                             @Param("deliveredStatus") Integer deliveredStatus);

    Long selectNotReceivedCountByDeliveryId(@Param("deliveryId") Long deliveryId,
                                            @Param("receivedStatus") Integer receivedStatus);

    List<TradeOrderPublicationIssueDO> selectDeliveredUnreceivedListByDeliveryId(
            @Param("deliveryId") Long deliveryId,
            @Param("deliveredStatus") Integer deliveredStatus,
            @Param("unreceivedStatus") Integer unreceivedStatus);

    List<TradeOrderPublicationIssueDO> selectAutoReceiveList(@Param("deliveredStatus") Integer deliveredStatus,
                                                             @Param("unreceivedStatus") Integer unreceivedStatus,
                                                             @Param("expireTime") LocalDateTime expireTime);

    default int updateDeliveredByIds(Collection<Long> ids, Integer oldStatus, Long batchId, LocalDateTime deliveryTime) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        return update(new TradeOrderPublicationIssueDO()
                        .setDeliveryStatus(cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryStatusEnum.DELIVERED.getStatus())
                        .setDeliveryBatchId(batchId)
                        .setDeliveryTime(deliveryTime),
                new LambdaUpdateWrapper<TradeOrderPublicationIssueDO>()
                        .in(TradeOrderPublicationIssueDO::getId, ids)
                        .eq(TradeOrderPublicationIssueDO::getDeliveryStatus, oldStatus)
                        .eq(TradeOrderPublicationIssueDO::getCanceled, false));
    }

    default int updateDeliveredById(Long id, Integer oldStatus, Long batchId, LocalDateTime deliveryTime,
                                    Long logisticsId, String logisticsNo) {
        return update(new TradeOrderPublicationIssueDO()
                        .setDeliveryStatus(cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryStatusEnum.DELIVERED.getStatus())
                        .setDeliveryBatchId(batchId)
                        .setDeliveryTime(deliveryTime)
                        .setLogisticsId(logisticsId)
                        .setLogisticsNo(logisticsNo),
                new LambdaUpdateWrapper<TradeOrderPublicationIssueDO>()
                        .eq(TradeOrderPublicationIssueDO::getId, id)
                        .eq(TradeOrderPublicationIssueDO::getDeliveryStatus, oldStatus)
                        .eq(TradeOrderPublicationIssueDO::getCanceled, false));
    }

    default int receiveByIds(Collection<Long> ids, Integer oldReceiveStatus, LocalDateTime receiveTime,
                             Long receiverUserId) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        return update(new TradeOrderPublicationIssueDO()
                        .setReceiveStatus(cn.iocoder.yudao.module.trade.enums.delivery.PublicationReceiveStatusEnum.RECEIVED.getStatus())
                        .setReceiveTime(receiveTime)
                        .setReceiverUserId(receiverUserId),
                new LambdaUpdateWrapper<TradeOrderPublicationIssueDO>()
                        .in(TradeOrderPublicationIssueDO::getId, ids)
                        .eq(TradeOrderPublicationIssueDO::getReceiveStatus, oldReceiveStatus)
                        .eq(TradeOrderPublicationIssueDO::getCanceled, false));
    }

    default int cancelUnfinishedByOrderItemId(Long orderItemId, Integer deliveredStatus, Integer receivedStatus) {
        return update(new TradeOrderPublicationIssueDO().setCanceled(true),
                new LambdaUpdateWrapper<TradeOrderPublicationIssueDO>()
                        .eq(TradeOrderPublicationIssueDO::getOrderItemId, orderItemId)
                        .eq(TradeOrderPublicationIssueDO::getCanceled, false)
                        .and(wrapper -> wrapper.ne(TradeOrderPublicationIssueDO::getDeliveryStatus, deliveredStatus)
                                .or().ne(TradeOrderPublicationIssueDO::getReceiveStatus, receivedStatus)));
    }

    default int cancelByOrderId(Long orderId) {
        return update(new TradeOrderPublicationIssueDO().setCanceled(true),
                new LambdaUpdateWrapper<TradeOrderPublicationIssueDO>()
                        .eq(TradeOrderPublicationIssueDO::getOrderId, orderId)
                        .eq(TradeOrderPublicationIssueDO::getCanceled, false));
    }

}
