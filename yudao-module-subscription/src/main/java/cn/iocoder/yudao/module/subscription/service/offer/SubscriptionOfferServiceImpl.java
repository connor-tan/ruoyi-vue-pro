package cn.iocoder.yudao.module.subscription.service.offer;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.api.gradecatalog.EduGradeCatalogApi;
import cn.iocoder.yudao.module.edu.api.gradecatalog.dto.EduGradeCatalogRespDTO;
import cn.iocoder.yudao.module.product.api.category.ProductCategoryApi;
import cn.iocoder.yudao.module.product.api.publication.ProductPublicationApi;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.*;
import cn.iocoder.yudao.module.subscription.dal.dataobject.*;
import cn.iocoder.yudao.module.subscription.dal.mysql.*;
import cn.iocoder.yudao.module.subscription.service.offersku.SubscriptionOfferSkuAvailabilityValidator;
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
public class SubscriptionOfferServiceImpl implements SubscriptionOfferService {

    private static final String CANDIDATE_STATUS_CAN_ADD = "CAN_ADD";
    private static final String CANDIDATE_STATUS_ADDED = "ADDED";
    private static final String CANDIDATE_STATUS_NO_MATCHED_SKU = "NO_MATCHED_SKU";
    private static final String CANDIDATE_STATUS_DISABLED = "DISABLED";

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
    private ProductCategoryApi productCategoryApi;
    @Resource
    private EduGradeCatalogApi gradeCatalogApi;
    @Resource
    private SubscriptionRuleMapper ruleMapper;
    @Resource
    private SubscriptionRuleConditionMapper ruleConditionMapper;
    @Resource
    private SubscriptionOfferSkuAvailabilityValidator offerSkuAvailabilityValidator;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SubscriptionOfferBatchCreateRespVO batchCreateOffer(SubscriptionOfferBatchCreateReqVO reqVO) {
        SubscriptionWindowDO window = windowService.validateWindowExists(reqVO.getWindowId());
        List<ProductPublicationRespDTO> publications = productPublicationApi.getPublicationList(reqVO.getProductSpuIds());
        Map<Long, ProductPublicationRespDTO> publicationMap = convertMap(publications, ProductPublicationRespDTO::getId);
        return batchCreateOffer0(window, reqVO.getProductSpuIds(), publicationMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SubscriptionOfferBatchCreateRespVO batchCreateByQuery(SubscriptionOfferBatchCreateByQueryReqVO reqVO) {
        SubscriptionOfferAvailablePageReqVO query = normalizeAvailableQuery(reqVO.getQuery());
        query.setWindowId(reqVO.getWindowId());
        query.setCandidateStatus(CANDIDATE_STATUS_CAN_ADD);
        SubscriptionWindowDO window = windowService.validateWindowExists(reqVO.getWindowId());
        List<SubscriptionOfferAvailableRespVO> candidates = queryAvailableCandidates(query, null, null);
        List<Long> productSpuIds = candidates.stream()
                .map(SubscriptionOfferAvailableRespVO::getProductSpuId)
                .toList();
        Map<Long, ProductPublicationRespDTO> publicationMap = convertMap(productPublicationApi.getPublicationList(productSpuIds),
                ProductPublicationRespDTO::getId);
        return batchCreateOffer0(window, productSpuIds, publicationMap);
    }

    @Override
    public PageResult<SubscriptionOfferAvailableRespVO> getAvailablePage(SubscriptionOfferAvailablePageReqVO reqVO) {
        windowService.validateWindowExists(reqVO.getWindowId());
        SubscriptionOfferAvailablePageReqVO query = normalizeAvailableQuery(reqVO);
        int offset = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        List<SubscriptionOfferAvailableRespVO> candidates =
                queryAvailableCandidates(query, offset, reqVO.getPageSize());
        Long total = offerMapper.selectAvailableCandidateCount(query);
        return new PageResult<>(candidates, total == null ? 0L : total);
    }

    private SubscriptionOfferAvailablePageReqVO normalizeAvailableQuery(SubscriptionOfferAvailablePageReqVO reqVO) {
        SubscriptionOfferAvailablePageReqVO query = BeanUtils.toBean(reqVO, SubscriptionOfferAvailablePageReqVO.class);
        if (CollUtil.isNotEmpty(query.getCategoryIds())) {
            query.setCategoryIds(new ArrayList<>(
                    productCategoryApi.getSelfAndDescendantCategoryIds(query.getCategoryIds())));
        }
        return query;
    }

    private SubscriptionOfferBatchCreateRespVO batchCreateOffer0(SubscriptionWindowDO window, Collection<Long> productSpuIds,
                                                                 Map<Long, ProductPublicationRespDTO> publicationMap) {
        SubscriptionOfferBatchCreateRespVO respVO = new SubscriptionOfferBatchCreateRespVO();
        List<Long> offerIds = new ArrayList<>();
        List<SubscriptionOfferBatchCreateRespVO.SkippedItem> skippedItems = new ArrayList<>();
        int createdOfferSkuCount = 0;
        for (Long productSpuId : productSpuIds) {
            ProductPublicationRespDTO publication = publicationMap.get(productSpuId);
            if (publication == null || !BizSceneEnum.isPublication(publication.getBizScene())) {
                skippedItems.add(buildSkippedItem(productSpuId, null, OFFER_PRODUCT_NOT_PUBLICATION.getMsg()));
                continue;
            }
            if (!ProductSpuStatusEnum.isEnable(publication.getStatus())) {
                skippedItems.add(buildSkippedItem(productSpuId, publication.getName(), "刊物商品未上架"));
                continue;
            }
            if (offerMapper.selectByWindowIdAndProductSpuId(window.getId(), productSpuId) != null) {
                skippedItems.add(buildSkippedItem(productSpuId, publication.getName(), OFFER_PRODUCT_DUPLICATE.getMsg()));
                continue;
            }
            List<SubscriptionWindowOfferSkuDO> offerSkus = buildMatchedOfferSkus(publication);
            if (CollUtil.isEmpty(offerSkus)) {
                skippedItems.add(buildSkippedItem(productSpuId, publication.getName(), OFFER_NO_MATCHED_SKU.getMsg()));
                continue;
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
            createdOfferSkuCount += offerSkus.size();
        }
        respVO.setCreatedOfferIds(offerIds);
        respVO.setCreatedOfferCount(offerIds.size());
        respVO.setCreatedOfferSkuCount(createdOfferSkuCount);
        respVO.setSkippedItems(skippedItems);
        respVO.setSkippedCount(skippedItems.size());
        return respVO;
    }

    private List<SubscriptionOfferAvailableRespVO> queryAvailableCandidates(SubscriptionOfferAvailablePageReqVO reqVO,
                                                                            Integer offset,
                                                                            Integer limit) {
        List<SubscriptionOfferAvailableRespVO> candidates =
                offerMapper.selectAvailableCandidates(reqVO, offset, limit);
        candidates.forEach(candidate -> {
            candidate.setMatchedGradeCatalogIds(parseGradeCatalogIds(candidate.getMatchedGradeCatalogIdText()));
            candidate.setCategoryIds(parseIdText(candidate.getCategoryIdText()));
            candidate.setCategoryNames(parseText(candidate.getCategoryNameText()));
            fillCandidateDisabledReason(candidate);
        });
        Set<Long> gradeCatalogIds = candidates.stream()
                .filter(candidate -> CollUtil.isNotEmpty(candidate.getMatchedGradeCatalogIds()))
                .flatMap(candidate -> candidate.getMatchedGradeCatalogIds().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, EduGradeCatalogRespDTO> gradeCatalogMap = CollUtil.isEmpty(gradeCatalogIds)
                ? Collections.emptyMap() : gradeCatalogApi.getGradeCatalogMap(gradeCatalogIds);
        candidates.forEach(candidate -> candidate.setMatchedGradeNames(convertList(candidate.getMatchedGradeCatalogIds(), gradeId -> {
            EduGradeCatalogRespDTO gradeCatalog = gradeCatalogMap.get(gradeId);
            return gradeCatalog == null ? null : gradeCatalog.getGradeName();
        })));
        return candidates;
    }

    private List<Long> parseGradeCatalogIds(String text) {
        return parseIdText(text);
    }

    private List<Long> parseIdText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(text.split(","))
                .filter(item -> item != null && !item.isBlank())
                .map(Long::valueOf)
                .toList();
    }

    private List<String> parseText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(text.split(","))
                .filter(item -> item != null && !item.isBlank())
                .toList();
    }

    private void fillCandidateDisabledReason(SubscriptionOfferAvailableRespVO candidate) {
        if (CANDIDATE_STATUS_ADDED.equals(candidate.getCandidateStatus())) {
            candidate.setDisabledReason(OFFER_PRODUCT_DUPLICATE.getMsg());
            return;
        }
        if (CANDIDATE_STATUS_NO_MATCHED_SKU.equals(candidate.getCandidateStatus())) {
            candidate.setDisabledReason(OFFER_NO_MATCHED_SKU.getMsg());
            return;
        }
        if (!CANDIDATE_STATUS_DISABLED.equals(candidate.getCandidateStatus())) {
            return;
        }
        if (!ProductSpuStatusEnum.isEnable(candidate.getProductStatus())) {
            candidate.setDisabledReason("刊物商品未上架");
            return;
        }
        if (candidate.getTotalSkuCount() == null || candidate.getTotalSkuCount() == 0) {
            candidate.setDisabledReason("刊物没有 SKU");
            return;
        }
        if (candidate.getEnabledSkuCount() == null || candidate.getEnabledSkuCount() == 0) {
            candidate.setDisabledReason("刊物 SKU 均未启用");
            return;
        }
        candidate.setDisabledReason("刊物不可添加");
    }

    private SubscriptionOfferBatchCreateRespVO.SkippedItem buildSkippedItem(Long productSpuId, String productName,
                                                                            String reason) {
        SubscriptionOfferBatchCreateRespVO.SkippedItem skippedItem =
                new SubscriptionOfferBatchCreateRespVO.SkippedItem();
        skippedItem.setProductSpuId(productSpuId);
        skippedItem.setProductName(productName);
        skippedItem.setReason(reason);
        return skippedItem;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
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
            offerSkuAvailabilityValidator.validateEnabledOfferHasEffectiveSku(reqVO.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteOffer(Long id) {
        validateOfferExists(id);
        List<SubscriptionRuleDO> rules = ruleMapper.selectListByOfferIds(Collections.singleton(id));
        ruleConditionMapper.deleteByRuleIds(convertSet(rules, SubscriptionRuleDO::getId));
        ruleMapper.deleteByOfferId(id);
        offerSkuMapper.deleteByOfferId(id);
        offerGradeRelMapper.deleteByOfferId(id);
        offerMapper.deleteById(id);
    }

    @Override
    public SubscriptionWindowOfferDO getOffer(Long id) {
        return id == null ? null : offerMapper.selectById(id);
    }

    @Override
    public SubscriptionWindowOfferDO validateOfferExists(Long id) {
        SubscriptionWindowOfferDO offer = getOffer(id);
        if (offer == null) {
            throw exception(OFFER_NOT_EXISTS);
        }
        return offer;
    }

    @Override
    public SubscriptionOfferRespVO getOfferResp(Long id) {
        return buildOfferRespList(Collections.singletonList(validateOfferExists(id))).get(0);
    }

    @Override
    public PageResult<SubscriptionOfferRespVO> getOfferPage(SubscriptionOfferPageReqVO reqVO) {
        PageResult<SubscriptionWindowOfferDO> pageResult = offerMapper.selectPage(reqVO);
        return new PageResult<>(buildOfferRespList(pageResult.getList()), pageResult.getTotal());
    }

    @Override
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

    @Override
    public Map<Long, List<SubscriptionWindowOfferGradeRelDO>> getGradeRelMap(Collection<Long> offerIds) {
        return convertMultiMap(offerGradeRelMapper.selectListByOfferIds(offerIds),
                SubscriptionWindowOfferGradeRelDO::getOfferId);
    }

    private List<SubscriptionWindowOfferSkuDO> buildMatchedOfferSkus(ProductPublicationRespDTO publication) {
        if (CollUtil.isEmpty(publication.getSkus())) {
            return Collections.emptyList();
        }
        return publication.getSkus().stream()
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
            respVO.setCategoryIds(publication.getCategoryIds());
            respVO.setCategoryNames(convertList(publication.getCategories(), ProductPublicationRespDTO.Category::getName));
            respVO.setCategories(publication.getCategories());
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
