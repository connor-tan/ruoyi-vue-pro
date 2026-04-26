package cn.iocoder.yudao.module.subscription.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionRuleConditionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface SubscriptionRuleConditionMapper extends BaseMapperX<SubscriptionRuleConditionDO> {

    default List<SubscriptionRuleConditionDO> selectListByRuleIds(Collection<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SubscriptionRuleConditionDO>()
                .in(SubscriptionRuleConditionDO::getRuleId, ruleIds)
                .orderByAsc(SubscriptionRuleConditionDO::getId));
    }

    default int deleteByRuleId(Long ruleId) {
        return delete(SubscriptionRuleConditionDO::getRuleId, ruleId);
    }

    default int deleteByRuleIds(Collection<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return 0;
        }
        return delete(new LambdaQueryWrapperX<SubscriptionRuleConditionDO>()
                .in(SubscriptionRuleConditionDO::getRuleId, ruleIds));
    }
}
