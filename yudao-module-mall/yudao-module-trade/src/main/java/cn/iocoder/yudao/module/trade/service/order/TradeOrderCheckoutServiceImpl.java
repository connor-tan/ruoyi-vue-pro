package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.member.api.address.MemberAddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.MemberAddressRespDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.subscription.api.order.SubscriptionOrderEligibilityApi;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementRespVO;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.CartDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.redis.no.TradeNoRedisDAO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderOperateTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderSourceEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.framework.order.core.annotations.TradeOrderLog;
import cn.iocoder.yudao.module.trade.framework.order.core.utils.TradeOrderLogUtils;
import cn.iocoder.yudao.module.trade.service.cart.CartService;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeOrderDeliveryBuildResult;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeOrderDeliveryGroupDraft;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeOrderPreparedCalculateRequest;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeOrderSubscriptionPurchaseKey;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderDeliveryGroupSupport;
import cn.iocoder.yudao.module.trade.service.price.TradePriceService;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getTerminal;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_ITEM_DELIVERY_TYPE_ILLEGAL;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_ITEM_DELIVERY_TYPE_REQUIRED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_NORMAL_STUDENT_NOT_ALLOWED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_PUBLICATION_MULTI_DELIVERY_FOR_STUDENT;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_PUBLICATION_OFFER_SKU_REQUIRED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_PUBLICATION_STUDENT_REQUIRED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_SCHOOL_WAREHOUSE_NOT_CONFIGURED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_SPLIT_MARKETING_NOT_SUPPORTED;

/**
 * 交易订单结算下单 Service 实现类
 */
@Service
public class TradeOrderCheckoutServiceImpl implements TradeOrderCheckoutService {

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Resource
    private TradeNoRedisDAO tradeNoRedisDAO;
    @Resource
    private List<TradeOrderHandler> tradeOrderHandlers;

    @Resource
    private CartService cartService;
    @Resource
    private TradePriceService tradePriceService;
    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private MemberAddressApi addressApi;
    @Resource
    private ProductSkuApi productSkuApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private SubscriptionOrderEligibilityApi subscriptionOrderEligibilityApi;
    @Resource
    private TradeOrderProperties tradeOrderProperties;
    @Resource
    private TradeOrderDeliveryGroupSupport deliveryGroupSupport;
    @Resource
    private TradeOrderPublicationIssueService publicationIssueService;

    @Override
    public AppTradeOrderSettlementRespVO settlementOrder(Long userId, AppTradeOrderSettlementReqVO settlementReqVO) {
        MemberAddressRespDTO address = getAddress(userId, settlementReqVO.getAddressId());
        if (address != null) {
            settlementReqVO.setAddressId(address.getId());
        }

        TradePriceCalculateRespBO calculateRespBO = calculatePrice(userId, settlementReqVO);
        TradeOrderDeliveryBuildResult deliveryBuildResult = deliveryGroupSupport.buildDeliveryBuildResult(
                calculateRespBO, address, false);
        fillPickUpGroupFacts(deliveryBuildResult, settlementReqVO, false);
        deliveryGroupSupport.applyPreviewDeliveryIdsToOrderItems(calculateRespBO, deliveryBuildResult);

        return TradeOrderConvert.INSTANCE.convert(calculateRespBO, address,
                deliveryGroupSupport.buildSettlementDeliveries(deliveryBuildResult));
    }

    private MemberAddressRespDTO getAddress(Long userId, Long addressId) {
        if (addressId != null) {
            return addressApi.getAddress(addressId, userId);
        }
        return addressApi.getDefaultAddress(userId);
    }

    private TradePriceCalculateRespBO calculatePrice(Long userId, AppTradeOrderSettlementReqVO settlementReqVO) {
        TradeOrderPreparedCalculateRequest prepared = prepareCalculateRequest(userId, settlementReqVO);
        return calculatePrice(prepared, settlementReqVO);
    }

