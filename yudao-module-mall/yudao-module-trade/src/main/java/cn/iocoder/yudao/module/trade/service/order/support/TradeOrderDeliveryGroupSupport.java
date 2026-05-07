package cn.iocoder.yudao.module.trade.service.order.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.member.api.address.dto.MemberAddressRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderDeliveryRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeOrderDeliveryBuildResult;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeOrderDeliveryGroupDraft;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_ITEM_DELIVERY_TYPE_ILLEGAL;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_ITEM_DELIVERY_TYPE_REQUIRED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_SCHOOL_STATION_NOT_CONFIGURED;

@Component
public class TradeOrderDeliveryGroupSupport {

    public TradeOrderDeliveryBuildResult buildDeliveryBuildResult(TradePriceCalculateRespBO calculateRespBO,
                                                                  MemberAddressRespDTO address,
                                                                  boolean strictExpressAddress) {
        List<TradePriceCalculateRespBO.OrderItem> orderItems = filterList(calculateRespBO.getItems(),
                TradePriceCalculateRespBO.OrderItem::getSelected);
        if (CollUtil.isEmpty(orderItems)) {
            return new TradeOrderDeliveryBuildResult(Collections.emptyList(), null);
        }
        Map<String, TradeOrderDeliveryGroupDraft> planMap = new LinkedHashMap<>();
        for (int i = 0; i < calculateRespBO.getItems().size(); i++) {
            TradePriceCalculateRespBO.OrderItem item = calculateRespBO.getItems().get(i);
            if (!Boolean.TRUE.equals(item.getSelected())) {
                continue;
            }
            Integer resolvedDeliveryType = item.getResolvedDeliveryType();
            if (resolvedDeliveryType == null) {
                throw exception(ORDER_ITEM_DELIVERY_TYPE_REQUIRED);
            }
            if (CollUtil.isEmpty(item.getDeliveryTypes()) || !item.getDeliveryTypes().contains(resolvedDeliveryType)
                    || Objects.equals(resolvedDeliveryType, DeliveryTypeEnum.MIXED.getType())) {
                throw exception(ORDER_ITEM_DELIVERY_TYPE_ILLEGAL);
            }
            String bizScene = item.getBizScene();
            if (BizSceneEnum.isPublication(bizScene)) {
                if (Objects.equals(resolvedDeliveryType, DeliveryTypeEnum.STATION.getType())) {
                    if (item.getSubscriptionStudentId() == null || item.getSubscriptionStationId() == null) {
                        throw exception(ORDER_SCHOOL_STATION_NOT_CONFIGURED);
                    }
                    String key = buildPublicationGroupKey(item.getSubscriptionStudentId(), resolvedDeliveryType);
                    TradeOrderDeliveryGroupDraft group = planMap.computeIfAbsent(key,
                            ignore -> TradeOrderDeliveryGroupDraft.forPublicationItem(item));
                    group.getItemIndexes().add(i);
                    continue;
                }
                if (Objects.equals(resolvedDeliveryType, DeliveryTypeEnum.EXPRESS.getType())) {
                    if (strictExpressAddress) {
                        Assert.notNull(address, "地址不能为空");
                    }
                    String key = buildPublicationGroupKey(item.getSubscriptionStudentId(), resolvedDeliveryType);
                    TradeOrderDeliveryGroupDraft group = planMap.computeIfAbsent(key,
                            ignore -> TradeOrderDeliveryGroupDraft.forPublicationItem(item));
                    group.setAddress(address);
                    group.getItemIndexes().add(i);
                    continue;
                }
                throw exception(ORDER_ITEM_DELIVERY_TYPE_ILLEGAL);
            }
            if (Objects.equals(resolvedDeliveryType, DeliveryTypeEnum.EXPRESS.getType())) {
                if (strictExpressAddress) {
                    Assert.notNull(address, "地址不能为空");
                }
                String key = buildNormalGroupKey(resolvedDeliveryType);
                TradeOrderDeliveryGroupDraft group = planMap.computeIfAbsent(key,
                        ignore -> TradeOrderDeliveryGroupDraft.forNormal(resolvedDeliveryType));
                group.setAddress(address);
                group.getItemIndexes().add(i);
                continue;
            }
            if (Objects.equals(resolvedDeliveryType, DeliveryTypeEnum.PICK_UP.getType())) {
                String key = buildNormalGroupKey(resolvedDeliveryType);
                TradeOrderDeliveryGroupDraft group = planMap.computeIfAbsent(key,
                        ignore -> TradeOrderDeliveryGroupDraft.forNormal(resolvedDeliveryType));
                group.getItemIndexes().add(i);
                continue;
            }
            throw exception(ORDER_ITEM_DELIVERY_TYPE_ILLEGAL);
        }
        List<TradeOrderDeliveryGroupDraft> plans = new ArrayList<>(planMap.values());
        plans.forEach(plan -> plan.setSourceItems(calculateRespBO.getItems()));
        assignPreviewDeliveryIds(plans);
        return new TradeOrderDeliveryBuildResult(plans, resolveSummaryDeliveryType(plans));
    }

