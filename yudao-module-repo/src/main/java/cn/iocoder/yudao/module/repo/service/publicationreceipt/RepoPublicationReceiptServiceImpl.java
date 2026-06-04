package cn.iocoder.yudao.module.repo.service.publicationreceipt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptCloseReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptCreateReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptDemandPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptDemandRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptReceiveReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt.RepoPublicationReceiptAllocationDO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt.RepoPublicationReceiptDO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt.RepoPublicationReceiptItemDO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt.RepoPublicationReceiptRecordDO;
import cn.iocoder.yudao.module.repo.dal.dataobject.supplier.RepoSupplierDO;
import cn.iocoder.yudao.module.repo.dal.dataobject.supplierpublication.RepoSupplierPublicationSkuDO;
import cn.iocoder.yudao.module.repo.dal.dataobject.warehouse.RepoWarehouseDO;
import cn.iocoder.yudao.module.repo.dal.mysql.publicationreceipt.RepoPublicationReceiptAllocationMapper;
import cn.iocoder.yudao.module.repo.dal.mysql.publicationreceipt.RepoPublicationReceiptItemMapper;
import cn.iocoder.yudao.module.repo.dal.mysql.publicationreceipt.RepoPublicationReceiptMapper;
import cn.iocoder.yudao.module.repo.dal.mysql.publicationreceipt.RepoPublicationReceiptRecordMapper;
import cn.iocoder.yudao.module.repo.enums.receipt.RepoPublicationReceiptStatusEnum;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptBalanceBO;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptBalanceKey;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptDeliveryAllocateReqBO;
import cn.iocoder.yudao.module.repo.service.supplier.RepoSupplierService;
import cn.iocoder.yudao.module.repo.service.supplierpublication.RepoSupplierPublicationSkuService;
import cn.iocoder.yudao.module.repo.service.warehouse.RepoWarehouseService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_RECEIPT_BALANCE_NOT_ENOUGH;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_RECEIPT_DEMAND_NOT_EXISTS;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_RECEIPT_ITEM_EXPECTED_COUNT_INVALID;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_RECEIPT_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_RECEIPT_ITEM_REQUIRED;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_RECEIPT_NOT_EXISTS;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_RECEIPT_RECORD_COUNT_INVALID;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_RECEIPT_STATUS_INVALID;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_RECEIPT_WAREHOUSE_REQUIRED;

@Service
@Validated
public class RepoPublicationReceiptServiceImpl implements RepoPublicationReceiptService {

    @Resource
    private RepoPublicationReceiptMapper publicationReceiptMapper;
    @Resource
    private RepoPublicationReceiptItemMapper publicationReceiptItemMapper;
    @Resource
    private RepoPublicationReceiptRecordMapper publicationReceiptRecordMapper;
    @Resource
    private RepoPublicationReceiptAllocationMapper publicationReceiptAllocationMapper;
    @Resource
    private RepoSupplierService supplierService;
    @Resource
    private RepoSupplierPublicationSkuService supplierPublicationSkuService;
    @Resource
    private RepoWarehouseService warehouseService;

