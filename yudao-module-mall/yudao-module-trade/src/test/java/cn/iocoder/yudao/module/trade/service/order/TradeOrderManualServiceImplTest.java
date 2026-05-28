package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.subscription.api.order.SubscriptionOrderEligibilityApi;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderManualCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.redis.no.TradeNoRedisDAO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderDeliveryGroupSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeOrderManualServiceImplTest {

    private static final long STUDENT_ID = 10L;
    private static final long OFFER_SKU_ID = 20L;
    private static final long SECOND_OFFER_SKU_ID = 21L;
    private static final long PRODUCT_SKU_ID = 30L;
    private static final long SECOND_PRODUCT_SKU_ID = 31L;
    private static final long PRODUCT_SPU_ID = 40L;
    private static final long SECOND_PRODUCT_SPU_ID = 41L;
    private static final int PRODUCT_PRICE = 1500;

    @Mock
    private TradeOrderMapper tradeOrderMapper;
    @Mock
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Mock
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Mock
    private TradeNoRedisDAO tradeNoRedisDAO;
    @Mock
    private ProductSkuApi productSkuApi;
    @Mock
    private ProductSpuApi productSpuApi;
    @Mock
    private SubscriptionOrderEligibilityApi subscriptionOrderEligibilityApi;
    @Spy
    private TradeOrderDeliveryGroupSupport deliveryGroupSupport = new TradeOrderDeliveryGroupSupport();
    @Mock
    private TradeOrderPublicationIssueService publicationIssueService;
    @Mock
    private TradeOrderHandler tradeOrderHandler;
    @Mock
    private PayOrderApi payOrderApi;
    @Mock
    private TradeOrderProperties tradeOrderProperties;
    @InjectMocks
    private TradeOrderManualServiceImpl tradeOrderManualService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tradeOrderManualService, "tradeOrderHandlers", List.of(tradeOrderHandler));
    }

    @Test
    void createManualOrder_shouldLockPublicationAnchor() {
        mockProducts(Map.of(PRODUCT_SKU_ID, productSku(PRODUCT_SKU_ID, PRODUCT_SPU_ID)),
                Map.of(PRODUCT_SPU_ID, publicationSpu(PRODUCT_SPU_ID)));
        when(subscriptionOrderEligibilityApi.validateOrder(any())).thenReturn(eligibility(OFFER_SKU_ID, 100L));
        mockPersistence("NO202605250001");

        tradeOrderManualService.createManualOrder(manualReq(List.of(singlePublicationItem(PRODUCT_SKU_ID, OFFER_SKU_ID, 2))));

        ArgumentCaptor<SubscriptionOrderEligibilityReqDTO> captor =
                ArgumentCaptor.forClass(SubscriptionOrderEligibilityReqDTO.class);
        verify(subscriptionOrderEligibilityApi).validateOrder(captor.capture());
        verify(subscriptionOrderEligibilityApi, never()).validateOrderList(any());
        SubscriptionOrderEligibilityReqDTO reqDTO = captor.getValue();
        assertEquals(Boolean.TRUE, reqDTO.getLockAnchor());
        assertEquals(Boolean.TRUE, reqDTO.getAdmin());
        assertEquals(STUDENT_ID, reqDTO.getStudentId());
        assertEquals(OFFER_SKU_ID, reqDTO.getOfferSkuId());
        assertEquals(PRODUCT_SKU_ID, reqDTO.getSkuId());
        assertEquals(2, reqDTO.getCount());
    }

    @Test
    void createImportOrder_shouldUseBatchEligibilityAndLockPublicationAnchors() {
        mockProducts(Map.of(
                        PRODUCT_SKU_ID, productSku(PRODUCT_SKU_ID, PRODUCT_SPU_ID),
                        SECOND_PRODUCT_SKU_ID, productSku(SECOND_PRODUCT_SKU_ID, SECOND_PRODUCT_SPU_ID)),
                Map.of(
                        PRODUCT_SPU_ID, publicationSpu(PRODUCT_SPU_ID),
                        SECOND_PRODUCT_SPU_ID, publicationSpu(SECOND_PRODUCT_SPU_ID)));
        when(subscriptionOrderEligibilityApi.validateOrderList(any())).thenReturn(List.of(
                eligibility(OFFER_SKU_ID, 100L),
                eligibility(SECOND_OFFER_SKU_ID, 101L)));
        mockPersistence("NO202605250002");

        tradeOrderManualService.createImportOrder(manualReq(List.of(
                singlePublicationItem(PRODUCT_SKU_ID, OFFER_SKU_ID, 1),
                singlePublicationItem(SECOND_PRODUCT_SKU_ID, SECOND_OFFER_SKU_ID, 3))));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SubscriptionOrderEligibilityReqDTO>> captor =
                ArgumentCaptor.forClass(List.class);
        verify(subscriptionOrderEligibilityApi).validateOrderList(captor.capture());
        verify(subscriptionOrderEligibilityApi, never()).validateOrder(any());
        List<SubscriptionOrderEligibilityReqDTO> reqList = captor.getValue();
        assertEquals(List.of(STUDENT_ID, STUDENT_ID), reqList.stream()
                .map(SubscriptionOrderEligibilityReqDTO::getStudentId).toList());
        assertEquals(List.of(OFFER_SKU_ID, SECOND_OFFER_SKU_ID), reqList.stream()
                .map(SubscriptionOrderEligibilityReqDTO::getOfferSkuId).toList());
        assertEquals(List.of(PRODUCT_SKU_ID, SECOND_PRODUCT_SKU_ID), reqList.stream()
                .map(SubscriptionOrderEligibilityReqDTO::getSkuId).toList());
        assertEquals(List.of(1, 3), reqList.stream()
                .map(SubscriptionOrderEligibilityReqDTO::getCount).toList());
        assertTrue(reqList.stream().allMatch(req -> Boolean.TRUE.equals(req.getLockAnchor())));
        assertTrue(reqList.stream().allMatch(req -> Boolean.TRUE.equals(req.getAdmin())));
    }

    private void mockProducts(Map<Long, ProductSkuRespDTO> skuMap, Map<Long, ProductSpuRespDTO> spuMap) {
        when(productSkuApi.getSkuList(anySet())).thenReturn(List.copyOf(skuMap.values()));
        when(productSpuApi.validateSpuList(anySet())).thenReturn(List.copyOf(spuMap.values()));
    }

    private void mockPersistence(String orderNo) {
        when(tradeNoRedisDAO.generate(any())).thenReturn(orderNo);
        when(tradeOrderMapper.insert(any(TradeOrderDO.class))).thenAnswer(invocation -> {
            TradeOrderDO order = invocation.getArgument(0);
            order.setId(1L);
            return 1;
        });
        when(tradeOrderDeliveryMapper.insert(any(TradeOrderDeliveryDO.class))).thenAnswer(invocation -> {
            TradeOrderDeliveryDO delivery = invocation.getArgument(0);
            delivery.setId(100L);
            return 1;
        });
        doReturn(Boolean.TRUE).when(tradeOrderItemMapper).insertBatch(any());
    }

    private TradeOrderManualCreateReqVO manualReq(List<TradeOrderManualCreateReqVO.Item> items) {
        return new TradeOrderManualCreateReqVO()
                .setDeliveryType(DeliveryTypeEnum.SCHOOL.getType())
                .setItems(items);
    }

    private TradeOrderManualCreateReqVO.Item singlePublicationItem(Long skuId, Long offerSkuId, Integer count) {
        return new TradeOrderManualCreateReqVO.Item()
                .setSkuId(skuId)
                .setCount(count)
                .setDeliveryType(DeliveryTypeEnum.SCHOOL.getType())
                .setStudentId(STUDENT_ID)
                .setOfferSkuId(offerSkuId);
    }

    private ProductSkuRespDTO productSku(Long skuId, Long spuId) {
        ProductSkuRespDTO sku = new ProductSkuRespDTO();
        sku.setId(skuId);
        sku.setSpuId(spuId);
        sku.setPrice(PRODUCT_PRICE);
        sku.setStock(100);
        return sku;
    }

    private ProductSpuRespDTO publicationSpu(Long spuId) {
        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setId(spuId);
        spu.setName("测试刊物" + spuId);
        spu.setBizScene(BizSceneEnum.PUBLICATION.getCode());
        spu.setStatus(ProductSpuStatusEnum.ENABLE.getStatus());
        spu.setDeliveryTypes(List.of(DeliveryTypeEnum.SCHOOL.getType(), DeliveryTypeEnum.EXPRESS.getType()));
        spu.setGiveIntegral(0);
        return spu;
    }

    private SubscriptionOrderEligibilityRespDTO eligibility(Long offerSkuId, Long offerId) {
        SubscriptionOrderEligibilityRespDTO respDTO = new SubscriptionOrderEligibilityRespDTO();
        respDTO.setStudentId(STUDENT_ID);
        respDTO.setStudentNameSnapshot("测试学生");
        respDTO.setSchoolId(200L);
        respDTO.setSchoolNameSnapshot("测试学校");
        respDTO.setSchoolAddressSnapshot("测试学校地址");
        respDTO.setClassId(201L);
        respDTO.setClassNameSnapshot("一年级1班");
        respDTO.setGradeCatalogId(202L);
        respDTO.setGradeNameSnapshot("一年级");
        respDTO.setWarehouseId(300L);
        respDTO.setWarehouseNameSnapshot("测试仓库");
        respDTO.setWarehouseAddressSnapshot("测试仓库地址");
        respDTO.setContactName("张老师");
        respDTO.setContactMobile("13900000000");
        respDTO.setWindowId(400L);
        respDTO.setWindowNameSnapshot("2026 秋季订刊");
        respDTO.setOfferId(offerId);
        respDTO.setOfferSkuId(offerSkuId);
        return respDTO;
    }

}