    private TradeOrderPreparedCalculateRequest prepareCalculateRequest(Long userId,
                                                                       AppTradeOrderSettlementReqVO settlementReqVO) {
        List<CartDO> cartList = cartService.getCartList(userId,
                convertSet(settlementReqVO.getItems(), AppTradeOrderSettlementReqVO.Item::getCartId));
        TradePriceCalculateReqBO baseReqBO = TradeOrderConvert.INSTANCE.convert(userId, settlementReqVO, cartList);
        baseReqBO.getItems().forEach(item -> Assert.isTrue(Boolean.TRUE.equals(item.getSelected()),
                "商品({}) 未设置为选中", item.getSkuId()));

        Map<Long, ProductSkuRespDTO> skuMap = productSkuApi.getSkuMap(
                convertSet(baseReqBO.getItems(), TradePriceCalculateReqBO.Item::getSkuId));
        Map<Long, ProductSpuRespDTO> spuMap = productSpuApi.getSpuMap(
                convertSet(skuMap.values(), ProductSkuRespDTO::getSpuId));
        Map<String, TradeOrderDeliveryGroupDraft> groupMap = new LinkedHashMap<>();
        Map<Long, Integer> studentDeliveryTypeMap = new HashMap<>();
        Map<TradeOrderSubscriptionPurchaseKey, Integer> publicationPurchaseCountMap = new HashMap<>();
        boolean publicationPresent = false;
        for (int i = 0; i < baseReqBO.getItems().size(); i++) {
            TradePriceCalculateReqBO.Item item = baseReqBO.getItems().get(i);
            ProductSkuRespDTO sku = skuMap.get(item.getSkuId());
            Assert.notNull(sku, "商品 SKU({}) 不存在", item.getSkuId());
            ProductSpuRespDTO spu = spuMap.get(sku.getSpuId());
            Assert.notNull(spu, "商品 SPU({}) 不存在", sku.getSpuId());

            Integer deliveryType = item.getDeliveryType() != null ? item.getDeliveryType() : baseReqBO.getDeliveryType();
            if (deliveryType == null) {
                throw exception(ORDER_ITEM_DELIVERY_TYPE_REQUIRED);
            }
            if (CollUtil.isEmpty(spu.getDeliveryTypes()) || !spu.getDeliveryTypes().contains(deliveryType)
                    || Objects.equals(deliveryType, DeliveryTypeEnum.MIXED.getType())) {
                throw exception(ORDER_ITEM_DELIVERY_TYPE_ILLEGAL);
            }
            String bizScene = spu.getBizScene();
            item.setDeliveryType(deliveryType);

            if (BizSceneEnum.isPublication(bizScene)) {
                publicationPresent = true;
                Long studentId = item.getSubscriptionStudentId();
                if (studentId == null) {
                    throw exception(ORDER_PUBLICATION_STUDENT_REQUIRED);
                }
                Long offerSkuId = resolveSubscriptionOfferSkuId(item);
                if (offerSkuId == null) {
                    throw exception(ORDER_PUBLICATION_OFFER_SKU_REQUIRED);
                }
                Integer accumulatedCount = accumulatePublicationPurchaseCount(publicationPurchaseCountMap, studentId,
                        offerSkuId, item.getCount());
                SubscriptionOrderEligibilityRespDTO eligibility = subscriptionOrderEligibilityApi.validateOrder(
                        new SubscriptionOrderEligibilityReqDTO()
                                .setUserId(userId)
                                .setStudentId(studentId)
                                .setOfferSkuId(offerSkuId)
                                .setSkuId(item.getSkuId())
                                .setCount(accumulatedCount));
                if (!Objects.equals(deliveryType, DeliveryTypeEnum.EXPRESS.getType())
                        && !Objects.equals(deliveryType, DeliveryTypeEnum.SCHOOL.getType())) {
                    throw exception(ORDER_ITEM_DELIVERY_TYPE_ILLEGAL);
                }
                Integer existedDeliveryType = studentDeliveryTypeMap.putIfAbsent(eligibility.getStudentId(), deliveryType);
                if (existedDeliveryType != null && !Objects.equals(existedDeliveryType, deliveryType)) {
                    throw exception(ORDER_PUBLICATION_MULTI_DELIVERY_FOR_STUDENT);
                }
                fillPublicationItemFacts(item, eligibility);
                if (Objects.equals(deliveryType, DeliveryTypeEnum.SCHOOL.getType()) && eligibility.getWarehouseId() == null) {
                    throw exception(ORDER_SCHOOL_WAREHOUSE_NOT_CONFIGURED);
                }
                String groupKey = deliveryGroupSupport.buildPublicationGroupKey(eligibility.getStudentId(), deliveryType);
                TradeOrderDeliveryGroupDraft group = groupMap.computeIfAbsent(groupKey,
                        ignore -> TradeOrderDeliveryGroupDraft.forPublication(eligibility, deliveryType));
                group.getItemIndexes().add(i);
                continue;
            }

            if (item.getSubscriptionStudentId() != null || resolveSubscriptionOfferSkuId(item) != null) {
                throw exception(ORDER_NORMAL_STUDENT_NOT_ALLOWED);
            }
            if (!Objects.equals(deliveryType, DeliveryTypeEnum.EXPRESS.getType())
                    && !Objects.equals(deliveryType, DeliveryTypeEnum.PICK_UP.getType())) {
                throw exception(ORDER_ITEM_DELIVERY_TYPE_ILLEGAL);
            }
            String groupKey = deliveryGroupSupport.buildNormalGroupKey(deliveryType);
            TradeOrderDeliveryGroupDraft group = groupMap.computeIfAbsent(groupKey,
                    ignore -> TradeOrderDeliveryGroupDraft.forNormal(deliveryType));
            group.getItemIndexes().add(i);
        }
        return new TradeOrderPreparedCalculateRequest(baseReqBO, new ArrayList<>(groupMap.values()), publicationPresent);
    }

