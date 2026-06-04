package cn.iocoder.yudao.module.trade.dal.mysql.order;

import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateGroupRespDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateItemRespDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidatePageReqDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateRespDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderPublicationIssueDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryStatusEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationReceiveStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeOrderPublicationIssueMapperTest extends BaseDbUnitTest {

    @Resource
    private TradeOrderPublicationIssueMapper publicationIssueMapper;
    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private DataSource dataSource;

    @Test
    void selectPublicationDeliveryCandidateItemList_shouldIncludePartRefundOrderIssue() {
        insertOrder(1L, TradeOrderStatusEnum.UNDELIVERED.getStatus(), TradeOrderRefundStatusEnum.PART.getStatus());
        insertIssue(9001L, 1L, false, PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());

        List<TradePublicationDeliveryCandidateItemRespDTO> items = publicationIssueMapper.selectPublicationDeliveryCandidateItemList(
                candidateReqVO(), TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(), null);

        assertEquals(1, items.size());
        assertEquals(9001L, items.get(0).getOrderIssueId());
        assertEquals(1L, items.get(0).getOrderId());
        assertEquals(5L, items.get(0).getOfferSkuId());
        assertEquals(1, items.get(0).getIssueNo());
    }

    @Test
    void selectPublicationDeliveryCandidateItemList_shouldExcludeCanceledAndDeliveredIssues() {
        insertOrder(1L, TradeOrderStatusEnum.UNDELIVERED.getStatus(), TradeOrderRefundStatusEnum.PART.getStatus());
        insertIssue(9001L, 1L, true, PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        insertIssue(9002L, 1L, false, PublicationDeliveryStatusEnum.DELIVERED.getStatus());

        List<TradePublicationDeliveryCandidateItemRespDTO> items = publicationIssueMapper.selectPublicationDeliveryCandidateItemList(
                candidateReqVO(), TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(), null);

        assertTrue(items.isEmpty());
    }

    @Test
    void selectPublicationDeliveryCandidatePage_shouldReturnReadableSkuFields() {
        insertOrder(1L, TradeOrderStatusEnum.UNDELIVERED.getStatus(), TradeOrderRefundStatusEnum.NONE.getStatus());
        insertIssue(9001L, 1L, false, PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        insertSku(6L, "测试刊物 SKU-全学年", "ISBN978-7-5436-9310-0");

        IPage<TradePublicationDeliveryCandidateRespDTO> page = publicationIssueMapper
                .selectPublicationDeliveryCandidatePage(new Page<>(1, 10), candidateReqVO(),
                        TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                        PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());

        assertEquals(1, page.getRecords().size());
        TradePublicationDeliveryCandidateRespDTO candidate = page.getRecords().get(0);
        assertEquals("测试刊物 SKU-全学年", candidate.getProductSkuName());
        assertEquals("ISBN978-7-5436-9310-0", candidate.getIsbn());
        assertEquals(1, candidate.getTotalCount());
        assertEquals(1, candidate.getOrderCount());
    }

    @Test
    void selectPublicationDeliveryCandidateGroupPage_shouldAggregateAndDeduplicateCounts() {
        insertOrder(1L, TradeOrderStatusEnum.UNDELIVERED.getStatus(), TradeOrderRefundStatusEnum.NONE.getStatus());
        insertOrder(2L, TradeOrderStatusEnum.UNDELIVERED.getStatus(), TradeOrderRefundStatusEnum.NONE.getStatus());
        insertIssue(9001L, 1L, 4L, 5L, 6L, 1, 1000L);
        insertIssue(9002L, 1L, 4L, 6L, 7L, 1, 1000L);
        insertIssue(9003L, 2L, 4L, 6L, 7L, 2, 1000L);

        IPage<TradePublicationDeliveryCandidateGroupRespDTO> page = publicationIssueMapper
                .selectPublicationDeliveryCandidateGroupPage(new Page<>(1, 10), groupReqVO(),
                        TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                        PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());

        assertEquals(1, page.getRecords().size());
        TradePublicationDeliveryCandidateGroupRespDTO group = page.getRecords().get(0);
        assertEquals(3, group.getTotalCount());
        assertEquals(2, group.getOrderCount());
        assertEquals(1, group.getStudentCount());
        assertEquals(2, group.getPublicationGroupCount());
        assertEquals(3, group.getIssueGroupCount());
    }

    @Test
    void selectPublicationDeliveryCandidateGroupAndChild_shouldNotSplitBySnapshotFields() {
        insertOrder(1L, TradeOrderStatusEnum.UNDELIVERED.getStatus(), TradeOrderRefundStatusEnum.NONE.getStatus());
        insertOrder(2L, TradeOrderStatusEnum.UNDELIVERED.getStatus(), TradeOrderRefundStatusEnum.NONE.getStatus());
        insertIssue(9001L, 1L, 4L, 5L, 6L, 1, 1000L,
                "实验小学", "城北站", "2026 春季订刊", "测试刊物", "第1期", LocalDate.of(2026, 5, 1));
        insertIssue(9002L, 2L, 4L, 5L, 6L, 1, 1001L,
                "实验小学旧名", "城北仓", "2026 春季订刊旧名", "测试刊物旧名", "第一期", LocalDate.of(2026, 5, 2));

        IPage<TradePublicationDeliveryCandidateGroupRespDTO> page = publicationIssueMapper
                .selectPublicationDeliveryCandidateGroupPage(new Page<>(1, 10), groupReqVO(),
                        TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                        PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        List<TradePublicationDeliveryCandidateRespDTO> children = publicationIssueMapper
                .selectPublicationDeliveryCandidateChildList(groupReqVO(),
                        TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                        PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());

        assertEquals(1, page.getRecords().size());
        TradePublicationDeliveryCandidateGroupRespDTO group = page.getRecords().get(0);
        assertEquals(2, group.getTotalCount());
        assertEquals(2, group.getOrderCount());
        assertEquals(2, group.getStudentCount());
        assertEquals(1, group.getPublicationGroupCount());
        assertEquals(1, group.getIssueGroupCount());
        assertEquals(1, children.size());
        assertEquals(2, children.get(0).getTotalCount());
        assertEquals(2, children.get(0).getOrderCount());
        assertEquals(LocalDate.of(2026, 5, 1), children.get(0).getPlannedDeliveryDate());
    }

    @Test
    void selectPublicationDeliveryCandidateChildList_shouldReturnPublicationIssueAggregates() {
        insertOrder(1L, TradeOrderStatusEnum.UNDELIVERED.getStatus(), TradeOrderRefundStatusEnum.NONE.getStatus());
        insertIssue(9001L, 1L, 4L, 5L, 6L, 1, 1000L);
        insertIssue(9002L, 1L, 4L, 6L, 7L, 2, 1000L);
        insertSku(6L, "测试刊物 SKU-一年级", "ISBN-1");
        insertSku(7L, "测试刊物 SKU-二年级", "ISBN-2");

        List<TradePublicationDeliveryCandidateRespDTO> children = publicationIssueMapper
                .selectPublicationDeliveryCandidateChildList(groupReqVO(),
                        TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                        PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());

        assertEquals(2, children.size());
        assertEquals(5L, children.get(0).getOfferSkuId());
        assertEquals("测试刊物 SKU-一年级", children.get(0).getProductSkuName());
        assertEquals("ISBN-1", children.get(0).getIsbn());
        assertEquals(6L, children.get(1).getOfferSkuId());
        assertEquals(2, children.get(1).getIssueNo());
    }

    @Test
    void selectPublicationDeliveryCandidateChildPage_shouldPagePublicationIssueAggregates() {
        insertOrder(1L, TradeOrderStatusEnum.UNDELIVERED.getStatus(), TradeOrderRefundStatusEnum.NONE.getStatus());
        insertIssue(9001L, 1L, 4L, 5L, 6L, 1, 1000L);
        insertIssue(9002L, 1L, 4L, 6L, 7L, 2, 1000L);
        insertIssue(9003L, 1L, 4L, 7L, 8L, 3, 1000L);

        IPage<TradePublicationDeliveryCandidateRespDTO> page = publicationIssueMapper
                .selectPublicationDeliveryCandidateChildPage(new Page<>(2, 1), groupReqVO(),
                        TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                        PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());

        assertEquals(3L, page.getTotal());
        assertEquals(1, page.getRecords().size());
        assertEquals(6L, page.getRecords().get(0).getOfferSkuId());
        assertEquals(2, page.getRecords().get(0).getIssueNo());
    }

    private TradePublicationDeliveryCandidatePageReqDTO candidateReqVO() {
        return new TradePublicationDeliveryCandidatePageReqDTO()
                .setDeliveryType(DeliveryTypeEnum.SCHOOL.getType())
                .setWindowId(3L)
                .setOfferId(4L)
                .setOfferSkuId(5L)
                .setSkuId(6L)
                .setIssueNo(1);
    }

    private TradePublicationDeliveryCandidatePageReqDTO groupReqVO() {
        return new TradePublicationDeliveryCandidatePageReqDTO()
                .setDeliveryType(DeliveryTypeEnum.SCHOOL.getType())
                .setSchoolId(100L)
                .setWarehouseId(200L)
                .setWindowId(3L);
    }

    private void insertOrder(Long id, Integer status, Integer refundStatus) {
        tradeOrderMapper.insert(new TradeOrderDO()
                .setId(id)
                .setNo("NO" + id)
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setTerminal(TerminalEnum.APP.getTerminal())
                .setUserId(10L)
                .setUserIp("127.0.0.1")
                .setStatus(status)
                .setProductCount(1)
                .setPayStatus(true)
                .setDiscountPrice(0)
                .setDeliveryPrice(0)
                .setAdjustPrice(0)
                .setPayPrice(1000)
                .setDeliveryType(DeliveryTypeEnum.SCHOOL.getType())
                .setReceiverName("张三")
                .setReceiverMobile("13800000000")
                .setReceiverAreaId(110000)
                .setReceiverDetailAddress("测试地址")
                .setRefundStatus(refundStatus)
                .setRefundPrice(0)
                .setCouponId(0L)
                .setCouponPrice(0)
                .setPointPrice(0)
                .setRefundPoint(0));
    }

    private void insertIssue(Long id, Long orderId, Boolean canceled, Integer deliveryStatus) {
        publicationIssueMapper.insert(new TradeOrderPublicationIssueDO()
                .setId(id)
                .setOrderId(orderId)
                .setOrderNo("NO" + orderId)
                .setOrderItemId(1001L)
                .setDeliveryId(11L)
                .setUserId(10L)
                .setDeliveryType(DeliveryTypeEnum.SCHOOL.getType())
                .setSkuId(6L)
                .setProductNameSnapshot("测试刊物")
                .setCount(1)
                .setSchoolId(100L)
                .setSchoolNameSnapshot("实验小学")
                .setWarehouseId(200L)
                .setWarehouseNameSnapshot("城北站")
                .setWindowId(3L)
                .setWindowNameSnapshot("2026 春季订刊")
                .setOfferId(4L)
                .setOfferSkuId(5L)
                .setIssueId(7001L)
                .setIssueNo(1)
                .setIssueName("第1期")
                .setDeliveryStatus(deliveryStatus)
                .setReceiveStatus(PublicationReceiveStatusEnum.UNRECEIVED.getStatus())
                .setCanceled(canceled));
    }

    private void insertIssue(Long id, Long orderId, Long offerId, Long offerSkuId, Long skuId, Integer issueNo,
                             Long studentId) {
        insertIssue(id, orderId, offerId, offerSkuId, skuId, issueNo, studentId,
                "实验小学", "城北站", "2026 春季订刊", "测试刊物", "第" + issueNo + "期", null);
    }

    private void insertIssue(Long id, Long orderId, Long offerId, Long offerSkuId, Long skuId, Integer issueNo,
                             Long studentId, String schoolName, String warehouseName, String windowName,
                             String productName, String issueName, LocalDate plannedDeliveryDate) {
        publicationIssueMapper.insert(new TradeOrderPublicationIssueDO()
                .setId(id)
                .setOrderId(orderId)
                .setOrderNo("NO" + orderId)
                .setOrderItemId(id + 1000)
                .setDeliveryId(11L)
                .setUserId(10L)
                .setDeliveryType(DeliveryTypeEnum.SCHOOL.getType())
                .setSkuId(skuId)
                .setProductNameSnapshot(productName)
                .setCount(1)
                .setStudentId(studentId)
                .setStudentNameSnapshot("学生" + studentId)
                .setSchoolId(100L)
                .setSchoolNameSnapshot(schoolName)
                .setWarehouseId(200L)
                .setWarehouseNameSnapshot(warehouseName)
                .setWindowId(3L)
                .setWindowNameSnapshot(windowName)
                .setOfferId(offerId)
                .setOfferSkuId(offerSkuId)
                .setIssueId(7000L + issueNo)
                .setIssueNo(issueNo)
                .setIssueName(issueName)
                .setPlannedDeliveryDate(plannedDeliveryDate)
                .setDeliveryStatus(PublicationDeliveryStatusEnum.UNDELIVERED.getStatus())
                .setReceiveStatus(PublicationReceiveStatusEnum.UNRECEIVED.getStatus())
                .setCanceled(false));
    }

    private void insertSku(Long skuId, String skuName, String isbn) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("""
                INSERT INTO product_sku (id, spu_id, name, deleted)
                VALUES (?, ?, ?, FALSE)
                """, skuId, 600L, skuName);
        jdbcTemplate.update("""
                INSERT INTO product_publication_sku_ext (sku_id, isbn, deleted)
                VALUES (?, ?, FALSE)
                """, skuId, isbn);
    }

}
