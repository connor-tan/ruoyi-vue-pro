package cn.iocoder.yudao.module.trade.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.trade.controller.admin.config.vo.TradeConfigSaveReqVO;
import cn.iocoder.yudao.module.trade.convert.config.TradeConfigConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.config.TradeConfigMapper;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageBindModeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageEnabledConditionEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;

/**
 * 交易中心配置 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class TradeConfigServiceImpl implements TradeConfigService {

    @Resource
    private TradeConfigMapper tradeConfigMapper;

    @Override
    public void saveTradeConfig(TradeConfigSaveReqVO saveReqVO) {
        validateBrokerageConfig(saveReqVO);

        // 存在，则进行更新
        TradeConfigDO dbConfig = getTradeConfig();
        TradeConfigDO config = TradeConfigConvert.INSTANCE.convert(saveReqVO);
        fillBrokerageConfigDefaults(config, dbConfig);
        if (dbConfig != null) {
            tradeConfigMapper.updateById(config.setId(dbConfig.getId()));
            return;
        }
        // 不存在，则进行插入
        tradeConfigMapper.insert(config);
    }

    @Override
    public TradeConfigDO getTradeConfig() {
        List<TradeConfigDO> list = tradeConfigMapper.selectList();
        return CollectionUtils.getFirst(list);
    }

    private void validateBrokerageConfig(TradeConfigSaveReqVO saveReqVO) {
        if (!BooleanUtil.isTrue(saveReqVO.getBrokerageEnabled())) {
            return;
        }
        if (saveReqVO.getBrokerageEnabledCondition() == null) {
            throw invalidParamException("分佣模式不能为空");
        }
        if (saveReqVO.getBrokerageBindMode() == null) {
            throw invalidParamException("分销关系绑定模式不能为空");
        }
        if (saveReqVO.getBrokerageFirstPercent() == null) {
            throw invalidParamException("一级返佣比例不能为空");
        }
        if (saveReqVO.getBrokerageSecondPercent() == null) {
            throw invalidParamException("二级返佣比例不能为空");
        }
        if (saveReqVO.getBrokerageWithdrawMinPrice() == null) {
            throw invalidParamException("用户提现最低金额不能为空");
        }
        if (saveReqVO.getBrokerageWithdrawFeePercent() == null) {
            throw invalidParamException("提现手续费不能为空");
        }
        if (saveReqVO.getBrokerageFrozenDays() == null) {
            throw invalidParamException("佣金冻结时间不能为空");
        }
        if (CollUtil.isEmpty(saveReqVO.getBrokerageWithdrawTypes())) {
            throw invalidParamException("提现方式不能为空");
        }
    }

    private void fillBrokerageConfigDefaults(TradeConfigDO config, TradeConfigDO dbConfig) {
        config.setBrokeragePosterUrls(defaultIfNull(config.getBrokeragePosterUrls(),
                dbConfig != null ? dbConfig.getBrokeragePosterUrls() : null, Collections.emptyList()));
        config.setBrokerageWithdrawTypes(defaultIfNull(config.getBrokerageWithdrawTypes(),
                dbConfig != null ? dbConfig.getBrokerageWithdrawTypes() : null, Collections.emptyList()));
        if (BooleanUtil.isTrue(config.getBrokerageEnabled())) {
            return;
        }

        config.setBrokerageEnabledCondition(defaultIfNull(config.getBrokerageEnabledCondition(),
                dbConfig != null ? dbConfig.getBrokerageEnabledCondition() : null,
                BrokerageEnabledConditionEnum.ADMIN.getCondition()));
        config.setBrokerageBindMode(defaultIfNull(config.getBrokerageBindMode(),
                dbConfig != null ? dbConfig.getBrokerageBindMode() : null, BrokerageBindModeEnum.ANYTIME.getMode()));
        config.setBrokerageFirstPercent(defaultIfNull(config.getBrokerageFirstPercent(),
                dbConfig != null ? dbConfig.getBrokerageFirstPercent() : null, 0));
        config.setBrokerageSecondPercent(defaultIfNull(config.getBrokerageSecondPercent(),
                dbConfig != null ? dbConfig.getBrokerageSecondPercent() : null, 0));
        config.setBrokerageWithdrawMinPrice(defaultIfNull(config.getBrokerageWithdrawMinPrice(),
                dbConfig != null ? dbConfig.getBrokerageWithdrawMinPrice() : null, 0));
        config.setBrokerageWithdrawFeePercent(defaultIfNull(config.getBrokerageWithdrawFeePercent(),
                dbConfig != null ? dbConfig.getBrokerageWithdrawFeePercent() : null, 0));
        config.setBrokerageFrozenDays(defaultIfNull(config.getBrokerageFrozenDays(),
                dbConfig != null ? dbConfig.getBrokerageFrozenDays() : null, 0));
    }

    private static <T> T defaultIfNull(T value, T dbValue, T defaultValue) {
        if (value != null) {
            return value;
        }
        return dbValue != null ? dbValue : defaultValue;
    }

}
