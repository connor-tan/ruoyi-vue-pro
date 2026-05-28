package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.module.edu.api.student.EduStudentApi;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentSubscriptionContextRespDTO;
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
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderAdminOnlineCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderAdminOnlineSettlementReqVO;
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
import cn.iocoder.yudao.module.trade.service.message.TradeMessageService;
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
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_ITEM_DELIVERY_TYPE_ILLEGAL;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_ITEM_DELIVERY_TYPE_REQUIRED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_ADMIN_ONLINE_EXPRESS_ADDRESS_REQUIRED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_ADMIN_ONLINE_ITEM_INVALID;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_ADMIN_ONLINE_ITEM_REQUIRED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_ADMIN_ONLINE_ONLY_PUBLICATION;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_ADMIN_ONLINE_PARENT_REQUIRED;
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
    private EduStudentApi eduStudentApi;
    @Resource
    private SubscriptionOrderEligibilityApi subscriptionOrderEligibilityApi;
    @Resource
    private TradeOrderProperties tradeOrderProperties;
    @Resource
    private TradeOrderDeliveryGroupSupport deliveryGroupSupport;
    @Resource
    private TradeOrderPublicationIssueService publicationIssueService;
    @Resource
    private TradeMessageService tradeMessageService;

    @Override
    public AppTradeOrderSettlementRespVO settlementOrder(Long userId, AppTradeOrderSettlementReqVO settlementReqVO) {
        TradeOrderCheckoutContext context = TradeOrderCheckoutContext.member(userId);
        MemberAddressRespDTO address = getAddress(userId, settlementReqVO.getAddressId());
        context.setExpressAddress(address);
        if (address != null) {
            settlementReqVO.setAddressId(address.getId());
        }

        TradePriceCalculateRespBO calculateRespBO = calculatePrice(context, settlementReqVO);
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

    @Override
    public List<MemberAddressRespDTO> getAdminOnlineAddressList(Long studentId) {
        Long parentUserId = getAdminOnlineParentUserId(studentId);
        return addressApi.getAddressList(parentUserId);
    }

    @Override
    public AppTradeOrderSettlementRespVO settlementAdminOnlineOrder(
            TradeOrderAdminOnlineSettlementReqVO settlementReqVO) {
        TradeOrderCheckoutContext context = buildAdminOnlineContext(settlementReqVO);
        AppTradeOrderSettlementReqVO appReqVO = buildAdminOnlineSettlementReq(settlementReqVO);
        applyAdminOnlineExpressAddress(settlementReqVO, appReqVO, context);

        TradePriceCalculateRespBO calculateRespBO = calculatePrice(context, appReqVO);
        TradeOrderDeliveryBuildResult deliveryBuildResult = deliveryGroupSupport.buildDeliveryBuildResult(
                calculateRespBO, context.getExpressAddress(), false);
        deliveryGroupSupport.applyPreviewDeliveryIdsToOrderItems(calculateRespBO, deliveryBuildResult);

        return TradeOrderConvert.INSTANCE.convert(calculateRespBO, context.getExpressAddress(),
                deliveryGroupSupport.buildSettlementDeliveries(deliveryBuildResult));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_CREATE)
    public TradeOrderDO createAdminOnlineOrder(TradeOrderAdminOnlineCreateReqVO createReqVO) {
        TradeOrderCheckoutContext context = buildAdminOnlineContext(createReqVO);
        AppTradeOrderCreateReqVO appReqVO = buildAdminOnlineCreateReq(createReqVO);
        applyAdminOnlineExpressAddress(createReqVO, appReqVO, context);
        return doCreateOrder(context, appReqVO);
    }

    private TradeOrderCheckoutContext buildAdminOnlineContext(TradeOrderAdminOnlineSettlementReqVO reqVO) {
        validateAdminOnlineItems(reqVO.getItems());
        Long parentUserId = getAdminOnlineParentUserId(reqVO.getStudentId());
        return TradeOrderCheckoutContext.adminOnline(parentUserId);
    }

    private void validateAdminOnlineItems(List<TradeOrderAdminOnlineSettlementReqVO.Item> items) {
        if (CollUtil.isEmpty(items)) {
            throw exception(ORDER_ADMIN_ONLINE_ITEM_REQUIRED);
        }
        for (TradeOrderAdminOnlineSettlementReqVO.Item item : items) {
            if (item == null || item.getOfferSkuId() == null || item.getSkuId() == null
                    || item.getCount() == null || item.getCount() < 1) {
                throw exception(ORDER_ADMIN_ONLINE_ITEM_INVALID);
            }
        }
    }

    private Long getAdminOnlineParentUserId(Long studentId) {
        Map<Long, EduStudentSubscriptionContextRespDTO> contextMap =
                eduStudentApi.getAdminSubscriptionStudentContextMap(Collections.singleton(studentId), null, null, null);
        EduStudentSubscriptionContextRespDTO student = contextMap.get(studentId);
        if (student == null || student.getParentUserId() == null) {
            throw exception(ORDER_ADMIN_ONLINE_PARENT_REQUIRED);
        }
        return student.getParentUserId();
    }

    private AppTradeOrderSettlementReqVO buildAdminOnlineSettlementReq(TradeOrderAdminOnlineSettlementReqVO reqVO) {
        AppTradeOrderSettlementReqVO appReqVO = new AppTradeOrderSettlementReqVO();
        appReqVO.setDeliveryType(reqVO.getDeliveryType());
        appReqVO.setPointStatus(false);
        appReqVO.setItems(convertList(reqVO.getItems(), item -> new AppTradeOrderSettlementReqVO.Item()
                .setSkuId(item.getSkuId())
                .setCount(item.getCount())
                .setDeliveryType(reqVO.getDeliveryType())
                .setStudentId(reqVO.getStudentId())
                .setOfferSkuId(item.getOfferSkuId())));
        return appReqVO;
    }

    private AppTradeOrderCreateReqVO buildAdminOnlineCreateReq(TradeOrderAdminOnlineCreateReqVO reqVO) {
        AppTradeOrderCreateReqVO appReqVO = new AppTradeOrderCreateReqVO();
        AppTradeOrderSettlementReqVO settlementReqVO = buildAdminOnlineSettlementReq(reqVO);
        appReqVO.setDeliveryType(settlementReqVO.getDeliveryType());
        appReqVO.setPointStatus(settlementReqVO.getPointStatus());
        appReqVO.setItems(settlementReqVO.getItems());
        appReqVO.setRemark(reqVO.getRemark());
        return appReqVO;
    }

    private void applyAdminOnlineExpressAddress(TradeOrderAdminOnlineSettlementReqVO reqVO,
                                                AppTradeOrderSettlementReqVO appReqVO,
                                                TradeOrderCheckoutContext context) {
        if (!Objects.equals(reqVO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            return;
        }
        MemberAddressRespDTO address;
        if (reqVO.getAddressId() != null) {
            address = addressApi.getAddress(reqVO.getAddressId(), context.getUserId());
        } else {
            if (StrUtil.isBlank(reqVO.getReceiverName()) || StrUtil.isBlank(reqVO.getReceiverMobile())
                    || reqVO.getReceiverAreaId() == null || StrUtil.isBlank(reqVO.getReceiverDetailAddress())) {
                throw exception(ORDER_ADMIN_ONLINE_EXPRESS_ADDRESS_REQUIRED);
            }
            address = new MemberAddressRespDTO()
                    .setName(reqVO.getReceiverName().trim())
                    .setMobile(reqVO.getReceiverMobile().trim())
                    .setAreaId(reqVO.getReceiverAreaId())
                    .setDetailAddress(reqVO.getReceiverDetailAddress().trim())
                    .setDefaultStatus(false)
                    .setUserId(context.getUserId());
        }
        if (address == null) {
            throw exception(ORDER_ADMIN_ONLINE_EXPRESS_ADDRESS_REQUIRED);
        }
        context.setExpressAddress(address);
        if (address.getId() != null) {
            appReqVO.setAddressId(address.getId());
        }
    }

    private TradePriceCalculateRespBO calculatePrice(TradeOrderCheckoutContext context,
                                                     AppTradeOrderSettlementReqVO settlementReqVO) {
        TradeOrderPreparedCalculateRequest prepared = prepareCalculateRequest(context, settlementReqVO, false);
        return calculatePrice(prepared, settlementReqVO);
    }

    private TradeOrderPreparedCalculateRequest prepareCalculateRequest(TradeOrderCheckoutContext context,
                                                                       AppTradeOrderSettlementReqVO settlementReqVO,
                                                                       boolean lockPublicationAnchor) {
        List<CartDO> cartList = context.isAdminOnline()
                ? Collections.emptyList()
                : cartService.getCartList(context.getUserId(),
                convertSet(settlementReqVO.getItems(), AppTradeOrderSettlementReqVO.Item::getCartId));
        TradePriceCalculateReqBO baseReqBO = TradeOrderConvert.INSTANCE.convert(context.getUserId(), settlementReqVO, cartList);
        if (context.getExpressAddress() != null) {
            baseReqBO.setReceiverAreaId(context.getExpressAddress().getAreaId());
        }
        baseReqBO.getItems().forEach(item -> Assert.isTrue(Boolean.TRUE.equals(item.getSelected()),
                "商品({}) 未设置为选中", item.getSkuId()));

        Map<Long, ProductSkuRespDTO> skuMap = productSkuApi.getSkuMap(
                convertSet(baseReqBO.getItems(), TradePriceCalculateReqBO.Item::getSkuId));
        Map<Long, ProductSpuRespDTO> spuMap = productSpuApi.getSpuMap(
                convertSet(skuMap.values(), ProductSkuRespDTO::getSpuId));
        Map<String, TradeOrderDeliveryGroupDraft> groupMap = new LinkedHashMap<>();
        Map<Long, Integer> studentDeliveryTypeMap = new HashMap<>();
        Map<TradeOrderSubscriptionPurchaseKey, Integer> publicationPurchaseCountMap = new HashMap<>();
        List<PublicationOrderItemCandidate> publicationCandidates = new ArrayList<>();
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

            if (context.isAdminOnline() && !BizSceneEnum.isPublication(bizScene)) {
                throw exception(ORDER_ADMIN_ONLINE_ONLY_PUBLICATION);
            }
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
                if (!Objects.equals(deliveryType, DeliveryTypeEnum.EXPRESS.getType())
                        && !Objects.equals(deliveryType, DeliveryTypeEnum.SCHOOL.getType())) {
                    throw exception(ORDER_ITEM_DELIVERY_TYPE_ILLEGAL);
                }
                Integer existedDeliveryType = studentDeliveryTypeMap.putIfAbsent(studentId, deliveryType);
                if (existedDeliveryType != null && !Objects.equals(existedDeliveryType, deliveryType)) {
                    throw exception(ORDER_PUBLICATION_MULTI_DELIVERY_FOR_STUDENT);
                }
                publicationCandidates.add(new PublicationOrderItemCandidate(i, item, studentId, offerSkuId,
                        accumulatedCount, deliveryType));
                continue;
            }

            if (item.getSubscriptionStudentId() != null || resolveSubscriptionOfferSkuId(item) != null) {
                throw exception(ORDER_NORMAL_STUDENT_NOT_ALLOWED);
            }
            if (!Objects.equals(deliveryType, DeliveryTypeEnum.EXPRESS.getType())
                    && !Objects.equals(deliveryType, DeliveryTypeEnum.PICK_UP.getType())) {
                throw exception(ORDER_ITEM_DELIVERY_TYPE_ILLEGAL);
            }
        }
        fillDeliveryGroupsInItemOrder(context, baseReqBO.getItems(), publicationCandidates, lockPublicationAnchor,
                groupMap);
        return new TradeOrderPreparedCalculateRequest(baseReqBO, new ArrayList<>(groupMap.values()), publicationPresent);
    }

    private void fillDeliveryGroupsInItemOrder(TradeOrderCheckoutContext context,
                                               List<TradePriceCalculateReqBO.Item> items,
                                               List<PublicationOrderItemCandidate> publicationCandidates,
                                               boolean lockPublicationAnchor,
                                               Map<String, TradeOrderDeliveryGroupDraft> groupMap) {
        Map<Integer, PublicationOrderItemCandidate> candidateMap = new HashMap<>();
        List<SubscriptionOrderEligibilityRespDTO> eligibilities = CollUtil.isEmpty(publicationCandidates)
                ? Collections.emptyList()
                : validatePublicationCandidates(context, publicationCandidates, lockPublicationAnchor);
        for (int i = 0; i < publicationCandidates.size(); i++) {
            PublicationOrderItemCandidate candidate = publicationCandidates.get(i);
            candidate.eligibility = eligibilities.get(i);
            candidateMap.put(candidate.itemIndex, candidate);
        }

        for (int i = 0; i < items.size(); i++) {
            PublicationOrderItemCandidate candidate = candidateMap.get(i);
            if (candidate == null) {
                TradePriceCalculateReqBO.Item item = items.get(i);
                Integer deliveryType = item.getDeliveryType();
                String groupKey = deliveryGroupSupport.buildNormalGroupKey(deliveryType);
                TradeOrderDeliveryGroupDraft group = groupMap.computeIfAbsent(groupKey,
                        ignore -> TradeOrderDeliveryGroupDraft.forNormal(deliveryType));
                group.getItemIndexes().add(i);
                continue;
            }
            SubscriptionOrderEligibilityRespDTO eligibility = candidate.eligibility;
            if (context.isAdminOnline() && eligibility.getParentUserId() == null) {
                throw exception(ORDER_ADMIN_ONLINE_PARENT_REQUIRED);
            }
            if (context.isAdminOnline() && !Objects.equals(context.getUserId(), eligibility.getParentUserId())) {
                throw exception(ORDER_ADMIN_ONLINE_PARENT_REQUIRED);
            }
            fillPublicationItemFacts(candidate.item, eligibility);
            if (Objects.equals(candidate.deliveryType, DeliveryTypeEnum.SCHOOL.getType())
                    && eligibility.getWarehouseId() == null) {
                throw exception(ORDER_SCHOOL_WAREHOUSE_NOT_CONFIGURED);
            }
            String groupKey = deliveryGroupSupport.buildPublicationGroupKey(eligibility.getStudentId(),
                    candidate.deliveryType);
            TradeOrderDeliveryGroupDraft group = groupMap.computeIfAbsent(groupKey,
                    ignore -> TradeOrderDeliveryGroupDraft.forPublication(eligibility, candidate.deliveryType));
            group.getItemIndexes().add(candidate.itemIndex);
        }
    }

    private List<SubscriptionOrderEligibilityRespDTO> validatePublicationCandidates(
            TradeOrderCheckoutContext context, List<PublicationOrderItemCandidate> publicationCandidates,
            boolean lockPublicationAnchor) {
        List<SubscriptionOrderEligibilityReqDTO> reqList = convertList(publicationCandidates, candidate ->
                new SubscriptionOrderEligibilityReqDTO()
                        .setUserId(context.isAdminOnline() ? null : context.getUserId())
                        .setAdmin(context.isAdminOnline())
                        .setStudentId(candidate.studentId)
                        .setOfferSkuId(candidate.offerSkuId)
                        .setSkuId(candidate.item.getSkuId())
                        .setCount(candidate.accumulatedCount)
                        .setLockAnchor(lockPublicationAnchor));
        if (lockPublicationAnchor && reqList.size() > 1) {
            return subscriptionOrderEligibilityApi.validateOrderList(reqList);
        }
        return convertList(reqList, subscriptionOrderEligibilityApi::validateOrder);
    }

    private static final class PublicationOrderItemCandidate {

        private final int itemIndex;
        private final TradePriceCalculateReqBO.Item item;
        private final Long studentId;
        private final Long offerSkuId;
        private final Integer accumulatedCount;
        private final Integer deliveryType;
        private SubscriptionOrderEligibilityRespDTO eligibility;

        private PublicationOrderItemCandidate(int itemIndex, TradePriceCalculateReqBO.Item item, Long studentId,
                                              Long offerSkuId, Integer accumulatedCount, Integer deliveryType) {
            this.itemIndex = itemIndex;
            this.item = item;
            this.studentId = studentId;
            this.offerSkuId = offerSkuId;
            this.accumulatedCount = accumulatedCount;
            this.deliveryType = deliveryType;
        }

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
            groupReqBO.setReceiverAreaId(baseReqBO.getReceiverAreaId());
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
        TradeOrderCheckoutContext context = TradeOrderCheckoutContext.member(userId);
        MemberAddressRespDTO address = getAddress(userId, createReqVO.getAddressId());
        context.setExpressAddress(address);
        if (address != null) {
            createReqVO.setAddressId(address.getId());
        }
        TradeOrderDO order = doCreateOrder(context, createReqVO);
        tradeMessageService.sendMessageWhenOrderCreated(order);
        return order;
    }

    private TradeOrderDO doCreateOrder(TradeOrderCheckoutContext context, AppTradeOrderCreateReqVO createReqVO) {
        TradeOrderPreparedCalculateRequest prepared = prepareCalculateRequest(context, createReqVO, true);
        TradePriceCalculateRespBO calculateRespBO = calculatePrice(prepared, createReqVO);
        MemberAddressRespDTO address = context.getExpressAddress();
        TradeOrderDeliveryBuildResult deliveryBuildResult = deliveryGroupSupport.buildDeliveryBuildResult(
                calculateRespBO, address, true);
        fillPickUpGroupFacts(deliveryBuildResult, createReqVO, true);
        TradeOrderDO order = buildTradeOrder(context, createReqVO, calculateRespBO, deliveryBuildResult);
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

    private TradeOrderDO buildTradeOrder(TradeOrderCheckoutContext context, AppTradeOrderCreateReqVO createReqVO,
                                         TradePriceCalculateRespBO calculateRespBO,
                                         TradeOrderDeliveryBuildResult deliveryBuildResult) {
        TradeOrderDO order = TradeOrderConvert.INSTANCE.convert(context.getUserId(), createReqVO, calculateRespBO);
        order.setType(calculateRespBO.getType());
        order.setNo(tradeNoRedisDAO.generate(TradeNoRedisDAO.TRADE_ORDER_NO_PREFIX));
        order.setStatus(TradeOrderStatusEnum.UNPAID.getStatus());
        order.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus());
        order.setProductCount(getSumValue(calculateRespBO.getItems(), TradePriceCalculateRespBO.OrderItem::getCount, Integer::sum));
        order.setUserIp(getClientIP()).setTerminal(context.getTerminal()).setOrderSource(context.getOrderSource().getSource());
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

    private static final class TradeOrderCheckoutContext {

        private final Long userId;
        private final boolean adminOnline;
        private final TradeOrderSourceEnum orderSource;
        private final Integer terminal;
        private MemberAddressRespDTO expressAddress;

        private TradeOrderCheckoutContext(Long userId, boolean adminOnline,
                                          TradeOrderSourceEnum orderSource, Integer terminal) {
            this.userId = userId;
            this.adminOnline = adminOnline;
            this.orderSource = orderSource;
            this.terminal = terminal;
        }

        static TradeOrderCheckoutContext member(Long userId) {
            return new TradeOrderCheckoutContext(userId, false, TradeOrderSourceEnum.APP,
                    cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getTerminal());
        }

        static TradeOrderCheckoutContext adminOnline(Long parentUserId) {
            return new TradeOrderCheckoutContext(parentUserId, true, TradeOrderSourceEnum.ADMIN_ONLINE,
                    TerminalEnum.ADMIN.getTerminal());
        }

        Long getUserId() {
            return userId;
        }

        boolean isAdminOnline() {
            return adminOnline;
        }

        TradeOrderSourceEnum getOrderSource() {
            return orderSource;
        }

        Integer getTerminal() {
            return terminal;
        }

        MemberAddressRespDTO getExpressAddress() {
            return expressAddress;
        }

        void setExpressAddress(MemberAddressRespDTO expressAddress) {
            this.expressAddress = expressAddress;
        }

    }

}
