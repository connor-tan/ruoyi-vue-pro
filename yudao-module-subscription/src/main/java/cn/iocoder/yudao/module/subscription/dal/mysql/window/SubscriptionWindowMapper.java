package cn.iocoder.yudao.module.subscription.dal.mysql.window;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowPageReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SubscriptionWindowMapper extends BaseMapperX<SubscriptionWindowDO> {

    default PageResult<SubscriptionWindowDO> selectPage(SubscriptionWindowPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SubscriptionWindowDO>()
                .likeIfPresent(SubscriptionWindowDO::getName, reqVO.getName())
                .eqIfPresent(SubscriptionWindowDO::getTargetYearStart, reqVO.getTargetYearStart())
                .eqIfPresent(SubscriptionWindowDO::getTargetYearEnd, reqVO.getTargetYearEnd())
                .eqIfPresent(SubscriptionWindowDO::getTargetPeriod, reqVO.getTargetPeriod())
                .eqIfPresent(SubscriptionWindowDO::getGradeCalcRule, reqVO.getGradeCalcRule())
                .eqIfPresent(SubscriptionWindowDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SubscriptionWindowDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SubscriptionWindowDO::getCreateTime, SubscriptionWindowDO::getId));
    }

    default SubscriptionWindowDO selectCurrentEnabledWindow(LocalDateTime now) {
        List<SubscriptionWindowDO> list = selectList(new LambdaQueryWrapperX<SubscriptionWindowDO>()
                .eq(SubscriptionWindowDO::getStatus, 0)
                .le(SubscriptionWindowDO::getStartTime, now)
                .ge(SubscriptionWindowDO::getEndTime, now)
                .orderByDesc(SubscriptionWindowDO::getCreateTime, SubscriptionWindowDO::getId));
        return list.isEmpty() ? null : list.get(0);
    }

    default long countEnabledOverlapWindowExceptId(Long excludeId, LocalDateTime startTime, LocalDateTime endTime) {
        return selectCount(new LambdaQueryWrapperX<SubscriptionWindowDO>()
                .eq(SubscriptionWindowDO::getStatus, 0)
                .neIfPresent(SubscriptionWindowDO::getId, excludeId)
                .lt(SubscriptionWindowDO::getStartTime, endTime)
                .gt(SubscriptionWindowDO::getEndTime, startTime));
    }

    default List<SubscriptionWindowDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowDO>()
                .eq(SubscriptionWindowDO::getStatus, 0)
                .orderByDesc(SubscriptionWindowDO::getCreateTime, SubscriptionWindowDO::getId));
    }

    default List<SubscriptionWindowDO> selectAllList() {
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowDO>()
                .orderByDesc(SubscriptionWindowDO::getCreateTime, SubscriptionWindowDO::getId));
    }

    default long countByTemplateId(Long templateId) {
        return selectCount(new LambdaQueryWrapperX<SubscriptionWindowDO>()
                .eq(SubscriptionWindowDO::getTemplateId, templateId));
    }
}
