package cn.iocoder.yudao.module.repo.service.publicationdelivery;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchCreateReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchGroupCreateReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchGroupCreateRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidateChildReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidateGroupRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidateItemRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidatePageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidateRespVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationdelivery.RepoPublicationDeliveryBatchDO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationdelivery.RepoPublicationDeliveryBatchItemDO;
import cn.iocoder.yudao.module.repo.dal.mysql.publicationdelivery.RepoPublicationDeliveryBatchItemMapper;
import cn.iocoder.yudao.module.repo.dal.mysql.publicationdelivery.RepoPublicationDeliveryBatchMapper;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.RepoPublicationReceiptService;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptBalanceBO;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptBalanceKey;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptDeliveryAllocateReqBO;
import cn.iocoder.yudao.module.trade.api.delivery.TradePublicationDeliveryApi;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateGroupRespDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateItemRespDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidatePageReqDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateRespDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryConfirmReqDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCreateReqDTO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryBatchStatusEnum;
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
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_BATCH_NOT_EXISTS;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_GROUP_EXPRESS_NOT_SUPPORTED;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_ISSUE_DUPLICATE;

@Service
public class RepoPublicationDeliveryBatchServiceImpl implements RepoPublicationDeliveryBatchService {

    private static final String PUBLICATION_DELIVERY_BATCH_NO_PREFIX = "PDB";

    @Resource
    private TradePublicationDeliveryApi tradePublicationDeliveryApi;
    @Resource
    private RepoPublicationReceiptService publicationReceiptService;
    @Resource
    private RepoPublicationDeliveryBatchMapper publicationDeliveryBatchMapper;
    @Resource
    private RepoPublicationDeliveryBatchItemMapper publicationDeliveryBatchItemMapper;

