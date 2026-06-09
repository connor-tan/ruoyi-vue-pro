package cn.iocoder.yudao.module.subscription.service.window;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowSaveReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowUpdateStatusReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;

import java.util.List;

/**
 * 订刊窗口 Service 接口
 */
public interface SubscriptionWindowService {

    Long createWindow(SubscriptionWindowSaveReqVO reqVO);

    void updateWindow(SubscriptionWindowSaveReqVO reqVO);

    void updateWindowStatus(SubscriptionWindowUpdateStatusReqVO reqVO);

    void updateWindowStatus(Long id, Integer status);

    void deleteWindow(Long id);

    SubscriptionWindowDO getWindow(Long id);

    SubscriptionWindowDO validateWindowExists(Long id);

    PageResult<SubscriptionWindowDO> getWindowPage(SubscriptionWindowPageReqVO reqVO);

    SubscriptionWindowRespVO getWindowResp(Long id);

    PageResult<SubscriptionWindowRespVO> getWindowPageResp(SubscriptionWindowPageReqVO reqVO);

    List<SubscriptionWindowRespVO> getWindowSimpleList(Integer status);

    SubscriptionWindowRespVO getCurrentOpenWindowResp();

    SubscriptionWindowDO getCurrentOpenWindow();

    boolean isOpen(SubscriptionWindowDO window);

}