    private void fillPublicationItemFacts(TradePriceCalculateReqBO.Item item, SubscriptionOrderEligibilityRespDTO eligibility) {
        item.setSubscriptionStudentId(eligibility.getStudentId())
                .setSubscriptionStudentNameSnapshot(eligibility.getStudentNameSnapshot())
                .setSubscriptionSchoolId(eligibility.getSchoolId())
                .setSubscriptionSchoolNameSnapshot(eligibility.getSchoolNameSnapshot())
                .setSubscriptionSchoolAddressSnapshot(eligibility.getSchoolAddressSnapshot())
                .setSubscriptionClassId(eligibility.getClassId())
                .setSubscriptionClassNameSnapshot(eligibility.getClassNameSnapshot())
                .setSubscriptionGradeCatalogId(eligibility.getGradeCatalogId())
                .setSubscriptionGradeNameSnapshot(eligibility.getGradeNameSnapshot())
                .setSubscriptionWarehouseId(eligibility.getWarehouseId())
                .setSubscriptionWarehouseNameSnapshot(eligibility.getWarehouseNameSnapshot())
                .setSubscriptionWarehouseAddressSnapshot(eligibility.getWarehouseAddressSnapshot())
                .setSubscriptionContactName(eligibility.getContactName())
                .setSubscriptionContactMobile(eligibility.getContactMobile())
                .setSubscriptionWindowId(eligibility.getWindowId())
                .setSubscriptionWindowNameSnapshot(eligibility.getWindowNameSnapshot())
                .setSubscriptionTargetYearStart(eligibility.getTargetYearStart())
                .setSubscriptionTargetYearEnd(eligibility.getTargetYearEnd())
                .setSubscriptionOfferId(eligibility.getOfferId())
                .setSubscriptionOfferSkuId(eligibility.getOfferSkuId())
                .setSubscriptionVisibilityReason(eligibility.getVisibilityReason())
                .setSubscriptionMatchedRuleId(eligibility.getMatchedRuleId())
                .setSubscriptionGradeApplicabilityOverride(eligibility.getGradeApplicabilityOverride())
                .setPublicationIssueMode(eligibility.getIssueMode())
                .setPublicationIssueTotalCount(eligibility.getIssueCount())
                .setPublicationIssues(convertList(eligibility.getIssues(), issue ->
                        new TradePriceCalculateReqBO.PublicationIssueSnapshot()
                                .setIssueId(issue.getIssueId())
                                .setIssueNo(issue.getIssueNo())
                                .setIssueName(issue.getIssueName())
                                .setPlannedPublishDate(issue.getPlannedPublishDate())
                                .setPlannedDeliveryDate(issue.getPlannedDeliveryDate())));
    }

