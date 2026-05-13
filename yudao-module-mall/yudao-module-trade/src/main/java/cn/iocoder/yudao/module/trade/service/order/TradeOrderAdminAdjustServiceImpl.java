package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderRemarkReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderUpdateAddressReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderUpdatePriceReqVO;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderOperateTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderSourceEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.core.annotations.TradeOrderLog;
import cn.iocoder.yudao.module.trade.framework.order.core.utils.TradeOrderLogUtils;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderDeliveryAccessSupport;
import cn.iocoder.yudao.module.trade.service.price.calculator.TradePriceCalculatorHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_UPDATE_ADDRESS_FAIL_EXPRESS_DELIVERY_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_UPDATE_ADDRESS_FAIL_STATUS_NOT_DELIVERED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_UPDATE_PRICE_FAIL_ALREADY;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_UPDATE_PRICE_FAIL_ADMIN_MANUAL;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_UPDATE_PRICE_FAIL_PAID;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_UPDATE_PRICE_FAIL_PRICE_ERROR;

/**
 * 交易订单后台调整 Service 实现类
 */
@Service
public class TradeOrderAdminAdjustServiceImpl implements TradeOrderAdminAdjustService {

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private TradeOrderDeliveryAccessSupport deliveryAccessSupport;

    @Override
    public void updateOrderRemark(TradeOrderRemarkReqVO reqVO) {
        validateOrderExists(reqVO.getId());
        TradeOrderDO order = TradeOrderConvert.INSTANCE.convert(reqVO);
        tradeOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_UPDATE_PRICE)
    public void updateOrderPrice(TradeOrderUpdatePriceReqVO reqVO) {
        TradeOrderDO order = validateOrderExists(reqVO.getId());
        if (order.getPayStatus()) {
            throw exception(ORDER_UPDATE_PRICE_FAIL_PAID);
        }
        if (TradeOrderSourceEnum.isAdmin(order.getOrderSource())) {
            throw exception(ORDER_UPDATE_PRICE_FAIL_ADMIN_MANUAL);
        }
        if (order.getAdjustPrice() > 0) {
            throw exception(ORDER_UPDATE_PRICE_FAIL_ALREADY);
        }
        int newPayPrice = order.getPayPrice() + reqVO.getAdjustPrice();
        if (newPayPrice <= 0) {
            throw exception(ORDER_UPDATE_PRICE_FAIL_PRICE_ERROR);
        }

        tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId())
                .setAdjustPrice(reqVO.getAdjustPrice() + order.getAdjustPrice()).setPayPrice(newPayPrice));

        List<TradeOrderItemDO> orderOrderItems = tradeOrderItemMapper.selectListByOrderId(order.getId());
        List<Integer> dividePrices = TradePriceCalculatorHelper.dividePrice2(orderOrderItems, reqVO.getAdjustPrice());
        List<TradeOrderItemDO> updateItems = new ArrayList<>();
        for (int i = 0; i < orderOrderItems.size(); i++) {
            TradeOrderItemDO item = orderOrderItems.get(i);
            updateItems.add(new TradeOrderItemDO().setId(item.getId())
                    .setAdjustPrice(item.getAdjustPrice() + dividePrices.get(i))
                    .setPayPrice(item.getPayPrice() + dividePrices.get(i)));
        }
        tradeOrderItemMapper.updateBatch(updateItems);

        if (order.getPayOrderId() != null) {
            payOrderApi.updatePayOrderPrice(order.getPayOrderId(), newPayPrice);
        }

        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus(),
                MapUtil.<String, Object>builder().put("oldPayPrice", MoneyUtils.fenToYuanStr(order.getPayPrice()))
                        .put("adjustPrice", MoneyUtils.fenToYuanStr(reqVO.getAdjustPrice()))
                        .put("newPayPrice", MoneyUtils.fenToYuanStr(newPayPrice)).build());
    }

    @Override
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_UPDATE_ADDRESS)
    public void updateOrderAddress(TradeOrderUpdateAddressReqVO reqVO) {
        TradeOrderDO order = validateOrderExists(reqVO.getId());
        if (!TradeOrderStatusEnum.isUndelivered(order.getStatus())) {
            throw exception(ORDER_UPDATE_ADDRESS_FAIL_STATUS_NOT_DELIVERED);
        }

        List<TradeOrderDeliveryDO> deliveries = deliveryAccessSupport.getDeliveryListByOrderId(order.getId());
        TradeOrderDeliveryDO expressDelivery = deliveryAccessSupport.findDeliveryByType(
                deliveries, DeliveryTypeEnum.EXPRESS.getType());
        if (expressDelivery == null) {
            throw exception(ORDER_UPDATE_ADDRESS_FAIL_EXPRESS_DELIVERY_NOT_FOUND);
        }

        tradeOrderMapper.updateById(TradeOrderConvert.INSTANCE.convert(reqVO));
        tradeOrderDeliveryMapper.updateById(new TradeOrderDeliveryDO().setId(expressDelivery.getId())
                .setReceiverName(reqVO.getReceiverName()).setReceiverMobile(reqVO.getReceiverMobile())
                .setReceiverAreaId(reqVO.getReceiverAreaId()).setReceiverDetailAddress(reqVO.getReceiverDetailAddress()));

        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus());
    }

    private TradeOrderDO validateOrderExists(Long id) {
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        return order;
    }

}
