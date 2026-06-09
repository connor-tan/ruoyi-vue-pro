package cn.iocoder.yudao.module.subscription.service.window;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.api.yearcatalog.EduYearCatalogApi;
import cn.iocoder.yudao.module.edu.api.yearcatalog.dto.EduYearCatalogRespDTO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferMapper;
import cn.iocoder.yudao.module.subscription.service.offersku.SubscriptionOfferSkuAvailabilityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_SKU_EFFECTIVE_REQUIRED;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.WINDOW_TIME_OVERLAP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionWindowServiceTest {

    private static final long YEAR_CATALOG_ID = 100L;

    @Mock
    private SubscriptionWindowMapper windowMapper;
    @Mock
    private EduYearCatalogApi yearCatalogApi;
    @Mock
    private SubscriptionWindowOfferMapper offerMapper;
    @Mock
    private SubscriptionOfferSkuAvailabilityValidator offerSkuAvailabilityValidator;
    @InjectMocks
    private SubscriptionWindowServiceImpl windowService;

    @BeforeEach
    void setUp() {
        lenient().when(windowMapper.getWindowMutationLock(anyString(), anyInt())).thenReturn(1);
        lenient().when(windowMapper.releaseWindowMutationLock(anyString())).thenReturn(1);
    }

    @Test
    void createWindow_shouldRejectEnabledTimeOverlap() {
        SubscriptionWindowSaveReqVO reqVO = windowReq(CommonStatusEnum.ENABLE.getStatus());
        when(yearCatalogApi.getYearCatalog(YEAR_CATALOG_ID)).thenReturn(yearCatalog());
        when(windowMapper.selectEnabledOverlapList(isNull(), eq(reqVO.getStartTime()), eq(reqVO.getEndTime()),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(List.of(new SubscriptionWindowDO()));

        assertServiceException(() -> windowService.createWindow(reqVO), WINDOW_TIME_OVERLAP);

        verify(windowMapper, never()).insert(any(SubscriptionWindowDO.class));
    }

    @Test
    void createWindow_shouldDeriveTargetYearSnapshotFromEduApi() {
        SubscriptionWindowSaveReqVO reqVO = windowReq(CommonStatusEnum.DISABLE.getStatus());
        reqVO.setTargetYearNameSnapshot("前端伪造学年");
        reqVO.setTargetYearStart(2099);
        reqVO.setTargetYearEnd(2100);
        when(yearCatalogApi.getYearCatalog(YEAR_CATALOG_ID)).thenReturn(yearCatalog());
        ArgumentCaptor<SubscriptionWindowDO> captor = ArgumentCaptor.forClass(SubscriptionWindowDO.class);

        windowService.createWindow(reqVO);

        verify(windowMapper).insert(captor.capture());
        SubscriptionWindowDO window = captor.getValue();
        assertEquals("2026-2027学年", window.getTargetYearNameSnapshot());
        assertEquals(2026, window.getTargetYearStart());
        assertEquals(2027, window.getTargetYearEnd());
        assertEquals("AUTO_TARGET_YEAR_GRADE", window.getGradeCalcRule());
        assertEquals("AUTO_TARGET_YEAR_GRADE", window.getGradeResolveMode());
    }

    @Test
    void createWindow_shouldRejectEnabledWithoutOfferSkus() {
        SubscriptionWindowSaveReqVO reqVO = windowReq(CommonStatusEnum.ENABLE.getStatus());
        when(yearCatalogApi.getYearCatalog(YEAR_CATALOG_ID)).thenReturn(yearCatalog());
        when(windowMapper.selectEnabledOverlapList(isNull(), eq(reqVO.getStartTime()), eq(reqVO.getEndTime()),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(List.of());

        assertServiceException(() -> windowService.createWindow(reqVO), OFFER_SKU_EFFECTIVE_REQUIRED);

        verify(windowMapper, never()).insert(any(SubscriptionWindowDO.class));
    }

    @Test
    void createWindow_shouldAllowDisabledTimeOverlap() {
        SubscriptionWindowSaveReqVO reqVO = windowReq(CommonStatusEnum.DISABLE.getStatus());
        when(yearCatalogApi.getYearCatalog(YEAR_CATALOG_ID)).thenReturn(yearCatalog());

        windowService.createWindow(reqVO);

        verify(windowMapper, never()).selectEnabledOverlapList(any(), any(), any(), any());
        verify(windowMapper).insert(any(SubscriptionWindowDO.class));
    }

    @Test
    void updateWindow_shouldRejectEnabledWindowWithoutOffers() {
        SubscriptionWindowSaveReqVO reqVO = windowReq(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setId(10L);
        when(windowMapper.selectById(10L)).thenReturn(windowDO(10L, CommonStatusEnum.DISABLE.getStatus()));
        when(yearCatalogApi.getYearCatalog(YEAR_CATALOG_ID)).thenReturn(yearCatalog());
        when(windowMapper.selectEnabledOverlapList(eq(10L), eq(reqVO.getStartTime()), eq(reqVO.getEndTime()),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(List.of());
        when(offerMapper.selectListByWindowId(10L)).thenReturn(List.of());

        assertServiceException(() -> windowService.updateWindow(reqVO), OFFER_SKU_EFFECTIVE_REQUIRED);

        verify(windowMapper, never()).updateById(any(SubscriptionWindowDO.class));
    }

    @Test
    void updateWindowStatus_shouldRejectEnabledWindowWithoutOffers() {
        SubscriptionWindowDO window = windowDO(10L, CommonStatusEnum.DISABLE.getStatus());
        when(windowMapper.selectById(10L)).thenReturn(window);
        when(windowMapper.selectEnabledOverlapList(eq(10L), eq(window.getStartTime()), eq(window.getEndTime()),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(List.of());
        when(offerMapper.selectListByWindowId(10L)).thenReturn(List.of());

        assertServiceException(() -> windowService.updateWindowStatus(10L, CommonStatusEnum.ENABLE.getStatus()),
                OFFER_SKU_EFFECTIVE_REQUIRED);

        verify(windowMapper, never()).updateById(any(SubscriptionWindowDO.class));
    }

    @Test
    void updateWindowStatus_shouldRejectEnabledWindowWithoutEnabledOffers() {
        SubscriptionWindowDO window = windowDO(10L, CommonStatusEnum.DISABLE.getStatus());
        when(windowMapper.selectById(10L)).thenReturn(window);
        when(windowMapper.selectEnabledOverlapList(eq(10L), eq(window.getStartTime()), eq(window.getEndTime()),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(List.of());
        when(offerMapper.selectListByWindowId(10L)).thenReturn(List.of(
                offer(20L, CommonStatusEnum.DISABLE.getStatus())));

        assertServiceException(() -> windowService.updateWindowStatus(10L, CommonStatusEnum.ENABLE.getStatus()),
                OFFER_SKU_EFFECTIVE_REQUIRED);

        verify(offerSkuAvailabilityValidator, never()).validateEnabledOfferHasEffectiveSku(anyLong());
        verify(windowMapper, never()).updateById(any(SubscriptionWindowDO.class));
    }

    @Test
    void updateWindowStatus_shouldRejectEnabledWindowWhenEnabledOfferHasNoEffectiveSku() {
        SubscriptionWindowDO window = windowDO(10L, CommonStatusEnum.DISABLE.getStatus());
        when(windowMapper.selectById(10L)).thenReturn(window);
        when(windowMapper.selectEnabledOverlapList(eq(10L), eq(window.getStartTime()), eq(window.getEndTime()),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(List.of());
        when(offerMapper.selectListByWindowId(10L)).thenReturn(List.of(
                offer(20L, CommonStatusEnum.ENABLE.getStatus())));
        doThrow(exception(OFFER_SKU_EFFECTIVE_REQUIRED))
                .when(offerSkuAvailabilityValidator).validateEnabledOfferHasEffectiveSku(20L);

        assertServiceException(() -> windowService.updateWindowStatus(10L, CommonStatusEnum.ENABLE.getStatus()),
                OFFER_SKU_EFFECTIVE_REQUIRED);

        verify(windowMapper, never()).updateById(any(SubscriptionWindowDO.class));
    }

    @Test
    void updateWindowStatus_shouldAllowEnabledWindowWithEffectiveOfferSku() {
        SubscriptionWindowDO window = windowDO(10L, CommonStatusEnum.DISABLE.getStatus());
        when(windowMapper.selectById(10L)).thenReturn(window);
        when(windowMapper.selectEnabledOverlapList(eq(10L), eq(window.getStartTime()), eq(window.getEndTime()),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(List.of());
        when(offerMapper.selectListByWindowId(10L)).thenReturn(List.of(
                offer(20L, CommonStatusEnum.DISABLE.getStatus()),
                offer(21L, CommonStatusEnum.ENABLE.getStatus())));
        ArgumentCaptor<SubscriptionWindowDO> captor = ArgumentCaptor.forClass(SubscriptionWindowDO.class);

        windowService.updateWindowStatus(10L, CommonStatusEnum.ENABLE.getStatus());

        verify(offerSkuAvailabilityValidator).validateEnabledOfferHasEffectiveSku(21L);
        verify(windowMapper).updateById(captor.capture());
        assertEquals(10L, captor.getValue().getId());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), captor.getValue().getStatus());
    }

    private SubscriptionWindowSaveReqVO windowReq(Integer status) {
        SubscriptionWindowSaveReqVO reqVO = new SubscriptionWindowSaveReqVO();
        reqVO.setName("春季订刊");
        reqVO.setTargetYearCatalogId(YEAR_CATALOG_ID);
        reqVO.setStartTime(LocalDateTime.of(2026, 5, 1, 0, 0));
        reqVO.setEndTime(LocalDateTime.of(2026, 6, 1, 0, 0));
        reqVO.setStatus(status);
        return reqVO;
    }

    private EduYearCatalogRespDTO yearCatalog() {
        EduYearCatalogRespDTO respDTO = new EduYearCatalogRespDTO();
        respDTO.setId(YEAR_CATALOG_ID);
        respDTO.setYearStart(2026);
        respDTO.setYearEnd(2027);
        respDTO.setName("2026-2027学年");
        return respDTO;
    }

    private SubscriptionWindowDO windowDO(Long id, Integer status) {
        return SubscriptionWindowDO.builder()
                .id(id)
                .name("春季订刊")
                .targetYearCatalogId(YEAR_CATALOG_ID)
                .startTime(LocalDateTime.of(2026, 5, 1, 0, 0))
                .endTime(LocalDateTime.of(2026, 6, 1, 0, 0))
                .status(status)
                .build();
    }

    private SubscriptionWindowOfferDO offer(Long id, Integer status) {
        return SubscriptionWindowOfferDO.builder()
                .id(id)
                .windowId(10L)
                .productSpuId(1000L)
                .status(status)
                .build();
    }

}
