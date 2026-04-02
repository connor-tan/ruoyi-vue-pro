package cn.iocoder.yudao.module.subscription.dal.mysql.window;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule.vo.SubscriptionWindowPublicationRulePageReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowPublicationRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface SubscriptionWindowPublicationRuleMapper extends BaseMapperX<SubscriptionWindowPublicationRuleDO> {

    default List<SubscriptionWindowPublicationRuleDO> selectListByWindowPublicationId(Long windowPublicationId) {
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowPublicationRuleDO>()
                .eq(SubscriptionWindowPublicationRuleDO::getWindowPublicationId, windowPublicationId)
                .orderByDesc(SubscriptionWindowPublicationRuleDO::getSort)
                .orderByDesc(SubscriptionWindowPublicationRuleDO::getId));
    }

    default List<SubscriptionWindowPublicationRuleDO> selectListByWindowPublicationIds(Collection<Long> windowPublicationIds) {
        if (windowPublicationIds == null || windowPublicationIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowPublicationRuleDO>()
                .in(SubscriptionWindowPublicationRuleDO::getWindowPublicationId, windowPublicationIds)
                .orderByDesc(SubscriptionWindowPublicationRuleDO::getSort)
                .orderByDesc(SubscriptionWindowPublicationRuleDO::getId));
    }

    default List<SubscriptionWindowPublicationRuleDO> selectPageList(SubscriptionWindowPublicationRulePageReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowPublicationRuleDO>()
                .eqIfPresent(SubscriptionWindowPublicationRuleDO::getWindowPublicationId, reqVO.getWindowPublicationId())
                .eqIfPresent(SubscriptionWindowPublicationRuleDO::getEffectType, reqVO.getEffectType())
                .eqIfPresent(SubscriptionWindowPublicationRuleDO::getScopeType, reqVO.getScopeType())
                .orderByDesc(SubscriptionWindowPublicationRuleDO::getSort)
                .orderByDesc(SubscriptionWindowPublicationRuleDO::getId));
    }
}
