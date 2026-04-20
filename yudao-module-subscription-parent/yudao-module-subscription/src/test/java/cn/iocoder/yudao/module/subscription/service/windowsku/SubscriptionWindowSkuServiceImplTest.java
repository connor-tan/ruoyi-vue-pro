package cn.iocoder.yudao.module.subscription.service.windowsku;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSkuMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuMapper;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubscriptionWindowSkuServiceImplTest {

    private SubscriptionWindowSkuServiceImpl service;
    private SubscriptionWindowSkuMapper subscriptionWindowSkuMapper;
    private SubscriptionWindowSpuMapper subscriptionWindowSpuMapper;
    private SubscriptionSupportService subscriptionSupportService;
    private SubscriptionWindowService subscriptionWindowService;

    @BeforeEach
    void setUp() {
        service = new SubscriptionWindowSkuServiceImpl();
        subscriptionWindowSkuMapper = mock(SubscriptionWindowSkuMapper.class);
        subscriptionWindowSpuMapper = mock(SubscriptionWindowSpuMapper.class);
        subscriptionSupportService = mock(SubscriptionSupportService.class);
        subscriptionWindowService = mock(SubscriptionWindowService.class);
        ReflectionTestUtils.setField(service, "subscriptionWindowSkuMapper", subscriptionWindowSkuMapper);
        ReflectionTestUtils.setField(service, "subscriptionWindowSpuMapper", subscriptionWindowSpuMapper);
        ReflectionTestUtils.setField(service, "subscriptionSupportService", subscriptionSupportService);
        ReflectionTestUtils.setField(service, "subscriptionWindowService", subscriptionWindowService);
    }

    @Test
    void getListShouldSyncMissingMatchedWindowSkuAsEnabled() {
        when(subscriptionWindowSpuMapper.selectById(20L)).thenReturn(SubscriptionWindowSpuDO.builder()
                .id(20L)
                .windowId(10L)
                .productSpuId(30L)
                .build());
        when(subscriptionWindowService.getWindowDO(10L)).thenReturn(SubscriptionWindowDO.builder()
                .id(10L)
                .targetPeriod("FIRST_TERM")
                .build());
        when(subscriptionWindowSkuMapper.selectListByWindowSpuId(20L))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList());
        when(subscriptionSupportService.getSkuListBySpuId(30L)).thenReturn(List.of(ProductSkuDO.builder()
                .id(40L)
                .build()));
        when(subscriptionSupportService.getSkuPublicationMap(any())).thenReturn(Map.of(40L,
                ProductSkuPublicationDO.builder()
                        .productSkuId(40L)
                        .targetPeriod("FIRST_TERM")
                        .build()));

        service.getWindowSkuListByWindowSpuId(20L);

        ArgumentCaptor<SubscriptionWindowSkuDO> captor = ArgumentCaptor.forClass(SubscriptionWindowSkuDO.class);
        verify(subscriptionWindowSkuMapper).insert(captor.capture());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), captor.getValue().getStatus());
    }
}
