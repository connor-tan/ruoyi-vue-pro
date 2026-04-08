package cn.iocoder.yudao.module.subscription.service.support;

import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportSchoolYearSimpleRespVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 订刊模块的基础引用数据服务。
 */
public interface SubscriptionSupportService {

    List<SubscriptionSupportSchoolYearSimpleRespVO> getSchoolYearSimpleList(Long schoolId);

    SchoolYearDO getSchoolYear(Long id);

    Map<Long, SchoolYearDO> getSchoolYearMap(Collection<Long> ids);
}
