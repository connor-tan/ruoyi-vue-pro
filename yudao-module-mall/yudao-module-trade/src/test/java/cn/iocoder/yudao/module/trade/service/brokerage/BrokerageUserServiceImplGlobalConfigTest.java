package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.BrokerageUserMapper;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageEnabledConditionEnum;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BrokerageUserServiceImplGlobalConfigTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BrokerageUserServiceImpl brokerageUserService;

    @Mock
    private BrokerageUserMapper brokerageUserMapper;
    @Mock
    private TradeConfigService tradeConfigService;
    @Mock
    private MemberUserApi memberUserApi;

    @Test
    void getOrCreateBrokerageUser_globalDisabled_doesNotCreate() {
        // 准备参数
        Long userId = 10L;
        when(brokerageUserMapper.selectById(userId)).thenReturn(null);
        when(tradeConfigService.getTradeConfig()).thenReturn(new TradeConfigDO()
                .setBrokerageEnabled(false)
                .setBrokerageEnabledCondition(BrokerageEnabledConditionEnum.ALL.getCondition()));

        // 调用
        BrokerageUserDO brokerageUser = brokerageUserService.getOrCreateBrokerageUser(userId);

        // 断言
        assertNull(brokerageUser);
        verify(brokerageUserMapper, never()).insert(any(BrokerageUserDO.class));
    }

    @Test
    void getOrCreateBrokerageUser_globalEnabledAndAllCondition_createUser() {
        // 准备参数
        Long userId = 10L;
        when(brokerageUserMapper.selectById(userId)).thenReturn(null);
        when(tradeConfigService.getTradeConfig()).thenReturn(new TradeConfigDO()
                .setBrokerageEnabled(true)
                .setBrokerageEnabledCondition(BrokerageEnabledConditionEnum.ALL.getCondition()));

        // 调用
        BrokerageUserDO brokerageUser = brokerageUserService.getOrCreateBrokerageUser(userId);

        // 断言
        ArgumentCaptor<BrokerageUserDO> captor = ArgumentCaptor.forClass(BrokerageUserDO.class);
        verify(brokerageUserMapper).insert(captor.capture());
        assertSame(brokerageUser, captor.getValue());
        assertEquals(userId, brokerageUser.getId());
        assertTrue(brokerageUser.getBrokerageEnabled());
        assertEquals(0, brokerageUser.getBrokeragePrice());
        assertEquals(0, brokerageUser.getFrozenPrice());
        assertNotNull(brokerageUser.getBrokerageTime());
    }

}
