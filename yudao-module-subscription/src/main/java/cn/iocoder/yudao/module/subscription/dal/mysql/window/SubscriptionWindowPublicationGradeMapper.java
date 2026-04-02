package cn.iocoder.yudao.module.subscription.dal.mysql.window;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowPublicationGradeDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface SubscriptionWindowPublicationGradeMapper extends BaseMapperX<SubscriptionWindowPublicationGradeDO> {

    default List<SubscriptionWindowPublicationGradeDO> selectListByWindowPublicationId(Long windowPublicationId) {
        return selectList(SubscriptionWindowPublicationGradeDO::getWindowPublicationId, windowPublicationId);
    }

    default List<SubscriptionWindowPublicationGradeDO> selectListByWindowPublicationIds(Collection<Long> windowPublicationIds) {
        if (windowPublicationIds == null || windowPublicationIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowPublicationGradeDO>()
                .in(SubscriptionWindowPublicationGradeDO::getWindowPublicationId, windowPublicationIds));
    }

    default void deleteByWindowPublicationId(Long windowPublicationId) {
        deletePhysicallyByWindowPublicationId(windowPublicationId);
    }

    @Delete("DELETE FROM sub_window_publication_grade WHERE window_publication_id = #{windowPublicationId}")
    void deletePhysicallyByWindowPublicationId(Long windowPublicationId);
}
