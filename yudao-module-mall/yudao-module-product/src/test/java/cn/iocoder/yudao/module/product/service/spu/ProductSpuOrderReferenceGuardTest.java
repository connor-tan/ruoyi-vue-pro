package cn.iocoder.yudao.module.product.service.spu;

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuUpdateStatusReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuCategoryRelMapper;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.enums.sku.ProductSkuStatusEnum;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.product.mq.producer.spu.ProductSpuProducer;
import cn.iocoder.yudao.module.product.service.brand.ProductBrandService;
import cn.iocoder.yudao.module.product.service.category.ProductCategoryService;
import cn.iocoder.yudao.module.product.service.publication.ProductPublicationService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import cn.iocoder.yudao.module.product.service.spu.scene.ProductSceneHandler;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.trade.api.order.TradeSubscriptionOrderApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PUBLICATION_PRODUCT_ORDER_REFERENCED;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PUBLICATION_SKU_ORDER_REFERENCED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSpuOrderReferenceGuardTest {

    @Mock
    private ProductSpuMapper productSpuMapper;
    @Mock
    private ProductSpuCategoryRelMapper productSpuCategoryRelMapper;
    @Mock
    private ProductSkuService productSkuService;
    @Mock
    private ProductBrandService brandService;
    @Mock
    private ProductCategoryService categoryService;
    @Mock
    private ProductPublicationService productPublicationService;
    @Mock
    private TradeSubscriptionOrderApi tradeSubscriptionOrderApi;
    @Mock
    private ProductSceneHandler publicationSceneHandler;
    @Mock
    private ProductSceneHandler normalSceneHandler;
    @Mock
    private ProductSpuProducer productSpuProducer;
    @InjectMocks
    private ProductSpuServiceImpl productSpuService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productSpuService, "productSceneHandlers",
                List.of(publicationSceneHandler, normalSceneHandler));
        lenient().when(publicationSceneHandler.getBizScene()).thenReturn(BizSceneEnum.PUBLICATION.getCode());
        lenient().when(normalSceneHandler.getBizScene()).thenReturn(BizSceneEnum.NORMAL.getCode());
    }

    @Test
    void updateSpuStatus_shouldAllowDisableReferencedPublication() {
        when(productSpuMapper.selectById(1L)).thenReturn(publicationSpu(ProductSpuStatusEnum.ENABLE));

        productSpuService.updateSpuStatus(statusReq(1L, ProductSpuStatusEnum.DISABLE));

        ArgumentCaptor<ProductSpuDO> captor = ArgumentCaptor.forClass(ProductSpuDO.class);
        verify(productSpuMapper).updateById(captor.capture());
        assertEquals(ProductSpuStatusEnum.DISABLE.getStatus(), captor.getValue().getStatus());
        verify(tradeSubscriptionOrderApi, never()).hasPublicationOrderReferenceByProductSpuId(any());
    }

    @Test
    void updateSpuStatus_shouldRejectRecycleReferencedPublication() {
        when(productSpuMapper.selectById(1L)).thenReturn(publicationSpu(ProductSpuStatusEnum.ENABLE));
        when(tradeSubscriptionOrderApi.hasPublicationOrderReferenceByProductSpuId(1L)).thenReturn(true);

        assertServiceException(() -> productSpuService.updateSpuStatus(statusReq(1L, ProductSpuStatusEnum.RECYCLE)),
                PUBLICATION_PRODUCT_ORDER_REFERENCED);

        verify(productSpuMapper, never()).updateById(any(ProductSpuDO.class));
    }

    @Test
    void updateSpuStatus_shouldKeepNormalProductBehavior() {
        when(productSpuMapper.selectById(1L)).thenReturn(normalSpu(ProductSpuStatusEnum.ENABLE));

        productSpuService.updateSpuStatus(statusReq(1L, ProductSpuStatusEnum.RECYCLE));

        ArgumentCaptor<ProductSpuDO> captor = ArgumentCaptor.forClass(ProductSpuDO.class);
        verify(productSpuMapper).updateById(captor.capture());
        assertEquals(ProductSpuStatusEnum.RECYCLE.getStatus(), captor.getValue().getStatus());
        verify(tradeSubscriptionOrderApi, never()).hasPublicationOrderReferenceByProductSpuId(any());
    }

    @Test
    void deleteSpu_shouldRejectReferencedPublicationInRecycle() {
        when(productSpuMapper.selectById(1L)).thenReturn(publicationSpu(ProductSpuStatusEnum.RECYCLE));
        when(tradeSubscriptionOrderApi.hasPublicationOrderReferenceByProductSpuId(1L)).thenReturn(true);

        assertServiceException(() -> productSpuService.deleteSpu(1L), PUBLICATION_PRODUCT_ORDER_REFERENCED);

        verify(productPublicationService, never()).clearPublication(any(), any());
        verify(productSpuMapper, never()).deleteById(any());
        verify(productSkuService, never()).deleteSkuBySpuId(any());
    }

    @Test
    void updateSpu_shouldRejectReferencedPublicationBizSceneChange() {
        when(productSpuMapper.selectById(1L)).thenReturn(publicationSpu(ProductSpuStatusEnum.ENABLE));
        when(productSkuService.getSkuListBySpuId(1L)).thenReturn(List.of(sku(10L)));
        when(tradeSubscriptionOrderApi.hasPublicationOrderReferenceByProductSpuId(1L)).thenReturn(true);

        assertServiceException(() -> productSpuService.updateSpu(updateReq(BizSceneEnum.NORMAL.getCode(), skuReq(10L))),
                PUBLICATION_PRODUCT_ORDER_REFERENCED);

        verify(productSpuMapper, never()).updateById(any(ProductSpuDO.class));
        verify(productSkuService, never()).updateSkuList(any(), any());
    }

    @Test
    void updateSpu_shouldRejectPublicationBizSceneChangeWhenOnlySkuReferenced() {
        when(productSpuMapper.selectById(1L)).thenReturn(publicationSpu(ProductSpuStatusEnum.ENABLE));
        when(productSkuService.getSkuListBySpuId(1L)).thenReturn(List.of(sku(10L)));
        when(tradeSubscriptionOrderApi.getPublicationOrderReferencedProductSkuIds(argThat(ids ->
                ids.size() == 1 && ids.contains(10L)))).thenReturn(Set.of(10L));

        assertServiceException(() -> productSpuService.updateSpu(updateReq(BizSceneEnum.NORMAL.getCode(), skuReq(10L))),
                PUBLICATION_PRODUCT_ORDER_REFERENCED);

        verify(productSpuMapper, never()).updateById(any(ProductSpuDO.class));
        verify(productSkuService, never()).updateSkuList(any(), any());
    }

    @Test
    void updateSpu_shouldRejectRemovingReferencedPublicationSku() {
        when(productSpuMapper.selectById(1L)).thenReturn(publicationSpu(ProductSpuStatusEnum.ENABLE));
        when(productSkuService.getSkuListBySpuId(1L)).thenReturn(List.of(sku(10L), sku(11L)));
        when(tradeSubscriptionOrderApi.getPublicationOrderReferencedProductSkuIds(argThat(ids ->
                ids.size() == 1 && ids.contains(10L)))).thenReturn(Set.of(10L));

        assertServiceException(() -> productSpuService.updateSpu(
                updateReq(BizSceneEnum.PUBLICATION.getCode(), skuReq(11L))), PUBLICATION_SKU_ORDER_REFERENCED);

        verify(productSpuMapper, never()).updateById(any(ProductSpuDO.class));
        verify(productSkuService, never()).updateSkuList(any(), any());
    }

    @Test
    void updateSpu_shouldAllowDisablingReferencedPublicationSku() {
        when(productSpuMapper.selectById(1L)).thenReturn(publicationSpu(ProductSpuStatusEnum.ENABLE));
        when(productSkuService.getSkuListBySpuId(1L)).thenReturn(List.of(sku(10L)));
        when(categoryService.validateLeafCategoryList(BizSceneEnum.PUBLICATION.getCode(), List.of(100L)))
                .thenReturn(List.of(new ProductCategoryDO().setId(100L)));
        when(productSkuService.updateSkuList(1L, List.of(skuReq(10L, ProductSkuStatusEnum.DISABLE))))
                .thenReturn(List.of(sku(10L)));

        productSpuService.updateSpu(updateReq(BizSceneEnum.PUBLICATION.getCode(),
                skuReq(10L, ProductSkuStatusEnum.DISABLE)));

        verify(tradeSubscriptionOrderApi, never()).getPublicationOrderReferencedProductSkuIds(any());
        verify(productSpuMapper).updateById(any(ProductSpuDO.class));
        verify(productSkuService).updateSkuList(1L, List.of(skuReq(10L, ProductSkuStatusEnum.DISABLE)));
    }

    private ProductSpuDO publicationSpu(ProductSpuStatusEnum status) {
        return ProductSpuDO.builder()
                .id(1L)
                .bizScene(BizSceneEnum.PUBLICATION.getCode())
                .status(status.getStatus())
                .build();
    }

    private ProductSpuDO normalSpu(ProductSpuStatusEnum status) {
        return ProductSpuDO.builder()
                .id(1L)
                .bizScene(BizSceneEnum.NORMAL.getCode())
                .status(status.getStatus())
                .build();
    }

    private ProductSkuDO sku(Long id) {
        return ProductSkuDO.builder().id(id).build();
    }

    private ProductSpuUpdateStatusReqVO statusReq(Long id, ProductSpuStatusEnum status) {
        ProductSpuUpdateStatusReqVO reqVO = new ProductSpuUpdateStatusReqVO();
        reqVO.setId(id);
        reqVO.setStatus(status.getStatus());
        return reqVO;
    }

    private ProductSpuSaveReqVO updateReq(String bizScene, ProductSkuSaveReqVO... skus) {
        ProductSpuSaveReqVO reqVO = new ProductSpuSaveReqVO();
        reqVO.setId(1L);
        reqVO.setName("测试刊物");
        reqVO.setKeyword("测试");
        reqVO.setIntroduction("简介");
        reqVO.setDescription("详情");
        reqVO.setBizScene(bizScene);
        reqVO.setCategoryIds(List.of(100L));
        reqVO.setPicUrl("https://example.com/a.png");
        reqVO.setSort(1);
        reqVO.setSpecType(true);
        reqVO.setGiveIntegral(0);
        reqVO.setSubCommissionType(false);
        reqVO.setSkus(List.of(skus));
        return reqVO;
    }

    private ProductSkuSaveReqVO skuReq(Long id) {
        return skuReq(id, ProductSkuStatusEnum.ENABLE);
    }

    private ProductSkuSaveReqVO skuReq(Long id, ProductSkuStatusEnum status) {
        ProductSkuSaveReqVO reqVO = new ProductSkuSaveReqVO();
        reqVO.setId(id);
        reqVO.setName("SKU-" + id);
        reqVO.setPrice(1000);
        reqVO.setMarketPrice(1000);
        reqVO.setCostPrice(800);
        reqVO.setPicUrl("https://example.com/sku.png");
        reqVO.setStock(0);
        reqVO.setStatus(status.getStatus());
        return reqVO;
    }

}
