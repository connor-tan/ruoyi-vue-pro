package cn.iocoder.yudao.module.subscription.service.app;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTitleDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.subscription.controller.app.publication.vo.AppSubscriptionPublicationRespVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityService;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionVisibleSpuBO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubscriptionAppQueryServiceImplTest {

    private SubscriptionAppQueryServiceImpl service;
    private SubscriptionWindowService subscriptionWindowService;
    private SubscriptionVisibilityService subscriptionVisibilityService;
    private SubscriptionSupportService subscriptionSupportService;

    @BeforeEach
    void setUp() {
        service = new SubscriptionAppQueryServiceImpl();
        subscriptionWindowService = mock(SubscriptionWindowService.class);
        subscriptionVisibilityService = mock(SubscriptionVisibilityService.class);
        subscriptionSupportService = mock(SubscriptionSupportService.class);
        ReflectionTestUtils.setField(service, "subscriptionWindowService", subscriptionWindowService);
        ReflectionTestUtils.setField(service, "subscriptionVisibilityService", subscriptionVisibilityService);
        ReflectionTestUtils.setField(service, "subscriptionSupportService", subscriptionSupportService);
    }

    @Test
    void getPublicationListBySpuIdsShouldFilterAndKeepRequestedOrder() {
        when(subscriptionWindowService.getCurrentOpenWindow()).thenReturn(openWindow(10L));
        when(subscriptionSupportService.getStudent(1L)).thenReturn(StudentDO.builder()
                .id(1L)
                .belongTo(100L)
                .build());
        when(subscriptionVisibilityService.calculate(1L, 10L)).thenReturn(visibilityResult(List.of(
                visibleSpu(20L, 30L, 40L, 50L, 2),
                visibleSpu(21L, 31L, 41L, 51L, 3))));
        when(subscriptionSupportService.getPublicationSpuMap(any())).thenReturn(Map.of(
                30L, productSpu(30L, "刊物A", 1000L, 100),
                31L, productSpu(31L, "刊物B", 1001L, 200)));
        when(subscriptionSupportService.getCategoryMap(any())).thenReturn(Map.of(
                1000L, category(1000L, "小学"),
                1001L, category(1001L, "初中")));
        when(subscriptionSupportService.getSpuPublicationMap(any())).thenReturn(Map.of(
                30L, spuPublication(30L, 2000L),
                31L, spuPublication(31L, 2001L)));
        when(subscriptionSupportService.getPublicationTitleMap(any())).thenReturn(Map.of(
                2000L, title(2000L, "主档A"),
                2001L, title(2001L, "主档B")));
        when(subscriptionSupportService.getSkuMapBySpuIds(any())).thenReturn(Map.of(
                30L, List.of(productSku(50L, 30L, 100)),
                31L, List.of(productSku(51L, 31L, 200))));
        when(subscriptionSupportService.getSkuPublicationMap(any())).thenReturn(Map.of(
                50L, skuPublication(50L, "上册"),
                51L, skuPublication(51L, "下册")));

        List<AppSubscriptionPublicationRespVO> result = service.getPublicationListBySpuIds(100L, 1L,
                List.of(31L, 99L, 30L));

        assertEquals(List.of(31L, 30L),
                result.stream().map(AppSubscriptionPublicationRespVO::getProductSpuId).toList());
        assertEquals("刊物B", result.get(0).getProductName());
        assertEquals(41L, result.get(0).getSkus().get(0).getWindowSkuId());
        assertEquals(3, result.get(0).getSkus().get(0).getMaxQuantityPerStudent());
        assertEquals("主档A", result.get(1).getPublicationTitleName());
    }

    @Test
    void getPublicationListBySpuIdsShouldRejectStudentOutsideParent() {
        when(subscriptionWindowService.getCurrentOpenWindow()).thenReturn(openWindow(10L));
        when(subscriptionSupportService.getStudent(1L)).thenReturn(StudentDO.builder()
                .id(1L)
                .belongTo(200L)
                .build());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.getPublicationListBySpuIds(100L, 1L, List.of(30L)));

        assertEquals(ErrorCodeConstants.APP_STUDENT_NOT_BELONG_TO_PARENT.getCode(), exception.getCode());
    }

    private SubscriptionWindowDO openWindow(Long id) {
        return SubscriptionWindowDO.builder()
                .id(id)
                .build();
    }

    private SubscriptionVisibilityResultBO visibilityResult(List<SubscriptionVisibleSpuBO> visibleSpus) {
        SubscriptionVisibilityResultBO resultBO = new SubscriptionVisibilityResultBO();
        resultBO.setVisibleSpus(visibleSpus);
        return resultBO;
    }

    private SubscriptionVisibleSpuBO visibleSpu(Long windowSpuId, Long productSpuId, Long windowSkuId,
                                                 Long productSkuId, Integer maxQuantityPerStudent) {
        SubscriptionVisibleSpuBO visibleSpu = new SubscriptionVisibleSpuBO();
        visibleSpu.setWindowSpu(SubscriptionWindowSpuDO.builder()
                .id(windowSpuId)
                .productSpuId(productSpuId)
                .recommendFlag(Boolean.TRUE)
                .build());
        visibleSpu.setWindowSkus(List.of(SubscriptionWindowSkuDO.builder()
                .id(windowSkuId)
                .productSkuId(productSkuId)
                .maxQuantityPerStudent(maxQuantityPerStudent)
                .build()));
        return visibleSpu;
    }

    private ProductSpuDO productSpu(Long id, String name, Long categoryId, Integer price) {
        return ProductSpuDO.builder()
                .id(id)
                .name(name)
                .categoryId(categoryId)
                .picUrl("https://example.com/" + id + ".png")
                .price(price)
                .keyword(name)
                .introduction(name + "简介")
                .description(name + "描述")
                .build();
    }

    private ProductCategoryDO category(Long id, String name) {
        ProductCategoryDO category = new ProductCategoryDO();
        category.setId(id);
        category.setName(name);
        return category;
    }

    private ProductSpuPublicationDO spuPublication(Long productSpuId, Long publicationTitleId) {
        return ProductSpuPublicationDO.builder()
                .productSpuId(productSpuId)
                .publicationTitleId(publicationTitleId)
                .build();
    }

    private ProductPublicationTitleDO title(Long id, String name) {
        return ProductPublicationTitleDO.builder()
                .id(id)
                .name(name)
                .build();
    }

    private ProductSkuDO productSku(Long id, Long spuId, Integer price) {
        return ProductSkuDO.builder()
                .id(id)
                .spuId(spuId)
                .price(price)
                .build();
    }

    private ProductSkuPublicationDO skuPublication(Long productSkuId, String volumeLabel) {
        return ProductSkuPublicationDO.builder()
                .productSkuId(productSkuId)
                .volumeLabel(volumeLabel)
                .build();
    }
}
