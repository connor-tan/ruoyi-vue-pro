package cn.iocoder.yudao.module.trade.service.config;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.trade.controller.admin.config.vo.TradeConfigSaveReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.config.TradeConfigMapper;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageBindModeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageEnabledConditionEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawTypeEnum;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeConfigServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TradeConfigServiceImpl tradeConfigService;

    @Mock
    private TradeConfigMapper tradeConfigMapper;

    @Test
    void saveTradeConfig_brokerageDisabledAndNoDb_insertDefaults() {
        // 准备参数
        when(tradeConfigMapper.selectList()).thenReturn(Collections.emptyList());
        TradeConfigSaveReqVO reqVO = createBaseReqVO(false);

        // 调用
        tradeConfigService.saveTradeConfig(reqVO);

        // 断言
        ArgumentCaptor<TradeConfigDO> captor = ArgumentCaptor.forClass(TradeConfigDO.class);
        verify(tradeConfigMapper).insert(captor.capture());
        TradeConfigDO config = captor.getValue();
        assertFalse(config.getBrokerageEnabled());
        assertEquals(BrokerageEnabledConditionEnum.ADMIN.getCondition(), config.getBrokerageEnabledCondition());
        assertEquals(BrokerageBindModeEnum.ANYTIME.getMode(), config.getBrokerageBindMode());
        assertEquals(0, config.getBrokerageFirstPercent());
        assertEquals(0, config.getBrokerageSecondPercent());
        assertEquals(0, config.getBrokerageWithdrawMinPrice());
        assertEquals(0, config.getBrokerageWithdrawFeePercent());
        assertEquals(0, config.getBrokerageFrozenDays());
        assertEquals(Collections.emptyList(), config.getBrokeragePosterUrls());
        assertEquals(Collections.emptyList(), config.getBrokerageWithdrawTypes());
    }

    @Test
    void saveTradeConfig_brokerageDisabledAndDbExists_preserveMissingBrokerageFields() {
        // 准备参数
        TradeConfigDO dbConfig = new TradeConfigDO().setId(10L)
                .setBrokerageEnabledCondition(BrokerageEnabledConditionEnum.ALL.getCondition())
                .setBrokerageBindMode(BrokerageBindModeEnum.REGISTER.getMode())
                .setBrokeragePosterUrls(Collections.singletonList("https://example.com/poster.png"))
                .setBrokerageFirstPercent(5)
                .setBrokerageSecondPercent(3)
                .setBrokerageWithdrawMinPrice(100)
                .setBrokerageWithdrawFeePercent(2)
                .setBrokerageFrozenDays(7)
                .setBrokerageWithdrawTypes(Collections.singletonList(BrokerageWithdrawTypeEnum.WALLET.getType()));
        when(tradeConfigMapper.selectList()).thenReturn(Collections.singletonList(dbConfig));
        TradeConfigSaveReqVO reqVO = createBaseReqVO(false);

        // 调用
        tradeConfigService.saveTradeConfig(reqVO);

        // 断言
        ArgumentCaptor<TradeConfigDO> captor = ArgumentCaptor.forClass(TradeConfigDO.class);
        verify(tradeConfigMapper).updateById(captor.capture());
        TradeConfigDO config = captor.getValue();
        assertEquals(10L, config.getId());
        assertFalse(config.getBrokerageEnabled());
        assertEquals(dbConfig.getBrokerageEnabledCondition(), config.getBrokerageEnabledCondition());
        assertEquals(dbConfig.getBrokerageBindMode(), config.getBrokerageBindMode());
        assertEquals(dbConfig.getBrokeragePosterUrls(), config.getBrokeragePosterUrls());
        assertEquals(dbConfig.getBrokerageFirstPercent(), config.getBrokerageFirstPercent());
        assertEquals(dbConfig.getBrokerageSecondPercent(), config.getBrokerageSecondPercent());
        assertEquals(dbConfig.getBrokerageWithdrawMinPrice(), config.getBrokerageWithdrawMinPrice());
        assertEquals(dbConfig.getBrokerageWithdrawFeePercent(), config.getBrokerageWithdrawFeePercent());
        assertEquals(dbConfig.getBrokerageFrozenDays(), config.getBrokerageFrozenDays());
        assertEquals(dbConfig.getBrokerageWithdrawTypes(), config.getBrokerageWithdrawTypes());
    }

    @Test
    void saveTradeConfig_brokerageEnabledAndWithdrawTypesEmpty_throwException() {
        // 准备参数
        TradeConfigSaveReqVO reqVO = createBaseReqVO(true);
        reqVO.setBrokerageEnabledCondition(BrokerageEnabledConditionEnum.ADMIN.getCondition())
                .setBrokerageBindMode(BrokerageBindModeEnum.ANYTIME.getMode())
                .setBrokerageFirstPercent(0)
                .setBrokerageSecondPercent(0)
                .setBrokerageWithdrawMinPrice(0)
                .setBrokerageWithdrawFeePercent(0)
                .setBrokerageFrozenDays(0)
                .setBrokerageWithdrawTypes(Collections.emptyList());

        // 调用，并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> tradeConfigService.saveTradeConfig(reqVO));
        assertEquals(BAD_REQUEST.getCode(), exception.getCode());
        assertEquals("提现方式不能为空", exception.getMessage());
    }

    private static TradeConfigSaveReqVO createBaseReqVO(Boolean brokerageEnabled) {
        TradeConfigSaveReqVO reqVO = new TradeConfigSaveReqVO();
        reqVO.setAfterSaleRefundReasons(List.of("不想要了"));
        reqVO.setAfterSaleReturnReasons(List.of("商品损坏"));
        reqVO.setDeliveryExpressFreeEnabled(false);
        reqVO.setDeliveryExpressFreePrice(0);
        reqVO.setDeliveryPickUpEnabled(false);
        reqVO.setBrokerageEnabled(brokerageEnabled);
        return reqVO;
    }

}
