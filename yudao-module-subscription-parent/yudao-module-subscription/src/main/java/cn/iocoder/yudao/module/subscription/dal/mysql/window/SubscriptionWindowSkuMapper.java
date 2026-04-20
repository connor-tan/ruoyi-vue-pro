package cn.iocoder.yudao.module.subscription.dal.mysql.window;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface SubscriptionWindowSkuMapper extends BaseMapperX<SubscriptionWindowSkuDO> {

    default List<SubscriptionWindowSkuDO> selectListByWindowSpuId(Long windowSpuId) {
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowSkuDO>()
                .eq(SubscriptionWindowSkuDO::getWindowSpuId, windowSpuId)
                .orderByAsc(SubscriptionWindowSkuDO::getSort)
                .orderByAsc(SubscriptionWindowSkuDO::getId));
    }

    default List<SubscriptionWindowSkuDO> selectListByWindowSpuIds(Collection<Long> windowSpuIds) {
        if (windowSpuIds == null || windowSpuIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowSkuDO>()
                .in(SubscriptionWindowSkuDO::getWindowSpuId, windowSpuIds)
                .orderByAsc(SubscriptionWindowSkuDO::getSort)
                .orderByAsc(SubscriptionWindowSkuDO::getId));
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

    @Select("SELECT COUNT(1) FROM sub_window_sku ws INNER JOIN sub_window_spu wsp ON wsp.id = ws.window_spu_id AND wsp.deleted = b'0' WHERE ws.deleted = b'0' AND wsp.window_id = #{windowId}")
    long countByWindowId(Long windowId);

    @Delete("DELETE FROM sub_window_sku WHERE window_spu_id = #{windowSpuId}")
    void deletePhysicallyByWindowSpuId(Long windowSpuId);

    @Delete({
            "<script>",
            "DELETE FROM sub_window_sku WHERE window_spu_id IN ",
            "<foreach collection='windowSpuIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    void deletePhysicallyByWindowSpuIds(Collection<Long> windowSpuIds);
}
