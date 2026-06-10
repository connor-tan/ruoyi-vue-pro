package cn.iocoder.yudao.module.trade.dal.mysql.order;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderPublicationIssueDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryStatusEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationReceiveStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradeOrderItemMapperTest extends BaseDbUnitTest {

    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Resource
    private TradeOrderPublicationIssueMapper publicationIssueMapper;

    @Test
    void selectPublicationOrderReferenceCountBySpuId_shouldIncludePublicationOrderItemAndIssue() {
        insertOrderItem(1001L, 600L, 700L, 800L);
        insertIssue(9001L, 600L, 701L);

        Long count = tradeOrderItemMapper.selectPublicationOrderReferenceCountBySpuId(600L);

        assertEquals(2L, count);
    }

    @Test
    void selectPublicationOrderReferenceCountBySpuId_shouldIgnoreNormalAndDeletedRows() {
        insertOrderItem(1001L, 600L, 700L, null);
        insertOrderItem(1002L, 600L, 701L, 800L);
        insertIssue(9001L, 600L, 702L);
        tradeOrderItemMapper.deleteById(1002L);
        publicationIssueMapper.deleteById(9001L);

        Long count = tradeOrderItemMapper.selectPublicationOrderReferenceCountBySpuId(600L);

        assertEquals(0L, count);
    }

    @Test
    void selectPublicationOrderReferencedSkuIds_shouldReturnOnlyPublicationReferencedSkuIds() {
        insertOrderItem(1001L, 600L, 700L, 800L);
        insertIssue(9001L, 600L, 701L);
        insertOrderItem(1002L, 600L, 702L, null);
        insertOrderItem(1003L, 600L, 703L, 801L);
        insertIssue(9002L, 600L, 704L);
        tradeOrderItemMapper.deleteById(1003L);
        publicationIssueMapper.deleteById(9002L);

        Set<Long> skuIds = tradeOrderItemMapper.selectPublicationOrderReferencedSkuIds(
                List.of(700L, 701L, 702L, 703L, 704L, 705L));

        assertEquals(Set.of(700L, 701L), skuIds);
    }

    private void insertOrderItem(Long id, Long spuId, Long skuId, Long offerSkuId) {
        tradeOrderItemMapper.insert(new TradeOrderItemDO()
                .setId(id)
                .setUserId(10L)
                .setOrderId(1L)
                .setSpuId(spuId)
                .setSpuName("测试刊物")
                .setSkuId(skuId)
                .setCount(1)
                .setCommentStatus(false)
                .setPrice(1000)
                .setDiscountPrice(0)
                .setPayPrice(1000)
                .setAfterSaleStatus(0)
                .setSubscriptionOfferSkuId(offerSkuId));
    }

    private void insertIssue(Long id, Long spuId, Long skuId) {
        publicationIssueMapper.insert(new TradeOrderPublicationIssueDO()
                .setId(id)
                .setOrderId(1L)
                .setOrderNo("NO1")
                .setOrderItemId(id + 1000)
                .setDeliveryId(11L)
                .setUserId(10L)
                .setDeliveryType(DeliveryTypeEnum.SCHOOL.getType())
                .setSpuId(spuId)
                .setSkuId(skuId)
                .setProductNameSnapshot("测试刊物")
                .setCount(1)
                .setDeliveryStatus(PublicationDeliveryStatusEnum.UNDELIVERED.getStatus())
                .setReceiveStatus(PublicationReceiveStatusEnum.UNRECEIVED.getStatus())
                .setCanceled(false));
    }

}
