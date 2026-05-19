package cn.iocoder.yudao.module.subscription.controller.admin.publication;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.subscription.controller.SubscriptionVisibilityVOAssembler;
import cn.iocoder.yudao.module.subscription.controller.app.vo.AppSubscriptionPublicationRespVO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityService;
import cn.iocoder.yudao.module.trade.api.order.TradeSubscriptionOrderApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 订刊刊物")
@RestController
@RequestMapping("/subscription/admin/publication")
@Validated
public class SubscriptionAdminPublicationController {

    private static final String PURCHASE_LIMIT_REACHED_DESC = "已达该刊物限购数量";

    @Resource
    private SubscriptionVisibilityService visibilityService;
    @Resource
    private TradeSubscriptionOrderApi tradeSubscriptionOrderApi;

    @GetMapping("/list")
    @Operation(summary = "获得学生后台可代下单刊物列表")
    @Parameter(name = "studentId", required = true)
    @PreAuthorize("@ss.hasPermission('trade:order:create')")
    public CommonResult<AppSubscriptionPublicationRespVO> list(@RequestParam("studentId") Long studentId,
                                                               @RequestParam(value = "productSpuIds", required = false)
                                                               Set<Long> productSpuIds) {
        SubscriptionVisibilityResultBO result = visibilityService.calculateForAdmin(studentId, null);
        List<SubscriptionVisibilityResultBO.VisibleOffer> visibleOffers = result.getVisibleOffers();
        if (productSpuIds != null && !productSpuIds.isEmpty()) {
            visibleOffers = visibleOffers.stream()
                    .filter(offer -> offer.getOffer() != null && productSpuIds.contains(offer.getOffer().getProductSpuId()))
                    .toList();
        }
        AppSubscriptionPublicationRespVO respVO = new AppSubscriptionPublicationRespVO();
        respVO.setWindow(SubscriptionVisibilityVOAssembler.buildAppWindow(result.getWindow()));
        respVO.setStudent(SubscriptionVisibilityVOAssembler.buildAppStudent(result.getStudent()));
        respVO.setBlockedReason(result.getBlockedReason());
        respVO.setBlockedReasonDesc(result.getBlockedReasonDesc());
        List<AppSubscriptionPublicationRespVO.Offer> offers =
                SubscriptionVisibilityVOAssembler.buildAppVisibleOffers(visibleOffers);
        fillPurchaseAvailability(studentId, offers);
        respVO.setOffers(offers);
        return success(respVO);
    }

    private void fillPurchaseAvailability(Long studentId, List<AppSubscriptionPublicationRespVO.Offer> offers) {
        if (offers == null || offers.isEmpty()) {
            return;
        }
        Set<Long> offerSkuIds = offers.stream()
                .filter(Objects::nonNull)
                .flatMap(offer -> getFinalSkus(offer).stream())
                .map(AppSubscriptionPublicationRespVO.OfferSku::getOfferSkuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Integer> orderedQuantityMap =
                tradeSubscriptionOrderApi.getEffectiveSubscriptionOrderItemQuantityMap(studentId, offerSkuIds);
        offers.forEach(offer -> fillOfferPurchaseAvailability(offer, orderedQuantityMap));
    }

    private List<AppSubscriptionPublicationRespVO.OfferSku> getFinalSkus(AppSubscriptionPublicationRespVO.Offer offer) {
        return offer.getFinalSkus() == null ? Collections.emptyList() : offer.getFinalSkus();
    }

    private void fillOfferPurchaseAvailability(AppSubscriptionPublicationRespVO.Offer offer,
                                               Map<Long, Integer> orderedQuantityMap) {
        if (offer == null || offer.getFinalSkus() == null || offer.getFinalSkus().isEmpty()) {
            if (offer != null) {
                offer.setPurchasable(false);
                offer.setPurchaseUnavailableReasonDesc(PURCHASE_LIMIT_REACHED_DESC);
            }
            return;
        }
        boolean purchasable = false;
        for (AppSubscriptionPublicationRespVO.OfferSku sku : offer.getFinalSkus()) {
            if (sku == null) {
                continue;
            }
            fillOfferSkuPurchaseAvailability(sku, orderedQuantityMap);
            purchasable = purchasable || Boolean.TRUE.equals(sku.getPurchasable());
        }
        offer.setPurchasable(purchasable);
        offer.setPurchaseUnavailableReasonDesc(purchasable ? null : PURCHASE_LIMIT_REACHED_DESC);
    }

    private void fillOfferSkuPurchaseAvailability(AppSubscriptionPublicationRespVO.OfferSku sku,
                                                  Map<Long, Integer> orderedQuantityMap) {
        Integer maxQuantity = sku.getMaxQuantityPerStudent() == null ? 1 : sku.getMaxQuantityPerStudent();
        Integer orderedQuantity = orderedQuantityMap == null ? 0 : orderedQuantityMap.getOrDefault(sku.getOfferSkuId(), 0);
        int remainingQuantity = Math.max(0, maxQuantity - Math.max(0, orderedQuantity));
        sku.setMaxQuantityPerStudent(maxQuantity);
        sku.setOrderedQuantity(Math.max(0, orderedQuantity));
        sku.setRemainingQuantity(remainingQuantity);
        sku.setPurchasable(remainingQuantity > 0);
        sku.setPurchaseUnavailableReasonDesc(remainingQuantity > 0 ? null : PURCHASE_LIMIT_REACHED_DESC);
    }

}
