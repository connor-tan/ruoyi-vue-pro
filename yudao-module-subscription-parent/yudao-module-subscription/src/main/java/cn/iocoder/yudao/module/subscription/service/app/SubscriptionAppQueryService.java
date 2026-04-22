package cn.iocoder.yudao.module.subscription.service.app;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.app.publication.vo.AppSubscriptionPublicationPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.app.publication.vo.AppSubscriptionPublicationRespVO;
import cn.iocoder.yudao.module.subscription.controller.app.window.vo.AppSubscriptionCurrentWindowRespVO;

import java.util.List;

public interface SubscriptionAppQueryService {

    AppSubscriptionCurrentWindowRespVO getCurrentWindow();

    PageResult<AppSubscriptionPublicationRespVO> getPublicationPage(Long loginUserId,
                                                                    AppSubscriptionPublicationPageReqVO reqVO);

    AppSubscriptionPublicationRespVO getPublication(Long loginUserId, Long studentId, Long productSpuId);

    List<AppSubscriptionPublicationRespVO> getPublicationListBySpuIds(Long loginUserId, Long studentId,
                                                                      List<Long> productSpuIds);
}
