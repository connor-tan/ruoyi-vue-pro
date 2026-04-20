package cn.iocoder.yudao.module.subscription.dal.mysql.window;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplatePageReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SubscriptionWindowTemplateMapper extends BaseMapperX<SubscriptionWindowTemplateDO> {

    default PageResult<SubscriptionWindowTemplateDO> selectPage(SubscriptionWindowTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SubscriptionWindowTemplateDO>()
                .likeIfPresent(SubscriptionWindowTemplateDO::getName, reqVO.getName())
                .eqIfPresent(SubscriptionWindowTemplateDO::getTargetPeriod, reqVO.getTargetPeriod())
                .eqIfPresent(SubscriptionWindowTemplateDO::getGradeCalcRule, reqVO.getGradeCalcRule())
                .eqIfPresent(SubscriptionWindowTemplateDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SubscriptionWindowTemplateDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(SubscriptionWindowTemplateDO::getSort)
                .orderByAsc(SubscriptionWindowTemplateDO::getId));
    }

    default SubscriptionWindowTemplateDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<SubscriptionWindowTemplateDO>()
                .eq(SubscriptionWindowTemplateDO::getCode, code));
    }

    default List<SubscriptionWindowTemplateDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<SubscriptionWindowTemplateDO>()
                .eq(SubscriptionWindowTemplateDO::getStatus, 0)
                .orderByAsc(SubscriptionWindowTemplateDO::getSort)
                .orderByAsc(SubscriptionWindowTemplateDO::getId));
    }

    default long countByName(String name, Long excludeId) {
        return selectCount(new LambdaQueryWrapperX<SubscriptionWindowTemplateDO>()
                .eq(SubscriptionWindowTemplateDO::getName, name)
                .neIfPresent(SubscriptionWindowTemplateDO::getId, excludeId));
    }

    default long countByCode(String code, Long excludeId) {
        return selectCount(new LambdaQueryWrapperX<SubscriptionWindowTemplateDO>()
                .eq(SubscriptionWindowTemplateDO::getCode, code)
                .neIfPresent(SubscriptionWindowTemplateDO::getId, excludeId));
    }
}
