package cn.iocoder.yudao.module.trade.api.delivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateGroupRespDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateItemRespDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidatePageReqDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateRespDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryConfirmReqDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCreateReqDTO;

import java.util.List;

/**
 * 刊物发货 API。
 *
 * <p>trade 负责判断订单期次是否可发，并在仓库完成出库后更新订单履约状态。</p>
 */
public interface TradePublicationDeliveryApi {

    PageResult<TradePublicationDeliveryCandidateRespDTO> getCandidatePage(
            TradePublicationDeliveryCandidatePageReqDTO reqDTO);

    PageResult<TradePublicationDeliveryCandidateGroupRespDTO> getCandidateGroupPage(
            TradePublicationDeliveryCandidatePageReqDTO reqDTO);

    List<TradePublicationDeliveryCandidateRespDTO> getCandidateChildList(
            TradePublicationDeliveryCandidatePageReqDTO reqDTO);

    PageResult<TradePublicationDeliveryCandidateRespDTO> getCandidateChildPage(
            TradePublicationDeliveryCandidatePageReqDTO reqDTO);

    List<TradePublicationDeliveryCandidateItemRespDTO> getCandidateItemList(
            TradePublicationDeliveryCandidatePageReqDTO reqDTO);

    List<TradePublicationDeliveryCandidateItemRespDTO> getDeliverableItemList(
            TradePublicationDeliveryCreateReqDTO reqDTO);

    void confirmDelivered(TradePublicationDeliveryConfirmReqDTO reqDTO);

}
