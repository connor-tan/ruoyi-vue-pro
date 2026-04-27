package cn.iocoder.yudao.module.subscription.service.offer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.*;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferGradeRelDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 订刊窗口刊物 Service 接口
 */
public interface SubscriptionOfferService {

    PageResult<SubscriptionOfferAvailableRespVO> getAvailablePage(SubscriptionOfferAvailablePageReqVO reqVO);

    SubscriptionOfferBatchCreateRespVO batchCreateOffer(SubscriptionOfferBatchCreateReqVO reqVO);

    SubscriptionOfferBatchCreateRespVO batchCreateByQuery(SubscriptionOfferBatchCreateByQueryReqVO reqVO);

    void updateOffer(SubscriptionOfferSaveReqVO reqVO);

    void deleteOffer(Long id);

    SubscriptionWindowOfferDO getOffer(Long id);

    SubscriptionWindowOfferDO validateOfferExists(Long id);

    SubscriptionOfferRespVO getOfferResp(Long id);

    PageResult<SubscriptionOfferRespVO> getOfferPage(SubscriptionOfferPageReqVO reqVO);

    List<SubscriptionOfferRespVO> buildOfferRespList(List<SubscriptionWindowOfferDO> offers);

    Map<Long, List<SubscriptionWindowOfferGradeRelDO>> getGradeRelMap(Collection<Long> offerIds);

}
