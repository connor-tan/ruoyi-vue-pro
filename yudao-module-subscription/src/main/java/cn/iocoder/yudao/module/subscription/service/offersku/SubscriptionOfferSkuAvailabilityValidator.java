package cn.iocoder.yudao.module.subscription.service.offersku;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.api.publication.ProductPublicationApi;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferSkuMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_NOT_EXISTS;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_SKU_EFFECTIVE_REQUIRED;

@Service
@Validated
public class SubscriptionOfferSkuAvailabilityValidator {

    @Resource
    private SubscriptionWindowOfferMapper offerMapper;
    @Resource
    private SubscriptionWindowOfferSkuMapper offerSkuMapper;
    @Resource
    private ProductPublicationApi productPublicationApi;

    public void validateEnabledOfferHasEffectiveSku(Long offerId) {
        SubscriptionWindowOfferDO offer = offerMapper.selectById(offerId);
        if (offer == null) {
            throw exception(OFFER_NOT_EXISTS);
        }
        if (!CommonStatusEnum.isEnable(offer.getStatus())) {
            return;
        }
        ProductPublicationRespDTO publication = productPublicationApi.getPublication(offer.getProductSpuId());
        if (publication == null || CollUtil.isEmpty(publication.getSkus())) {
            throw exception(OFFER_SKU_EFFECTIVE_REQUIRED);
        }
        Map<Long, ProductPublicationRespDTO.PublicationSkuDTO> productSkuMap = convertMap(publication.getSkus(),
                ProductPublicationRespDTO.PublicationSkuDTO::getId);
        List<SubscriptionWindowOfferSkuDO> offerSkus = offerSkuMapper.selectListByOfferId(offerId);
        boolean hasEffectiveSku = offerSkus.stream()
                .filter(offerSku -> CommonStatusEnum.isEnable(offerSku.getStatus()))
                .map(offerSku -> productSkuMap.get(offerSku.getProductSkuId()))
                .filter(Objects::nonNull)
                .anyMatch(productSku -> CommonStatusEnum.isEnable(productSku.getStatus()));
        if (!hasEffectiveSku) {
            throw exception(OFFER_SKU_EFFECTIVE_REQUIRED);
        }
    }
}
