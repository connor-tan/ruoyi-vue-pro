package cn.iocoder.yudao.module.subscription.service.windowpublication;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublication.vo.SubscriptionWindowPublicationPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublication.vo.SubscriptionWindowPublicationRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublication.vo.SubscriptionWindowPublicationSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowPublicationDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowPublicationGradeDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SubscriptionWindowPublicationService {

    Long createWindowPublication(@Valid SubscriptionWindowPublicationSaveReqVO createReqVO);

    void updateWindowPublication(@Valid SubscriptionWindowPublicationSaveReqVO updateReqVO);

    PageResult<SubscriptionWindowPublicationRespVO> getWindowPublicationPage(SubscriptionWindowPublicationPageReqVO pageReqVO);

    SubscriptionWindowPublicationDO getWindowPublicationDO(Long id);

    List<SubscriptionWindowPublicationDO> getWindowPublicationDOListByWindowId(Long windowId);

    Map<Long, List<SubscriptionWindowPublicationGradeDO>> getGradeDOMap(Collection<Long> windowPublicationIds);
}
