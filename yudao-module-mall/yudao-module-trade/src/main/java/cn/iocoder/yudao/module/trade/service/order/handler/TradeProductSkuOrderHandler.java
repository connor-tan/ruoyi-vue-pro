package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateSalesCountReqDTO;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * 商品 SKU 库存与销量的 {@link TradeOrderHandler} 实现类
 *
 * @author 芋道源码
 */
@Component
public class TradeProductSkuOrderHandler implements TradeOrderHandler {

    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    public void beforeOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        List<TradeOrderItemDO> normalItems = filterNormalOrderItems(orderItems);
        if (CollUtil.isNotEmpty(normalItems)) {
            productSkuApi.updateSkuStock(TradeOrderConvert.INSTANCE.convertNegative(normalItems));
        }
        updatePublicationSalesCount(filterPublicationOrderItems(orderItems), 1);
    }

    @Override
    public void afterCancelOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 售后的订单项，已经在 afterCancelOrderItem 回滚库存或销量，所以这里不需要重复回滚
        orderItems = filterOrderItemListByNoneAfterSale(orderItems);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        List<TradeOrderItemDO> normalItems = filterNormalOrderItems(orderItems);
        if (CollUtil.isNotEmpty(normalItems)) {
            productSkuApi.updateSkuStock(TradeOrderConvert.INSTANCE.convert(normalItems));
        }
        updatePublicationSalesCount(filterPublicationOrderItems(orderItems), -1);
    }

    @Override
    public void afterCancelOrderItem(TradeOrderDO order, TradeOrderItemDO orderItem) {
        if (isPublicationOrderItem(orderItem)) {
            updatePublicationSalesCount(singletonList(orderItem), -1);
            return;
        }
        productSkuApi.updateSkuStock(TradeOrderConvert.INSTANCE.convert(singletonList(orderItem)));
    }

    private List<TradeOrderItemDO> filterNormalOrderItems(List<TradeOrderItemDO> orderItems) {
        return orderItems.stream()
                .filter(item -> !isPublicationOrderItem(item))
                .toList();
    }

    private List<TradeOrderItemDO> filterPublicationOrderItems(List<TradeOrderItemDO> orderItems) {
        return orderItems.stream()
                .filter(this::isPublicationOrderItem)
                .toList();
    }

    private boolean isPublicationOrderItem(TradeOrderItemDO orderItem) {
        return orderItem.getSubscriptionOfferSkuId() != null
                || (orderItem.getPublicationIssueTotalCount() != null
                && orderItem.getPublicationIssueTotalCount() > 0);
    }

    private void updatePublicationSalesCount(List<TradeOrderItemDO> orderItems, int direction) {
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        List<ProductSkuUpdateSalesCountReqDTO.Item> items = orderItems.stream()
                .map(item -> new ProductSkuUpdateSalesCountReqDTO.Item()
                        .setId(item.getSkuId())
                        .setIncrCount(item.getCount() * direction))
                .toList();
        productSkuApi.updateSkuSalesCount(new ProductSkuUpdateSalesCountReqDTO(items));
    }

}
