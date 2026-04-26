package cn.iocoder.yudao.module.subscription.service.offer;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.api.gradecatalog.EduGradeCatalogApi;
import cn.iocoder.yudao.module.edu.api.gradecatalog.dto.EduGradeCatalogRespDTO;
import cn.iocoder.yudao.module.product.api.publication.ProductPublicationApi;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.*;
import cn.iocoder.yudao.module.subscription.dal.dataobject.*;
import cn.iocoder.yudao.module.subscription.dal.mysql.*;
import cn.iocoder.yudao.module.subscription.service.offersku.SubscriptionOfferSkuAvailabilityService;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.*;

@Service
@Validated
public class SubscriptionOfferService {

    @Resource
    private SubscriptionWindowOfferMapper offerMapper;
    @Resource
    private SubscriptionWindowOfferSkuMapper offerSkuMapper;
    @Resource
    private SubscriptionWindowOfferGradeRelMapper offerGradeRelMapper;
    @Resource
    private SubscriptionWindowService windowService;
    @Resource
    private ProductPublicationApi productPublicationApi;
    @Resource
    private EduGradeCatalogApi gradeCatalogApi;
    @Resource
    private SubscriptionRuleMapper ruleMapper;
    @Resource
    private SubscriptionRuleConditionMapper ruleConditionMapper;
    @Resource
    private SubscriptionOfferSkuAvailabilityService offerSkuAvailabilityService;

