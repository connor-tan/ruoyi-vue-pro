package cn.iocoder.yudao.module.subscription.service.windowspu;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationPublisherDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTitleDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTypeDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuGradeDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationTitleMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductSpuPublicationMapper;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuAvailablePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuAvailableRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuBatchCreateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuBatchCreateRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspu.vo.SubscriptionWindowSpuSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuGradeDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSkuMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuGradeMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuRuleMapper;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class SubscriptionWindowSpuServiceImpl implements SubscriptionWindowSpuService {

    @Resource
    private SubscriptionWindowSpuMapper subscriptionWindowSpuMapper;
    @Resource
    private SubscriptionWindowSpuGradeMapper subscriptionWindowSpuGradeMapper;
    @Resource
    private SubscriptionWindowSpuRuleMapper subscriptionWindowSpuRuleMapper;
    @Resource
    private SubscriptionWindowSkuMapper subscriptionWindowSkuMapper;
    @Resource
    private SubscriptionWindowService subscriptionWindowService;
    @Resource
    private SubscriptionSupportService subscriptionSupportService;
    @Resource
    private ProductPublicationTitleMapper productPublicationTitleMapper;
    @Resource
    private ProductSpuPublicationMapper productSpuPublicationMapper;

    @Override
    public PageResult<SubscriptionWindowSpuRespVO> getWindowSpuPage(SubscriptionWindowSpuPageReqVO reqVO) {
        subscriptionWindowService.getWindowDO(reqVO.getWindowId());
        Set<Long> matchedProductSpuIds = resolveMatchedProductSpuIds(reqVO.getProductName(), reqVO.getCategoryId(),
                null, reqVO.getPublicationTypeId(), reqVO.getPublisherId());
        if (matchedProductSpuIds != null && matchedProductSpuIds.isEmpty()) {
            return PageResult.empty();
        }
        PageResult<SubscriptionWindowSpuDO> pageResult = subscriptionWindowSpuMapper.selectPage(reqVO, matchedProductSpuIds);
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }
        return new PageResult<>(buildWindowSpuRespList(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public PageResult<SubscriptionWindowSpuAvailableRespVO> getAvailablePage(SubscriptionWindowSpuAvailablePageReqVO reqVO) {
        subscriptionWindowService.getWindowDO(reqVO.getWindowId());
        subscriptionSupportService.validateGradeCatalogIds(Collections.singleton(reqVO.getBaseGradeCatalogId()));
        List<ProductSpuDO> candidates = subscriptionSupportService.getPublicationSpuList(reqVO.getProductName(),
                reqVO.getCategoryId(), reqVO.getBaseGradeCatalogId(), true);
        if (candidates.isEmpty()) {
            return PageResult.empty();
        }
        Set<Long> matchedProductSpuIds = resolveMatchedProductSpuIds(reqVO.getProductName(), reqVO.getCategoryId(),
                reqVO.getBaseGradeCatalogId(), reqVO.getPublicationTypeId(), reqVO.getPublisherId());
        if (matchedProductSpuIds != null) {
            candidates = candidates.stream().filter(spu -> matchedProductSpuIds.contains(spu.getId())).toList();
        }
        if (candidates.isEmpty()) {
            return PageResult.empty();
        }
        Map<Long, SubscriptionWindowSpuDO> existingMap = CollectionUtils.convertMap(
                subscriptionWindowSpuMapper.selectListByWindowId(reqVO.getWindowId()),
                SubscriptionWindowSpuDO::getProductSpuId);
        Map<Long, List<SubscriptionWindowSpuGradeDO>> existingGradeMap = getGradeDOMap(existingMap.values().stream()
                .map(SubscriptionWindowSpuDO::getId).toList());
        candidates = candidates.stream()
                .filter(spu -> {
                    SubscriptionWindowSpuDO existing = existingMap.get(spu.getId());
                    if (existing == null) {
                        return true;
                    }
                    return existingGradeMap.getOrDefault(existing.getId(), Collections.emptyList()).stream()
                            .noneMatch(item -> Objects.equals(item.getGradeCatalogId(), reqVO.getBaseGradeCatalogId()));
                })
                .toList();
        if (candidates.isEmpty()) {
            return PageResult.empty();
        }

        List<Long> productSpuIds = CollectionUtils.convertList(candidates, ProductSpuDO::getId);
        Map<Long, ProductCategoryDO> categoryMap = subscriptionSupportService.getCategoryMap(candidates.stream()
                .map(ProductSpuDO::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        Map<Long, ProductSpuPublicationDO> spuPublicationMap = subscriptionSupportService.getSpuPublicationMap(productSpuIds);
        Map<Long, ProductPublicationTitleDO> titleMap = subscriptionSupportService.getPublicationTitleMap(spuPublicationMap.values().stream()
                .map(ProductSpuPublicationDO::getPublicationTitleId)
                .collect(Collectors.toSet()));
        Map<Long, ProductPublicationTypeDO> typeMap = subscriptionSupportService.getPublicationTypeMap(titleMap.values().stream()
                .map(ProductPublicationTitleDO::getTypeId)
                .collect(Collectors.toSet()));
        Map<Long, ProductPublicationPublisherDO> publisherMap = subscriptionSupportService.getPublicationPublisherMap(titleMap.values().stream()
                .map(ProductPublicationTitleDO::getPublisherId)
                .collect(Collectors.toSet()));
        Map<Long, List<ProductSpuGradeDO>> spuGradeMap = subscriptionSupportService.getPublicationSpuGradeMap(productSpuIds);
        Map<Long, GradeCatalogDO> gradeCatalogMap = subscriptionSupportService.getGradeCatalogMap(spuGradeMap.values().stream()
                .flatMap(List::stream)
                .map(ProductSpuGradeDO::getGradeCatalogId)
                .collect(Collectors.toSet()));

        List<SubscriptionWindowSpuAvailableRespVO> records = candidates.stream()
                .map(spu -> buildAvailableResp(spu, categoryMap.get(spu.getCategoryId()), spuPublicationMap.get(spu.getId()),
                        titleMap, typeMap, publisherMap, spuGradeMap.getOrDefault(spu.getId(), Collections.emptyList()), gradeCatalogMap))
                .toList();
        return paginate(records, reqVO.getPageNo(), reqVO.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionWindowSpuBatchCreateRespVO batchCreate(SubscriptionWindowSpuBatchCreateReqVO reqVO) {
        subscriptionWindowService.getWindowDO(reqVO.getWindowId());
        subscriptionSupportService.validateGradeCatalogIds(Collections.singleton(reqVO.getBaseGradeCatalogId()));
        SubscriptionWindowSpuBatchCreateRespVO respVO = new SubscriptionWindowSpuBatchCreateRespVO();
        List<SubscriptionWindowSpuBatchCreateRespVO.SkippedItem> skippedItems = new ArrayList<>();
        int createdCount = 0;
        for (Long productSpuId : reqVO.getProductSpuIds()) {
            ProductSpuDO productSpu = subscriptionSupportService.getPublicationSpu(productSpuId, true);
            if (productSpu == null) {
                throw exception(ErrorCodeConstants.WINDOW_SPU_NOT_EXISTS);
            }
            validateSpuGradeMatch(productSpu.getId(), reqVO.getBaseGradeCatalogId());
            SubscriptionWindowSpuDO exists = subscriptionWindowSpuMapper.selectByWindowIdAndProductSpuId(reqVO.getWindowId(), productSpuId);
            if (exists != null) {
                if (subscriptionWindowSpuGradeMapper.selectByWindowSpuIdAndGradeCatalogId(exists.getId(), reqVO.getBaseGradeCatalogId()) != null) {
                    skippedItems.add(buildSkippedItem(productSpu, ErrorCodeConstants.WINDOW_SPU_GRADE_DUPLICATE.getMsg()));
                    continue;
                }
                subscriptionWindowSpuGradeMapper.insert(SubscriptionWindowSpuGradeDO.builder()
                        .windowSpuId(exists.getId())
                        .gradeCatalogId(reqVO.getBaseGradeCatalogId())
                        .build());
                createdCount++;
                continue;
            }

            SubscriptionWindowSpuDO windowSpu = SubscriptionWindowSpuDO.builder()
                    .windowId(reqVO.getWindowId())
                    .productSpuId(productSpu.getId())
                    .recommendFlag(Boolean.FALSE)
                    .sort(productSpu.getSort() == null ? 0 : productSpu.getSort())
                    .remark(null)
                    .build();
            subscriptionWindowSpuMapper.insert(windowSpu);
            subscriptionWindowSpuGradeMapper.insert(SubscriptionWindowSpuGradeDO.builder()
                    .windowSpuId(windowSpu.getId())
                    .gradeCatalogId(reqVO.getBaseGradeCatalogId())
                    .build());
            initializeWindowSkus(windowSpu.getId(), productSpu.getId());
            createdCount++;
        }
        respVO.setCreatedCount(createdCount);
        respVO.setSkippedCount(skippedItems.size());
        respVO.setSkippedItems(skippedItems);
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWindowSpu(SubscriptionWindowSpuSaveReqVO reqVO) {
        SubscriptionWindowSpuDO windowSpu = getWindowSpuDO(reqVO.getId());
        subscriptionSupportService.validateGradeCatalogIds(reqVO.getGradeCatalogIds());
        windowSpu.setRecommendFlag(reqVO.getRecommendFlag());
        windowSpu.setSort(reqVO.getSort());
        windowSpu.setRemark(reqVO.getRemark());
        subscriptionWindowSpuMapper.updateById(windowSpu);

        subscriptionWindowSpuGradeMapper.deleteByWindowSpuId(windowSpu.getId());
        reqVO.getGradeCatalogIds().stream()
                .distinct()
                .map(gradeCatalogId -> SubscriptionWindowSpuGradeDO.builder()
                        .windowSpuId(windowSpu.getId())
                        .gradeCatalogId(gradeCatalogId)
                        .build())
                .forEach(subscriptionWindowSpuGradeMapper::insert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWindowSpu(Long id) {
        getWindowSpuDO(id);
        subscriptionWindowSpuGradeMapper.deleteByWindowSpuId(id);
        subscriptionWindowSpuRuleMapper.deleteByWindowSpuId(id);
        subscriptionWindowSkuMapper.deleteByWindowSpuId(id);
        subscriptionWindowSpuMapper.deleteById(id);
    }

    @Override
    public SubscriptionWindowSpuDO getWindowSpuDO(Long id) {
        SubscriptionWindowSpuDO windowSpu = subscriptionWindowSpuMapper.selectById(id);
        if (windowSpu == null) {
            throw exception(ErrorCodeConstants.WINDOW_SPU_NOT_EXISTS);
        }
        return windowSpu;
    }

    @Override
    public List<SubscriptionWindowSpuDO> getWindowSpuDOListByWindowId(Long windowId) {
        return subscriptionWindowSpuMapper.selectListByWindowId(windowId);
    }

    @Override
    public Map<Long, List<SubscriptionWindowSpuGradeDO>> getGradeDOMap(Collection<Long> windowSpuIds) {
        return CollectionUtils.convertMultiMap(subscriptionWindowSpuGradeMapper.selectListByWindowSpuIds(windowSpuIds),
                SubscriptionWindowSpuGradeDO::getWindowSpuId);
    }

    private void initializeWindowSkus(Long windowSpuId, Long productSpuId) {
        List<ProductSkuDO> productSkus = subscriptionSupportService.getSkuListBySpuId(productSpuId);
        productSkus.sort(Comparator.comparing(ProductSkuDO::getId));
        int sort = 1;
        for (ProductSkuDO productSku : productSkus) {
            subscriptionWindowSkuMapper.insert(SubscriptionWindowSkuDO.builder()
                    .windowSpuId(windowSpuId)
                    .productSkuId(productSku.getId())
                    .status(CommonStatusEnum.ENABLE.getStatus())
                    .sort(sort++)
                    .maxQuantityPerStudent(1)
                    .remark(null)
                    .build());
        }
    }

    private void validateSpuGradeMatch(Long productSpuId, Long gradeCatalogId) {
        boolean matched = subscriptionSupportService.getPublicationSpuGradeMap(Collections.singleton(productSpuId))
                .getOrDefault(productSpuId, Collections.emptyList())
                .stream()
                .anyMatch(item -> Objects.equals(item.getGradeCatalogId(), gradeCatalogId));
        if (!matched) {
            throw exception(ErrorCodeConstants.WINDOW_SPU_GRADE_NOT_MATCH);
        }
    }

    private SubscriptionWindowSpuBatchCreateRespVO.SkippedItem buildSkippedItem(ProductSpuDO productSpu, String reason) {
        SubscriptionWindowSpuBatchCreateRespVO.SkippedItem skippedItem = new SubscriptionWindowSpuBatchCreateRespVO.SkippedItem();
        skippedItem.setProductSpuId(productSpu.getId());
        skippedItem.setProductName(productSpu.getName());
        skippedItem.setReason(reason);
        return skippedItem;
    }

    private Set<Long> resolveMatchedProductSpuIds(String productName, Long categoryId, Long gradeCatalogId,
                                                  Long publicationTypeId, Long publisherId) {
        Set<Long> matchedSpuIds = null;
        if (productName != null || categoryId != null || gradeCatalogId != null) {
            matchedSpuIds = CollectionUtils.convertSet(
                    subscriptionSupportService.getPublicationSpuList(productName, categoryId, gradeCatalogId, false),
                    ProductSpuDO::getId);
        }
        if (publicationTypeId != null) {
            matchedSpuIds = intersect(matchedSpuIds, collectSpuIdsByTitleIds(CollectionUtils.convertSet(
                    productPublicationTitleMapper.selectListByTypeIds(Collections.singleton(publicationTypeId)),
                    ProductPublicationTitleDO::getId)));
        }
        if (publisherId != null) {
            matchedSpuIds = intersect(matchedSpuIds, collectSpuIdsByTitleIds(CollectionUtils.convertSet(
                    productPublicationTitleMapper.selectListByPublisherIds(Collections.singleton(publisherId)),
                    ProductPublicationTitleDO::getId)));
        }
        return matchedSpuIds;
    }

    private Set<Long> collectSpuIdsByTitleIds(Collection<Long> titleIds) {
        if (CollUtil.isEmpty(titleIds)) {
            return Collections.emptySet();
        }
        return CollectionUtils.convertSet(productSpuPublicationMapper.selectListByPublicationTitleIds(titleIds),
                ProductSpuPublicationDO::getProductSpuId);
    }

    private Set<Long> intersect(Set<Long> source, Set<Long> other) {
        if (source == null) {
            return other;
        }
        if (source.isEmpty() || other.isEmpty()) {
            return Collections.emptySet();
        }
        return source.stream().filter(other::contains).collect(Collectors.toSet());
    }

    private List<SubscriptionWindowSpuRespVO> buildWindowSpuRespList(List<SubscriptionWindowSpuDO> windowSpus) {
        List<Long> productSpuIds = windowSpus.stream().map(SubscriptionWindowSpuDO::getProductSpuId).distinct().toList();
        List<Long> windowSpuIds = windowSpus.stream().map(SubscriptionWindowSpuDO::getId).toList();
        Map<Long, ProductSpuDO> productSpuMap = subscriptionSupportService.getPublicationSpuMap(productSpuIds);
        Map<Long, ProductCategoryDO> categoryMap = subscriptionSupportService.getCategoryMap(productSpuMap.values().stream()
                .map(ProductSpuDO::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        Map<Long, ProductSpuPublicationDO> spuPublicationMap = subscriptionSupportService.getSpuPublicationMap(productSpuIds);
        Map<Long, ProductPublicationTitleDO> titleMap = subscriptionSupportService.getPublicationTitleMap(spuPublicationMap.values().stream()
                .map(ProductSpuPublicationDO::getPublicationTitleId)
                .collect(Collectors.toSet()));
        Map<Long, ProductPublicationTypeDO> typeMap = subscriptionSupportService.getPublicationTypeMap(titleMap.values().stream()
                .map(ProductPublicationTitleDO::getTypeId)
                .collect(Collectors.toSet()));
        Map<Long, ProductPublicationPublisherDO> publisherMap = subscriptionSupportService.getPublicationPublisherMap(titleMap.values().stream()
                .map(ProductPublicationTitleDO::getPublisherId)
                .collect(Collectors.toSet()));
        Map<Long, List<SubscriptionWindowSpuGradeDO>> gradeMap = getGradeDOMap(windowSpuIds);
        Map<Long, GradeCatalogDO> gradeCatalogMap = subscriptionSupportService.getGradeCatalogMap(gradeMap.values().stream()
                .flatMap(List::stream)
                .map(SubscriptionWindowSpuGradeDO::getGradeCatalogId)
                .collect(Collectors.toSet()));
        Map<Long, List<SubscriptionWindowSkuDO>> windowSkuMap =
                CollectionUtils.convertMultiMap(subscriptionWindowSkuMapper.selectListByWindowSpuIds(windowSpuIds),
                        SubscriptionWindowSkuDO::getWindowSpuId);

        return windowSpus.stream()
                .map(windowSpu -> buildWindowSpuResp(windowSpu, productSpuMap.get(windowSpu.getProductSpuId()),
                        categoryMap, spuPublicationMap.get(windowSpu.getProductSpuId()), titleMap, typeMap, publisherMap,
                        gradeMap.getOrDefault(windowSpu.getId(), Collections.emptyList()), gradeCatalogMap,
                        windowSkuMap.getOrDefault(windowSpu.getId(), Collections.emptyList())))
                .toList();
    }

    private SubscriptionWindowSpuRespVO buildWindowSpuResp(SubscriptionWindowSpuDO windowSpu,
                                                           ProductSpuDO productSpu,
                                                           Map<Long, ProductCategoryDO> categoryMap,
                                                           ProductSpuPublicationDO spuPublication,
                                                           Map<Long, ProductPublicationTitleDO> titleMap,
                                                           Map<Long, ProductPublicationTypeDO> typeMap,
                                                           Map<Long, ProductPublicationPublisherDO> publisherMap,
                                                           List<SubscriptionWindowSpuGradeDO> grades,
                                                           Map<Long, GradeCatalogDO> gradeCatalogMap,
                                                           List<SubscriptionWindowSkuDO> windowSkus) {
        SubscriptionWindowSpuRespVO respVO = new SubscriptionWindowSpuRespVO();
        respVO.setId(windowSpu.getId());
        respVO.setWindowId(windowSpu.getWindowId());
        respVO.setProductSpuId(windowSpu.getProductSpuId());
        respVO.setRecommendFlag(Boolean.TRUE.equals(windowSpu.getRecommendFlag()));
        respVO.setSort(windowSpu.getSort());
        respVO.setRemark(windowSpu.getRemark());
        respVO.setCreateTime(windowSpu.getCreateTime());
        if (productSpu != null) {
            respVO.setProductName(productSpu.getName());
            respVO.setCategoryId(productSpu.getCategoryId());
            ProductCategoryDO category = categoryMap.get(productSpu.getCategoryId());
            respVO.setCategoryName(category == null ? null : category.getName());
            respVO.setPicUrl(productSpu.getPicUrl());
            respVO.setPrice(productSpu.getPrice());
        }
        ProductPublicationTitleDO title = spuPublication == null ? null : titleMap.get(spuPublication.getPublicationTitleId());
        if (title != null) {
            respVO.setPublicationTitleId(title.getId());
            respVO.setPublicationTitleName(title.getName());
            ProductPublicationTypeDO type = typeMap.get(title.getTypeId());
            if (type != null) {
                respVO.setPublicationTypeId(type.getId());
                respVO.setPublicationTypeName(type.getName());
            }
            ProductPublicationPublisherDO publisher = publisherMap.get(title.getPublisherId());
            if (publisher != null) {
                respVO.setPublisherId(publisher.getId());
                respVO.setPublisherName(publisher.getName());
            }
        }
        List<Long> gradeCatalogIds = grades.stream()
                .map(SubscriptionWindowSpuGradeDO::getGradeCatalogId)
                .distinct()
                .toList();
        respVO.setGradeCatalogIds(gradeCatalogIds);
        respVO.setGradeNames(joinGradeNames(gradeCatalogIds, gradeCatalogMap));
        respVO.setEnabledSkuCount((int) windowSkus.stream().filter(item -> CommonStatusEnum.isEnable(item.getStatus())).count());
        respVO.setTotalSkuCount(windowSkus.size());
        return respVO;
    }

    private SubscriptionWindowSpuAvailableRespVO buildAvailableResp(ProductSpuDO productSpu,
                                                                    ProductCategoryDO category,
                                                                    ProductSpuPublicationDO spuPublication,
                                                                    Map<Long, ProductPublicationTitleDO> titleMap,
                                                                    Map<Long, ProductPublicationTypeDO> typeMap,
                                                                    Map<Long, ProductPublicationPublisherDO> publisherMap,
                                                                    List<ProductSpuGradeDO> spuGrades,
                                                                    Map<Long, GradeCatalogDO> gradeCatalogMap) {
        SubscriptionWindowSpuAvailableRespVO respVO = new SubscriptionWindowSpuAvailableRespVO();
        respVO.setProductSpuId(productSpu.getId());
        respVO.setProductName(productSpu.getName());
        respVO.setCategoryId(productSpu.getCategoryId());
        respVO.setCategoryName(category == null ? null : category.getName());
        respVO.setPicUrl(productSpu.getPicUrl());
        ProductPublicationTitleDO title = spuPublication == null ? null : titleMap.get(spuPublication.getPublicationTitleId());
        if (title != null) {
            respVO.setPublicationTitleId(title.getId());
            respVO.setPublicationTitleName(title.getName());
            ProductPublicationTypeDO type = typeMap.get(title.getTypeId());
            if (type != null) {
                respVO.setPublicationTypeId(type.getId());
                respVO.setPublicationTypeName(type.getName());
            }
            ProductPublicationPublisherDO publisher = publisherMap.get(title.getPublisherId());
            if (publisher != null) {
                respVO.setPublisherId(publisher.getId());
                respVO.setPublisherName(publisher.getName());
            }
        }
        respVO.setApplicableGradeNames(joinGradeNames(spuGrades.stream()
                .map(ProductSpuGradeDO::getGradeCatalogId)
                .distinct()
                .toList(), gradeCatalogMap));
        return respVO;
    }

    private String joinGradeNames(List<Long> gradeCatalogIds, Map<Long, GradeCatalogDO> gradeCatalogMap) {
        return gradeCatalogIds.stream()
                .map(gradeCatalogMap::get)
                .filter(Objects::nonNull)
                .map(grade -> grade.getGradeNo() + "/" + grade.getGradeName()
                        + (grade.getAliasName() == null ? "" : "（" + grade.getAliasName() + "）"))
                .collect(Collectors.joining("、"));
    }

    private <T> PageResult<T> paginate(List<T> records, Integer pageNo, Integer pageSize) {
        if (records.isEmpty()) {
            return PageResult.empty();
        }
        long total = records.size();
        int safePageNo = pageNo == null || pageNo <= 0 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize <= 0 ? records.size() : pageSize;
        int start = Math.max((safePageNo - 1) * safePageSize, 0);
        if (start >= records.size()) {
            return PageResult.empty(total);
        }
        int end = Math.min(start + safePageSize, records.size());
        return new PageResult<>(records.subList(start, end), total);
    }
}
