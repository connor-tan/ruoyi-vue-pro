package cn.iocoder.yudao.module.subscription.dal.mysql.window;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo.SubscriptionWindowSpuRulePageReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuRuleDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface SubscriptionWindowSpuRuleMapper extends BaseMapperX<SubscriptionWindowSpuRuleDO> {

    default PageResult<SubscriptionWindowSpuRuleDO> selectPage(SubscriptionWindowSpuRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SubscriptionWindowSpuRuleDO>()
                .eq(SubscriptionWindowSpuRuleDO::getWindowSpuId, reqVO.getWindowSpuId())
                .eqIfPresent(SubscriptionWindowSpuRuleDO::getEffectType, reqVO.getEffectType())
                .eqIfPresent(SubscriptionWindowSpuRuleDO::getScopeType, reqVO.getScopeType())
                .orderByDesc(SubscriptionWindowSpuRuleDO::getSort)
                .orderByDesc(SubscriptionWindowSpuRuleDO::getId));
    }

    default List<SubscriptionWindowSpuRuleDO> selectListByWindowSpuIds(Collection<Long> windowSpuIds) {
        if (windowSpuIds == null || windowSpuIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowSpuRuleDO>()
                .in(SubscriptionWindowSpuRuleDO::getWindowSpuId, windowSpuIds)
                .orderByDesc(SubscriptionWindowSpuRuleDO::getSort)
                .orderByDesc(SubscriptionWindowSpuRuleDO::getId));
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

    @Select("SELECT COUNT(1) FROM sub_window_spu_rule wr INNER JOIN sub_window_spu wsp ON wsp.id = wr.window_spu_id AND wsp.deleted = b'0' WHERE wr.deleted = b'0' AND wsp.window_id = #{windowId}")
    long countByWindowId(Long windowId);

    @Delete("DELETE FROM sub_window_spu_rule WHERE window_spu_id = #{windowSpuId}")
    void deletePhysicallyByWindowSpuId(Long windowSpuId);

    @Delete({
            "<script>",
            "DELETE FROM sub_window_spu_rule WHERE window_spu_id IN ",
            "<foreach collection='windowSpuIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    void deletePhysicallyByWindowSpuIds(Collection<Long> windowSpuIds);
}