    public String buildPublicationGroupKey(Long studentId, Integer deliveryType) {
        return "publication:" + studentId + ":" + deliveryType;
    }

    public String buildNormalGroupKey(Integer deliveryType) {
        return "normal:" + deliveryType;
    }

    public List<AppTradeOrderDeliveryRespVO> buildSettlementDeliveries(
            TradeOrderDeliveryBuildResult deliveryBuildResult) {
        return convertList(deliveryBuildResult.plans(), plan -> {
            AppTradeOrderDeliveryRespVO respVO = new AppTradeOrderDeliveryRespVO()
                    .setId(plan.getPreviewDeliveryId())
                    .setBizScene(plan.getBizScene())
                    .setDeliveryType(plan.getDeliveryType())
                    .setProductCount(sumProductCount(plan))
                    .setPayPrice(sumPayPrice(plan))
                    .setDeliveryPrice(sumDeliveryPrice(plan))
                    .setStudentId(plan.getStudentId())
                    .setStudentNameSnapshot(plan.getStudentNameSnapshot())
                    .setSchoolId(plan.getSchoolId())
                    .setSchoolNameSnapshot(plan.getSchoolNameSnapshot())
                    .setClassId(plan.getClassId())
                    .setClassNameSnapshot(plan.getClassNameSnapshot())
                    .setGradeCatalogId(plan.getGradeCatalogId())
                    .setGradeNameSnapshot(plan.getGradeNameSnapshot())
                    .setStationId(plan.getStationId())
                    .setStationNameSnapshot(plan.getStationNameSnapshot())
                    .setStationAddressSnapshot(plan.getStationAddressSnapshot())
                    .setContactName(plan.getContactName())
                    .setContactMobile(plan.getContactMobile());
            if (Objects.equals(plan.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType()) && plan.getAddress() != null) {
                respVO.setReceiverName(plan.getAddress().getName())
                        .setReceiverMobile(plan.getAddress().getMobile())
                        .setReceiverAreaId(plan.getAddress().getAreaId())
                        .setReceiverAreaName(AreaUtils.format(plan.getAddress().getAreaId()))
                        .setReceiverDetailAddress(plan.getAddress().getDetailAddress());
            } else if (Objects.equals(plan.getDeliveryType(), DeliveryTypeEnum.PICK_UP.getType())) {
                respVO.setPickUpStoreId(plan.getPickUpStoreId())
                        .setPickUpVerifyCode(plan.getPickUpVerifyCode())
                        .setReceiverName(plan.getReceiverName())
                        .setReceiverMobile(plan.getReceiverMobile());
            }
            return respVO;
        });
    }

    public void applyPreviewDeliveryIdsToOrderItems(TradePriceCalculateRespBO calculateRespBO,
                                                    TradeOrderDeliveryBuildResult deliveryBuildResult) {
        for (TradeOrderDeliveryGroupDraft plan : deliveryBuildResult.plans()) {
            if (plan.getPreviewDeliveryId() == null) {
                continue;
            }
            for (Integer itemIndex : plan.getItemIndexes()) {
                calculateRespBO.getItems().get(itemIndex).setDeliveryId(plan.getPreviewDeliveryId());
            }
        }
    }

