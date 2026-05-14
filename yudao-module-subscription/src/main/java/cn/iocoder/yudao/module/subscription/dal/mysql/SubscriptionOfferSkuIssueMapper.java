package cn.iocoder.yudao.module.subscription.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionOfferSkuIssueDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface SubscriptionOfferSkuIssueMapper extends BaseMapperX<SubscriptionOfferSkuIssueDO> {

    default List<SubscriptionOfferSkuIssueDO> selectListByOfferSkuId(Long offerSkuId) {
        return selectList(new LambdaQueryWrapperX<SubscriptionOfferSkuIssueDO>()
                .eq(SubscriptionOfferSkuIssueDO::getOfferSkuId, offerSkuId)
                .orderByAsc(SubscriptionOfferSkuIssueDO::getSort)
                .orderByAsc(SubscriptionOfferSkuIssueDO::getIssueNo)
                .orderByAsc(SubscriptionOfferSkuIssueDO::getId));
    }

    default List<SubscriptionOfferSkuIssueDO> selectListByOfferSkuIds(Collection<Long> offerSkuIds) {
        if (CollUtil.isEmpty(offerSkuIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SubscriptionOfferSkuIssueDO>()
                .in(SubscriptionOfferSkuIssueDO::getOfferSkuId, offerSkuIds)
                .orderByAsc(SubscriptionOfferSkuIssueDO::getOfferSkuId)
                .orderByAsc(SubscriptionOfferSkuIssueDO::getSort)
                .orderByAsc(SubscriptionOfferSkuIssueDO::getIssueNo)
                .orderByAsc(SubscriptionOfferSkuIssueDO::getId));
    }

    default List<SubscriptionOfferSkuIssueDO> selectEnabledListByOfferSkuId(Long offerSkuId, Integer status) {
        return selectList(new LambdaQueryWrapperX<SubscriptionOfferSkuIssueDO>()
                .eq(SubscriptionOfferSkuIssueDO::getOfferSkuId, offerSkuId)
                .eq(SubscriptionOfferSkuIssueDO::getStatus, status)
                .orderByAsc(SubscriptionOfferSkuIssueDO::getSort)
                .orderByAsc(SubscriptionOfferSkuIssueDO::getIssueNo)
                .orderByAsc(SubscriptionOfferSkuIssueDO::getId));
    }

    default SubscriptionOfferSkuIssueDO selectByOfferSkuIdAndIssueNoAndIdNot(Long offerSkuId, Integer issueNo,
                                                                             Long excludeId) {
        return selectOne(new LambdaQueryWrapperX<SubscriptionOfferSkuIssueDO>()
                .eq(SubscriptionOfferSkuIssueDO::getOfferSkuId, offerSkuId)
                .eq(SubscriptionOfferSkuIssueDO::getIssueNo, issueNo)
                .ne(excludeId != null, SubscriptionOfferSkuIssueDO::getId, excludeId)
                .last("LIMIT 1"));
    }

    default int deleteByOfferSkuId(Long offerSkuId) {
        if (offerSkuId == null) {
            return 0;
        }
        return delete(new LambdaQueryWrapperX<SubscriptionOfferSkuIssueDO>()
                .eq(SubscriptionOfferSkuIssueDO::getOfferSkuId, offerSkuId));
    }

    default int deleteByOfferSkuIds(Collection<Long> offerSkuIds) {
        if (CollUtil.isEmpty(offerSkuIds)) {
            return 0;
        }
        return delete(new LambdaQueryWrapperX<SubscriptionOfferSkuIssueDO>()
                .in(SubscriptionOfferSkuIssueDO::getOfferSkuId, offerSkuIds));
    }

}