    Integer accumulatePublicationPurchaseCount(Map<TradeOrderSubscriptionPurchaseKey, Integer> purchaseCountMap,
                                               Long studentId, Long offerSkuId, Integer count) {
        return purchaseCountMap.merge(new TradeOrderSubscriptionPurchaseKey(studentId, offerSkuId), count, Integer::sum);
    }

    private Long resolveSubscriptionOfferSkuId(TradePriceCalculateReqBO.Item item) {
        return item.getSubscriptionOfferSkuId();
    }

    private TradePriceCalculateRespBO calculatePrice(TradeOrderPreparedCalculateRequest prepared,
                                                     AppTradeOrderSettlementReqVO settlementReqVO) {
        if ((prepared.publicationPresent() || prepared.groupDrafts().size() > 1)
                && hasComplexMarketing(settlementReqVO)) {
            throw exception(ORDER_SPLIT_MARKETING_NOT_SUPPORTED);
        }

        List<TradePriceCalculateRespBO> groupResults = new ArrayList<>(prepared.groupDrafts().size());
        for (TradeOrderDeliveryGroupDraft groupDraft : prepared.groupDrafts()) {
            TradePriceCalculateReqBO groupReqBO = buildGroupCalculateReqBO(prepared.baseReqBO(), groupDraft, settlementReqVO);
            groupResults.add(tradePriceService.calculateOrderPrice(groupReqBO));
        }
        return mergeGroupCalculateResult(prepared, groupResults);
    }

    private boolean hasComplexMarketing(AppTradeOrderSettlementReqVO settlementReqVO) {
        return settlementReqVO.getCouponId() != null
                || Boolean.TRUE.equals(settlementReqVO.getPointStatus())
                || settlementReqVO.getSeckillActivityId() != null
                || settlementReqVO.getCombinationActivityId() != null
                || settlementReqVO.getCombinationHeadId() != null
                || settlementReqVO.getBargainRecordId() != null
                || settlementReqVO.getPointActivityId() != null;
    }

