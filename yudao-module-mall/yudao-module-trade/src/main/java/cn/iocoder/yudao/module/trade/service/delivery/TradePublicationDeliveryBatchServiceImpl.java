package cn.iocoder.yudao.module.trade.service.delivery;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchPageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidatePageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidateRespVO;
import cn.iocoder.yudao.module.trade.convert.delivery.TradePublicationDeliveryBatchConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.TradePublicationDeliveryBatchDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.TradePublicationDeliveryBatchItemDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.TradePublicationDeliveryBatchItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.TradePublicationDeliveryBatchMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.redis.no.TradeNoRedisDAO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryBatchStatusEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.service.delivery.bo.TradePublicationDeliveryCandidateItemBO;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderStatusAggregateSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_BATCH_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_DUPLICATE_ORDER_ITEM;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL;

/**
 * 刊物站点发货批次 Service 实现类
 */
@Service
public class TradePublicationDeliveryBatchServiceImpl implements TradePublicationDeliveryBatchService {

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Resource
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Resource
    private TradePublicationDeliveryBatchMapper publicationDeliveryBatchMapper;
    @Resource
    private TradePublicationDeliveryBatchItemMapper publicationDeliveryBatchItemMapper;
    @Resource
    private TradeOrderStatusAggregateSupport statusAggregateSupport;
    @Resource
    private TradeNoRedisDAO tradeNoRedisDAO;

