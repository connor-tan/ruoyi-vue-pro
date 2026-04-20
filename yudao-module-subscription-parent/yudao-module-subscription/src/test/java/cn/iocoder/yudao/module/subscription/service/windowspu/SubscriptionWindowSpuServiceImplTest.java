package cn.iocoder.yudao.module.subscription.service.windowspu;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuGradeDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuBatchCreateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuBatchCreateRespVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSkuMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuGradeMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuRuleMapper;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubscriptionWindowSpuServiceImplTest {

    private SubscriptionWindowSpuServiceImpl service;
    private SubscriptionWindowSpuMapper subscriptionWindowSpuMapper;
    private SubscriptionWindowSpuGradeMapper subscriptionWindowSpuGradeMapper;
    private SubscriptionWindowSpuRuleMapper subscriptionWindowSpuRuleMapper;
    private SubscriptionWindowSkuMapper subscriptionWindowSkuMapper;
    private SubscriptionWindowService subscriptionWindowService;
    private SubscriptionSupportService subscriptionSupportService;

    @BeforeEach
    void setUp() {
        service = new SubscriptionWindowSpuServiceImpl();
        subscriptionWindowSpuMapper = mock(SubscriptionWindowSpuMapper.class);
        subscriptionWindowSpuGradeMapper = mock(SubscriptionWindowSpuGradeMapper.class);
        subscriptionWindowSpuRuleMapper = mock(SubscriptionWindowSpuRuleMapper.class);
        subscriptionWindowSkuMapper = mock(SubscriptionWindowSkuMapper.class);
        subscriptionWindowService = mock(SubscriptionWindowService.class);
        subscriptionSupportService = mock(SubscriptionSupportService.class);
        ReflectionTestUtils.setField(service, "subscriptionWindowSpuMapper", subscriptionWindowSpuMapper);
        ReflectionTestUtils.setField(service, "subscriptionWindowSpuGradeMapper", subscriptionWindowSpuGradeMapper);
        ReflectionTestUtils.setField(service, "subscriptionWindowSpuRuleMapper", subscriptionWindowSpuRuleMapper);
        ReflectionTestUtils.setField(service, "subscriptionWindowSkuMapper", subscriptionWindowSkuMapper);
        ReflectionTestUtils.setField(service, "subscriptionWindowService", subscriptionWindowService);
        ReflectionTestUtils.setField(service, "subscriptionSupportService", subscriptionSupportService);
    }

    @Test
    void batchCreateShouldInitializeOnlyMatchedSkuAsEnabled() {
        when(subscriptionWindowService.getWindowDO(10L)).thenReturn(SubscriptionWindowDO.builder()
                .id(10L)
                .targetPeriod("FIRST_TERM")
                .build());
        when(subscriptionSupportService.getPublicationSpu(30L, true)).thenReturn(ProductSpuDO.builder()
                .id(30L)
                .sort(1)
                .build());
        when(subscriptionSupportService.getPublicationSpuGradeMap(any())).thenReturn(Map.of(30L, List.of(
                ProductSpuGradeDO.builder()
                        .productSpuId(30L)
                        .gradeCatalogId(1L)
                        .build())));
        when(subscriptionWindowSpuMapper.selectByWindowIdAndProductSpuId(10L, 30L)).thenReturn(null);
        doAnswer(invocation -> {
            SubscriptionWindowSpuDO windowSpu = invocation.getArgument(0);
            windowSpu.setId(20L);
            return 1;
        }).when(subscriptionWindowSpuMapper).insert(any(SubscriptionWindowSpuDO.class));
        when(subscriptionSupportService.getSkuListBySpuId(30L)).thenReturn(List.of(
                ProductSkuDO.builder().id(40L).build(),
                ProductSkuDO.builder().id(41L).build()));
        when(subscriptionSupportService.getSkuPublicationMap(any())).thenReturn(Map.of(
                40L, ProductSkuPublicationDO.builder().productSkuId(40L).targetPeriod("FIRST_TERM").build(),
                41L, ProductSkuPublicationDO.builder().productSkuId(41L).targetPeriod("SECOND_TERM").build()));

        SubscriptionWindowSpuBatchCreateReqVO.Item item = new SubscriptionWindowSpuBatchCreateReqVO.Item();
        item.setGradeCatalogId(1L);
        item.setProductSpuId(30L);
        SubscriptionWindowSpuBatchCreateReqVO reqVO = new SubscriptionWindowSpuBatchCreateReqVO();
        reqVO.setWindowId(10L);
        reqVO.setItems(List.of(item));

        SubscriptionWindowSpuBatchCreateRespVO respVO = service.batchCreate(reqVO);

        ArgumentCaptor<SubscriptionWindowSkuDO> captor = ArgumentCaptor.forClass(SubscriptionWindowSkuDO.class);
        verify(subscriptionWindowSkuMapper).insert(captor.capture());
        assertEquals(40L, captor.getValue().getProductSkuId());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), captor.getValue().getStatus());
        assertEquals(1, respVO.getCreatedWindowSpuCount());
        assertEquals(1, respVO.getCreatedGradeCount());
    }
}
