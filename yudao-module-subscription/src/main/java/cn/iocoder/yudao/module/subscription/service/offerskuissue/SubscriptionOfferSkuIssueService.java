package cn.iocoder.yudao.module.subscription.service.offerskuissue;

import cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo.SubscriptionOfferSkuIssueApplyDefaultTemplateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo.SubscriptionOfferSkuIssueGenerateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo.SubscriptionOfferSkuIssueRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo.SubscriptionOfferSkuIssueSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionOfferSkuIssueDO;

import java.util.List;

public interface SubscriptionOfferSkuIssueService {

    List<SubscriptionOfferSkuIssueRespVO> getIssueList(Long offerSkuId);

    List<SubscriptionOfferSkuIssueDO> getEnabledIssueList(Long offerSkuId);

    Long saveIssue(SubscriptionOfferSkuIssueSaveReqVO reqVO);

    int generateIssues(SubscriptionOfferSkuIssueGenerateReqVO reqVO);

    int applyDefaultTemplate(SubscriptionOfferSkuIssueApplyDefaultTemplateReqVO reqVO);

    void deleteIssue(Long id);

}
