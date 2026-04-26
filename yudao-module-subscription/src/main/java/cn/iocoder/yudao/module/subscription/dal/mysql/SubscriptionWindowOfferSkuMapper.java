package cn.iocoder.yudao.module.subscription.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface SubscriptionWindowOfferSkuMapper extends BaseMapperX<SubscriptionWindowOfferSkuDO> {

    default List<SubscriptionWindowOfferSkuDO> selectListByOfferId(Long offerId) {
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowOfferSkuDO>()
                .eq(SubscriptionWindowOfferSkuDO::getOfferId, offerId)
                .orderByAsc(SubscriptionWindowOfferSkuDO::getSort)
                .orderByAsc(SubscriptionWindowOfferSkuDO::getId));
    }

    default List<SubscriptionWindowOfferSkuDO> selectListByOfferIds(Collection<Long> offerIds) {
        if (offerIds == null || offerIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowOfferSkuDO>()
                .in(SubscriptionWindowOfferSkuDO::getOfferId, offerIds)
                .orderByAsc(SubscriptionWindowOfferSkuDO::getSort)
                .orderByAsc(SubscriptionWindowOfferSkuDO::getId));
    }

    default SubscriptionWindowOfferSkuDO selectByOfferIdAndProductSkuId(Long offerId, Long productSkuId) {
        return selectOne(new LambdaQueryWrapperX<SubscriptionWindowOfferSkuDO>()
                .eq(SubscriptionWindowOfferSkuDO::getOfferId, offerId)
                .eq(SubscriptionWindowOfferSkuDO::getProductSkuId, productSkuId)
                .last("LIMIT 1"));
    }

    default SubscriptionWindowOfferSkuDO selectByOfferIdAndProductSkuIdAndIdNot(Long offerId, Long productSkuId,
                                                                                Long excludeId) {
        return selectOne(new LambdaQueryWrapperX<SubscriptionWindowOfferSkuDO>()
                .eq(SubscriptionWindowOfferSkuDO::getOfferId, offerId)
                .eq(SubscriptionWindowOfferSkuDO::getProductSkuId, productSkuId)
                .ne(excludeId != null, SubscriptionWindowOfferSkuDO::getId, excludeId)
                .last("LIMIT 1"));
    }

    default int deleteByOfferId(Long offerId) {
        return delete(SubscriptionWindowOfferSkuDO::getOfferId, offerId);
    }

    default int deleteByOfferIds(Collection<Long> offerIds) {
        if (offerIds == null || offerIds.isEmpty()) {
            return 0;
        }
        return delete(new LambdaQueryWrapperX<SubscriptionWindowOfferSkuDO>()
                .in(SubscriptionWindowOfferSkuDO::getOfferId, offerIds));
    }
}
