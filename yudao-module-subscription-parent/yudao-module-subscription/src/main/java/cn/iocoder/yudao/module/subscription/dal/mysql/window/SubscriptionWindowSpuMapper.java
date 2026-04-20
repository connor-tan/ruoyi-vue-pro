package cn.iocoder.yudao.module.subscription.dal.mysql.window;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuPageReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface SubscriptionWindowSpuMapper extends BaseMapperX<SubscriptionWindowSpuDO> {

    default PageResult<SubscriptionWindowSpuDO> selectPage(SubscriptionWindowSpuPageReqVO reqVO, Collection<Long> productSpuIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SubscriptionWindowSpuDO>()
                .eq(SubscriptionWindowSpuDO::getWindowId, reqVO.getWindowId())
                .inIfPresent(SubscriptionWindowSpuDO::getProductSpuId, productSpuIds)
                .eqIfPresent(SubscriptionWindowSpuDO::getRecommendFlag, reqVO.getRecommendFlag())
                .orderByDesc(SubscriptionWindowSpuDO::getSort)
                .orderByDesc(SubscriptionWindowSpuDO::getId));
    }

    default SubscriptionWindowSpuDO selectByWindowIdAndProductSpuId(Long windowId, Long productSpuId) {
        return selectOne(new LambdaQueryWrapperX<SubscriptionWindowSpuDO>()
                .eq(SubscriptionWindowSpuDO::getWindowId, windowId)
                .eq(SubscriptionWindowSpuDO::getProductSpuId, productSpuId));
    }

    default List<SubscriptionWindowSpuDO> selectListByWindowId(Long windowId) {
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowSpuDO>()
                .eq(SubscriptionWindowSpuDO::getWindowId, windowId)
                .orderByDesc(SubscriptionWindowSpuDO::getSort)
                .orderByDesc(SubscriptionWindowSpuDO::getId));
    }

    default long countByWindowId(Long windowId) {
        return selectCount(new LambdaQueryWrapperX<SubscriptionWindowSpuDO>()
                .eq(SubscriptionWindowSpuDO::getWindowId, windowId));
    }

    default List<SubscriptionWindowSpuDO> selectListByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return selectBatchIds(ids);
    }
}