    private TradePriceCalculateReqBO buildGroupCalculateReqBO(TradePriceCalculateReqBO baseReqBO,
                                                              TradeOrderDeliveryGroupDraft groupDraft,
                                                              AppTradeOrderSettlementReqVO settlementReqVO) {
        TradePriceCalculateReqBO groupReqBO = new TradePriceCalculateReqBO()
                .setUserId(baseReqBO.getUserId())
                .setPointStatus(baseReqBO.getPointStatus())
                .setCouponId(baseReqBO.getCouponId())
                .setDeliveryType(groupDraft.getDeliveryType())
                .setSeckillActivityId(baseReqBO.getSeckillActivityId())
                .setBargainRecordId(baseReqBO.getBargainRecordId())
                .setCombinationActivityId(baseReqBO.getCombinationActivityId())
                .setCombinationHeadId(baseReqBO.getCombinationHeadId())
                .setPointActivityId(baseReqBO.getPointActivityId())
                .setItems(new ArrayList<>(groupDraft.getItemIndexes().size()));
        if (Objects.equals(groupDraft.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            groupReqBO.setAddressId(settlementReqVO.getAddressId());
        } else if (Objects.equals(groupDraft.getDeliveryType(), DeliveryTypeEnum.PICK_UP.getType())) {
            groupReqBO.setPickUpStoreId(settlementReqVO.getPickUpStoreId());
        }
        for (Integer itemIndex : groupDraft.getItemIndexes()) {
            groupReqBO.getItems().add(baseReqBO.getItems().get(itemIndex));
        }
        return groupReqBO;
    }

    private TradePriceCalculateRespBO mergeGroupCalculateResult(TradeOrderPreparedCalculateRequest prepared,
                                                                List<TradePriceCalculateRespBO> groupResults) {
        TradePriceCalculateRespBO merged = new TradePriceCalculateRespBO();
        merged.setType(groupResults.isEmpty() ? null : groupResults.get(0).getType());
        merged.setPrice(new TradePriceCalculateRespBO.Price()
                .setTotalPrice(0).setDiscountPrice(0).setDeliveryPrice(0)
                .setCouponPrice(0).setPointPrice(0).setVipPrice(0).setPayPrice(0));
        merged.setItems(new ArrayList<>(Collections.<TradePriceCalculateRespBO.OrderItem>nCopies(
                prepared.baseReqBO().getItems().size(), null)));
        merged.setPromotions(new ArrayList<>());
        merged.setCoupons(prepared.groupDrafts().size() == 1 && !groupResults.isEmpty()
                ? groupResults.get(0).getCoupons() : Collections.emptyList());
        merged.setCouponId(prepared.groupDrafts().size() == 1 && !groupResults.isEmpty()
                ? groupResults.get(0).getCouponId() : null);
        merged.setTotalPoint(groupResults.isEmpty() ? 0 : groupResults.get(0).getTotalPoint());
        merged.setUsePoint(groupResults.isEmpty() ? 0 : groupResults.get(0).getUsePoint());
        merged.setGivePoint(0);
        merged.setFreeDelivery(groupResults.stream().allMatch(resp -> Boolean.TRUE.equals(resp.getFreeDelivery())));
        merged.setGiveCouponTemplateCounts(new LinkedHashMap<>());

        for (int i = 0; i < prepared.groupDrafts().size(); i++) {
            TradeOrderDeliveryGroupDraft groupDraft = prepared.groupDrafts().get(i);
            TradePriceCalculateRespBO groupRespBO = groupResults.get(i);
            mergePrice(merged.getPrice(), groupRespBO.getPrice());
            merged.getPromotions().addAll(groupRespBO.getPromotions() == null ? Collections.emptyList() : groupRespBO.getPromotions());
            merged.setGivePoint(merged.getGivePoint() + ObjectUtil.defaultIfNull(groupRespBO.getGivePoint(), 0));
            mergeGiveCouponTemplateCounts(merged.getGiveCouponTemplateCounts(), groupRespBO.getGiveCouponTemplateCounts());
            for (int itemIndex = 0; itemIndex < groupDraft.getItemIndexes().size(); itemIndex++) {
                merged.getItems().set(groupDraft.getItemIndexes().get(itemIndex), groupRespBO.getItems().get(itemIndex));
            }
        }
        merged.getItems().removeIf(Objects::isNull);
        return merged;
    }

    private void mergePrice(TradePriceCalculateRespBO.Price target, TradePriceCalculateRespBO.Price source) {
        target.setTotalPrice(target.getTotalPrice() + ObjectUtil.defaultIfNull(source.getTotalPrice(), 0));
        target.setDiscountPrice(target.getDiscountPrice() + ObjectUtil.defaultIfNull(source.getDiscountPrice(), 0));
        target.setDeliveryPrice(target.getDeliveryPrice() + ObjectUtil.defaultIfNull(source.getDeliveryPrice(), 0));
        target.setCouponPrice(target.getCouponPrice() + ObjectUtil.defaultIfNull(source.getCouponPrice(), 0));
        target.setPointPrice(target.getPointPrice() + ObjectUtil.defaultIfNull(source.getPointPrice(), 0));
        target.setVipPrice(target.getVipPrice() + ObjectUtil.defaultIfNull(source.getVipPrice(), 0));
        target.setPayPrice(target.getPayPrice() + ObjectUtil.defaultIfNull(source.getPayPrice(), 0));
    }

    private void mergeGiveCouponTemplateCounts(Map<Long, Integer> target, Map<Long, Integer> source) {
        if (source == null || source.isEmpty()) {
            return;
        }
        source.forEach((couponTemplateId, count) -> target.merge(couponTemplateId, count, Integer::sum));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_CREATE)
    public TradeOrderDO createOrder(Long userId, AppTradeOrderCreateReqVO createReqVO) {
        TradeOrderPreparedCalculateRequest prepared = prepareCalculateRequest(userId, createReqVO);
        TradePriceCalculateRespBO calculateRespBO = calculatePrice(prepared, createReqVO);
        MemberAddressRespDTO address = getAddress(userId, createReqVO.getAddressId());
        TradeOrderDeliveryBuildResult deliveryBuildResult = deliveryGroupSupport.buildDeliveryBuildResult(
                calculateRespBO, address, true);
        fillPickUpGroupFacts(deliveryBuildResult, createReqVO, true);
        TradeOrderDO order = buildTradeOrder(userId, createReqVO, calculateRespBO, deliveryBuildResult);
        List<TradeOrderItemDO> previewOrderItems = buildTradeOrderItems(order, calculateRespBO);

        tradeOrderHandlers.forEach(handler -> handler.beforeOrderCreate(order, previewOrderItems));

        tradeOrderMapper.insert(order);
        List<TradeOrderDeliveryDO> orderDeliveries = deliveryGroupSupport.buildTradeOrderDeliveries(order, deliveryBuildResult);
        orderDeliveries.forEach(tradeOrderDeliveryMapper::insert);
        deliveryGroupSupport.applyDeliveryIdsToOrderItems(calculateRespBO, deliveryBuildResult);
        List<TradeOrderItemDO> orderItems = buildTradeOrderItems(order, calculateRespBO);
        orderItems.forEach(orderItem -> orderItem.setOrderId(order.getId()));
        tradeOrderItemMapper.insertBatch(orderItems);

        publicationIssueService.createOrderIssues(order, orderItems, calculateRespBO.getItems());
        afterCreateTradeOrder(order, orderItems, createReqVO);
        return order;
    }

    private TradeOrderDO buildTradeOrder(Long userId, AppTradeOrderCreateReqVO createReqVO,
                                         TradePriceCalculateRespBO calculateRespBO,
                                         TradeOrderDeliveryBuildResult deliveryBuildResult) {
        TradeOrderDO order = TradeOrderConvert.INSTANCE.convert(userId, createReqVO, calculateRespBO);
        order.setType(calculateRespBO.getType());
        order.setNo(tradeNoRedisDAO.generate(TradeNoRedisDAO.TRADE_ORDER_NO_PREFIX));
        order.setStatus(TradeOrderStatusEnum.UNPAID.getStatus());
        order.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus());
        order.setProductCount(getSumValue(calculateRespBO.getItems(), TradePriceCalculateRespBO.OrderItem::getCount, Integer::sum));
        order.setUserIp(getClientIP()).setTerminal(getTerminal()).setOrderSource(TradeOrderSourceEnum.APP.getSource());
        order.setGiveCouponTemplateCounts(calculateRespBO.getGiveCouponTemplateCounts());
        order.setAdjustPrice(0).setPayStatus(false);
        order.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus()).setRefundPrice(0);
        order.setDeliveryType(deliveryBuildResult.summaryDeliveryType());
        TradeOrderDeliveryGroupDraft expressGroup = deliveryBuildResult.findByDeliveryType(DeliveryTypeEnum.EXPRESS.getType());
        if (expressGroup != null && expressGroup.getAddress() != null) {
            MemberAddressRespDTO address = expressGroup.getAddress();
            order.setReceiverName(address.getName()).setReceiverMobile(address.getMobile())
                    .setReceiverAreaId(address.getAreaId()).setReceiverDetailAddress(address.getDetailAddress());
        } else {
            TradeOrderDeliveryGroupDraft schoolGroup = deliveryBuildResult.findByDeliveryType(DeliveryTypeEnum.SCHOOL.getType());
            if (schoolGroup != null) {
                fillSchoolOrderReceiver(order, schoolGroup);
                return order;
            }
            TradeOrderDeliveryGroupDraft pickUpGroup = deliveryBuildResult.findByDeliveryType(DeliveryTypeEnum.PICK_UP.getType());
            if (pickUpGroup != null) {
                order.setReceiverName(pickUpGroup.getReceiverName()).setReceiverMobile(pickUpGroup.getReceiverMobile());
                order.setPickUpVerifyCode(pickUpGroup.getPickUpVerifyCode());
                order.setPickUpStoreId(pickUpGroup.getPickUpStoreId());
            }
        }
        return order;
    }

    private void fillSchoolOrderReceiver(TradeOrderDO order, TradeOrderDeliveryGroupDraft schoolGroup) {
        order.setReceiverName(StrUtil.blankToDefault(schoolGroup.getSchoolNameSnapshot(),
                        StrUtil.blankToDefault(schoolGroup.getContactName(), schoolGroup.getStudentNameSnapshot())))
                .setReceiverMobile(StrUtil.nullToDefault(schoolGroup.getContactMobile(), ""))
                .setReceiverDetailAddress(schoolGroup.getSchoolAddressSnapshot());
    }

    private void fillPickUpGroupFacts(TradeOrderDeliveryBuildResult deliveryBuildResult,
                                      AppTradeOrderSettlementReqVO reqVO,
                                      boolean generateVerifyCode) {
        TradeOrderDeliveryGroupDraft pickUpGroup = deliveryBuildResult.findByDeliveryType(DeliveryTypeEnum.PICK_UP.getType());
        if (pickUpGroup == null) {
            return;
        }
        pickUpGroup.setPickUpStoreId(reqVO.getPickUpStoreId());
        pickUpGroup.setReceiverName(reqVO.getReceiverName());
        pickUpGroup.setReceiverMobile(reqVO.getReceiverMobile());
        if (generateVerifyCode) {
            pickUpGroup.setPickUpVerifyCode(RandomUtil.randomNumbers(8));
        }
    }

    private List<TradeOrderItemDO> buildTradeOrderItems(TradeOrderDO tradeOrderDO,
                                                        TradePriceCalculateRespBO calculateRespBO) {
        return TradeOrderConvert.INSTANCE.convertList(tradeOrderDO, calculateRespBO);
    }

    private void afterCreateTradeOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems,
                                       AppTradeOrderCreateReqVO createReqVO) {
        tradeOrderHandlers.forEach(handler -> handler.afterOrderCreate(order, orderItems));

        Set<Long> cartIds = convertSet(createReqVO.getItems(), AppTradeOrderSettlementReqVO.Item::getCartId);
        if (CollUtil.isNotEmpty(cartIds)) {
            cartService.deleteCart(order.getUserId(), cartIds);
        }

        if (order.getPayPrice() > 0) {
            createPayOrder(order, orderItems);
        }

        TradeOrderLogUtils.setOrderInfo(order.getId(), null, order.getStatus());
    }

    private void createPayOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        PayOrderCreateReqDTO payOrderCreateReqDTO = TradeOrderConvert.INSTANCE.convert(
                order, orderItems, tradeOrderProperties);
        Long payOrderId = payOrderApi.createOrder(payOrderCreateReqDTO);

        tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId()).setPayOrderId(payOrderId));
        order.setPayOrderId(payOrderId);
    }

}
