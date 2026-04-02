package cn.iocoder.yudao.module.subscription.dal.mysql.window;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublication.vo.SubscriptionWindowPublicationPageReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowPublicationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface SubscriptionWindowPublicationMapper extends BaseMapperX<SubscriptionWindowPublicationDO> {

    default PageResult<SubscriptionWindowPublicationDO> selectPage(SubscriptionWindowPublicationPageReqVO reqVO, Collection<Long> productSpuIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SubscriptionWindowPublicationDO>()
                .eqIfPresent(SubscriptionWindowPublicationDO::getWindowId, reqVO.getWindowId())
                .inIfPresent(SubscriptionWindowPublicationDO::getProductSpuId, productSpuIds)
                .eqIfPresent(SubscriptionWindowPublicationDO::getStatus, reqVO.getStatus())
                .orderByDesc(SubscriptionWindowPublicationDO::getSort)
                .orderByDesc(SubscriptionWindowPublicationDO::getId));
    }

    default SubscriptionWindowPublicationDO selectByWindowIdAndProductSpuId(Long windowId, Long productSpuId) {
        return selectOne(new LambdaQueryWrapperX<SubscriptionWindowPublicationDO>()
                .eq(SubscriptionWindowPublicationDO::getWindowId, windowId)
                .eq(SubscriptionWindowPublicationDO::getProductSpuId, productSpuId));
    }

    default List<SubscriptionWindowPublicationDO> selectListByWindowId(Long windowId) {
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowPublicationDO>()
                .eq(SubscriptionWindowPublicationDO::getWindowId, windowId)
                .orderByDesc(SubscriptionWindowPublicationDO::getSort)
                .orderByDesc(SubscriptionWindowPublicationDO::getId));
    }

    default List<SubscriptionWindowPublicationDO> selectListByWindowIds(Collection<Long> windowIds) {
        if (windowIds == null || windowIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowPublicationDO>()
                .in(SubscriptionWindowPublicationDO::getWindowId, windowIds));
    }
}