    public List<TradeOrderDeliveryDO> buildTradeOrderDeliveries(TradeOrderDO order,
                                                                TradeOrderDeliveryBuildResult deliveryBuildResult) {
        return convertList(deliveryBuildResult.plans(), plan -> {
            TradeOrderDeliveryDO delivery = new TradeOrderDeliveryDO()
                    .setOrderId(order.getId())
                    .setBizScene(plan.getBizScene())
                    .setDeliveryType(plan.getDeliveryType())
                    .setStatus(order.getStatus())
                    .setProductCount(sumProductCount(plan))
                    .setPayPrice(sumPayPrice(plan))
                    .setDeliveryPrice(sumDeliveryPrice(plan))
                    .setStudentId(plan.getStudentId())
                    .setStudentNameSnapshot(plan.getStudentNameSnapshot())
                    .setSchoolId(plan.getSchoolId())
                    .setSchoolNameSnapshot(plan.getSchoolNameSnapshot())
                    .setClassId(plan.getClassId())
                    .setClassNameSnapshot(plan.getClassNameSnapshot())
                    .setGradeCatalogId(plan.getGradeCatalogId())
                    .setGradeNameSnapshot(plan.getGradeNameSnapshot())
                    .setStationId(plan.getStationId())
                    .setStationNameSnapshot(plan.getStationNameSnapshot())
                    .setStationAddressSnapshot(plan.getStationAddressSnapshot())
                    .setContactName(plan.getContactName())
                    .setContactMobile(plan.getContactMobile());
            if (Objects.equals(plan.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType()) && plan.getAddress() != null) {
                delivery.setReceiverName(plan.getAddress().getName())
                        .setReceiverMobile(plan.getAddress().getMobile())
                        .setReceiverAreaId(plan.getAddress().getAreaId().intValue())
                        .setReceiverDetailAddress(plan.getAddress().getDetailAddress());
            } else if (Objects.equals(plan.getDeliveryType(), DeliveryTypeEnum.PICK_UP.getType())) {
                delivery.setPickUpStoreId(plan.getPickUpStoreId())
                        .setPickUpVerifyCode(plan.getPickUpVerifyCode())
                        .setReceiverName(plan.getReceiverName())
                        .setReceiverMobile(plan.getReceiverMobile());
            }
            plan.setPersistedDelivery(delivery);
            return delivery;
        });
    }

    public void applyDeliveryIdsToOrderItems(TradePriceCalculateRespBO calculateRespBO,
                                             TradeOrderDeliveryBuildResult deliveryBuildResult) {
        for (TradeOrderDeliveryGroupDraft plan : deliveryBuildResult.plans()) {
            if (plan.getPersistedDelivery() == null || plan.getPersistedDelivery().getId() == null) {
                continue;
            }
            for (Integer itemIndex : plan.getItemIndexes()) {
                calculateRespBO.getItems().get(itemIndex).setDeliveryId(plan.getPersistedDelivery().getId());
            }
        }
    }

    private void assignPreviewDeliveryIds(List<TradeOrderDeliveryGroupDraft> plans) {
        for (int i = 0; i < plans.size(); i++) {
            plans.get(i).setPreviewDeliveryId(-1L * (i + 1));
        }
    }

    private Integer resolveSummaryDeliveryType(List<TradeOrderDeliveryGroupDraft> plans) {
        if (CollUtil.isEmpty(plans)) {
            return null;
        }
        Set<Integer> deliveryTypes = convertSet(plans, TradeOrderDeliveryGroupDraft::getDeliveryType);
        return deliveryTypes.size() == 1 ? plans.get(0).getDeliveryType() : DeliveryTypeEnum.MIXED.getType();
    }

    private int sumProductCount(TradeOrderDeliveryGroupDraft plan) {
        return getSumValue(plan.getItemIndexes(), index -> calculateOrderItem(plan, index).getCount(), Integer::sum);
    }

    private int sumPayPrice(TradeOrderDeliveryGroupDraft plan) {
        return getSumValue(plan.getItemIndexes(), index -> calculateOrderItem(plan, index).getPayPrice(), Integer::sum);
    }

    private int sumDeliveryPrice(TradeOrderDeliveryGroupDraft plan) {
        return getSumValue(plan.getItemIndexes(), index -> calculateOrderItem(plan, index).getDeliveryPrice(), Integer::sum);
    }

    private TradePriceCalculateRespBO.OrderItem calculateOrderItem(TradeOrderDeliveryGroupDraft plan, Integer index) {
        return plan.getSourceItems().get(index);
    }

}