    @Transactional(rollbackFor = Exception.class)
    public List<Long> batchCreateOffer(SubscriptionOfferBatchCreateReqVO reqVO) {
        SubscriptionWindowDO window = windowService.validateWindowExists(reqVO.getWindowId());
        List<ProductPublicationRespDTO> publications = productPublicationApi.getPublicationList(reqVO.getProductSpuIds());
        Map<Long, ProductPublicationRespDTO> publicationMap = convertMap(publications, ProductPublicationRespDTO::getId);
        List<Long> offerIds = new ArrayList<>();
        for (Long productSpuId : reqVO.getProductSpuIds()) {
            ProductPublicationRespDTO publication = publicationMap.get(productSpuId);
            if (publication == null || !BizSceneEnum.isPublication(publication.getBizScene())) {
                throw exception(OFFER_PRODUCT_NOT_PUBLICATION);
            }
            if (offerMapper.selectByWindowIdAndProductSpuId(window.getId(), productSpuId) != null) {
                throw exception(OFFER_PRODUCT_DUPLICATE);
            }
            List<SubscriptionWindowOfferSkuDO> offerSkus = buildMatchedOfferSkus(window, publication);
            if (CollUtil.isEmpty(offerSkus)) {
                throw exception(OFFER_NO_MATCHED_SKU);
            }
            SubscriptionWindowOfferDO offer = new SubscriptionWindowOfferDO()
                    .setWindowId(window.getId())
                    .setProductSpuId(productSpuId)
                    .setRecommendFlag(false)
                    .setSort(0)
                    .setStatus(CommonStatusEnum.ENABLE.getStatus());
            offerMapper.insert(offer);
            offerSkus.forEach(sku -> sku.setOfferId(offer.getId()));
            offerSkuMapper.insertBatch(offerSkus);
            offerIds.add(offer.getId());
        }
        return offerIds;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOffer(SubscriptionOfferSaveReqVO reqVO) {
        SubscriptionWindowOfferDO offer = validateOfferExists(reqVO.getId());
        if ((reqVO.getWindowId() != null && !Objects.equals(reqVO.getWindowId(), offer.getWindowId()))
                || (reqVO.getProductSpuId() != null && !Objects.equals(reqVO.getProductSpuId(), offer.getProductSpuId()))) {
            throw exception(OFFER_ANCHOR_IMMUTABLE);
        }
        SubscriptionWindowOfferDO updateObj = new SubscriptionWindowOfferDO()
                .setId(reqVO.getId())
                .setRecommendFlag(reqVO.getRecommendFlag() == null ? offer.getRecommendFlag() : reqVO.getRecommendFlag())
                .setSort(reqVO.getSort() == null ? offer.getSort() : reqVO.getSort())
                .setStatus(reqVO.getStatus() == null ? offer.getStatus() : reqVO.getStatus())
                .setRemark(reqVO.getRemark());
        offerMapper.updateById(updateObj);
        saveOfferGrades(reqVO.getId(), reqVO.getGradeCatalogIds());
        if (CommonStatusEnum.isEnable(updateObj.getStatus())) {
            offerSkuAvailabilityService.validateEnabledOfferHasEffectiveSku(reqVO.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteOffer(Long id) {
        validateOfferExists(id);
        List<SubscriptionRuleDO> rules = ruleMapper.selectListByOfferIds(Collections.singleton(id));
        ruleConditionMapper.deleteByRuleIds(convertSet(rules, SubscriptionRuleDO::getId));
        ruleMapper.deleteByOfferId(id);
        offerSkuMapper.deleteByOfferId(id);
        offerGradeRelMapper.deleteByOfferId(id);
        offerMapper.deleteById(id);
    }

    public SubscriptionWindowOfferDO getOffer(Long id) {
        return id == null ? null : offerMapper.selectById(id);
    }

    public SubscriptionWindowOfferDO validateOfferExists(Long id) {
        SubscriptionWindowOfferDO offer = getOffer(id);
        if (offer == null) {
            throw exception(OFFER_NOT_EXISTS);
        }
        return offer;
    }

    public SubscriptionOfferRespVO getOfferResp(Long id) {
        return buildOfferRespList(Collections.singletonList(validateOfferExists(id))).get(0);
    }

    public PageResult<SubscriptionOfferRespVO> getOfferPage(SubscriptionOfferPageReqVO reqVO) {
        PageResult<SubscriptionWindowOfferDO> pageResult = offerMapper.selectPage(reqVO);
        return new PageResult<>(buildOfferRespList(pageResult.getList()), pageResult.getTotal());
    }

    public List<SubscriptionOfferRespVO> buildOfferRespList(List<SubscriptionWindowOfferDO> offers) {
        if (CollUtil.isEmpty(offers)) {
            return Collections.emptyList();
        }
        Map<Long, ProductPublicationRespDTO> publicationMap = convertMap(
                productPublicationApi.getPublicationList(convertSet(offers, SubscriptionWindowOfferDO::getProductSpuId)),
                ProductPublicationRespDTO::getId);
        Map<Long, List<SubscriptionWindowOfferSkuDO>> offerSkuMap = convertMultiMap(
                offerSkuMapper.selectListByOfferIds(convertSet(offers, SubscriptionWindowOfferDO::getId)),
                SubscriptionWindowOfferSkuDO::getOfferId);
        Map<Long, List<SubscriptionWindowOfferGradeRelDO>> gradeRelMap = convertMultiMap(
                offerGradeRelMapper.selectListByOfferIds(convertSet(offers, SubscriptionWindowOfferDO::getId)),
                SubscriptionWindowOfferGradeRelDO::getOfferId);
        Set<Long> gradeCatalogIds = gradeRelMap.values().stream().flatMap(Collection::stream)
                .map(SubscriptionWindowOfferGradeRelDO::getGradeCatalogId).collect(Collectors.toSet());
        Map<Long, EduGradeCatalogRespDTO> gradeCatalogMap = gradeCatalogApi.getGradeCatalogMap(gradeCatalogIds);
        return offers.stream()
                .map(offer -> buildOfferResp(offer, publicationMap.get(offer.getProductSpuId()),
                        offerSkuMap.get(offer.getId()), gradeRelMap.get(offer.getId()), gradeCatalogMap))
                .toList();
    }

    public Map<Long, List<SubscriptionWindowOfferGradeRelDO>> getGradeRelMap(Collection<Long> offerIds) {
        return convertMultiMap(offerGradeRelMapper.selectListByOfferIds(offerIds),
                SubscriptionWindowOfferGradeRelDO::getOfferId);
    }

    private List<SubscriptionWindowOfferSkuDO> buildMatchedOfferSkus(SubscriptionWindowDO window,
                                                                     ProductPublicationRespDTO publication) {
        if (CollUtil.isEmpty(publication.getSkus())) {
            return Collections.emptyList();
        }
        return publication.getSkus().stream()
                .filter(sku -> sku.getPublicationExt() != null)
                .filter(sku -> Objects.equals(window.getTargetPeriod(), sku.getPublicationExt().getTargetPeriod()))
                .filter(sku -> CommonStatusEnum.isEnable(sku.getStatus()))
                .map(sku -> new SubscriptionWindowOfferSkuDO()
                        .setProductSkuId(sku.getId())
                        .setSort(0)
                        .setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setMaxQuantityPerStudent(1))
                .toList();
    }

    private SubscriptionOfferRespVO buildOfferResp(
            SubscriptionWindowOfferDO offer,
            ProductPublicationRespDTO publication,
            List<SubscriptionWindowOfferSkuDO> offerSkus,
            List<SubscriptionWindowOfferGradeRelDO> gradeRels,
            Map<Long, EduGradeCatalogRespDTO> gradeCatalogMap) {
        SubscriptionOfferRespVO respVO = BeanUtils.toBean(offer, SubscriptionOfferRespVO.class);
        if (publication != null) {
            respVO.setProductName(publication.getName());
            respVO.setCategoryId(publication.getCategoryId());
            respVO.setCategoryName(publication.getCategoryName());
            respVO.setPicUrl(publication.getPicUrl());
            respVO.setPrice(publication.getPrice());
            respVO.setPublication(publication);
            ProductPublicationRespDTO.PublicationSpuExtDTO ext = publication.getPublicationExt();
            if (ext != null) {
                respVO.setPublisherId(ext.getPublisherId());
                respVO.setPublisherName(ext.getPublisherName());
                respVO.setPublicationTypeId(ext.getPublicationTypeId());
                respVO.setPublicationTypeName(ext.getPublicationTypeName());
            }
        }
        List<SubscriptionWindowOfferSkuDO> skuList = offerSkus == null ? Collections.emptyList() : offerSkus;
        respVO.setTotalSkuCount(skuList.size());
        respVO.setEnabledSkuCount((int) skuList.stream().filter(sku -> CommonStatusEnum.isEnable(sku.getStatus())).count());
        List<SubscriptionWindowOfferGradeRelDO> relList = gradeRels == null ? Collections.emptyList() : gradeRels;
        respVO.setGradeCatalogIds(convertList(relList, SubscriptionWindowOfferGradeRelDO::getGradeCatalogId));
        respVO.setGradeNames(convertList(relList, rel -> {
            EduGradeCatalogRespDTO gradeCatalog = gradeCatalogMap.get(rel.getGradeCatalogId());
            return gradeCatalog == null ? null : gradeCatalog.getGradeName();
        }));
        return respVO;
    }

    private void saveOfferGrades(Long offerId, List<Long> gradeCatalogIds) {
        offerGradeRelMapper.deleteByOfferId(offerId);
        if (CollUtil.isEmpty(gradeCatalogIds)) {
            return;
        }
        List<SubscriptionWindowOfferGradeRelDO> rels = gradeCatalogIds.stream()
                .distinct()
                .map(gradeCatalogId -> new SubscriptionWindowOfferGradeRelDO()
                        .setOfferId(offerId)
                        .setGradeCatalogId(gradeCatalogId))
                .toList();
        offerGradeRelMapper.insertBatch(rels);
    }
}
