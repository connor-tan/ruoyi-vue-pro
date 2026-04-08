package cn.iocoder.yudao.module.subscription.service.window;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowSaveReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowUpdateStatusReqVO;
import jakarta.validation.Valid;

import java.util.List;

public interface SubscriptionWindowService {

    Long createWindow(@Valid SubscriptionWindowSaveReqVO createReqVO);

    void updateWindow(@Valid SubscriptionWindowSaveReqVO updateReqVO);

    SubscriptionWindowRespVO getWindow(Long id);

    PageResult<SubscriptionWindowRespVO> getWindowPage(SubscriptionWindowPageReqVO pageReqVO);

    List<SubscriptionWindowSimpleRespVO> getWindowSimpleList();

    void updateWindowStatus(@Valid SubscriptionWindowUpdateStatusReqVO reqVO);
}
