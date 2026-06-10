package cn.iocoder.yudao.module.trade.api.order;

import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeSubscriptionOrderApiImplTest {

    @Mock
    private TradeOrderQueryService tradeOrderQueryService;
    @InjectMocks
    private TradeSubscriptionOrderApiImpl api;

    @Test
    void hasPublicationOrderReferenceByProductSpuId_shouldDelegateToQueryService() {
        when(tradeOrderQueryService.hasPublicationOrderReferenceByProductSpuId(1L)).thenReturn(true);

        assertTrue(api.hasPublicationOrderReferenceByProductSpuId(1L));

        verify(tradeOrderQueryService).hasPublicationOrderReferenceByProductSpuId(1L);
    }

    @Test
    void getPublicationOrderReferencedProductSkuIds_shouldDelegateToQueryService() {
        when(tradeOrderQueryService.getPublicationOrderReferencedProductSkuIds(List.of(10L, 11L)))
                .thenReturn(Set.of(10L));

        Set<Long> result = api.getPublicationOrderReferencedProductSkuIds(List.of(10L, 11L));

        assertEquals(Set.of(10L), result);
        verify(tradeOrderQueryService).getPublicationOrderReferencedProductSkuIds(List.of(10L, 11L));
    }

}