    @Override
    public PageResult<RepoPublicationDeliveryCandidateRespVO> getCandidatePage(
            RepoPublicationDeliveryCandidatePageReqVO reqVO) {
        PageResult<TradePublicationDeliveryCandidateRespDTO> page = tradePublicationDeliveryApi.getCandidatePage(
                BeanUtils.toBean(reqVO, TradePublicationDeliveryCandidatePageReqDTO.class));
        List<RepoPublicationDeliveryCandidateRespVO> list = BeanUtils.toBean(page.getList(),
                RepoPublicationDeliveryCandidateRespVO.class);
        fillCandidateBalances(list);
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public PageResult<RepoPublicationDeliveryCandidateGroupRespVO> getCandidateGroupPage(
            RepoPublicationDeliveryCandidatePageReqVO reqVO) {
        PageResult<TradePublicationDeliveryCandidateGroupRespDTO> page = tradePublicationDeliveryApi.getCandidateGroupPage(
                BeanUtils.toBean(reqVO, TradePublicationDeliveryCandidatePageReqDTO.class));
        List<RepoPublicationDeliveryCandidateGroupRespVO> list = BeanUtils.toBean(page.getList(),
                RepoPublicationDeliveryCandidateGroupRespVO.class);
        fillGroupBalances(list);
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public List<RepoPublicationDeliveryCandidateRespVO> getCandidateChildList(
            RepoPublicationDeliveryCandidateChildReqVO reqVO) {
        List<TradePublicationDeliveryCandidateRespDTO> children = tradePublicationDeliveryApi.getCandidateChildList(
                BeanUtils.toBean(reqVO, TradePublicationDeliveryCandidatePageReqDTO.class));
        List<RepoPublicationDeliveryCandidateRespVO> list = BeanUtils.toBean(children,
                RepoPublicationDeliveryCandidateRespVO.class);
        fillCandidateBalances(list);
        return list;
    }

    @Override
    public PageResult<RepoPublicationDeliveryCandidateRespVO> getCandidateChildPage(
            RepoPublicationDeliveryCandidateChildReqVO reqVO) {
        PageResult<TradePublicationDeliveryCandidateRespDTO> page = tradePublicationDeliveryApi.getCandidateChildPage(
                BeanUtils.toBean(reqVO, TradePublicationDeliveryCandidatePageReqDTO.class));
        List<RepoPublicationDeliveryCandidateRespVO> list = BeanUtils.toBean(page.getList(),
                RepoPublicationDeliveryCandidateRespVO.class);
        fillCandidateBalances(list);
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public List<RepoPublicationDeliveryCandidateItemRespVO> getCandidateItemList(
            RepoPublicationDeliveryCandidatePageReqVO reqVO) {
        List<TradePublicationDeliveryCandidateItemRespDTO> items = tradePublicationDeliveryApi.getCandidateItemList(
                BeanUtils.toBean(reqVO, TradePublicationDeliveryCandidatePageReqDTO.class));
        return BeanUtils.toBean(items, RepoPublicationDeliveryCandidateItemRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAndDeliver(RepoPublicationDeliveryBatchCreateReqVO reqVO, Long operatorUserId) {
        return createAndDeliverInternal(reqVO, operatorUserId, LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepoPublicationDeliveryBatchGroupCreateRespVO createGroupAndDeliver(
            RepoPublicationDeliveryBatchGroupCreateReqVO reqVO, Long operatorUserId) {
        if (Objects.equals(reqVO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            throw exception(PUBLICATION_DELIVERY_GROUP_EXPRESS_NOT_SUPPORTED);
        }
        List<RepoPublicationDeliveryCandidateRespVO> children = getCandidateChildList(
                BeanUtils.toBean(reqVO, RepoPublicationDeliveryCandidateChildReqVO.class));
        if (CollUtil.isEmpty(children)) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }
        LocalDateTime deliveryTime = LocalDateTime.now();
        List<Long> batchIds = new ArrayList<>(children.size());
        int totalCount = 0;
        for (RepoPublicationDeliveryCandidateRespVO child : children) {
            Long batchId = createAndDeliverInternal(buildCreateReqVO(child, reqVO.getRemark()),
                    operatorUserId, deliveryTime);
            batchIds.add(batchId);
            totalCount += defaultZero(child.getTotalCount());
        }
        return new RepoPublicationDeliveryBatchGroupCreateRespVO()
                .setBatchCount(batchIds.size())
                .setBatchIds(batchIds)
                .setTotalCount(totalCount);
    }

    @Override
    public PageResult<RepoPublicationDeliveryBatchRespVO> getBatchPage(RepoPublicationDeliveryBatchPageReqVO reqVO) {
        PageResult<RepoPublicationDeliveryBatchDO> page = publicationDeliveryBatchMapper.selectPage(reqVO);
        return new PageResult<>(BeanUtils.toBean(page.getList(), RepoPublicationDeliveryBatchRespVO.class),
                page.getTotal());
    }

    @Override
    public RepoPublicationDeliveryBatchRespVO getBatch(Long id) {
        RepoPublicationDeliveryBatchDO batch = publicationDeliveryBatchMapper.selectById(id);
        if (batch == null) {
            throw exception(PUBLICATION_DELIVERY_BATCH_NOT_EXISTS);
        }
        RepoPublicationDeliveryBatchRespVO respVO = BeanUtils.toBean(batch, RepoPublicationDeliveryBatchRespVO.class);
        List<RepoPublicationDeliveryBatchItemDO> items = publicationDeliveryBatchItemMapper.selectListByBatchId(id);
        respVO.setItems(BeanUtils.toBean(items, RepoPublicationDeliveryBatchRespVO.Item.class));
        return respVO;
    }

    private Long createAndDeliverInternal(RepoPublicationDeliveryBatchCreateReqVO reqVO, Long operatorUserId,
                                          LocalDateTime deliveryTime) {
        List<TradePublicationDeliveryCandidateItemRespDTO> deliverableItems = tradePublicationDeliveryApi
                .getDeliverableItemList(BeanUtils.toBean(reqVO, TradePublicationDeliveryCreateReqDTO.class));
        Map<Long, RepoPublicationDeliveryBatchCreateReqVO.ExpressItem> expressItemMap = buildExpressItemMap(reqVO);

        RepoPublicationDeliveryBatchDO batch = buildBatch(deliverableItems, operatorUserId, reqVO.getRemark(), deliveryTime);
        publicationDeliveryBatchMapper.insert(batch);
        List<RepoPublicationDeliveryBatchItemDO> batchItems = buildBatchItems(batch.getId(), deliverableItems, expressItemMap);
        try {
            publicationDeliveryBatchItemMapper.insertBatch(batchItems);
        } catch (DuplicateKeyException ex) {
            throw exception(PUBLICATION_DELIVERY_ISSUE_DUPLICATE);
        }

        allocateReceiptBalance(batch, deliverableItems, operatorUserId, deliveryTime);
        tradePublicationDeliveryApi.confirmDelivered(buildConfirmReq(batch, batchItems, deliveryTime));
        return batch.getId();
    }

    private RepoPublicationDeliveryBatchCreateReqVO buildCreateReqVO(RepoPublicationDeliveryCandidateRespVO child,
                                                                     String remark) {
        return new RepoPublicationDeliveryBatchCreateReqVO()
                .setDeliveryType(child.getDeliveryType())
                .setSchoolId(child.getSchoolId())
                .setWarehouseId(child.getWarehouseId())
                .setWindowId(child.getWindowId())
                .setOfferId(child.getOfferId())
                .setOfferSkuId(child.getOfferSkuId())
                .setSkuId(child.getSkuId())
                .setIssueId(child.getIssueId())
                .setIssueNo(child.getIssueNo())
                .setRemark(remark);
    }

    private RepoPublicationDeliveryBatchDO buildBatch(List<TradePublicationDeliveryCandidateItemRespDTO> items,
                                                      Long operatorUserId, String remark, LocalDateTime deliveryTime) {
        TradePublicationDeliveryCandidateItemRespDTO first = items.get(0);
        return new RepoPublicationDeliveryBatchDO()
                .setBatchNo(PUBLICATION_DELIVERY_BATCH_NO_PREFIX + IdUtil.getSnowflakeNextIdStr())
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
                .setTotalCount(sum(items, TradePublicationDeliveryCandidateItemRespDTO::getCount))
                .setOrderCount(countDistinct(items, TradePublicationDeliveryCandidateItemRespDTO::getOrderId))
                .setStudentCount(countDistinct(items, TradePublicationDeliveryCandidateItemRespDTO::getStudentId))
                .setStatus(PublicationDeliveryBatchStatusEnum.DELIVERED.getStatus())
                .setDeliveryTime(deliveryTime)
                .setOperatorUserId(operatorUserId)
                .setRemark(remark);
    }

    private List<RepoPublicationDeliveryBatchItemDO> buildBatchItems(
            Long batchId, List<TradePublicationDeliveryCandidateItemRespDTO> items,
            Map<Long, RepoPublicationDeliveryBatchCreateReqVO.ExpressItem> expressItemMap) {
        return convertList(items, item -> {
            RepoPublicationDeliveryBatchCreateReqVO.ExpressItem expressItem = expressItemMap == null
                    ? null : expressItemMap.get(item.getOrderIssueId());
            return new RepoPublicationDeliveryBatchItemDO()
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
                    .setLogisticsId(expressItem == null ? null : expressItem.getLogisticsId())
                    .setLogisticsNo(expressItem == null ? null : expressItem.getLogisticsNo())
                    .setStudentId(item.getStudentId())
                    .setStudentNameSnapshot(item.getStudentNameSnapshot())
                    .setClassId(item.getClassId())
                    .setClassNameSnapshot(item.getClassNameSnapshot());
        });
    }

    private void allocateReceiptBalance(RepoPublicationDeliveryBatchDO batch,
                                        List<TradePublicationDeliveryCandidateItemRespDTO> items,
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

    private TradePublicationDeliveryConfirmReqDTO buildConfirmReq(RepoPublicationDeliveryBatchDO batch,
                                                                  List<RepoPublicationDeliveryBatchItemDO> items,
                                                                  LocalDateTime deliveryTime) {
        return new TradePublicationDeliveryConfirmReqDTO()
                .setDeliveryBatchId(batch.getId())
                .setDeliveryType(batch.getDeliveryType())
                .setDeliveryTime(deliveryTime)
                .setItems(convertList(items, item -> new TradePublicationDeliveryConfirmReqDTO.Item()
                        .setOrderIssueId(item.getOrderIssueId())
                        .setLogisticsId(item.getLogisticsId())
                        .setLogisticsNo(item.getLogisticsNo())));
    }

    private void fillCandidateBalances(List<RepoPublicationDeliveryCandidateRespVO> candidates) {
        if (CollUtil.isEmpty(candidates)) {
            return;
        }
        Map<RepoPublicationReceiptBalanceKey, RepoPublicationReceiptBalanceBO> balanceMap =
                publicationReceiptService.getBalanceMap(convertList(candidates, this::buildBalanceKey));
        candidates.forEach(candidate -> fillCandidateBalance(candidate, balanceMap.get(buildBalanceKey(candidate))));
    }

    private void fillGroupBalances(List<RepoPublicationDeliveryCandidateGroupRespVO> groups) {
        if (CollUtil.isEmpty(groups)) {
            return;
        }
        for (RepoPublicationDeliveryCandidateGroupRespVO group : groups) {
            RepoPublicationDeliveryCandidateChildReqVO childReqVO = new RepoPublicationDeliveryCandidateChildReqVO();
            childReqVO.setDeliveryType(group.getDeliveryType())
                    .setSchoolId(group.getSchoolId())
                    .setWarehouseId(group.getWarehouseId())
                    .setWindowId(group.getWindowId());
            List<RepoPublicationDeliveryCandidateRespVO> children = getCandidateChildList(childReqVO);
            group.setReceivedCount(sum(children, RepoPublicationDeliveryCandidateRespVO::getReceivedCount))
                    .setAllocatedCount(sum(children, RepoPublicationDeliveryCandidateRespVO::getAllocatedCount))
                    .setAvailableCount(sum(children, RepoPublicationDeliveryCandidateRespVO::getAvailableCount))
                    .setShortageCount(sum(children, RepoPublicationDeliveryCandidateRespVO::getShortageCount));
        }
    }

    private void fillCandidateBalance(RepoPublicationDeliveryCandidateRespVO candidate,
                                      RepoPublicationReceiptBalanceBO balance) {
        int receivedCount = balance == null ? 0 : defaultZero(balance.getReceivedCount());
        int allocatedCount = balance == null ? 0 : defaultZero(balance.getAllocatedCount());
        int availableCount = balance == null ? 0 : defaultZero(balance.getAvailableCount());
        candidate.setReceivedCount(receivedCount)
                .setAllocatedCount(allocatedCount)
                .setAvailableCount(availableCount)
                .setShortageCount(Math.max(defaultZero(candidate.getTotalCount()) - availableCount, 0));
    }

    private RepoPublicationReceiptBalanceKey buildBalanceKey(RepoPublicationDeliveryCandidateRespVO candidate) {
        return new RepoPublicationReceiptBalanceKey()
                .setWarehouseId(candidate.getWarehouseId())
                .setWindowId(candidate.getWindowId())
                .setOfferId(candidate.getOfferId())
                .setOfferSkuId(candidate.getOfferSkuId())
                .setSkuId(candidate.getSkuId())
                .setIssueId(candidate.getIssueId())
                .setIssueNo(candidate.getIssueNo());
    }

    private RepoPublicationReceiptBalanceKey buildBalanceKey(TradePublicationDeliveryCandidateItemRespDTO item) {
        return new RepoPublicationReceiptBalanceKey()
                .setWarehouseId(item.getWarehouseId())
                .setWindowId(item.getWindowId())
                .setOfferId(item.getOfferId())
                .setOfferSkuId(item.getOfferSkuId())
                .setSkuId(item.getSkuId())
                .setIssueId(item.getIssueId())
                .setIssueNo(item.getIssueNo());
    }

    private Map<Long, RepoPublicationDeliveryBatchCreateReqVO.ExpressItem> buildExpressItemMap(
            RepoPublicationDeliveryBatchCreateReqVO reqVO) {
        if (!Objects.equals(reqVO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            return null;
        }
        return convertMap(reqVO.getExpressItems(), RepoPublicationDeliveryBatchCreateReqVO.ExpressItem::getOrderIssueId,
                Function.identity());
    }

    private <T> int countDistinct(List<T> items, java.util.function.Function<T, Long> mapper) {
        return (int) items.stream().map(mapper).filter(Objects::nonNull).distinct().count();
    }

    private <T> int sum(Collection<T> items, java.util.function.Function<T, Integer> mapper) {
        return items.stream().map(mapper).filter(Objects::nonNull).reduce(0, Integer::sum);
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

}
