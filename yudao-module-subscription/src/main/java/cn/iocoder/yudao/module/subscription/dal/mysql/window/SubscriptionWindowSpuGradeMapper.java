package cn.iocoder.yudao.module.subscription.dal.mysql.window;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuGradeDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface SubscriptionWindowSpuGradeMapper extends BaseMapperX<SubscriptionWindowSpuGradeDO> {

    default List<SubscriptionWindowSpuGradeDO> selectListByWindowSpuIds(Collection<Long> windowSpuIds) {
        if (windowSpuIds == null || windowSpuIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowSpuGradeDO>()
                .in(SubscriptionWindowSpuGradeDO::getWindowSpuId, windowSpuIds));
    }

    default List<SubscriptionWindowSpuGradeDO> selectListByWindowSpuId(Long windowSpuId) {
        return selectList(SubscriptionWindowSpuGradeDO::getWindowSpuId, windowSpuId);
    }

    default SubscriptionWindowSpuGradeDO selectByWindowSpuIdAndGradeCatalogId(Long windowSpuId, Long gradeCatalogId) {
        return selectOne(new LambdaQueryWrapperX<SubscriptionWindowSpuGradeDO>()
                .eq(SubscriptionWindowSpuGradeDO::getWindowSpuId, windowSpuId)
                .eq(SubscriptionWindowSpuGradeDO::getGradeCatalogId, gradeCatalogId));
    }

    default void deleteByWindowSpuId(Long windowSpuId) {
        deletePhysicallyByWindowSpuId(windowSpuId);
    }

    default void deleteByWindowSpuIds(Collection<Long> windowSpuIds) {
        if (windowSpuIds == null || windowSpuIds.isEmpty()) {
            return;
        }
        deletePhysicallyByWindowSpuIds(windowSpuIds);
    }

    @Delete("DELETE FROM sub_window_spu_grade WHERE window_spu_id = #{windowSpuId}")
    void deletePhysicallyByWindowSpuId(Long windowSpuId);

    @Delete({
            "<script>",
            "DELETE FROM sub_window_spu_grade WHERE window_spu_id IN ",
            "<foreach collection='windowSpuIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    void deletePhysicallyByWindowSpuIds(Collection<Long> windowSpuIds);
}
