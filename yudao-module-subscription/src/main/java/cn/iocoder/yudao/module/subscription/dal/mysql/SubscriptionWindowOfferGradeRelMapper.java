package cn.iocoder.yudao.module.subscription.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferGradeRelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface SubscriptionWindowOfferGradeRelMapper extends BaseMapperX<SubscriptionWindowOfferGradeRelDO> {

    default List<SubscriptionWindowOfferGradeRelDO> selectListByOfferIds(Collection<Long> offerIds) {
        if (offerIds == null || offerIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowOfferGradeRelDO>()
                .in(SubscriptionWindowOfferGradeRelDO::getOfferId, offerIds));
    }

    default int deleteByOfferId(Long offerId) {
        return delete(SubscriptionWindowOfferGradeRelDO::getOfferId, offerId);
    }

    default int deleteByOfferIds(Collection<Long> offerIds) {
        if (offerIds == null || offerIds.isEmpty()) {
            return 0;
        }
        return delete(new LambdaQueryWrapperX<SubscriptionWindowOfferGradeRelDO>()
                .in(SubscriptionWindowOfferGradeRelDO::getOfferId, offerIds));
    }
}
