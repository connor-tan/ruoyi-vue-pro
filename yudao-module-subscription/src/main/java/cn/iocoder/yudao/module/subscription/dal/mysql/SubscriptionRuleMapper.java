package cn.iocoder.yudao.module.subscription.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.controller.admin.rule.vo.SubscriptionRulePageReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionRuleDO;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleScopeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface SubscriptionRuleMapper extends BaseMapperX<SubscriptionRuleDO> {

    default PageResult<SubscriptionRuleDO> selectPage(SubscriptionRulePageReqVO reqVO) {
        LambdaQueryWrapperX<SubscriptionRuleDO> wrapper = new LambdaQueryWrapperX<SubscriptionRuleDO>()
                .eq(SubscriptionRuleDO::getWindowId, reqVO.getWindowId())
                .eqIfPresent(SubscriptionRuleDO::getEffectType, reqVO.getEffectType())
                .eqIfPresent(SubscriptionRuleDO::getStatus, reqVO.getStatus())
                .orderByDesc(SubscriptionRuleDO::getId);
        if (SubscriptionRuleScopeEnum.isWindow(reqVO.getScope())) {
            wrapper.isNull(SubscriptionRuleDO::getOfferId);
        } else if (SubscriptionRuleScopeEnum.isOffer(reqVO.getScope())) {
            wrapper.eq(SubscriptionRuleDO::getOfferId, reqVO.getOfferId());
        }
        return selectPage(reqVO, wrapper);
    }

    default List<SubscriptionRuleDO> selectListByWindowId(Long windowId) {
        return selectList(new LambdaQueryWrapperX<SubscriptionRuleDO>()
                .eq(SubscriptionRuleDO::getWindowId, windowId)
                .orderByDesc(SubscriptionRuleDO::getId));
    }

    default List<SubscriptionRuleDO> selectListByOfferIds(Collection<Long> offerIds) {
        if (offerIds == null || offerIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SubscriptionRuleDO>()
                .in(SubscriptionRuleDO::getOfferId, offerIds)
                .orderByDesc(SubscriptionRuleDO::getId));
    }

    default int deleteByWindowId(Long windowId) {
        return delete(SubscriptionRuleDO::getWindowId, windowId);
    }

    default int deleteByOfferId(Long offerId) {
        return delete(SubscriptionRuleDO::getOfferId, offerId);
    }

    default int deleteByOfferIds(Collection<Long> offerIds) {
        if (offerIds == null || offerIds.isEmpty()) {
            return 0;
        }
        return delete(new LambdaQueryWrapperX<SubscriptionRuleDO>()
                .in(SubscriptionRuleDO::getOfferId, offerIds));
    }
}
