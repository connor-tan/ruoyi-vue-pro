package cn.iocoder.yudao.module.subscription.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.SubscriptionOfferPageReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface SubscriptionWindowOfferMapper extends BaseMapperX<SubscriptionWindowOfferDO> {

    default PageResult<SubscriptionWindowOfferDO> selectPage(SubscriptionOfferPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SubscriptionWindowOfferDO>()
                .eq(SubscriptionWindowOfferDO::getWindowId, reqVO.getWindowId())
                .eqIfPresent(SubscriptionWindowOfferDO::getStatus, reqVO.getStatus())
                .orderByAsc(SubscriptionWindowOfferDO::getSort)
                .orderByDesc(SubscriptionWindowOfferDO::getId));
    }

    default List<SubscriptionWindowOfferDO> selectListByWindowId(Long windowId) {
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowOfferDO>()
                .eq(SubscriptionWindowOfferDO::getWindowId, windowId)
                .orderByAsc(SubscriptionWindowOfferDO::getSort)
                .orderByDesc(SubscriptionWindowOfferDO::getId));
    }

    default SubscriptionWindowOfferDO selectByWindowIdAndProductSpuId(Long windowId, Long productSpuId) {
        return selectOne(new LambdaQueryWrapperX<SubscriptionWindowOfferDO>()
                .eq(SubscriptionWindowOfferDO::getWindowId, windowId)
                .eq(SubscriptionWindowOfferDO::getProductSpuId, productSpuId)
                .last("LIMIT 1"));
    }

    default List<SubscriptionWindowOfferDO> selectListByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowOfferDO>()
                .in(SubscriptionWindowOfferDO::getId, ids));
    }

    default int deleteByWindowId(Long windowId) {
        return delete(SubscriptionWindowOfferDO::getWindowId, windowId);
    }
}
