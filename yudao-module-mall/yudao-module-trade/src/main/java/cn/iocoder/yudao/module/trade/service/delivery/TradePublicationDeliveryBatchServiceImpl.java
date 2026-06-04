package cn.iocoder.yudao.module.trade.service.delivery;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.RepoPublicationReceiptService;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptBalanceBO;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptBalanceKey;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptDeliveryAllocateReqBO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchGroupCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchGroupCreateRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchPageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidateChildReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidateGroupPageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidateGroupRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidateItemRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidatePageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidateRespVO;
import cn.iocoder.yudao.module.trade.convert.delivery.TradePublicationDeliveryBatchConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.TradePublicationDeliveryBatchDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.TradePublicationDeliveryBatchItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.TradePublicationDeliveryBatchItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.TradePublicationDeliveryBatchMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderPublicationIssueMapper;
import cn.iocoder.yudao.module.trade.dal.redis.no.TradeNoRedisDAO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryBatchStatusEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.service.delivery.bo.TradePublicationDeliveryCandidateItemBO;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderPublicationIssueService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_BATCH_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_GROUP_DELIVERY_EXPRESS_NOT_SUPPORTED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_EXPRESS_LOGISTICS_REQUIRED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_EXPRESS_BATCH_TOO_LARGE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_ISSUE_DELIVERY_DUPLICATE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL;

/**
 * 刊物学校配送发货批次 Service 实现类
 */
@Service
public class TradePublicationDeliveryBatchServiceImpl implements TradePublicationDeliveryBatchService {

    private static final int EXPRESS_BATCH_ITEM_LIMIT = 500;

    @Resource
    private TradeOrderPublicationIssueMapper publicationIssueMapper;
    @Resource
    private TradePublicationDeliveryBatchMapper publicationDeliveryBatchMapper;
    @Resource
    private TradePublicationDeliveryBatchItemMapper publicationDeliveryBatchItemMapper;
    @Resource
    private TradeNoRedisDAO tradeNoRedisDAO;
    @Resource
    private DeliveryExpressService deliveryExpressService;
    @Resource
    private TradeOrderPublicationIssueService publicationIssueService;
    @Resource
    private RepoPublicationReceiptService publicationReceiptService;

