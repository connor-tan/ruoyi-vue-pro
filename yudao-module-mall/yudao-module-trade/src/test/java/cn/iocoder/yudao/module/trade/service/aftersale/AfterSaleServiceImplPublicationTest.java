package cn.iocoder.yudao.module.trade.service.aftersale;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.promotion.api.combination.CombinationRecordApi;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppAfterSaleCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.AfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.aftersale.AfterSaleMapper;
import cn.iocoder.yudao.module.trade.dal.redis.no.TradeNoRedisDAO;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleWayEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderAfterSaleSyncService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.AFTER_SALE_PUBLICATION_PARTIAL_REFUND_NOT_SUPPORTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AfterSaleServiceImplPublicationTest {

    @Mock
    private TradeOrderAfterSaleSyncService tradeOrderAfterSaleSyncService;
    @Mock
    private TradeOrderQueryService tradeOrderQueryService;
    @Mock
    private DeliveryExpressService deliveryExpressService;
    @Mock
    private AfterSaleMapper tradeAfterSaleMapper;
    @Mock
    private TradeNoRedisDAO tradeNoRedisDAO;
    @Mock
    private PayRefundApi payRefundApi;
    @Mock
    private CombinationRecordApi combinationRecordApi;
    @Mock
    private TradeOrderProperties tradeOrderProperties;
    @InjectMocks
    private AfterSaleServiceImpl afterSaleService;

    @Test
    void createAfterSale_shouldRejectPublicationPartialRefund() {
        Long userId = 2L;
        TradeOrderItemDO orderItem = orderItem(userId).setSubscriptionOfferSkuId(5L);
        when(tradeOrderQueryService.getOrderItem(userId, orderItem.getId())).thenReturn(orderItem);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> afterSaleService.createAfterSale(userId, createReqVO(orderItem.getId(), 500)));

        assertEquals(AFTER_SALE_PUBLICATION_PARTIAL_REFUND_NOT_SUPPORTED.getCode(), ex.getCode());
        verify(tradeOrderQueryService, never()).getOrder(eq(userId), any());
        verify(tradeAfterSaleMapper, never()).insert(any(AfterSaleDO.class));
        verify(tradeOrderAfterSaleSyncService, never()).updateOrderItemWhenAfterSaleCreate(any(), any());
    }

    @Test
    void createAfterSale_shouldAllowNormalOrderItemPartialRefund() {
        Long userId = 2L;
        TradeOrderItemDO orderItem = orderItem(userId);
        TradeOrderDO order = order(userId);
        when(tradeOrderQueryService.getOrderItem(userId, orderItem.getId())).thenReturn(orderItem);
        when(tradeOrderQueryService.getOrder(userId, order.getId())).thenReturn(order);
        when(tradeNoRedisDAO.generate(TradeNoRedisDAO.AFTER_SALE_NO_PREFIX)).thenReturn("r100");
        doAnswer(invocation -> {
            AfterSaleDO afterSale = invocation.getArgument(0);
            afterSale.setId(3001L);
            return 1;
        }).when(tradeAfterSaleMapper).insert(any(AfterSaleDO.class));

        Long afterSaleId = afterSaleService.createAfterSale(userId, createReqVO(orderItem.getId(), 500));

        assertEquals(3001L, afterSaleId);
        ArgumentCaptor<AfterSaleDO> captor = ArgumentCaptor.forClass(AfterSaleDO.class);
        verify(tradeAfterSaleMapper).insert(captor.capture());
        assertEquals(500, captor.getValue().getRefundPrice());
        verify(tradeOrderAfterSaleSyncService).updateOrderItemWhenAfterSaleCreate(orderItem.getId(), afterSaleId);
    }

    private AppAfterSaleCreateReqVO createReqVO(Long orderItemId, Integer refundPrice) {
        return new AppAfterSaleCreateReqVO()
                .setOrderItemId(orderItemId)
                .setWay(AfterSaleWayEnum.REFUND.getWay())
                .setRefundPrice(refundPrice)
                .setApplyReason("不想要了");
    }

    private TradeOrderItemDO orderItem(Long userId) {
        return new TradeOrderItemDO()
                .setId(1001L)
                .setUserId(userId)
                .setOrderId(1L)
                .setSpuId(11L)
                .setSpuName("普通商品")
                .setSkuId(12L)
                .setCount(1)
                .setPayPrice(1000)
                .setAfterSaleStatus(TradeOrderItemAfterSaleStatusEnum.NONE.getStatus());
    }

    private TradeOrderDO order(Long userId) {
        return new TradeOrderDO()
                .setId(1L)
                .setNo("NO1")
                .setUserId(userId)
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus());
    }

}
