package cn.iocoder.yudao.module.trade.convert.delivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.publication.TradePublicationDeliveryBatchRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.TradePublicationDeliveryBatchDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.TradePublicationDeliveryBatchItemDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TradePublicationDeliveryBatchConvert {

    TradePublicationDeliveryBatchConvert INSTANCE = Mappers.getMapper(TradePublicationDeliveryBatchConvert.class);

    TradePublicationDeliveryBatchRespVO convert(TradePublicationDeliveryBatchDO bean);

    List<TradePublicationDeliveryBatchRespVO.Item> convertItemList(List<TradePublicationDeliveryBatchItemDO> list);

    PageResult<TradePublicationDeliveryBatchRespVO> convertPage(PageResult<TradePublicationDeliveryBatchDO> page);

}
