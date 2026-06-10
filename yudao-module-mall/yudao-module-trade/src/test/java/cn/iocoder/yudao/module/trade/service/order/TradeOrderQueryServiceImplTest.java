package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeOrderQueryServiceImplTest {

    @Mock
    private TradeOrderItemMapper tradeOrderItemMapper;
    @InjectMocks
    private TradeOrderQueryServiceImpl tradeOrderQueryService;

    @Test
    void hasPublicationOrderReferenceByProductSpuId_shouldReturnFalseWhenSpuIdNull() {
        assertFalse(tradeOrderQueryService.hasPublicationOrderReferenceByProductSpuId(null));

        verify(tradeOrderItemMapper, never()).selectPublicationOrderReferenceCountBySpuId(null);
    }

    @Test
    void hasPublicationOrderReferenceByProductSpuId_shouldReturnTrueWhenMapperCountPositive() {
        when(tradeOrderItemMapper.selectPublicationOrderReferenceCountBySpuId(1L)).thenReturn(1L);

        assertTrue(tradeOrderQueryService.hasPublicationOrderReferenceByProductSpuId(1L));
    }

    @Test
    void getPublicationOrderReferencedProductSkuIds_shouldReturnEmptyWhenSkuIdsEmpty() {
        assertEquals(Collections.emptySet(),
                tradeOrderQueryService.getPublicationOrderReferencedProductSkuIds(Collections.emptyList()));

        verify(tradeOrderItemMapper, never()).selectPublicationOrderReferencedSkuIds(Collections.emptyList());
    }

    @Test
    void getPublicationOrderReferencedProductSkuIds_shouldDelegateToMapper() {
        when(tradeOrderItemMapper.selectPublicationOrderReferencedSkuIds(List.of(10L, 11L)))
                .thenReturn(Set.of(10L));

        Set<Long> result = tradeOrderQueryService.getPublicationOrderReferencedProductSkuIds(List.of(10L, 11L));

        assertEquals(Set.of(10L), result);
    }

}
