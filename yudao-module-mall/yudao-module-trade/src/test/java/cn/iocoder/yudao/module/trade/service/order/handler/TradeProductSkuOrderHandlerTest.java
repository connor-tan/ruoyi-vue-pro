package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateSalesCountReqDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradeProductSkuOrderHandlerTest {

    @Mock
    private ProductSkuApi productSkuApi;
    @InjectMocks
    private TradeProductSkuOrderHandler handler;

    @Test
    void beforeOrderCreate_shouldDeductNormalStockAndOnlyIncreasePublicationSales() {
        TradeOrderItemDO normalItem = orderItem(100L, 2);
        TradeOrderItemDO publicationItem = publicationOrderItem(200L, 3);

        handler.beforeOrderCreate(new TradeOrderDO(), List.of(normalItem, publicationItem));

        ArgumentCaptor<ProductSkuUpdateStockReqDTO> stockCaptor =
                ArgumentCaptor.forClass(ProductSkuUpdateStockReqDTO.class);
        verify(productSkuApi).updateSkuStock(stockCaptor.capture());
        ProductSkuUpdateStockReqDTO.Item stockItem = stockCaptor.getValue().getItems().get(0);
        assertEquals(1, stockCaptor.getValue().getItems().size());
        assertEquals(100L, stockItem.getId());
        assertEquals(-2, stockItem.getIncrCount());

        ArgumentCaptor<ProductSkuUpdateSalesCountReqDTO> salesCaptor =
                ArgumentCaptor.forClass(ProductSkuUpdateSalesCountReqDTO.class);
        verify(productSkuApi).updateSkuSalesCount(salesCaptor.capture());
        ProductSkuUpdateSalesCountReqDTO.Item salesItem = salesCaptor.getValue().getItems().get(0);
        assertEquals(1, salesCaptor.getValue().getItems().size());
        assertEquals(200L, salesItem.getId());
        assertEquals(3, salesItem.getIncrCount());
    }

    @Test
    void afterCancelOrder_shouldRestoreNormalStockAndOnlyDecreasePublicationSales() {
        TradeOrderItemDO normalItem = orderItem(100L, 2);
        TradeOrderItemDO publicationItem = publicationOrderItem(200L, 3);
        TradeOrderItemDO afterSaleItem = publicationOrderItem(300L, 4)
                .setAfterSaleStatus(TradeOrderItemAfterSaleStatusEnum.SUCCESS.getStatus());

        handler.afterCancelOrder(new TradeOrderDO(), List.of(normalItem, publicationItem, afterSaleItem));

        ArgumentCaptor<ProductSkuUpdateStockReqDTO> stockCaptor =
                ArgumentCaptor.forClass(ProductSkuUpdateStockReqDTO.class);
        verify(productSkuApi).updateSkuStock(stockCaptor.capture());
        ProductSkuUpdateStockReqDTO.Item stockItem = stockCaptor.getValue().getItems().get(0);
        assertEquals(1, stockCaptor.getValue().getItems().size());
        assertEquals(100L, stockItem.getId());
        assertEquals(2, stockItem.getIncrCount());

        ArgumentCaptor<ProductSkuUpdateSalesCountReqDTO> salesCaptor =
                ArgumentCaptor.forClass(ProductSkuUpdateSalesCountReqDTO.class);
        verify(productSkuApi).updateSkuSalesCount(salesCaptor.capture());
        ProductSkuUpdateSalesCountReqDTO.Item salesItem = salesCaptor.getValue().getItems().get(0);
        assertEquals(1, salesCaptor.getValue().getItems().size());
        assertEquals(200L, salesItem.getId());
        assertEquals(-3, salesItem.getIncrCount());
    }

    @Test
    void afterCancelOrderItem_shouldOnlyDecreasePublicationSales() {
        TradeOrderItemDO publicationItem = publicationOrderItem(200L, 3);

        handler.afterCancelOrderItem(new TradeOrderDO(), publicationItem);

        verify(productSkuApi, never()).updateSkuStock(any(ProductSkuUpdateStockReqDTO.class));
        ArgumentCaptor<ProductSkuUpdateSalesCountReqDTO> salesCaptor =
                ArgumentCaptor.forClass(ProductSkuUpdateSalesCountReqDTO.class);
        verify(productSkuApi).updateSkuSalesCount(salesCaptor.capture());
        ProductSkuUpdateSalesCountReqDTO.Item salesItem = salesCaptor.getValue().getItems().get(0);
        assertEquals(1, salesCaptor.getValue().getItems().size());
        assertEquals(200L, salesItem.getId());
        assertEquals(-3, salesItem.getIncrCount());
    }

    private TradeOrderItemDO orderItem(Long skuId, Integer count) {
        return new TradeOrderItemDO()
                .setSkuId(skuId)
                .setCount(count)
                .setAfterSaleStatus(TradeOrderItemAfterSaleStatusEnum.NONE.getStatus());
    }

    private TradeOrderItemDO publicationOrderItem(Long skuId, Integer count) {
        return orderItem(skuId, count)
                .setSubscriptionOfferSkuId(1000L);
    }

}
