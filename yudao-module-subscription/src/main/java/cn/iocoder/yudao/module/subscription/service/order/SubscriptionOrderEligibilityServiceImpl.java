package cn.iocoder.yudao.module.subscription.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.PublicationIssueModeEnum;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentSubscriptionContextRespDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionOfferSkuIssueDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import cn.iocoder.yudao.module.subscription.service.offer.SubscriptionOfferService;
import cn.iocoder.yudao.module.subscription.service.offersku.SubscriptionOfferSkuService;
import cn.iocoder.yudao.module.subscription.service.offerskuissue.SubscriptionOfferSkuIssueService;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityService;
import cn.iocoder.yudao.module.trade.api.order.TradeSubscriptionOrderApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.*;

@Service
@Validated
public class SubscriptionOrderEligibilityServiceImpl implements SubscriptionOrderEligibilityService {

    @Resource
    private SubscriptionVisibilityService visibilityService;
    @Resource
    private SubscriptionOfferService offerService;
    @Resource
    private SubscriptionOfferSkuService offerSkuService;
    @Resource
    private SubscriptionOfferSkuIssueService offerSkuIssueService;
    @Resource
    private TradeSubscriptionOrderApi tradeSubscriptionOrderApi;

    @Override
    public SubscriptionOrderEligibilityRespDTO validateOrder(SubscriptionOrderEligibilityReqDTO reqDTO) {
        if (reqDTO.getCount() == null || reqDTO.getCount() <= 0) {
            throw exception(ORDER_ITEM_COUNT_INVALID);
        }
        SubscriptionWindowOfferSkuDO offerSku = offerSkuService.validateOfferSkuExists(reqDTO.getOfferSkuId());
        if (!Objects.equals(offerSku.getProductSkuId(), reqDTO.getSkuId())) {
            throw exception(ORDER_OFFER_SKU_PRODUCT_SKU_MISMATCH);
        }
        SubscriptionWindowOfferDO offer;
        if (Boolean.TRUE.equals(reqDTO.getLockAnchor())) {
            offer = offerService.validateOfferExistsForUpdate(offerSku.getOfferId());
            offerSku = offerSkuService.validateOfferSkuExistsForUpdate(reqDTO.getOfferSkuId());
            if (!Objects.equals(offerSku.getOfferId(), offer.getId())) {
                throw exception(ORDER_OFFER_SKU_NOT_AVAILABLE);
            }
            if (!Objects.equals(offerSku.getProductSkuId(), reqDTO.getSkuId())) {
                throw exception(ORDER_OFFER_SKU_PRODUCT_SKU_MISMATCH);
            }
        } else {
            offer = offerService.validateOfferExists(offerSku.getOfferId());
        }
        validateOrderAnchorAvailable(offer, offerSku);
        SubscriptionVisibilityResultBO visibility = Boolean.TRUE.equals(reqDTO.getAdmin())
                ? visibilityService.calculateForAdmin(reqDTO.getStudentId(), offer.getWindowId())
                : visibilityService.calculate(reqDTO.getUserId(), reqDTO.getStudentId(), offer.getWindowId());
        SubscriptionVisibilityResultBO.VisibleOfferSku visibleSku = findVisibleSku(visibility, offer.getId(),
                offerSku.getId(), reqDTO.getSkuId());
        if (visibleSku == null) {
            throw exception(ORDER_OFFER_SKU_NOT_AVAILABLE);
        }

        Integer maxQuantity = offerSku.getMaxQuantityPerStudent() == null ? 1 : offerSku.getMaxQuantityPerStudent();
        Integer orderedQuantity = tradeSubscriptionOrderApi.getEffectiveSubscriptionOrderItemQuantity(
                reqDTO.getStudentId(), offerSku.getId());
        if (orderedQuantity + reqDTO.getCount() > maxQuantity) {
            throw exception(ORDER_MAX_QUANTITY_EXCEEDED);
        }
        return buildResp(visibility, offer, offerSku, visibleSku, maxQuantity, orderedQuantity);
    }

    @Override
    public List<SubscriptionOrderEligibilityRespDTO> validateOrderList(List<SubscriptionOrderEligibilityReqDTO> reqList) {
        if (CollUtil.isEmpty(reqList)) {
            return Collections.emptyList();
        }
        List<SubscriptionOrderEligibilityRespDTO> results = new ArrayList<>(Collections.nCopies(reqList.size(), null));
        for (Integer index : buildOrderValidationIndexes(reqList)) {
            results.set(index, validateOrder(reqList.get(index)));
        }
        return results;
    }