    @Override
    public PageResult<RepoPublicationReceiptDemandRespVO> getDemandPage(RepoPublicationReceiptDemandPageReqVO pageReqVO) {
        IPage<RepoPublicationReceiptDemandRespVO> page = publicationReceiptItemMapper.selectDemandPage(
                MyBatisUtils.buildPage(pageReqVO), pageReqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReceipt(RepoPublicationReceiptCreateReqVO createReqVO) {
        if (CollUtil.isEmpty(createReqVO.getItems())) {
            throw exception(PUBLICATION_RECEIPT_ITEM_REQUIRED);
        }
        RepoSupplierDO supplier = supplierService.validateSupplierEnabled(createReqVO.getSupplierId());
        RepoWarehouseDO warehouse = warehouseService.validateWarehouseBindable(createReqVO.getWarehouseId());

        List<RepoPublicationReceiptItemDO> items = convertList(createReqVO.getItems(),
                itemReqVO -> buildReceiptItem(createReqVO, itemReqVO));
        int expectedCount = sum(items, RepoPublicationReceiptItemDO::getExpectedCount);

        RepoPublicationReceiptDO receipt = new RepoPublicationReceiptDO()
                .setReceiptNo(generateReceiptNo())
                .setSupplierId(supplier.getId())
                .setSupplierNameSnapshot(supplier.getName())
                .setWarehouseId(warehouse.getId())
                .setWarehouseNameSnapshot(warehouse.getName())
                .setStatus(RepoPublicationReceiptStatusEnum.DRAFT.getStatus())
                .setExpectedCount(expectedCount)
                .setReceivedCount(0)
                .setAllocatedCount(0)
                .setRemark(createReqVO.getRemark());
        publicationReceiptMapper.insert(receipt);

        items.forEach(item -> item.setReceiptId(receipt.getId()));
        publicationReceiptItemMapper.insertBatch(items);
        return receipt.getId();
    }

    @Override
    public void submitReceipt(Long id) {
        RepoPublicationReceiptDO receipt = validateReceiptExists(id);
        if (!Objects.equals(receipt.getStatus(), RepoPublicationReceiptStatusEnum.DRAFT.getStatus())) {
            throw exception(PUBLICATION_RECEIPT_STATUS_INVALID);
        }
        publicationReceiptMapper.updateById(new RepoPublicationReceiptDO()
                .setId(id)
                .setStatus(RepoPublicationReceiptStatusEnum.PENDING_RECEIVE.getStatus())
                .setSubmitTime(LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveReceipt(RepoPublicationReceiptReceiveReqVO reqVO, Long operatorUserId) {
        RepoPublicationReceiptDO receipt = validateReceiptExists(reqVO.getReceiptId());
        validateReceivable(receipt);
        List<RepoPublicationReceiptItemDO> items = publicationReceiptItemMapper.selectListByReceiptId(receipt.getId());
        Map<Long, RepoPublicationReceiptItemDO> itemMap = new HashMap<>();
        items.forEach(item -> itemMap.put(item.getId(), item));

        LocalDateTime now = LocalDateTime.now();
        for (RepoPublicationReceiptReceiveReqVO.Item itemReqVO : reqVO.getItems()) {
            if (itemReqVO.getReceivedCount() == null || itemReqVO.getReceivedCount() <= 0) {
                throw exception(PUBLICATION_RECEIPT_RECORD_COUNT_INVALID);
            }
            RepoPublicationReceiptItemDO item = itemMap.get(itemReqVO.getReceiptItemId());
            if (item == null) {
                throw exception(PUBLICATION_RECEIPT_ITEM_NOT_EXISTS);
            }
            publicationReceiptItemMapper.updateById(new RepoPublicationReceiptItemDO()
                    .setId(item.getId())
                    .setReceivedCount(defaultZero(item.getReceivedCount()) + itemReqVO.getReceivedCount()));
            publicationReceiptRecordMapper.insert(new RepoPublicationReceiptRecordDO()
                    .setReceiptId(receipt.getId())
                    .setReceiptItemId(item.getId())
                    .setReceivedTime(now)
                    .setBundleCount(itemReqVO.getBundleCount())
                    .setReceivedCount(itemReqVO.getReceivedCount())
                    .setOperatorUserId(operatorUserId)
                    .setRemark(itemReqVO.getRemark()));
        }
        refreshReceiptCounts(receipt.getId());
    }

    @Override
    public void closeReceipt(RepoPublicationReceiptCloseReqVO reqVO) {
        RepoPublicationReceiptDO receipt = validateReceiptExists(reqVO.getId());
        if (Objects.equals(receipt.getStatus(), RepoPublicationReceiptStatusEnum.CLOSED.getStatus())) {
            throw exception(PUBLICATION_RECEIPT_STATUS_INVALID);
        }
        publicationReceiptMapper.updateById(new RepoPublicationReceiptDO()
                .setId(reqVO.getId())
                .setStatus(RepoPublicationReceiptStatusEnum.CLOSED.getStatus())
                .setCloseTime(LocalDateTime.now())
                .setCloseReason(reqVO.getCloseReason()));
    }

    @Override
    public RepoPublicationReceiptDO getReceipt(Long id) {
        return publicationReceiptMapper.selectById(id);
    }

    @Override
    public List<RepoPublicationReceiptItemDO> getReceiptItemList(Long receiptId) {
        return publicationReceiptItemMapper.selectListByReceiptId(receiptId);
    }

    @Override
    public PageResult<RepoPublicationReceiptDO> getReceiptPage(RepoPublicationReceiptPageReqVO pageReqVO) {
        return publicationReceiptMapper.selectPage(pageReqVO);
    }

    @Override
    public Map<RepoPublicationReceiptBalanceKey, RepoPublicationReceiptBalanceBO> getBalanceMap(
            Collection<RepoPublicationReceiptBalanceKey> keys) {
        if (CollUtil.isEmpty(keys)) {
            return Collections.emptyMap();
        }
        List<RepoPublicationReceiptBalanceKey> filteredKeys = keys.stream()
                .filter(this::isValidBalanceKey)
                .distinct()
                .toList();
        if (CollUtil.isEmpty(filteredKeys)) {
            return Collections.emptyMap();
        }
        List<RepoPublicationReceiptBalanceBO> balances = publicationReceiptItemMapper.selectBalanceList(filteredKeys);
        Map<RepoPublicationReceiptBalanceKey, RepoPublicationReceiptBalanceBO> result = new HashMap<>(balances.size());
        balances.forEach(balance -> result.put(buildKey(balance), balance));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void allocateDeliveryBatch(RepoPublicationReceiptDeliveryAllocateReqBO reqBO) {
        RepoPublicationReceiptBalanceKey key = reqBO.getKey();
        if (key == null || key.getWarehouseId() == null) {
            throw exception(PUBLICATION_RECEIPT_WAREHOUSE_REQUIRED);
        }
        if (!isValidBalanceKey(key) || reqBO.getCount() == null || reqBO.getCount() <= 0) {
            throw exception(PUBLICATION_RECEIPT_BALANCE_NOT_ENOUGH);
        }
        int remaining = reqBO.getCount();
        List<RepoPublicationReceiptItemDO> availableItems = publicationReceiptItemMapper.selectAvailableListForUpdate(key);
        Set<Long> touchedReceiptIds = convertSet(availableItems, RepoPublicationReceiptItemDO::getReceiptId);
        for (RepoPublicationReceiptItemDO item : availableItems) {
            int available = defaultZero(item.getReceivedCount()) - defaultZero(item.getAllocatedCount());
            if (available <= 0) {
                continue;
            }
            int allocated = Math.min(available, remaining);
            publicationReceiptItemMapper.updateById(new RepoPublicationReceiptItemDO()
                    .setId(item.getId())
                    .setAllocatedCount(defaultZero(item.getAllocatedCount()) + allocated));
            publicationReceiptAllocationMapper.insert(new RepoPublicationReceiptAllocationDO()
                    .setReceiptItemId(item.getId())
                    .setDeliveryBatchId(reqBO.getDeliveryBatchId())
                    .setAllocatedCount(allocated)
                    .setOperatorUserId(reqBO.getOperatorUserId())
                    .setDeliveryTime(reqBO.getDeliveryTime())
                    .setRemark(reqBO.getRemark()));
            remaining -= allocated;
            if (remaining == 0) {
                break;
            }
        }
        if (remaining > 0) {
            throw exception(PUBLICATION_RECEIPT_BALANCE_NOT_ENOUGH);
        }
        touchedReceiptIds.forEach(this::refreshReceiptCounts);
    }

    private RepoPublicationReceiptItemDO buildReceiptItem(RepoPublicationReceiptCreateReqVO createReqVO,
                                                         RepoPublicationReceiptCreateReqVO.Item itemReqVO) {
        if (itemReqVO.getExpectedCount() == null || itemReqVO.getExpectedCount() <= 0) {
            throw exception(PUBLICATION_RECEIPT_ITEM_EXPECTED_COUNT_INVALID);
        }
        RepoSupplierPublicationSkuDO relation = supplierPublicationSkuService.validateSupplierPublicationSkuEnabled(
                createReqVO.getSupplierId(), itemReqVO.getSkuId());
        RepoPublicationReceiptDemandRespVO demand = publicationReceiptItemMapper.selectDemandByKey(
                createReqVO.getWarehouseId(), itemReqVO.getWindowId(), itemReqVO.getOfferId(),
                itemReqVO.getOfferSkuId(), itemReqVO.getSkuId(), itemReqVO.getIssueId(), itemReqVO.getIssueNo());
        if (demand == null) {
            throw exception(PUBLICATION_RECEIPT_DEMAND_NOT_EXISTS);
        }
        return new RepoPublicationReceiptItemDO()
                .setSupplierId(createReqVO.getSupplierId())
                .setWarehouseId(createReqVO.getWarehouseId())
                .setWindowId(itemReqVO.getWindowId())
                .setWindowNameSnapshot(demand.getWindowNameSnapshot())
                .setOfferId(itemReqVO.getOfferId())
                .setOfferSkuId(itemReqVO.getOfferSkuId())
                .setSpuId(relation.getSpuId())
                .setSkuId(itemReqVO.getSkuId())
                .setProductNameSnapshot(relation.getProductNameSnapshot())
                .setProductSkuNameSnapshot(relation.getProductSkuNameSnapshot())
                .setIsbn(relation.getIsbn())
                .setIssueId(itemReqVO.getIssueId())
                .setIssueNo(itemReqVO.getIssueNo())
                .setIssueName(demand.getIssueName())
                .setExpectedCount(itemReqVO.getExpectedCount())
                .setReceivedCount(0)
                .setAllocatedCount(0)
                .setRemark(itemReqVO.getRemark());
    }

    private void refreshReceiptCounts(Long receiptId) {
        RepoPublicationReceiptDO receipt = publicationReceiptMapper.selectById(receiptId);
        if (receipt == null) {
            return;
        }
        List<RepoPublicationReceiptItemDO> items = publicationReceiptItemMapper.selectListByReceiptId(receiptId);
        int expectedCount = sum(items, RepoPublicationReceiptItemDO::getExpectedCount);
        int receivedCount = sum(items, RepoPublicationReceiptItemDO::getReceivedCount);
        int allocatedCount = sum(items, RepoPublicationReceiptItemDO::getAllocatedCount);
        RepoPublicationReceiptDO updateObj = new RepoPublicationReceiptDO()
                .setId(receiptId)
                .setExpectedCount(expectedCount)
                .setReceivedCount(receivedCount)
                .setAllocatedCount(allocatedCount);
        if (!Objects.equals(receipt.getStatus(), RepoPublicationReceiptStatusEnum.CLOSED.getStatus())
                && !Objects.equals(receipt.getStatus(), RepoPublicationReceiptStatusEnum.DRAFT.getStatus())) {
            updateObj.setStatus(receivedCount >= expectedCount
                    ? RepoPublicationReceiptStatusEnum.RECEIVED.getStatus()
                    : RepoPublicationReceiptStatusEnum.PARTIAL_RECEIVED.getStatus());
        }
        publicationReceiptMapper.updateById(updateObj);
    }

    private void validateReceivable(RepoPublicationReceiptDO receipt) {
        if (Objects.equals(receipt.getStatus(), RepoPublicationReceiptStatusEnum.DRAFT.getStatus())
                || Objects.equals(receipt.getStatus(), RepoPublicationReceiptStatusEnum.CLOSED.getStatus())) {
            throw exception(PUBLICATION_RECEIPT_STATUS_INVALID);
        }
    }

    private RepoPublicationReceiptDO validateReceiptExists(Long id) {
        RepoPublicationReceiptDO receipt = publicationReceiptMapper.selectById(id);
        if (receipt == null) {
            throw exception(PUBLICATION_RECEIPT_NOT_EXISTS);
        }
        return receipt;
    }

    private String generateReceiptNo() {
        return "pr" + IdUtil.getSnowflakeNextIdStr();
    }

    private boolean isValidBalanceKey(RepoPublicationReceiptBalanceKey key) {
        return key != null
                && key.getWarehouseId() != null
                && key.getWindowId() != null
                && key.getOfferId() != null
                && key.getOfferSkuId() != null
                && key.getSkuId() != null
                && key.getIssueNo() != null;
    }

    private RepoPublicationReceiptBalanceKey buildKey(RepoPublicationReceiptBalanceBO balance) {
        return new RepoPublicationReceiptBalanceKey()
                .setWarehouseId(balance.getWarehouseId())
                .setWindowId(balance.getWindowId())
                .setOfferId(balance.getOfferId())
                .setOfferSkuId(balance.getOfferSkuId())
                .setSkuId(balance.getSkuId())
                .setIssueId(balance.getIssueId())
                .setIssueNo(balance.getIssueNo());
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private int sum(List<RepoPublicationReceiptItemDO> items,
                    java.util.function.Function<RepoPublicationReceiptItemDO, Integer> mapper) {
        return items.stream().map(mapper).filter(Objects::nonNull).reduce(0, Integer::sum);
    }

}
