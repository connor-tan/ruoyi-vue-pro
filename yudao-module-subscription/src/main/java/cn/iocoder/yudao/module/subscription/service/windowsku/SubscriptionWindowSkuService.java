package cn.iocoder.yudao.module.subscription.service.windowsku;

import cn.iocoder.yudao.module.subscription.controller.admin.windowsku.vo.SubscriptionWindowSkuBatchUpdateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowsku.vo.SubscriptionWindowSkuRespVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;

import java.util.Collection;
import java.util.List;

public interface SubscriptionWindowSkuService {

    List<SubscriptionWindowSkuRespVO> getWindowSkuListByWindowSpuId(Long windowSpuId);

    void batchUpdateWindowSku(SubscriptionWindowSkuBatchUpdateReqVO reqVO);

    List<SubscriptionWindowSkuDO> getWindowSkuDOList(Collection<Long> windowSpuIds);
}
