package cn.iocoder.yudao.module.subscription.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowPageReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SubscriptionWindowMapper extends BaseMapperX<SubscriptionWindowDO> {

    default PageResult<SubscriptionWindowDO> selectPage(SubscriptionWindowPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SubscriptionWindowDO>()
                .likeIfPresent(SubscriptionWindowDO::getName, reqVO.getName())
                .eqIfPresent(SubscriptionWindowDO::getTargetYearCatalogId, reqVO.getTargetYearCatalogId())
                .eqIfPresent(SubscriptionWindowDO::getStatus, reqVO.getStatus())
                .orderByDesc(SubscriptionWindowDO::getId));
    }

    default List<SubscriptionWindowDO> selectOpenList(LocalDateTime now, Integer enabledStatus) {
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowDO>()
                .eq(SubscriptionWindowDO::getStatus, enabledStatus)
                .le(SubscriptionWindowDO::getStartTime, now)
                .gt(SubscriptionWindowDO::getEndTime, now)
                .orderByDesc(SubscriptionWindowDO::getStartTime)
                .orderByDesc(SubscriptionWindowDO::getId));
    }

    default List<SubscriptionWindowDO> selectEnabledOverlapList(Long excludeId, LocalDateTime startTime,
                                                                LocalDateTime endTime, Integer enabledStatus) {
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowDO>()
                .eq(SubscriptionWindowDO::getStatus, enabledStatus)
                .ne(excludeId != null, SubscriptionWindowDO::getId, excludeId)
                .lt(SubscriptionWindowDO::getStartTime, endTime)
                .gt(SubscriptionWindowDO::getEndTime, startTime));
    }
}
