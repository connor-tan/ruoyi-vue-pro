package cn.iocoder.yudao.module.trade.service.delivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchPageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidateItemRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidatePageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryCandidateRespVO;

import java.util.List;

public interface TradePublicationDeliveryBatchService {

    PageResult<TradePublicationDeliveryCandidateRespVO> getCandidatePage(TradePublicationDeliveryCandidatePageReqVO reqVO);

    List<TradePublicationDeliveryCandidateItemRespVO> getCandidateItemList(TradePublicationDeliveryCandidatePageReqVO reqVO);

    Long createAndDeliver(TradePublicationDeliveryBatchCreateReqVO reqVO, Long operatorUserId);

    PageResult<TradePublicationDeliveryBatchRespVO> getBatchPage(TradePublicationDeliveryBatchPageReqVO reqVO);

    TradePublicationDeliveryBatchRespVO getBatch(Long id);

}
