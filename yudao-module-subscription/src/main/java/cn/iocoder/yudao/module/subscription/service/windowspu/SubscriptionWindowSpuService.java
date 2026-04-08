package cn.iocoder.yudao.module.subscription.service.windowspu;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuAvailablePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuAvailableRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuBatchCreateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuBatchCreateRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuGradeDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SubscriptionWindowSpuService {

    PageResult<SubscriptionWindowSpuRespVO> getWindowSpuPage(SubscriptionWindowSpuPageReqVO reqVO);

    PageResult<SubscriptionWindowSpuAvailableRespVO> getAvailablePage(SubscriptionWindowSpuAvailablePageReqVO reqVO);

    SubscriptionWindowSpuBatchCreateRespVO batchCreate(SubscriptionWindowSpuBatchCreateReqVO reqVO);

    void updateWindowSpu(SubscriptionWindowSpuSaveReqVO reqVO);

    void deleteWindowSpu(Long id);

    SubscriptionWindowSpuDO getWindowSpuDO(Long id);

    List<SubscriptionWindowSpuDO> getWindowSpuDOListByWindowId(Long windowId);

    Map<Long, List<SubscriptionWindowSpuGradeDO>> getGradeDOMap(Collection<Long> windowSpuIds);
}
