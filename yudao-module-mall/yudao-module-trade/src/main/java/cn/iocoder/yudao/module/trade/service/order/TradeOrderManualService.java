package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderManualCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderManualImportExcelVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderManualImportRespVO;
import jakarta.validation.Valid;

import java.util.List;

public interface TradeOrderManualService {

    Long createManualOrder(@Valid TradeOrderManualCreateReqVO reqVO);

    Long createImportOrder(@Valid TradeOrderManualCreateReqVO reqVO);

    TradeOrderManualImportRespVO importManualOrders(List<TradeOrderManualImportExcelVO> rows);

    Long confirmOfflinePay(Long id);

    void cancelManualOrder(Long id);

}
