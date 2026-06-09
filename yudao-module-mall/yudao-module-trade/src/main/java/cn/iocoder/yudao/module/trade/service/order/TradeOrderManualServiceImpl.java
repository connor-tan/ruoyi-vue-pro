package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.member.api.address.dto.MemberAddressRespDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderOfflineCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.subscription.api.order.SubscriptionOrderEligibilityApi;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderManualCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderManualImportExcelVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderManualImportRespVO;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderDeliveryMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.redis.no.TradeNoRedisDAO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderCancelTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderOperateTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderSourceEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.framework.order.core.annotations.TradeOrderLog;
import cn.iocoder.yudao.module.trade.framework.order.core.utils.TradeOrderLogUtils;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeOrderDeliveryBuildResult;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeOrderDeliveryGroupDraft;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeOrderSubscriptionPurchaseKey;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import cn.iocoder.yudao.module.trade.service.order.support.TradeOrderDeliveryGroupSupport;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import cn.iocoder.yudao.module.trade.service.price.calculator.TradePriceCalculatorHelper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_ENABLE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;

@Service
@Validated
public class TradeOrderManualServiceImpl implements TradeOrderManualService {

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderDeliveryMapper tradeOrderDeliveryMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Resource
    private TradeNoRedisDAO tradeNoRedisDAO;
    @Resource
    private ProductSkuApi productSkuApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private SubscriptionOrderEligibilityApi subscriptionOrderEligibilityApi;
    @Resource
    private TradeOrderDeliveryGroupSupport deliveryGroupSupport;
    @Resource
    private TradeOrderPublicationIssueService publicationIssueService;
    @Resource
    private List<TradeOrderHandler> tradeOrderHandlers;
    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private TradeOrderProperties tradeOrderProperties;
    @Resource
    @Lazy
    private TradeOrderManualService self;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_CREATE)
    public Long createManualOrder(TradeOrderManualCreateReqVO reqVO) {
        return doCreateOrder(reqVO, TradeOrderSourceEnum.ADMIN_MANUAL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_CREATE)
    public Long createImportOrder(TradeOrderManualCreateReqVO reqVO) {
        return doCreateOrder(reqVO, TradeOrderSourceEnum.ADMIN_IMPORT);
    }

    @Override
    public TradeOrderManualImportRespVO importManualOrders(List<TradeOrderManualImportExcelVO> rows) {
        Map<String, List<TradeOrderManualImportExcelVO>> groupMap = groupImportRows(rows);
        List<TradeOrderManualImportRespVO.Item> results = new ArrayList<>(groupMap.size());
        int successCount = 0;
        for (Map.Entry<String, List<TradeOrderManualImportExcelVO>> entry : groupMap.entrySet()) {
            TradeOrderManualImportRespVO.Item result = new TradeOrderManualImportRespVO.Item()
                    .setImportOrderNo(entry.getKey());
            try {
                Long orderId = self.createImportOrder(convertImportGroup(entry.getValue()));
                result.setSuccess(true).setOrderId(orderId).setMessage("导入成功");
                successCount++;
            } catch (ServiceException e) {
                result.setSuccess(false).setMessage(e.getMessage());
            } catch (Exception e) {
                result.setSuccess(false).setMessage("导入失败：" + e.getMessage());
            }
            results.add(result);
        }
        return new TradeOrderManualImportRespVO()
                .setSuccessCount(successCount)
                .setFailureCount(results.size() - successCount)
                .setItems(results);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_OFFLINE_PAY)
    public Long confirmOfflinePay(Long id) {
        TradeOrderDO order = validateManualOrder(id, ORDER_MANUAL_CONFIRM_PAY_SOURCE_INVALID);
        if (!TradeOrderStatusEnum.isUnpaid(order.getStatus()) || Boolean.TRUE.equals(order.getPayStatus())
                || order.getPayOrderId() != null) {
            throw exception(ORDER_MANUAL_CONFIRM_PAY_STATUS_INVALID);
        }
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(id);
        Long payOrderId = payOrderApi.createOfflinePaidOrder(new PayOrderOfflineCreateReqDTO()
                .setAppKey(tradeOrderProperties.getPayAppKey())
                .setUserIp(StrUtil.blankToDefault(order.getUserIp(), NetUtil.getLocalhostStr()))
                .setUserId(order.getUserId())
                .setUserType(order.getUserId() == null ? null : UserTypeEnum.MEMBER.getValue())
                .setMerchantOrderId(String.valueOf(order.getId()))
                .setSubject(buildPaySubject(orderItems))
                .setBody(buildPaySubject(orderItems))
                .setPrice(order.getPayPrice()));
        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(),
                new TradeOrderDO().setStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus())
                        .setPayStatus(true).setPayOrderId(payOrderId)
                        .setPayTime(LocalDateTime.now()).setPayChannelCode(payOrder.getChannelCode()));
        if (updateCount == 0) {
            throw exception(ORDER_UPDATE_PAID_STATUS_NOT_UNPAID);
        }
        for (TradeOrderDeliveryDO delivery : tradeOrderDeliveryMapper.selectListByOrderId(id)) {
            if (TradeOrderStatusEnum.isUnpaid(delivery.getStatus())) {
                tradeOrderDeliveryMapper.updateById(new TradeOrderDeliveryDO().setId(delivery.getId())
                        .setStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus()));
            }
        }
        order.setPayOrderId(payOrderId).setPayStatus(true).setPayChannelCode(payOrder.getChannelCode());
        tradeOrderHandlers.forEach(handler -> handler.afterPayOrder(order, orderItems));
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.UNDELIVERED.getStatus());
        return payOrderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_CANCEL)
    public void cancelManualOrder(Long id) {
        TradeOrderDO order = validateManualOrder(id, ORDER_MANUAL_CANCEL_SOURCE_INVALID);
        if (!TradeOrderStatusEnum.isUnpaid(order.getStatus()) || Boolean.TRUE.equals(order.getPayStatus())) {
            throw exception(ORDER_CANCEL_FAIL_STATUS_NOT_UNPAID);
        }
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(),
                new TradeOrderDO().setStatus(TradeOrderStatusEnum.CANCELED.getStatus())
                        .setCancelType(TradeOrderCancelTypeEnum.ADMIN_CANCEL.getType())
                        .setCancelTime(LocalDateTime.now()));
        if (updateCount == 0) {
            throw exception(ORDER_CANCEL_FAIL_STATUS_NOT_UNPAID);
        }
        for (TradeOrderDeliveryDO delivery : tradeOrderDeliveryMapper.selectListByOrderId(id)) {
            if (TradeOrderStatusEnum.isUnpaid(delivery.getStatus())) {
                tradeOrderDeliveryMapper.updateById(new TradeOrderDeliveryDO().setId(delivery.getId())
                        .setStatus(TradeOrderStatusEnum.CANCELED.getStatus()));
            }
        }
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(id);
        publicationIssueService.cancelByOrderId(id);
        tradeOrderHandlers.forEach(handler -> handler.afterCancelOrder(order, orderItems));
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.CANCELED.getStatus());
    }

    private Long doCreateOrder(TradeOrderManualCreateReqVO reqVO, TradeOrderSourceEnum source) {
        TradePriceCalculateReqBO baseReqBO = buildBaseCalculateReq(reqVO);
        List<ProductSkuRespDTO> skuList = productSkuApi.getSkuList(convertSet(baseReqBO.getItems(),
                TradePriceCalculateReqBO.Item::getSkuId));
        Map<Long, ProductSkuRespDTO> skuMap = skuList.stream()
                .collect(java.util.stream.Collectors.toMap(ProductSkuRespDTO::getId, item -> item));
        List<ProductSpuRespDTO> spuList = productSpuApi.validateSpuList(convertSet(skuList, ProductSkuRespDTO::getSpuId));
        Map<Long, ProductSpuRespDTO> spuMap = spuList.stream()
                .collect(java.util.stream.Collectors.toMap(ProductSpuRespDTO::getId, item -> item));
        fillManualSubscriptionFacts(baseReqBO, skuMap, spuMap);

        TradePriceCalculateRespBO calculateRespBO = TradePriceCalculatorHelper.buildCalculateResp(baseReqBO, spuList, skuList);
        applyManualPrices(reqVO, calculateRespBO);
        MemberAddressRespDTO address = buildManualAddressIfNecessary(calculateRespBO, reqVO);
        TradeOrderDeliveryBuildResult deliveryBuildResult = deliveryGroupSupport.buildDeliveryBuildResult(
                calculateRespBO, address, true);
        fillManualPickUpGroupFacts(deliveryBuildResult, reqVO);
        TradeOrderDO order = buildManualTradeOrder(reqVO, source, calculateRespBO, deliveryBuildResult);
        List<TradeOrderItemDO> previewItems = buildManualTradeOrderItems(order, calculateRespBO);
        tradeOrderHandlers.forEach(handler -> handler.beforeOrderCreate(order, previewItems));

        tradeOrderMapper.insert(order);
        List<TradeOrderDeliveryDO> deliveries = deliveryGroupSupport.buildTradeOrderDeliveries(order, deliveryBuildResult);
        deliveries.forEach(tradeOrderDeliveryMapper::insert);
        deliveryGroupSupport.applyDeliveryIdsToOrderItems(calculateRespBO, deliveryBuildResult);
        List<TradeOrderItemDO> orderItems = buildManualTradeOrderItems(order, calculateRespBO);
        orderItems.forEach(orderItem -> orderItem.setOrderId(order.getId()));
        tradeOrderItemMapper.insertBatch(orderItems);
        publicationIssueService.createOrderIssues(order, orderItems, calculateRespBO.getItems());
        tradeOrderHandlers.forEach(handler -> handler.afterOrderCreate(order, orderItems));
        TradeOrderLogUtils.setOrderInfo(order.getId(), null, order.getStatus());
        return order.getId();
    }

    private TradePriceCalculateReqBO buildBaseCalculateReq(TradeOrderManualCreateReqVO reqVO) {
        if (CollUtil.isEmpty(reqVO.getItems())) {
            throw exception(ORDER_MANUAL_ITEM_REQUIRED);
        }
        TradePriceCalculateReqBO reqBO = new TradePriceCalculateReqBO()
                .setUserId(null)
                .setPointStatus(false)
                .setDeliveryType(reqVO.getDeliveryType())
                .setItems(new ArrayList<>(reqVO.getItems().size()));
        for (TradeOrderManualCreateReqVO.Item item : reqVO.getItems()) {
            if (item.getCount() == null || item.getCount() <= 0) {
                throw exception(ORDER_MANUAL_ITEM_COUNT_INVALID);
            }
            reqBO.getItems().add(new TradePriceCalculateReqBO.Item()
                    .setSkuId(item.getSkuId())
                    .setCount(item.getCount())
                    .setSelected(true)
                    .setDeliveryType(item.getDeliveryType() != null ? item.getDeliveryType() : reqVO.getDeliveryType())
                    .setSubscriptionStudentId(item.getStudentId())
                    .setSubscriptionOfferSkuId(item.getOfferSkuId()));
        }
        return reqBO;
    }

    private void fillManualSubscriptionFacts(TradePriceCalculateReqBO reqBO, Map<Long, ProductSkuRespDTO> skuMap,
                                             Map<Long, ProductSpuRespDTO> spuMap) {
        Map<TradeOrderSubscriptionPurchaseKey, Integer> purchaseCountMap = new HashMap<>();
        Map<Long, Integer> studentDeliveryTypeMap = new HashMap<>();
        List<ManualPublicationOrderItemCandidate> publicationCandidates = new ArrayList<>();
        for (TradePriceCalculateReqBO.Item item : reqBO.getItems()) {
            ProductSkuRespDTO sku = skuMap.get(item.getSkuId());
            if (sku == null) {
                throw exception(ORDER_ITEM_NOT_FOUND);
            }
            if (!CommonStatusEnum.isEnable(sku.getStatus())) {
                throw exception(SKU_NOT_ENABLE);
            }
            ProductSpuRespDTO spu = spuMap.get(sku.getSpuId());
            if (spu == null) {
                throw exception(ORDER_ITEM_NOT_FOUND);
            }
            Integer deliveryType = item.getDeliveryType() != null ? item.getDeliveryType() : reqBO.getDeliveryType();
            if (deliveryType == null) {
                throw exception(ORDER_ITEM_DELIVERY_TYPE_REQUIRED);
            }
            if (CollUtil.isEmpty(spu.getDeliveryTypes()) || !spu.getDeliveryTypes().contains(deliveryType)
                    || Objects.equals(deliveryType, DeliveryTypeEnum.MIXED.getType())) {
                throw exception(ORDER_ITEM_DELIVERY_TYPE_ILLEGAL);
            }
            item.setDeliveryType(deliveryType);
            if (!BizSceneEnum.isPublication(spu.getBizScene())) {
                if (item.getSubscriptionStudentId() != null || item.getSubscriptionOfferSkuId() != null) {
                    throw exception(ORDER_NORMAL_STUDENT_NOT_ALLOWED);
                }
                if (!Objects.equals(deliveryType, DeliveryTypeEnum.EXPRESS.getType())) {
                    throw exception(ORDER_ITEM_DELIVERY_TYPE_ILLEGAL);
                }
                continue;
            }
            if (item.getSubscriptionStudentId() == null) {
                throw exception(ORDER_MANUAL_STUDENT_REQUIRED);
            }
            if (item.getSubscriptionOfferSkuId() == null) {
                throw exception(ORDER_PUBLICATION_OFFER_SKU_REQUIRED);
            }
            if (!Objects.equals(deliveryType, DeliveryTypeEnum.EXPRESS.getType())
                    && !Objects.equals(deliveryType, DeliveryTypeEnum.SCHOOL.getType())) {
                throw exception(ORDER_ITEM_DELIVERY_TYPE_ILLEGAL);
            }
            Integer existedDeliveryType = studentDeliveryTypeMap.putIfAbsent(item.getSubscriptionStudentId(), deliveryType);
            if (existedDeliveryType != null && !Objects.equals(existedDeliveryType, deliveryType)) {
                throw exception(ORDER_PUBLICATION_MULTI_DELIVERY_FOR_STUDENT);
            }
            TradeOrderSubscriptionPurchaseKey key = new TradeOrderSubscriptionPurchaseKey(
                    item.getSubscriptionStudentId(), item.getSubscriptionOfferSkuId());
            Integer accumulatedCount = purchaseCountMap.merge(key, item.getCount(), Integer::sum);
            publicationCandidates.add(new ManualPublicationOrderItemCandidate(item, item.getSubscriptionStudentId(),
                    item.getSubscriptionOfferSkuId(), accumulatedCount, deliveryType));
        }
        fillManualPublicationFacts(publicationCandidates);
    }

    private void fillManualPublicationFacts(List<ManualPublicationOrderItemCandidate> publicationCandidates) {
        if (CollUtil.isEmpty(publicationCandidates)) {
            return;
        }
        List<SubscriptionOrderEligibilityReqDTO> reqList = convertList(publicationCandidates, candidate ->
                new SubscriptionOrderEligibilityReqDTO()
                        .setAdmin(true)
                        .setUserId(null)
                        .setStudentId(candidate.studentId)
                        .setOfferSkuId(candidate.offerSkuId)
                        .setSkuId(candidate.item.getSkuId())
                        .setCount(candidate.accumulatedCount)
                        .setLockAnchor(true));
        List<SubscriptionOrderEligibilityRespDTO> eligibilities = reqList.size() > 1
                ? subscriptionOrderEligibilityApi.validateOrderList(reqList)
                : convertList(reqList, subscriptionOrderEligibilityApi::validateOrder);
        for (int i = 0; i < publicationCandidates.size(); i++) {
            ManualPublicationOrderItemCandidate candidate = publicationCandidates.get(i);
            SubscriptionOrderEligibilityRespDTO eligibility = eligibilities.get(i);
            if (Objects.equals(candidate.deliveryType, DeliveryTypeEnum.SCHOOL.getType())
                    && eligibility.getWarehouseId() == null) {
                throw exception(ORDER_SCHOOL_WAREHOUSE_NOT_CONFIGURED);
            }
            fillPublicationItemFacts(candidate.item, eligibility);
        }
    }

    private static final class ManualPublicationOrderItemCandidate {

        private final TradePriceCalculateReqBO.Item item;
        private final Long studentId;
        private final Long offerSkuId;
        private final Integer accumulatedCount;
        private final Integer deliveryType;

        private ManualPublicationOrderItemCandidate(TradePriceCalculateReqBO.Item item, Long studentId,
                                                    Long offerSkuId, Integer accumulatedCount, Integer deliveryType) {
            this.item = item;
            this.studentId = studentId;
            this.offerSkuId = offerSkuId;
            this.accumulatedCount = accumulatedCount;
            this.deliveryType = deliveryType;
        }

    }

    private void fillPublicationItemFacts(TradePriceCalculateReqBO.Item item,
                                          SubscriptionOrderEligibilityRespDTO eligibility) {
        item.setSubscriptionStudentId(eligibility.getStudentId())
                .setSubscriptionStudentNameSnapshot(eligibility.getStudentNameSnapshot())
                .setSubscriptionSchoolId(eligibility.getSchoolId())
                .setSubscriptionSchoolNameSnapshot(eligibility.getSchoolNameSnapshot())
                .setSubscriptionSchoolAddressSnapshot(eligibility.getSchoolAddressSnapshot())
                .setSubscriptionStationId(eligibility.getStationId())
                .setSubscriptionStationNameSnapshot(eligibility.getStationNameSnapshot())
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

    private void applyManualPrices(TradeOrderManualCreateReqVO reqVO, TradePriceCalculateRespBO calculateRespBO) {
        for (int i = 0; i < calculateRespBO.getItems().size(); i++) {
            TradePriceCalculateRespBO.OrderItem orderItem = calculateRespBO.getItems().get(i);
            Integer unitPrice = reqVO.getItems().get(i).getManualUnitPrice();
            if (unitPrice != null) {
                orderItem.setPrice(unitPrice);
            }
            orderItem.setDiscountPrice(0).setDeliveryPrice(0).setCouponPrice(0).setPointPrice(0)
                    .setVipPrice(0).setUsePoint(0).setGivePoint(0)
                    .setPayPrice(orderItem.getPrice() * orderItem.getCount());
        }
        int totalPrice = getSumValue(calculateRespBO.getItems(),
                item -> item.getPrice() * item.getCount(), Integer::sum);
        int manualOrderPrice = reqVO.getManualOrderPrice() == null ? totalPrice : reqVO.getManualOrderPrice();
        if (manualOrderPrice <= 0) {
            throw exception(ORDER_MANUAL_PRICE_INVALID);
        }
        List<Integer> adjustmentShares = divideAdjustment(calculateRespBO.getItems(), manualOrderPrice - totalPrice);
        for (int i = 0; i < calculateRespBO.getItems().size(); i++) {
            TradePriceCalculateRespBO.OrderItem item = calculateRespBO.getItems().get(i);
            item.setPayPrice(item.getPayPrice() + adjustmentShares.get(i));
            if (item.getPayPrice() <= 0) {
                throw exception(ORDER_MANUAL_PRICE_INVALID);
            }
        }
        calculateRespBO.setCouponId(null);
        calculateRespBO.setCoupons(Collections.emptyList());
        calculateRespBO.setPromotions(Collections.emptyList());
        calculateRespBO.setTotalPoint(0);
        calculateRespBO.setUsePoint(0);
        calculateRespBO.setGivePoint(0);
        calculateRespBO.setFreeDelivery(true);
        calculateRespBO.setGiveCouponTemplateCounts(new LinkedHashMap<>());
        calculateRespBO.setPrice(new TradePriceCalculateRespBO.Price()
                .setTotalPrice(totalPrice).setDiscountPrice(0).setDeliveryPrice(0)
                .setCouponPrice(0).setPointPrice(0).setVipPrice(0).setPayPrice(manualOrderPrice));
    }

    private List<Integer> divideAdjustment(List<TradePriceCalculateRespBO.OrderItem> items, Integer adjustment) {
        Integer total = getSumValue(items, item -> item.getPrice() * item.getCount(), Integer::sum);
        List<Integer> prices = new ArrayList<>(items.size());
        int remainPrice = adjustment;
        for (int i = 0; i < items.size(); i++) {
            int partPrice;
            if (i < items.size() - 1) {
                partPrice = total == 0 ? 0 : (int) (adjustment * (1.0D * items.get(i).getPrice() * items.get(i).getCount() / total));
                remainPrice -= partPrice;
            } else {
                partPrice = remainPrice;
            }
            prices.add(partPrice);
        }
        return prices;
    }

    private MemberAddressRespDTO buildManualAddressIfNecessary(TradePriceCalculateRespBO calculateRespBO,
                                                               TradeOrderManualCreateReqVO reqVO) {
        boolean hasExpress = calculateRespBO.getItems().stream()
                .anyMatch(item -> Objects.equals(item.getResolvedDeliveryType(), DeliveryTypeEnum.EXPRESS.getType()));
        if (!hasExpress) {
            return null;
        }
        if (StrUtil.isBlank(reqVO.getReceiverName()) || StrUtil.isBlank(reqVO.getReceiverMobile())
                || reqVO.getReceiverAreaId() == null || StrUtil.isBlank(reqVO.getReceiverDetailAddress())) {
            throw exception(ORDER_MANUAL_DELIVERY_ADDRESS_REQUIRED);
        }
        return new MemberAddressRespDTO()
                .setId(0L)
                .setName(reqVO.getReceiverName())
                .setMobile(reqVO.getReceiverMobile())
                .setAreaId(reqVO.getReceiverAreaId())
                .setDetailAddress(reqVO.getReceiverDetailAddress());
    }

    private void fillManualPickUpGroupFacts(TradeOrderDeliveryBuildResult deliveryBuildResult,
                                            TradeOrderManualCreateReqVO reqVO) {
        TradeOrderDeliveryGroupDraft pickUpGroup = deliveryBuildResult.findByDeliveryType(DeliveryTypeEnum.PICK_UP.getType());
        if (pickUpGroup == null) {
            return;
        }
        if (reqVO.getPickUpStoreId() == null || StrUtil.isBlank(reqVO.getReceiverName())
                || StrUtil.isBlank(reqVO.getReceiverMobile())) {
            throw exception(ORDER_MANUAL_PICK_UP_REQUIRED);
        }
        pickUpGroup.setPickUpStoreId(reqVO.getPickUpStoreId());
        pickUpGroup.setReceiverName(reqVO.getReceiverName());
        pickUpGroup.setReceiverMobile(reqVO.getReceiverMobile());
        pickUpGroup.setPickUpVerifyCode(RandomUtil.randomNumbers(8));
    }

    private TradeOrderDO buildManualTradeOrder(TradeOrderManualCreateReqVO reqVO, TradeOrderSourceEnum source,
                                               TradePriceCalculateRespBO calculateRespBO,
                                               TradeOrderDeliveryBuildResult deliveryBuildResult) {
        TradeOrderDO order = new TradeOrderDO()
                .setNo(tradeNoRedisDAO.generate(TradeNoRedisDAO.TRADE_ORDER_NO_PREFIX))
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setTerminal(TerminalEnum.ADMIN.getTerminal())
                .setOrderSource(source.getSource())
                .setUserIp(StrUtil.blankToDefault(getClientIP(), NetUtil.getLocalhostStr()))
                .setRemark(reqVO.getRemark())
                .setStatus(TradeOrderStatusEnum.UNPAID.getStatus())
                .setProductCount(getSumValue(calculateRespBO.getItems(), TradePriceCalculateRespBO.OrderItem::getCount,
                        Integer::sum))
                .setCommentStatus(false)
                .setPayStatus(false)
                .setTotalPrice(calculateRespBO.getPrice().getTotalPrice())
                .setDiscountPrice(0)
                .setDeliveryPrice(0)
                .setAdjustPrice(calculateRespBO.getPrice().getPayPrice() - calculateRespBO.getPrice().getTotalPrice())
                .setPayPrice(calculateRespBO.getPrice().getPayPrice())
                .setDeliveryType(deliveryBuildResult.summaryDeliveryType())
                .setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus())
                .setRefundPrice(0)
                .setCouponPrice(0)
                .setUsePoint(0)
                .setPointPrice(0)
                .setGivePoint(0)
                .setRefundPoint(0)
                .setVipPrice(0)
                .setGiveCouponTemplateCounts(new LinkedHashMap<>());
        fillManualOrderReceiver(order, deliveryBuildResult);
        return order;
    }

    private void fillManualOrderReceiver(TradeOrderDO order, TradeOrderDeliveryBuildResult deliveryBuildResult) {
        TradeOrderDeliveryGroupDraft expressGroup = deliveryBuildResult.findByDeliveryType(DeliveryTypeEnum.EXPRESS.getType());
        if (expressGroup != null && expressGroup.getAddress() != null) {
            MemberAddressRespDTO address = expressGroup.getAddress();
            order.setReceiverName(address.getName()).setReceiverMobile(address.getMobile())
                    .setReceiverAreaId(address.getAreaId()).setReceiverDetailAddress(address.getDetailAddress());
            return;
        }
        TradeOrderDeliveryGroupDraft schoolGroup = deliveryBuildResult.findByDeliveryType(DeliveryTypeEnum.SCHOOL.getType());
        if (schoolGroup != null) {
            fillSchoolOrderReceiver(order, schoolGroup);
            return;
        }
        TradeOrderDeliveryGroupDraft pickUpGroup = deliveryBuildResult.findByDeliveryType(DeliveryTypeEnum.PICK_UP.getType());
        if (pickUpGroup != null) {
            order.setReceiverName(pickUpGroup.getReceiverName()).setReceiverMobile(pickUpGroup.getReceiverMobile())
                    .setPickUpStoreId(pickUpGroup.getPickUpStoreId())
                    .setPickUpVerifyCode(pickUpGroup.getPickUpVerifyCode());
            return;
        }
    }

    private void fillSchoolOrderReceiver(TradeOrderDO order, TradeOrderDeliveryGroupDraft schoolGroup) {
        order.setReceiverName(StrUtil.blankToDefault(schoolGroup.getSchoolNameSnapshot(),
                        StrUtil.blankToDefault(schoolGroup.getContactName(), schoolGroup.getStudentNameSnapshot())))
                .setReceiverMobile(StrUtil.nullToDefault(schoolGroup.getContactMobile(), ""))
                .setReceiverDetailAddress(schoolGroup.getSchoolAddressSnapshot());
    }

    private List<TradeOrderItemDO> buildManualTradeOrderItems(TradeOrderDO order,
                                                              TradePriceCalculateRespBO calculateRespBO) {
        List<TradeOrderItemDO> items = TradeOrderConvert.INSTANCE.convertList(order, calculateRespBO);
        for (TradeOrderItemDO item : items) {
            item.setAdjustPrice(item.getPayPrice() - item.getPrice() * item.getCount());
            item.setDiscountPrice(0);
            item.setDeliveryPrice(0);
            item.setCouponPrice(0);
            item.setPointPrice(0);
            item.setUsePoint(0);
            item.setGivePoint(0);
            item.setVipPrice(0);
        }
        return items;
    }

    private TradeOrderDO validateManualOrder(Long id, cn.iocoder.yudao.framework.common.exception.ErrorCode errorCode) {
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (!TradeOrderSourceEnum.isAdmin(order.getOrderSource())) {
            throw exception(errorCode);
        }
        return order;
    }

    private String buildPaySubject(List<TradeOrderItemDO> orderItems) {
        String subject = CollUtil.isEmpty(orderItems) ? "线下订单" : orderItems.get(0).getSpuName();
        return StrUtils.maxLength(subject, 32);
    }

    private Map<String, List<TradeOrderManualImportExcelVO>> groupImportRows(List<TradeOrderManualImportExcelVO> rows) {
        if (CollUtil.isEmpty(rows)) {
            return Collections.emptyMap();
        }
        Map<String, List<TradeOrderManualImportExcelVO>> groupMap = new LinkedHashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            TradeOrderManualImportExcelVO row = rows.get(i);
            String key = StrUtil.blankToDefault(row.getImportOrderNo(), "ROW-" + (i + 1));
            groupMap.computeIfAbsent(key, ignore -> new ArrayList<>()).add(row);
        }
        return groupMap;
    }

    private TradeOrderManualCreateReqVO convertImportGroup(List<TradeOrderManualImportExcelVO> rows) {
        TradeOrderManualCreateReqVO reqVO = new TradeOrderManualCreateReqVO()
                .setManualOrderPrice(resolveImportGroupValue(rows, "整单金额", TradeOrderManualImportExcelVO::getManualOrderPrice))
                .setDeliveryType(resolveImportGroupValue(rows, "默认配送方式", TradeOrderManualImportExcelVO::getDeliveryType))
                .setReceiverName(resolveImportGroupValue(rows, "收件人", TradeOrderManualImportExcelVO::getReceiverName))
                .setReceiverMobile(resolveImportGroupValue(rows, "收件手机号", TradeOrderManualImportExcelVO::getReceiverMobile))
                .setReceiverAreaId(resolveImportGroupValue(rows, "收件地区编号", TradeOrderManualImportExcelVO::getReceiverAreaId))
                .setReceiverDetailAddress(resolveImportGroupValue(rows, "收件详细地址", TradeOrderManualImportExcelVO::getReceiverDetailAddress))
                .setPickUpStoreId(resolveImportGroupValue(rows, "自提门店编号", TradeOrderManualImportExcelVO::getPickUpStoreId))
                .setRemark(resolveImportGroupValue(rows, "商家备注", TradeOrderManualImportExcelVO::getRemark))
                .setItems(new ArrayList<>(rows.size()));
        for (TradeOrderManualImportExcelVO row : rows) {
            reqVO.getItems().add(new TradeOrderManualCreateReqVO.Item()
                    .setSkuId(row.getSkuId())
                    .setCount(row.getCount())
                    .setDeliveryType(row.getDeliveryType())
                    .setStudentId(row.getStudentId())
                    .setOfferSkuId(row.getOfferSkuId())
                    .setManualUnitPrice(row.getManualUnitPrice()));
        }
        return reqVO;
    }

    @SuppressWarnings("unchecked")
    private <T> T resolveImportGroupValue(List<TradeOrderManualImportExcelVO> rows, String fieldName,
                                          Function<TradeOrderManualImportExcelVO, T> getter) {
        T resolved = null;
        for (TradeOrderManualImportExcelVO row : rows) {
            T value = getter.apply(row);
            if (value instanceof String str) {
                value = (T) StrUtil.trimToNull(str);
            }
            if (value == null) {
                continue;
            }
            if (resolved == null) {
                resolved = value;
                continue;
            }
            if (!Objects.equals(resolved, value)) {
                throw exception(ORDER_MANUAL_IMPORT_GROUP_FIELD_CONFLICT, fieldName);
            }
        }
        return resolved;
    }

}
