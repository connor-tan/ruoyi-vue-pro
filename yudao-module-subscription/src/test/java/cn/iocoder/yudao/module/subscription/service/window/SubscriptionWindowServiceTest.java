package cn.iocoder.yudao.module.subscription.service.window;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.api.yearcatalog.EduYearCatalogApi;
import cn.iocoder.yudao.module.edu.api.yearcatalog.dto.EduYearCatalogRespDTO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
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
    @InjectMocks
    private SubscriptionWindowService windowService;

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
        SubscriptionWindowSaveReqVO reqVO = windowReq(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setTargetYearNameSnapshot("前端伪造学年");
        reqVO.setTargetYearStart(2099);
        reqVO.setTargetYearEnd(2100);
        when(yearCatalogApi.getYearCatalog(YEAR_CATALOG_ID)).thenReturn(yearCatalog());
        when(windowMapper.selectEnabledOverlapList(isNull(), eq(reqVO.getStartTime()), eq(reqVO.getEndTime()),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(List.of());
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
    void createWindow_shouldAllowDisabledTimeOverlap() {
        SubscriptionWindowSaveReqVO reqVO = windowReq(CommonStatusEnum.DISABLE.getStatus());
        when(yearCatalogApi.getYearCatalog(YEAR_CATALOG_ID)).thenReturn(yearCatalog());

        windowService.createWindow(reqVO);

        verify(windowMapper, never()).selectEnabledOverlapList(any(), any(), any(), any());
        verify(windowMapper).insert(any(SubscriptionWindowDO.class));
    }

    private SubscriptionWindowSaveReqVO windowReq(Integer status) {
        SubscriptionWindowSaveReqVO reqVO = new SubscriptionWindowSaveReqVO();
        reqVO.setName("春季订刊");
        reqVO.setTargetYearCatalogId(YEAR_CATALOG_ID);
        reqVO.setTargetPeriod("FULL_YEAR");
        reqVO.setStartTime(LocalDateTime.of(2026, 5, 1, 0, 0));
        reqVO.setEndTime(LocalDateTime.of(2026, 6, 1, 0, 0));
        reqVO.setGradeCalcRule("CURRENT_GRADE");
        reqVO.setGradeResolveMode("CURRENT_CHAIN");
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

}