    private List<Integer> buildOrderValidationIndexes(List<SubscriptionOrderEligibilityReqDTO> reqList) {
        List<Integer> indexes = new ArrayList<>(reqList.size());
        boolean lockAnchor = false;
        for (int i = 0; i < reqList.size(); i++) {
            indexes.add(i);
            SubscriptionOrderEligibilityReqDTO reqDTO = reqList.get(i);
            lockAnchor = lockAnchor || (reqDTO != null && Boolean.TRUE.equals(reqDTO.getLockAnchor()));
        }
        if (!lockAnchor) {
            return indexes;
        }

        Map<Long, SubscriptionWindowOfferSkuDO> offerSkuMap = new HashMap<>();
        for (SubscriptionOrderEligibilityReqDTO reqDTO : reqList) {
            if (reqDTO == null || !Boolean.TRUE.equals(reqDTO.getLockAnchor()) || reqDTO.getOfferSkuId() == null) {
                continue;
            }
            offerSkuMap.computeIfAbsent(reqDTO.getOfferSkuId(), offerSkuService::validateOfferSkuExists);
        }
        indexes.sort(Comparator
                .comparing((Integer index) -> getOrderLockOfferId(reqList.get(index), offerSkuMap),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(index -> getOrderLockOfferSkuId(reqList.get(index)),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(Integer::intValue));
        return indexes;
    }

    private Long getOrderLockOfferId(SubscriptionOrderEligibilityReqDTO reqDTO,
                                     Map<Long, SubscriptionWindowOfferSkuDO> offerSkuMap) {
        if (reqDTO == null || !Boolean.TRUE.equals(reqDTO.getLockAnchor()) || reqDTO.getOfferSkuId() == null) {
            return null;
        }
        SubscriptionWindowOfferSkuDO offerSku = offerSkuMap.get(reqDTO.getOfferSkuId());
        return offerSku == null ? null : offerSku.getOfferId();
    }

    private Long getOrderLockOfferSkuId(SubscriptionOrderEligibilityReqDTO reqDTO) {
        return reqDTO == null ? null : reqDTO.getOfferSkuId();
    }

    private void validateOrderAnchorAvailable(SubscriptionWindowOfferDO offer,
                                              SubscriptionWindowOfferSkuDO offerSku) {
        if (!CommonStatusEnum.isEnable(offer.getStatus()) || !CommonStatusEnum.isEnable(offerSku.getStatus())) {
            throw exception(ORDER_OFFER_SKU_NOT_AVAILABLE);
        }
    }

    private SubscriptionVisibilityResultBO.VisibleOfferSku findVisibleSku(SubscriptionVisibilityResultBO visibility,
                                                                          Long offerId, Long offerSkuId, Long skuId) {
        if (visibility == null || visibility.getVisibleOffers() == null) {
            return null;
        }
        for (SubscriptionVisibilityResultBO.VisibleOffer offer : visibility.getVisibleOffers()) {
            if (offer.getOffer() == null || !Objects.equals(offer.getOffer().getId(), offerId)
                    || offer.getSkus() == null) {
                continue;
            }
            for (SubscriptionVisibilityResultBO.VisibleOfferSku sku : offer.getSkus()) {
                if (sku.getOfferSku() != null && Objects.equals(sku.getOfferSku().getId(), offerSkuId)
                        && sku.getProductSku() != null && Objects.equals(sku.getProductSku().getId(), skuId)) {
                    return sku;
                }
            }
        }
        return null;
    }

    private SubscriptionOrderEligibilityRespDTO buildResp(SubscriptionVisibilityResultBO visibility,
                                                          SubscriptionWindowOfferDO offer,
                                                          SubscriptionWindowOfferSkuDO offerSku,
                                                          SubscriptionVisibilityResultBO.VisibleOfferSku visibleSku,
                                                          Integer maxQuantity,
                                                          Integer orderedQuantity) {
        SubscriptionOrderEligibilityRespDTO respDTO = new SubscriptionOrderEligibilityRespDTO();
        EduStudentSubscriptionContextRespDTO student = visibility.getStudent();
        if (student != null) {
            respDTO.setStudentId(student.getStudentId());
            respDTO.setParentUserId(student.getParentUserId());
            respDTO.setStudentNameSnapshot(student.getStudentName());
            respDTO.setSchoolId(student.getSchoolId());
            respDTO.setSchoolNameSnapshot(student.getSchoolName());
            respDTO.setSchoolAddressSnapshot(student.getSchoolAddress());
            respDTO.setClassId(student.getClassId());
            respDTO.setClassNameSnapshot(student.getClassName());
            respDTO.setGradeCatalogId(student.getGradeCatalogId());
            respDTO.setGradeNameSnapshot(student.getGradeName());
            respDTO.setWarehouseId(student.getWarehouseId());
            respDTO.setWarehouseNameSnapshot(student.getWarehouseName());
            respDTO.setWarehouseAddressSnapshot(student.getWarehouseAddress());
            respDTO.setWarehousePrincipalSnapshot(student.getWarehousePrincipal());
            respDTO.setContactName(student.getContactName());
            respDTO.setContactMobile(student.getContactMobile());
        }
        SubscriptionWindowDO window = visibility.getWindow();
        if (window != null) {
            respDTO.setWindowId(window.getId());
            respDTO.setWindowNameSnapshot(window.getName());
            respDTO.setTargetYearStart(window.getTargetYearStart());
            respDTO.setTargetYearEnd(window.getTargetYearEnd());
        }
        respDTO.setOfferId(offer.getId());
        respDTO.setOfferSkuId(offerSku.getId());
        respDTO.setVisibilityReason(visibleSku.getReason());
        respDTO.setMatchedRuleId(visibleSku.getMatchedRule() == null ? null : visibleSku.getMatchedRule().getId());
        respDTO.setGradeApplicabilityOverride(Boolean.TRUE.equals(visibleSku.getGradeApplicabilityOverride()));
        respDTO.setMaxQuantityPerStudent(maxQuantity);
        respDTO.setOrderedQuantity(orderedQuantity);
        fillIssueSnapshot(respDTO, visibility, offer, offerSku);
        return respDTO;
    }

    private void fillIssueSnapshot(SubscriptionOrderEligibilityRespDTO respDTO,
                                   SubscriptionVisibilityResultBO visibility,
                                   SubscriptionWindowOfferDO offer,
                                   SubscriptionWindowOfferSkuDO offerSku) {
        ProductPublicationRespDTO.PublicationSpuExtDTO spuExt = resolvePublicationExt(visibility, offer.getId());
        String issueMode = PublicationIssueModeEnum.normalize(spuExt == null ? null : spuExt.getIssueMode());
        respDTO.setIssueMode(issueMode);
        if (PublicationIssueModeEnum.isSingle(issueMode)) {
            respDTO.setIssueCount(1);
            respDTO.setIssues(List.of(new SubscriptionOrderEligibilityRespDTO.Issue()
                    .setIssueNo(1)
                    .setIssueName("单次配送")));
            return;
        }
        List<SubscriptionOfferSkuIssueDO> issues = offerSkuIssueService.getEnabledIssueList(offerSku.getId());
        if (CollUtil.isEmpty(issues)) {
            throw exception(ORDER_PERIODICAL_ISSUE_REQUIRED);
        }
        respDTO.setIssueCount(issues.size());
        respDTO.setIssues(convertList(issues, issue -> new SubscriptionOrderEligibilityRespDTO.Issue()
                .setIssueId(issue.getId())
                .setIssueNo(issue.getIssueNo())
                .setIssueName(issue.getIssueName())
                .setPlannedPublishDate(issue.getPlannedPublishDate())
                .setPlannedDeliveryDate(issue.getPlannedDeliveryDate())));
    }

    private ProductPublicationRespDTO.PublicationSpuExtDTO resolvePublicationExt(SubscriptionVisibilityResultBO visibility,
                                                                                 Long offerId) {
        if (visibility == null || visibility.getVisibleOffers() == null) {
            return null;
        }
        return visibility.getVisibleOffers().stream()
                .filter(visibleOffer -> visibleOffer.getOffer() != null
                        && Objects.equals(visibleOffer.getOffer().getId(), offerId))
                .map(SubscriptionVisibilityResultBO.VisibleOffer::getPublication)
                .filter(Objects::nonNull)
                .map(ProductPublicationRespDTO::getPublicationExt)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

}