    @Override
    public PageResult<TradePublicationDeliveryCandidateRespVO> getCandidatePage(
            TradePublicationDeliveryCandidatePageReqVO reqVO) {
        IPage<TradePublicationDeliveryCandidateRespVO> page = publicationIssueMapper.selectPublicationDeliveryCandidatePage(
                MyBatisUtils.buildPage(reqVO), reqVO, TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        fillCandidateBalances(page.getRecords());
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public PageResult<TradePublicationDeliveryCandidateGroupRespVO> getCandidateGroupPage(
            TradePublicationDeliveryCandidateGroupPageReqVO reqVO) {
        IPage<TradePublicationDeliveryCandidateGroupRespVO> page = publicationIssueMapper
                .selectPublicationDeliveryCandidateGroupPage(MyBatisUtils.buildPage(reqVO), reqVO,
                        TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                        PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        fillGroupBalances(page.getRecords());
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<TradePublicationDeliveryCandidateRespVO> getCandidateChildList(
            TradePublicationDeliveryCandidateChildReqVO reqVO) {
        validateGroupScopeReq(reqVO);
        List<TradePublicationDeliveryCandidateRespVO> list = publicationIssueMapper.selectPublicationDeliveryCandidateChildList(reqVO,
                TradeOrderStatusEnum.UNDELIVERED.getStatus(), PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        fillCandidateBalances(list);
        return list;
    }

    @Override
    public PageResult<TradePublicationDeliveryCandidateRespVO> getCandidateChildPage(
            TradePublicationDeliveryCandidateChildReqVO reqVO) {
        validateGroupScopeReq(reqVO);
        IPage<TradePublicationDeliveryCandidateRespVO> page = publicationIssueMapper
                .selectPublicationDeliveryCandidateChildPage(MyBatisUtils.buildPage(reqVO), reqVO,
                        TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                        PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        fillCandidateBalances(page.getRecords());
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<TradePublicationDeliveryCandidateItemRespVO> getCandidateItemList(
            TradePublicationDeliveryCandidatePageReqVO reqVO) {
        List<TradePublicationDeliveryCandidateItemBO> items = publicationIssueMapper.selectPublicationDeliveryCandidateItemList(
                reqVO, TradeOrderStatusEnum.UNDELIVERED.getStatus(), PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(),
                buildCandidateItemLimit(reqVO.getDeliveryType()));
        validateExpressBatchSize(reqVO.getDeliveryType(), items);
        return BeanUtils.toBean(items, TradePublicationDeliveryCandidateItemRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAndDeliver(TradePublicationDeliveryBatchCreateReqVO reqVO, Long operatorUserId) {
        return createAndDeliverInternal(reqVO, operatorUserId, LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradePublicationDeliveryBatchGroupCreateRespVO createGroupAndDeliver(
            TradePublicationDeliveryBatchGroupCreateReqVO reqVO, Long operatorUserId) {
        validateGroupCreateReq(reqVO);
        List<TradePublicationDeliveryCandidateRespVO> children = publicationIssueMapper
                .selectPublicationDeliveryCandidateChildList(reqVO, TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                        PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        if (CollUtil.isEmpty(children)) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }
        LocalDateTime deliveryTime = LocalDateTime.now();
        List<Long> batchIds = new ArrayList<>(children.size());
        int totalCount = 0;
        for (TradePublicationDeliveryCandidateRespVO child : children) {
            Long batchId = createAndDeliverInternal(buildCreateReqVO(child, reqVO.getRemark()),
                    operatorUserId, deliveryTime);
            batchIds.add(batchId);
            totalCount += child.getTotalCount() == null ? 0 : child.getTotalCount();
        }
        return new TradePublicationDeliveryBatchGroupCreateRespVO()
                .setBatchCount(batchIds.size())
                .setBatchIds(batchIds)
                .setTotalCount(totalCount);
    }

    private Long createAndDeliverInternal(TradePublicationDeliveryBatchCreateReqVO reqVO, Long operatorUserId,
                                          LocalDateTime deliveryTime) {
        TradePublicationDeliveryCandidatePageReqVO candidateReqVO = buildCandidateReqVO(reqVO);
        validateCreateReq(reqVO);
        List<TradePublicationDeliveryCandidateItemBO> items = publicationIssueMapper.selectPublicationDeliveryCandidateItemList(
                candidateReqVO, TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(), buildCandidateItemLimit(reqVO.getDeliveryType()));
        if (CollUtil.isEmpty(items)) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }
        validateExpressBatchSize(reqVO.getDeliveryType(), items);
        Map<Long, TradePublicationDeliveryBatchCreateReqVO.ExpressItem> expressItemMap = buildExpressItemMap(reqVO, items);

        TradePublicationDeliveryBatchDO batch = buildBatch(items, operatorUserId, reqVO.getRemark(), deliveryTime);
        publicationDeliveryBatchMapper.insert(batch);

        try {
            publicationDeliveryBatchItemMapper.insertBatch(buildBatchItems(batch.getId(), items, expressItemMap));
        } catch (DuplicateKeyException ex) {
            throw exception(PUBLICATION_ISSUE_DELIVERY_DUPLICATE);
        }

        allocateReceiptBalance(batch, items, operatorUserId, deliveryTime);
        Set<Long> orderIssueIds = convertSet(items, TradePublicationDeliveryCandidateItemBO::getOrderIssueId);
        updateIssueDelivered(batch.getId(), items, expressItemMap, deliveryTime);
        publicationIssueService.afterIssueDelivered(orderIssueIds, deliveryTime);
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
                .setWarehouseId(reqVO.getWarehouseId())
                .setDeliveryType(reqVO.getDeliveryType())
                .setWindowId(reqVO.getWindowId())
                .setOfferId(reqVO.getOfferId())
                .setOfferSkuId(reqVO.getOfferSkuId())
                .setSkuId(reqVO.getSkuId())
                .setIssueId(reqVO.getIssueId())
                .setIssueNo(reqVO.getIssueNo());
    }

    private TradePublicationDeliveryBatchCreateReqVO buildCreateReqVO(TradePublicationDeliveryCandidateRespVO child,
                                                                      String remark) {
        return new TradePublicationDeliveryBatchCreateReqVO()
                .setSchoolId(child.getSchoolId())
                .setWarehouseId(child.getWarehouseId())
                .setDeliveryType(child.getDeliveryType())
                .setWindowId(child.getWindowId())
                .setOfferId(child.getOfferId())
                .setOfferSkuId(child.getOfferSkuId())
                .setSkuId(child.getSkuId())
                .setIssueId(child.getIssueId())
                .setIssueNo(child.getIssueNo())
                .setRemark(remark);
    }

    private void validateGroupScopeReq(TradePublicationDeliveryCandidatePageReqVO reqVO) {
        if (reqVO.getDeliveryType() == null || reqVO.getSchoolId() == null || reqVO.getWindowId() == null) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }
        if (Objects.equals(reqVO.getDeliveryType(), DeliveryTypeEnum.SCHOOL.getType())) {
            if (reqVO.getWarehouseId() == null) {
                throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
            }
            return;
        }
        if (!Objects.equals(reqVO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }
    }

    private void validateGroupCreateReq(TradePublicationDeliveryBatchGroupCreateReqVO reqVO) {
        validateGroupScopeReq(reqVO);
        if (Objects.equals(reqVO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            throw exception(PUBLICATION_GROUP_DELIVERY_EXPRESS_NOT_SUPPORTED);
        }
    }

    private TradePublicationDeliveryBatchDO buildBatch(List<TradePublicationDeliveryCandidateItemBO> items,
                                                       Long operatorUserId, String remark, LocalDateTime deliveryTime) {
        TradePublicationDeliveryCandidateItemBO first = items.get(0);
        return new TradePublicationDeliveryBatchDO()
                .setBatchNo(tradeNoRedisDAO.generate(TradeNoRedisDAO.PUBLICATION_DELIVERY_BATCH_NO_PREFIX))
                .setDeliveryType(first.getDeliveryType())
                .setSchoolId(first.getSchoolId())
                .setSchoolNameSnapshot(first.getSchoolNameSnapshot())
                .setWarehouseId(first.getWarehouseId())
                .setWarehouseNameSnapshot(first.getWarehouseNameSnapshot())
                .setWindowId(first.getWindowId())
                .setWindowNameSnapshot(first.getWindowNameSnapshot())
                .setOfferId(first.getOfferId())
                .setOfferSkuId(first.getOfferSkuId())
                .setSkuId(first.getSkuId())
                .setProductNameSnapshot(first.getProductNameSnapshot())
                .setIssueId(first.getIssueId())
                .setIssueNo(first.getIssueNo())
                .setIssueName(first.getIssueName())
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
            Long batchId, List<TradePublicationDeliveryCandidateItemBO> items,
            Map<Long, TradePublicationDeliveryBatchCreateReqVO.ExpressItem> expressItemMap) {
        return convertList(items, item -> new TradePublicationDeliveryBatchItemDO()
                .setBatchId(batchId)
                .setOrderId(item.getOrderId())
                .setOrderNo(item.getOrderNo())
                .setOrderItemId(item.getOrderItemId())
                .setOrderIssueId(item.getOrderIssueId())
                .setDeliveryId(item.getDeliveryId())
                .setUserId(item.getUserId())
                .setCount(item.getCount())
                .setIssueNo(item.getIssueNo())
                .setIssueName(item.getIssueName())
                .setLogisticsId(expressItemMap == null || expressItemMap.get(item.getOrderIssueId()) == null
                        ? null : expressItemMap.get(item.getOrderIssueId()).getLogisticsId())
                .setLogisticsNo(expressItemMap == null || expressItemMap.get(item.getOrderIssueId()) == null
                        ? null : expressItemMap.get(item.getOrderIssueId()).getLogisticsNo())
                .setStudentId(item.getStudentId())
                .setStudentNameSnapshot(item.getStudentNameSnapshot())
                .setClassId(item.getClassId())
                .setClassNameSnapshot(item.getClassNameSnapshot()));
    }

    private void validateCreateReq(TradePublicationDeliveryBatchCreateReqVO reqVO) {
        if (Objects.equals(reqVO.getDeliveryType(), DeliveryTypeEnum.SCHOOL.getType()) && reqVO.getWarehouseId() == null) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }
        if (Objects.equals(reqVO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            if (CollUtil.isEmpty(reqVO.getExpressItems())) {
                throw exception(PUBLICATION_EXPRESS_LOGISTICS_REQUIRED);
            }
            for (TradePublicationDeliveryBatchCreateReqVO.ExpressItem item : reqVO.getExpressItems()) {
                if (item.getLogisticsId() == null || StrUtil.isBlank(item.getLogisticsNo())) {
                    throw exception(PUBLICATION_EXPRESS_LOGISTICS_REQUIRED);
                }
                deliveryExpressService.validateDeliveryExpress(item.getLogisticsId());
            }
            return;
        }
        if (!Objects.equals(reqVO.getDeliveryType(), DeliveryTypeEnum.SCHOOL.getType())) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }
    }

    private Integer buildCandidateItemLimit(Integer deliveryType) {
        return Objects.equals(deliveryType, DeliveryTypeEnum.EXPRESS.getType()) ? EXPRESS_BATCH_ITEM_LIMIT + 1 : null;
    }

    private void fillCandidateBalances(List<TradePublicationDeliveryCandidateRespVO> candidates) {
        if (CollUtil.isEmpty(candidates)) {
            return;
        }
        Map<RepoPublicationReceiptBalanceKey, RepoPublicationReceiptBalanceBO> balanceMap =
                publicationReceiptService.getBalanceMap(convertList(candidates, this::buildBalanceKey));
        candidates.forEach(candidate -> fillCandidateBalance(candidate, balanceMap.get(buildBalanceKey(candidate))));
    }

    private void fillGroupBalances(List<TradePublicationDeliveryCandidateGroupRespVO> groups) {
        if (CollUtil.isEmpty(groups)) {
            return;
        }
        for (TradePublicationDeliveryCandidateGroupRespVO group : groups) {
            TradePublicationDeliveryCandidateChildReqVO childReqVO = new TradePublicationDeliveryCandidateChildReqVO();
            childReqVO.setDeliveryType(group.getDeliveryType())
                    .setSchoolId(group.getSchoolId())
                    .setWarehouseId(group.getWarehouseId())
                    .setWindowId(group.getWindowId());
            List<TradePublicationDeliveryCandidateRespVO> children = publicationIssueMapper
                    .selectPublicationDeliveryCandidateChildList(childReqVO, TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                            PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
            fillCandidateBalances(children);
            group.setReceivedCount(sum(children, TradePublicationDeliveryCandidateRespVO::getReceivedCount))
                    .setAllocatedCount(sum(children, TradePublicationDeliveryCandidateRespVO::getAllocatedCount))
                    .setAvailableCount(sum(children, TradePublicationDeliveryCandidateRespVO::getAvailableCount))
                    .setShortageCount(sum(children, TradePublicationDeliveryCandidateRespVO::getShortageCount));
        }
    }

    private void fillCandidateBalance(TradePublicationDeliveryCandidateRespVO candidate,
                                      RepoPublicationReceiptBalanceBO balance) {
        int receivedCount = balance == null ? 0 : defaultZero(balance.getReceivedCount());
        int allocatedCount = balance == null ? 0 : defaultZero(balance.getAllocatedCount());
        int availableCount = balance == null ? 0 : defaultZero(balance.getAvailableCount());
        candidate.setReceivedCount(receivedCount)
                .setAllocatedCount(allocatedCount)
                .setAvailableCount(availableCount)
                .setShortageCount(Math.max(defaultZero(candidate.getTotalCount()) - availableCount, 0));
    }

    private RepoPublicationReceiptBalanceKey buildBalanceKey(TradePublicationDeliveryCandidateRespVO candidate) {
        return new RepoPublicationReceiptBalanceKey()
                .setWarehouseId(candidate.getWarehouseId())
                .setWindowId(candidate.getWindowId())
                .setOfferId(candidate.getOfferId())
                .setOfferSkuId(candidate.getOfferSkuId())
                .setSkuId(candidate.getSkuId())
                .setIssueId(candidate.getIssueId())
                .setIssueNo(candidate.getIssueNo());
    }

    private RepoPublicationReceiptBalanceKey buildBalanceKey(TradePublicationDeliveryCandidateItemBO item) {
        return new RepoPublicationReceiptBalanceKey()
                .setWarehouseId(item.getWarehouseId())
                .setWindowId(item.getWindowId())
                .setOfferId(item.getOfferId())
                .setOfferSkuId(item.getOfferSkuId())
                .setSkuId(item.getSkuId())
                .setIssueId(item.getIssueId())
                .setIssueNo(item.getIssueNo());
    }

    private void allocateReceiptBalance(TradePublicationDeliveryBatchDO batch,
                                        List<TradePublicationDeliveryCandidateItemBO> items,
                                        Long operatorUserId,
                                        LocalDateTime deliveryTime) {
        publicationReceiptService.allocateDeliveryBatch(new RepoPublicationReceiptDeliveryAllocateReqBO()
                .setDeliveryBatchId(batch.getId())
                .setOperatorUserId(operatorUserId)
                .setDeliveryTime(deliveryTime)
                .setKey(buildBalanceKey(items.get(0)))
                .setCount(batch.getTotalCount())
                .setRemark(batch.getRemark()));
    }

    private void validateExpressBatchSize(Integer deliveryType, List<TradePublicationDeliveryCandidateItemBO> items) {
        if (Objects.equals(deliveryType, DeliveryTypeEnum.EXPRESS.getType()) && items.size() > EXPRESS_BATCH_ITEM_LIMIT) {
            throw exception(PUBLICATION_EXPRESS_BATCH_TOO_LARGE, EXPRESS_BATCH_ITEM_LIMIT);
        }
    }

    private Map<Long, TradePublicationDeliveryBatchCreateReqVO.ExpressItem> buildExpressItemMap(
            TradePublicationDeliveryBatchCreateReqVO reqVO, List<TradePublicationDeliveryCandidateItemBO> items) {
        if (!Objects.equals(reqVO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            return null;
        }
        Map<Long, TradePublicationDeliveryBatchCreateReqVO.ExpressItem> expressItemMap = convertMap(
                reqVO.getExpressItems(), TradePublicationDeliveryBatchCreateReqVO.ExpressItem::getOrderIssueId,
                Function.identity());
        Set<Long> orderIssueIds = convertSet(items, TradePublicationDeliveryCandidateItemBO::getOrderIssueId);
        if (!expressItemMap.keySet().containsAll(orderIssueIds) || expressItemMap.size() != orderIssueIds.size()) {
            throw exception(PUBLICATION_EXPRESS_LOGISTICS_REQUIRED);
        }
        return expressItemMap;
    }

    private void updateIssueDelivered(Long batchId, List<TradePublicationDeliveryCandidateItemBO> items,
                                      Map<Long, TradePublicationDeliveryBatchCreateReqVO.ExpressItem> expressItemMap,
                                      LocalDateTime deliveryTime) {
        if (Objects.equals(items.get(0).getDeliveryType(), DeliveryTypeEnum.SCHOOL.getType())) {
            int updatedCount = publicationIssueMapper.updateDeliveredByIds(
                    convertSet(items, TradePublicationDeliveryCandidateItemBO::getOrderIssueId),
                    PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(), batchId, deliveryTime);
            if (updatedCount != items.size()) {
                throw exception(PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL);
            }
            return;
        }
        int updatedCount = 0;
        for (TradePublicationDeliveryCandidateItemBO item : items) {
            TradePublicationDeliveryBatchCreateReqVO.ExpressItem expressItem = expressItemMap.get(item.getOrderIssueId());
            updatedCount += publicationIssueMapper.updateDeliveredById(item.getOrderIssueId(),
                    PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(), batchId, deliveryTime,
                    expressItem.getLogisticsId(), expressItem.getLogisticsNo());
        }
        if (updatedCount != items.size()) {
            throw exception(PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL);
        }
    }

    private <T> int countDistinct(List<TradePublicationDeliveryCandidateItemBO> items,
                                  java.util.function.Function<TradePublicationDeliveryCandidateItemBO, T> mapper) {
        return (int) items.stream().map(mapper).filter(Objects::nonNull).distinct().count();
    }

    private <T> int sum(Collection<T> items, java.util.function.Function<T, Integer> mapper) {
        return items.stream().map(mapper).filter(Objects::nonNull).reduce(0, Integer::sum);
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

}