    @Override
    public PageResult<TradePublicationDeliveryCandidateRespVO> getCandidatePage(
            TradePublicationDeliveryCandidatePageReqVO reqVO) {
        IPage<TradePublicationDeliveryCandidateRespVO> page = tradeOrderItemMapper.selectPublicationDeliveryCandidatePage(
                MyBatisUtils.buildPage(reqVO), reqVO, BizSceneEnum.PUBLICATION.getCode(),
                DeliveryTypeEnum.STATION.getType(), TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                TradeOrderRefundStatusEnum.NONE.getStatus(), TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAndDeliver(TradePublicationDeliveryBatchCreateReqVO reqVO, Long operatorUserId) {
        TradePublicationDeliveryCandidatePageReqVO candidateReqVO = buildCandidateReqVO(reqVO);
        List<TradePublicationDeliveryCandidateItemBO> items = tradeOrderItemMapper.selectPublicationDeliveryCandidateItemList(
                candidateReqVO, BizSceneEnum.PUBLICATION.getCode(), DeliveryTypeEnum.STATION.getType(),
                TradeOrderStatusEnum.UNDELIVERED.getStatus(), TradeOrderRefundStatusEnum.NONE.getStatus(),
                TradeOrderStatusEnum.UNDELIVERED.getStatus(), PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        if (CollUtil.isEmpty(items)) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }

        LocalDateTime deliveryTime = LocalDateTime.now();
        TradePublicationDeliveryBatchDO batch = buildBatch(items, operatorUserId, reqVO.getRemark(), deliveryTime);
        publicationDeliveryBatchMapper.insert(batch);

        try {
            publicationDeliveryBatchItemMapper.insertBatch(buildBatchItems(batch.getId(), items));
        } catch (DuplicateKeyException ex) {
            throw exception(PUBLICATION_DELIVERY_DUPLICATE_ORDER_ITEM);
        }

        Set<Long> orderItemIds = convertSet(items, TradePublicationDeliveryCandidateItemBO::getOrderItemId);
        int updatedCount = tradeOrderItemMapper.updatePublicationDeliveryByIds(orderItemIds,
                PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(), PublicationDeliveryStatusEnum.DELIVERED.getStatus(),
                batch.getId(), deliveryTime);
        if (updatedCount != orderItemIds.size()) {
            throw exception(PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL);
        }

        refreshCompletedDeliveries(convertSet(items, TradePublicationDeliveryCandidateItemBO::getDeliveryId), deliveryTime);
        return batch.getId();
    }

    @Override
    public PageResult<TradePublicationDeliveryBatchRespVO> getBatchPage(TradePublicationDeliveryBatchPageReqVO reqVO) {
        return TradePublicationDeliveryBatchConvert.INSTANCE.convertPage(publicationDeliveryBatchMapper.selectPage(reqVO));
    }

    @Override
    public TradePublicationDeliveryBatchRespVO getBatch(Long id) {
        TradePublicationDeliveryBatchDO batch = publicationDeliveryBatchMapper.selectById(id);
        if (batch == null) {
            throw exception(PUBLICATION_DELIVERY_BATCH_NOT_FOUND);
        }
        TradePublicationDeliveryBatchRespVO respVO = TradePublicationDeliveryBatchConvert.INSTANCE.convert(batch);
        respVO.setItems(TradePublicationDeliveryBatchConvert.INSTANCE.convertItemList(
                publicationDeliveryBatchItemMapper.selectListByBatchId(id)));
        return respVO;
    }

    private TradePublicationDeliveryCandidatePageReqVO buildCandidateReqVO(TradePublicationDeliveryBatchCreateReqVO reqVO) {
        return new TradePublicationDeliveryCandidatePageReqVO()
                .setSchoolId(reqVO.getSchoolId())
                .setStationId(reqVO.getStationId())
                .setWindowId(reqVO.getWindowId())
                .setOfferId(reqVO.getOfferId())
                .setOfferSkuId(reqVO.getOfferSkuId())
                .setSkuId(reqVO.getSkuId());
    }

    private TradePublicationDeliveryBatchDO buildBatch(List<TradePublicationDeliveryCandidateItemBO> items,
                                                       Long operatorUserId, String remark, LocalDateTime deliveryTime) {
        TradePublicationDeliveryCandidateItemBO first = items.get(0);
        return new TradePublicationDeliveryBatchDO()
                .setBatchNo(tradeNoRedisDAO.generate(TradeNoRedisDAO.PUBLICATION_DELIVERY_BATCH_NO_PREFIX))
                .setSchoolId(first.getSchoolId())
                .setSchoolNameSnapshot(first.getSchoolNameSnapshot())
                .setStationId(first.getStationId())
                .setStationNameSnapshot(first.getStationNameSnapshot())
                .setWindowId(first.getWindowId())
                .setWindowNameSnapshot(first.getWindowNameSnapshot())
                .setOfferId(first.getOfferId())
                .setOfferSkuId(first.getOfferSkuId())
                .setSkuId(first.getSkuId())
                .setProductNameSnapshot(first.getProductNameSnapshot())
                .setTargetPeriod(first.getTargetPeriod())
                .setTotalCount(items.stream().map(TradePublicationDeliveryCandidateItemBO::getCount)
                        .filter(Objects::nonNull).reduce(0, Integer::sum))
                .setOrderCount(countDistinct(items, TradePublicationDeliveryCandidateItemBO::getOrderId))
                .setStudentCount(countDistinct(items, TradePublicationDeliveryCandidateItemBO::getStudentId))
                .setStatus(PublicationDeliveryBatchStatusEnum.DELIVERED.getStatus())
                .setDeliveryTime(deliveryTime)
                .setOperatorUserId(operatorUserId)
                .setRemark(remark);
    }

    private List<TradePublicationDeliveryBatchItemDO> buildBatchItems(
            Long batchId, List<TradePublicationDeliveryCandidateItemBO> items) {
        return convertList(items, item -> new TradePublicationDeliveryBatchItemDO()
                .setBatchId(batchId)
                .setOrderId(item.getOrderId())
                .setOrderNo(item.getOrderNo())
                .setOrderItemId(item.getOrderItemId())
                .setDeliveryId(item.getDeliveryId())
                .setUserId(item.getUserId())
                .setCount(item.getCount())
                .setStudentId(item.getStudentId())
                .setStudentNameSnapshot(item.getStudentNameSnapshot())
                .setClassId(item.getClassId())
                .setClassNameSnapshot(item.getClassNameSnapshot()));
    }

    private void refreshCompletedDeliveries(Collection<Long> deliveryIds, LocalDateTime deliveryTime) {
        if (CollUtil.isEmpty(deliveryIds)) {
            return;
        }
        List<TradeOrderDeliveryDO> deliveries = tradeOrderDeliveryMapper.selectByIds(deliveryIds);
        Map<Long, TradeOrderDO> orderMap = tradeOrderMapper.selectByIds(convertSet(deliveries, TradeOrderDeliveryDO::getOrderId))
                .stream().collect(java.util.stream.Collectors.toMap(TradeOrderDO::getId, order -> order));
        for (TradeOrderDeliveryDO delivery : deliveries) {
            Long undeliveredCount = tradeOrderItemMapper.selectUndeliveredCountByDeliveryId(
                    delivery.getId(), PublicationDeliveryStatusEnum.DELIVERED.getStatus());
            if (undeliveredCount != null && undeliveredCount > 0) {
                continue;
            }
            if (!TradeOrderStatusEnum.isUndelivered(delivery.getStatus())) {
                continue;
            }
            int updateCount = tradeOrderDeliveryMapper.updateByIdAndStatus(delivery.getId(),
                    TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                    new TradeOrderDeliveryDO().setStatus(TradeOrderStatusEnum.DELIVERED.getStatus())
                            .setDeliveryTime(deliveryTime));
            if (updateCount == 0) {
                throw exception(PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL);
            }
            TradeOrderDO order = orderMap.get(delivery.getOrderId());
            if (order != null) {
                statusAggregateSupport.refreshOrderStatusByDeliveries(order);
            }
        }
    }

    private <T> int countDistinct(List<TradePublicationDeliveryCandidateItemBO> items,
                                  java.util.function.Function<TradePublicationDeliveryCandidateItemBO, T> mapper) {
        return (int) items.stream().map(mapper).filter(Objects::nonNull).distinct().count();
    }

}
