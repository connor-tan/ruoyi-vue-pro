package cn.iocoder.yudao.module.trade.dal.mysql.delivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.TradePublicationDeliveryBatchDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradePublicationDeliveryBatchMapper extends BaseMapperX<TradePublicationDeliveryBatchDO> {

    default PageResult<TradePublicationDeliveryBatchDO> selectPage(TradePublicationDeliveryBatchPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TradePublicationDeliveryBatchDO>()
                .likeIfPresent(TradePublicationDeliveryBatchDO::getBatchNo, reqVO.getBatchNo())
                .eqIfPresent(TradePublicationDeliveryBatchDO::getSchoolId, reqVO.getSchoolId())
                .eqIfPresent(TradePublicationDeliveryBatchDO::getStationId, reqVO.getStationId())
                .eqIfPresent(TradePublicationDeliveryBatchDO::getDeliveryType, reqVO.getDeliveryType())
                .eqIfPresent(TradePublicationDeliveryBatchDO::getWindowId, reqVO.getWindowId())
                .eqIfPresent(TradePublicationDeliveryBatchDO::getOfferId, reqVO.getOfferId())
                .eqIfPresent(TradePublicationDeliveryBatchDO::getOfferSkuId, reqVO.getOfferSkuId())
                .eqIfPresent(TradePublicationDeliveryBatchDO::getSkuId, reqVO.getSkuId())
                .eqIfPresent(TradePublicationDeliveryBatchDO::getIssueId, reqVO.getIssueId())
                .eqIfPresent(TradePublicationDeliveryBatchDO::getIssueNo, reqVO.getIssueNo())
                .eqIfPresent(TradePublicationDeliveryBatchDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(TradePublicationDeliveryBatchDO::getDeliveryTime, reqVO.getDeliveryTime())
                .orderByDesc(TradePublicationDeliveryBatchDO::getId));
    }

}
