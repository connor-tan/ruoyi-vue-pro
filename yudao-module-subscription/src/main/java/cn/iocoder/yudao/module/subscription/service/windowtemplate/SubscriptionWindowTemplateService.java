package cn.iocoder.yudao.module.subscription.service.windowtemplate;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplatePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplateRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplateSaveReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplateSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplateUpdateStatusReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowTemplateDO;
import jakarta.validation.Valid;

import java.util.List;

public interface SubscriptionWindowTemplateService {

    Long createWindowTemplate(@Valid SubscriptionWindowTemplateSaveReqVO createReqVO);

    void updateWindowTemplate(@Valid SubscriptionWindowTemplateSaveReqVO updateReqVO);

    void updateWindowTemplateStatus(@Valid SubscriptionWindowTemplateUpdateStatusReqVO reqVO);

    void deleteWindowTemplate(Long id);

    SubscriptionWindowTemplateRespVO getWindowTemplate(Long id);

    SubscriptionWindowTemplateDO getWindowTemplateDO(Long id);

    SubscriptionWindowTemplateDO getEnabledWindowTemplate(Long id);

    PageResult<SubscriptionWindowTemplateRespVO> getWindowTemplatePage(SubscriptionWindowTemplatePageReqVO pageReqVO);

    List<SubscriptionWindowTemplateSimpleRespVO> getWindowTemplateSimpleList();
}
