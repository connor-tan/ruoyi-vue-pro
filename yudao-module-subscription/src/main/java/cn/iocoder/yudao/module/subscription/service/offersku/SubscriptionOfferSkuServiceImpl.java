package cn.iocoder.yudao.module.subscription.service.offersku;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.publication.ProductPublicationApi;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo.SubscriptionOfferSkuBatchUpdateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo.SubscriptionOfferSkuRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo.SubscriptionOfferSkuSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionOfferSkuIssueMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferSkuMapper;
import cn.iocoder.yudao.module.subscription.service.offer.SubscriptionOfferService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.*;

@Service
@Validated
public class SubscriptionOfferSkuServiceImpl implements SubscriptionOfferSkuService {

    @Resource
    private SubscriptionWindowOfferSkuMapper offerSkuMapper;
    @Resource
    private SubscriptionOfferSkuIssueMapper offerSkuIssueMapper;
    @Resource
    private SubscriptionOfferService offerService;
    @Resource
    private ProductPublicationApi productPublicationApi;
    @Resource
    private SubscriptionOfferSkuAvailabilityValidator offerSkuAvailabilityValidator;

    @Override
    public List<SubscriptionOfferSkuRespVO> getOfferSkuList(Long offerId) {
        SubscriptionWindowOfferDO offer = offerService.validateOfferExists(offerId);
        ProductPublicationRespDTO publication = productPublicationApi.getPublication(offer.getProductSpuId());
        List<SubscriptionWindowOfferSkuDO> offerSkus = offerSkuMapper.selectListByOfferId(offerId);
        if (CollUtil.isEmpty(offerSkus)) {
            return Collections.emptyList();
        }
        return offerSkus.stream()
                .map(offerSku -> buildOfferSkuResp(offerSku, publication))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int syncMatchedOfferSkus(Long offerId) {
        SubscriptionWindowOfferDO offer = offerService.validateOfferExists(offerId);
        ProductPublicationRespDTO publication = productPublicationApi.getPublication(offer.getProductSpuId());
        if (publication == null || CollUtil.isEmpty(publication.getSkus())) {
            offerSkuAvailabilityValidator.validateEnabledOfferHasEffectiveSku(offerId);
            return 0;
        }
        List<SubscriptionWindowOfferSkuDO> existedOfferSkus = offerSkuMapper.selectListByOfferId(offerId);
        Set<Long> existedProductSkuIds = convertSet(existedOfferSkus, SubscriptionWindowOfferSkuDO::getProductSkuId);
        AtomicInteger nextSort = new AtomicInteger(existedOfferSkus.stream()
                .map(SubscriptionWindowOfferSkuDO::getSort)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0));
        List<SubscriptionWindowOfferSkuDO> insertList = publication.getSkus().stream()
                .filter(sku -> !existedProductSkuIds.contains(sku.getId()))
                .filter(sku -> CommonStatusEnum.isEnable(sku.getStatus()))
                .map(sku -> new SubscriptionWindowOfferSkuDO()
                        .setOfferId(offerId)
                        .setProductSkuId(sku.getId())
                        .setSort(nextSort.incrementAndGet())
                        .setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setMaxQuantityPerStudent(1))
                .toList();
        if (CollUtil.isEmpty(insertList)) {
            offerSkuAvailabilityValidator.validateEnabledOfferHasEffectiveSku(offerId);
            return 0;
        }
        offerSkuMapper.insertBatch(insertList);
        offerSkuAvailabilityValidator.validateEnabledOfferHasEffectiveSku(offerId);
        return insertList.size();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUpdate(SubscriptionOfferSkuBatchUpdateReqVO reqVO) {
        offerService.validateOfferExists(reqVO.getOfferId());
        for (SubscriptionOfferSkuSaveReqVO sku : reqVO.getSkus()) {
            sku.setOfferId(reqVO.getOfferId());
            saveOfferSku(sku, false);
        }
        offerSkuAvailabilityValidator.validateEnabledOfferHasEffectiveSku(reqVO.getOfferId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveOfferSku(SubscriptionOfferSkuSaveReqVO reqVO) {
        return saveOfferSku(reqVO, true);
    }

    private Long saveOfferSku(SubscriptionOfferSkuSaveReqVO reqVO, boolean validateAvailability) {
        SubscriptionWindowOfferDO offer = offerService.validateOfferExists(reqVO.getOfferId());
        SubscriptionWindowOfferSkuDO oldOfferSku = reqVO.getId() == null ? null : validateOfferSkuExists(reqVO.getId());
        if (oldOfferSku != null && !Objects.equals(oldOfferSku.getOfferId(), reqVO.getOfferId())) {
            throw exception(OFFER_SKU_BELONG_ERROR);
        }
        ProductPublicationRespDTO publication = productPublicationApi.getPublication(offer.getProductSpuId());
        ProductPublicationRespDTO.PublicationSkuDTO productSku = findPublicationSku(publication, reqVO.getProductSkuId());
        if (productSku == null) {
            throw exception(OFFER_SKU_PRODUCT_MISMATCH);
        }
        SubscriptionWindowOfferSkuDO existed = offerSkuMapper.selectByOfferIdAndProductSkuIdAndIdNot(
                reqVO.getOfferId(), reqVO.getProductSkuId(), reqVO.getId());
        if (existed != null) {
            throw exception(OFFER_SKU_DUPLICATE);
        }
        SubscriptionWindowOfferSkuDO offerSku = BeanUtils.toBean(reqVO, SubscriptionWindowOfferSkuDO.class);
        if (offerSku.getSort() == null) {
            offerSku.setSort(0);
        }
        if (offerSku.getStatus() == null) {
            offerSku.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (offerSku.getMaxQuantityPerStudent() == null) {
            offerSku.setMaxQuantityPerStudent(1);
        }
        if (reqVO.getId() == null) {
            offerSkuMapper.insert(offerSku);
            if (validateAvailability) {
                offerSkuAvailabilityValidator.validateEnabledOfferHasEffectiveSku(reqVO.getOfferId());
            }
            return offerSku.getId();
        }
        offerSkuMapper.updateById(offerSku);
        if (validateAvailability) {
            offerSkuAvailabilityValidator.validateEnabledOfferHasEffectiveSku(reqVO.getOfferId());
        }
        return reqVO.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteOfferSku(Long id) {
        SubscriptionWindowOfferSkuDO offerSku = validateOfferSkuExists(id);
        offerSkuMapper.deleteById(id);
        offerSkuAvailabilityValidator.validateEnabledOfferHasEffectiveSku(offerSku.getOfferId());
    }

    @Override
    public SubscriptionWindowOfferSkuDO getOfferSku(Long id) {
        return id == null ? null : offerSkuMapper.selectById(id);
    }

    @Override
    public SubscriptionWindowOfferSkuDO validateOfferSkuExists(Long id) {
        SubscriptionWindowOfferSkuDO offerSku = getOfferSku(id);
        if (offerSku == null) {
            throw exception(OFFER_SKU_NOT_EXISTS);
        }
        return offerSku;
    }

    private ProductPublicationRespDTO.PublicationSkuDTO findPublicationSku(ProductPublicationRespDTO publication,
                                                                           Long productSkuId) {
        if (publication == null || CollUtil.isEmpty(publication.getSkus())) {
            return null;
        }
        return publication.getSkus().stream()
                .filter(sku -> Objects.equals(sku.getId(), productSkuId))
                .findFirst()
                .orElse(null);
    }

    private SubscriptionOfferSkuRespVO buildOfferSkuResp(SubscriptionWindowOfferSkuDO offerSku,
                                                         ProductPublicationRespDTO publication) {
        SubscriptionOfferSkuRespVO respVO = BeanUtils.toBean(offerSku, SubscriptionOfferSkuRespVO.class);
        ProductPublicationRespDTO.PublicationSkuDTO productSku = findPublicationSku(publication, offerSku.getProductSkuId());
        if (productSku == null) {
            return respVO;
        }
        respVO.setProductSkuName(productSku.getName());
        respVO.setPrice(productSku.getPrice());
        respVO.setStock(productSku.getStock());
        ProductPublicationRespDTO.PublicationSpuExtDTO spuExt = publication == null ? null : publication.getPublicationExt();
        respVO.setIssueMode(spuExt == null ? null : spuExt.getIssueMode());
        respVO.setIssueCount(offerSkuIssueMapper.selectEnabledListByOfferSkuId(offerSku.getId(),
                CommonStatusEnum.ENABLE.getStatus()).size());
        respVO.setApplicableGradeCatalogIds(productSku.getApplicableGradeCatalogIds());
        respVO.setApplicableGradeNames(productSku.getApplicableGradeNames());
        ProductPublicationRespDTO.PublicationSkuExtDTO ext = productSku.getPublicationExt();
        if (ext != null) {
            respVO.setVolumeLabel(ext.getVolumeLabel());
            respVO.setEditionLabel(ext.getEditionLabel());
            respVO.setIsbn(ext.getIsbn());
        }
        return respVO;
    }
}
