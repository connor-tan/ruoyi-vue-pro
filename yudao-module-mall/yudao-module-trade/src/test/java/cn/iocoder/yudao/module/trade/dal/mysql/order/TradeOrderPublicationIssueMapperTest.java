package cn.iocoder.yudao.module.trade.dal.mysql.order;

import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidatePageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidateRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderPublicationIssueDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryStatusEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationReceiveStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.delivery.bo.TradePublicationDeliveryCandidateItemBO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
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

        List<TradePublicationDeliveryCandidateItemBO> items = publicationIssueMapper.selectPublicationDeliveryCandidateItemList(
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

        List<TradePublicationDeliveryCandidateItemBO> items = publicationIssueMapper.selectPublicationDeliveryCandidateItemList(
                candidateReqVO(), TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(), null);

        assertTrue(items.isEmpty());
    }

    @Test
    void selectPublicationDeliveryCandidatePage_shouldReturnReadableSkuFields() {
        insertOrder(1L, TradeOrderStatusEnum.UNDELIVERED.getStatus(), TradeOrderRefundStatusEnum.NONE.getStatus());
        insertIssue(9001L, 1L, false, PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        insertSku(6L, "测试刊物 SKU-全学年", "ISBN978-7-5436-9310-0");

        IPage<TradePublicationDeliveryCandidateRespVO> page = publicationIssueMapper
                .selectPublicationDeliveryCandidatePage(new Page<>(1, 10), candidateReqVO(),
                        TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                        PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());

        assertEquals(1, page.getRecords().size());
        TradePublicationDeliveryCandidateRespVO candidate = page.getRecords().get(0);
        assertEquals("测试刊物 SKU-全学年", candidate.getProductSkuName());
        assertEquals("ISBN978-7-5436-9310-0", candidate.getIsbn());
        assertEquals(1, candidate.getTotalCount());
        assertEquals(1, candidate.getOrderCount());
    }

    private TradePublicationDeliveryCandidatePageReqVO candidateReqVO() {
        return new TradePublicationDeliveryCandidatePageReqVO()
                .setDeliveryType(DeliveryTypeEnum.SCHOOL.getType())
                .setWindowId(3L)
                .setOfferId(4L)
                .setOfferSkuId(5L)
                .setSkuId(6L)
                .setIssueNo(1);
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
