package cn.iocoder.yudao.module.repo.dal.mysql.publicationreceipt;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt.RepoPublicationReceiptDO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt.RepoPublicationReceiptItemDO;
import cn.iocoder.yudao.module.repo.enums.receipt.RepoPublicationReceiptStatusEnum;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptBalanceBO;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptBalanceKey;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RepoPublicationReceiptItemMapperTest extends BaseDbUnitTest {

    @Resource
    private RepoPublicationReceiptMapper receiptMapper;
    @Resource
    private RepoPublicationReceiptItemMapper receiptItemMapper;

    @Test
    void selectAvailableListForUpdate_includesClosedReceivedBalance() {
        RepoPublicationReceiptDO receipt = RepoPublicationReceiptDO.builder()
                .id(100L)
                .receiptNo("PR100")
                .supplierId(10L)
                .warehouseId(20L)
                .status(RepoPublicationReceiptStatusEnum.CLOSED.getStatus())
                .expectedCount(10)
                .receivedCount(7)
                .allocatedCount(2)
                .build();
        receiptMapper.insert(receipt);
        RepoPublicationReceiptItemDO item = RepoPublicationReceiptItemDO.builder()
                .id(200L)
                .receiptId(receipt.getId())
                .supplierId(receipt.getSupplierId())
                .warehouseId(receipt.getWarehouseId())
                .windowId(30L)
                .offerId(40L)
                .offerSkuId(50L)
                .spuId(60L)
                .skuId(70L)
                .issueId(80L)
                .issueNo(1)
                .expectedCount(10)
                .receivedCount(7)
                .allocatedCount(2)
                .build();
        receiptItemMapper.insert(item);

        RepoPublicationReceiptBalanceKey key = new RepoPublicationReceiptBalanceKey()
                .setWarehouseId(item.getWarehouseId())
                .setWindowId(item.getWindowId())
                .setOfferId(item.getOfferId())
                .setOfferSkuId(item.getOfferSkuId())
                .setSkuId(item.getSkuId())
                .setIssueId(item.getIssueId())
                .setIssueNo(item.getIssueNo());
        List<RepoPublicationReceiptBalanceBO> balances = receiptItemMapper.selectBalanceList(List.of(key));
        assertEquals(1, balances.size());
        assertEquals(7, balances.get(0).getReceivedCount());
        assertEquals(2, balances.get(0).getAllocatedCount());
        assertEquals(5, balances.get(0).getAvailableCount());

        List<RepoPublicationReceiptItemDO> availableItems = receiptItemMapper.selectAvailableListForUpdate(key);
        assertEquals(1, availableItems.size());
        assertEquals(item.getId(), availableItems.get(0).getId());
    }

}
