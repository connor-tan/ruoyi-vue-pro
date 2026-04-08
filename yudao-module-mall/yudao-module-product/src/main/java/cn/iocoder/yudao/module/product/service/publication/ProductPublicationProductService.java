package cn.iocoder.yudao.module.product.service.publication;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.GradeCatalogSimpleRespVO;
import cn.iocoder.yudao.module.edu.service.school.SchoolService;
import cn.iocoder.yudao.module.product.controller.admin.publicationproduct.vo.*;
import cn.iocoder.yudao.module.product.controller.admin.publicationtitle.vo.ProductPublicationTitleRespVO;
import cn.iocoder.yudao.module.product.controller.admin.publicationtitle.vo.ProductPublicationTitleSimpleRespVO;
import cn.iocoder.yudao.module.product.controller.admin.publicationspurelation.vo.ProductPublicationSpuRelationSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.publicationspugrade.vo.ProductPublicationSpuGradeSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuUpdateStatusReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.*;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.*;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.enums.publication.ProductDomainTypeEnum;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.product.service.category.ProductCategoryService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ProductPublicationProductService {

    @Resource
    private ProductSpuService productSpuService;
    @Resource
    private ProductSpuMapper productSpuMapper;
    @Resource
    private ProductSkuService productSkuService;
    @Resource
    private ProductCategoryService categoryService;
    @Resource
    private ProductPublicationTitleService publicationTitleService;
    @Resource
    private ProductPublicationTypeService publicationTypeService;
    @Resource
    private ProductPublicationPublisherService publicationPublisherService;
    @Resource
    private ProductPublicationSpuRelationService publicationSpuRelationService;
    @Resource
    private ProductPublicationSpuGradeService publicationSpuGradeService;
    @Resource
    private ProductSpuPublicationMapper productSpuPublicationMapper;
    @Resource
    private ProductSpuGradeMapper productSpuGradeMapper;
    @Resource
    private ProductSkuPublicationMapper productSkuPublicationMapper;
    @Resource
    private SchoolService schoolService;

    public PageResult<ProductPublicationProductRespVO> getPage(ProductPublicationProductPageReqVO reqVO) {
        Set<Long> filterSpuIds = null;
        if (reqVO.getPublicationTitleId() != null) {
            filterSpuIds = collectSpuIdsByTitleIds(Collections.singleton(reqVO.getPublicationTitleId()));
        }
        if (reqVO.getPublicationTypeId() != null) {
            filterSpuIds = intersect(filterSpuIds,
                    collectSpuIdsByTitleIds(publicationTitleService.getTitleIdsByTypeId(reqVO.getPublicationTypeId())));
        }
        if (reqVO.getPublisherId() != null) {
            filterSpuIds = intersect(filterSpuIds,
                    collectSpuIdsByTitleIds(publicationTitleService.getTitleIdsByPublisherId(reqVO.getPublisherId())));
        }
        if (reqVO.getGradeCatalogId() != null) {
            Set<Long> gradeSpuIds = CollectionUtils.convertSet(
                    productSpuGradeMapper.selectListByGradeCatalogIds(Collections.singleton(reqVO.getGradeCatalogId())),
                    ProductSpuGradeDO::getProductSpuId);
            filterSpuIds = intersect(filterSpuIds, gradeSpuIds);
        }
        if (filterSpuIds != null && filterSpuIds.isEmpty()) {
            return PageResult.empty();
        }

        LambdaQueryWrapperX<ProductSpuDO> queryWrapper = new LambdaQueryWrapperX<ProductSpuDO>()
                .eq(ProductSpuDO::getDomainType, ProductDomainTypeEnum.PUBLICATION.getCode())
                .likeIfPresent(ProductSpuDO::getName, reqVO.getName())
                .eqIfPresent(ProductSpuDO::getCategoryId, reqVO.getCategoryId())
                .betweenIfPresent(ProductSpuDO::getCreateTime, reqVO.getCreateTime())
                .inIfPresent(ProductSpuDO::getId, filterSpuIds)
                .orderByDesc(ProductSpuDO::getId);
        ProductSpuMapper.appendTabQuery(reqVO.getTabType(), queryWrapper);
        PageResult<ProductSpuDO> pageResult = productSpuMapper.selectPage(reqVO, queryWrapper);
        return new PageResult<>(buildRespList(pageResult.getList()), pageResult.getTotal());
    }

    public Map<Integer, Long> getTabsCount() {
        return productSpuService.getTabsCount(ProductDomainTypeEnum.PUBLICATION.getCode());
    }

    public ProductPublicationProductRespVO get(Long id) {
        ProductSpuDO spu = validatePublicationSpu(id);
        List<ProductSkuDO> skus = productSkuService.getSkuListBySpuId(id);
        ProductSpuPublicationDO spuPublication = productSpuPublicationMapper.selectByProductSpuId(id);
        List<Long> gradeCatalogIds = publicationSpuGradeService.getGradeCatalogIds(id);
        return buildResp(spu, skus, spuPublication, gradeCatalogIds,
                buildPublicationTitleContext(spuPublication == null
                        ? Collections.emptySet() : Collections.singleton(spuPublication.getPublicationTitleId())),
                buildGradeCatalogMap(),
                buildSkuPublicationMap(skus));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(ProductPublicationProductSaveReqVO reqVO) {
        validatePublicationReq(reqVO);
        Long spuId = productSpuService.createSpu(buildSpuSaveReq(reqVO, null));
        ProductSpuUpdateStatusReqVO updateStatusReqVO = new ProductSpuUpdateStatusReqVO();
        updateStatusReqVO.setId(spuId);
        updateStatusReqVO.setStatus(ProductSpuStatusEnum.ENABLE.getStatus());
        productSpuService.updateSpuStatus(updateStatusReqVO);
        savePublicationExtension(spuId, reqVO);
        return spuId;
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(ProductPublicationProductSaveReqVO reqVO) {
        ProductSpuDO oldSpu = validatePublicationSpu(reqVO.getId());
        validatePublicationReq(reqVO);
        List<Long> oldSkuIds = CollectionUtils.convertList(productSkuService.getSkuListBySpuId(reqVO.getId()), ProductSkuDO::getId);
        if (CollUtil.isNotEmpty(oldSkuIds)) {
            productSkuPublicationMapper.deleteByProductSkuIds(oldSkuIds);
        }
        productSpuService.updateSpu(buildSpuSaveReq(reqVO, oldSpu));
        savePublicationExtension(reqVO.getId(), reqVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(ProductSpuUpdateStatusReqVO reqVO) {
        validatePublicationSpu(reqVO.getId());
        productSpuService.updateSpuStatus(reqVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        validatePublicationSpu(id);
        List<Long> skuIds = CollectionUtils.convertList(productSkuService.getSkuListBySpuId(id), ProductSkuDO::getId);
        productSpuService.deleteSpu(id);
        productSpuPublicationMapper.deleteById(id);
        productSpuGradeMapper.deleteByProductSpuId(id);
        if (CollUtil.isNotEmpty(skuIds)) {
            productSkuPublicationMapper.deleteByProductSkuIds(skuIds);
        }
    }

    private ProductSpuSaveReqVO buildSpuSaveReq(ProductPublicationProductSaveReqVO reqVO, ProductSpuDO oldSpu) {
        ProductSpuSaveReqVO saveReqVO = new ProductSpuSaveReqVO();
        saveReqVO.setId(reqVO.getId());
        saveReqVO.setName(reqVO.getName());
        saveReqVO.setKeyword(reqVO.getKeyword());
        saveReqVO.setIntroduction(reqVO.getIntroduction());
        saveReqVO.setDescription(reqVO.getDescription());
        saveReqVO.setCategoryId(reqVO.getCategoryId());
        saveReqVO.setBrandId(reqVO.getBrandId());
        saveReqVO.setPicUrl(reqVO.getPicUrl());
        saveReqVO.setSliderPicUrls(reqVO.getSliderPicUrls());
        saveReqVO.setSort(reqVO.getSort());
        saveReqVO.setDomainType(ProductDomainTypeEnum.PUBLICATION.getCode());
        saveReqVO.setSpecType(reqVO.getSpecType());
        saveReqVO.setDeliveryTypes(reqVO.getDeliveryTypes());
        saveReqVO.setDeliveryTemplateId(reqVO.getDeliveryTemplateId());
        saveReqVO.setGiveIntegral(reqVO.getGiveIntegral());
        saveReqVO.setSubCommissionType(reqVO.getSubCommissionType());
        saveReqVO.setVirtualSalesCount(reqVO.getVirtualSalesCount());
        saveReqVO.setSalesCount(oldSpu == null ? reqVO.getSalesCount() : oldSpu.getSalesCount());
        saveReqVO.setBrowseCount(oldSpu == null ? reqVO.getBrowseCount() : oldSpu.getBrowseCount());
        saveReqVO.setSkus(CollectionUtils.convertList(reqVO.getSkus(), this::buildSkuSaveReq));
        return saveReqVO;
    }

    private ProductSkuSaveReqVO buildSkuSaveReq(ProductPublicationProductSkuSaveReqVO sku) {
        ProductSkuSaveReqVO saveReqVO = new ProductSkuSaveReqVO();
        saveReqVO.setName(sku.getName());
        saveReqVO.setPrice(sku.getPrice());
        saveReqVO.setMarketPrice(sku.getMarketPrice());
        saveReqVO.setCostPrice(sku.getCostPrice());
        saveReqVO.setBarCode(sku.getBarCode());
        saveReqVO.setPicUrl(sku.getPicUrl());
        saveReqVO.setStock(sku.getStock());
        saveReqVO.setWeight(sku.getWeight());
        saveReqVO.setVolume(sku.getVolume());
        saveReqVO.setFirstBrokeragePrice(sku.getFirstBrokeragePrice());
        saveReqVO.setSecondBrokeragePrice(sku.getSecondBrokeragePrice());
        saveReqVO.setProperties(sku.getProperties());
        return saveReqVO;
    }

    private void savePublicationExtension(Long spuId, ProductPublicationProductSaveReqVO reqVO) {
        ProductPublicationSpuRelationSaveReqVO relationSaveReqVO = new ProductPublicationSpuRelationSaveReqVO();
        relationSaveReqVO.setProductSpuId(spuId);
        relationSaveReqVO.setPublicationTitleId(reqVO.getPublicationTitleId());
        publicationSpuRelationService.createOrUpdate(relationSaveReqVO);
        ProductPublicationSpuGradeSaveReqVO spuGradeSaveReqVO = new ProductPublicationSpuGradeSaveReqVO();
        spuGradeSaveReqVO.setProductSpuId(spuId);
        spuGradeSaveReqVO.setGradeCatalogIds(reqVO.getApplicableGradeCatalogIds());
        publicationSpuGradeService.createOrUpdate(spuGradeSaveReqVO);

        List<ProductSkuDO> skuDOs = productSkuService.getSkuListBySpuId(spuId);
        Map<Long, ProductSkuDO> existingSkuIdMap = CollectionUtils.convertMap(skuDOs, ProductSkuDO::getId);
        Map<String, ProductSkuDO> existingPropertyMap = skuDOs.stream()
                .collect(Collectors.toMap(this::buildPropertySignature, Function.identity(), (item1, item2) -> item1));
        List<ProductSkuPublicationDO> skuPublications = new ArrayList<>();
        for (ProductPublicationProductSkuSaveReqVO skuReqVO : reqVO.getSkus()) {
            ProductSkuDO skuDO = skuReqVO.getSkuId() != null ? existingSkuIdMap.get(skuReqVO.getSkuId()) : null;
            if (skuDO == null) {
                skuDO = existingPropertyMap.get(buildPropertySignature(skuReqVO.getProperties()));
            }
            if (skuDO == null) {
                continue;
            }
            skuPublications.add(ProductSkuPublicationDO.builder()
                    .productSkuId(skuDO.getId())
                    .volumeLabel(skuReqVO.getVolumeLabel())
                    .editionLabel(skuReqVO.getEditionLabel())
                    .isbn(skuReqVO.getIsbn())
                    .remark(skuReqVO.getRemark())
                    .build());
        }
        if (!skuPublications.isEmpty()) {
            productSkuPublicationMapper.insertBatch(skuPublications);
        }
    }

    private void validatePublicationReq(ProductPublicationProductSaveReqVO reqVO) {
        categoryService.validateCategoryForDomain(reqVO.getCategoryId(), ProductDomainTypeEnum.PUBLICATION.getCode());
        if (reqVO.getPublicationTitleId() == null) {
            throw exception(PUBLICATION_PRODUCT_TITLE_REQUIRED);
        }
        if (CollUtil.isEmpty(reqVO.getApplicableGradeCatalogIds())) {
            throw exception(PUBLICATION_PRODUCT_GRADE_REQUIRED);
        }
        if (CollUtil.isEmpty(reqVO.getSkus())) {
            throw exception(PUBLICATION_PRODUCT_SKU_REQUIRED);
        }
        ProductPublicationTitleDO title = publicationTitleService.validateExists(reqVO.getPublicationTitleId());
        if (publicationTitleService.requiresPeriodicalIdentifier(title.getTypeId())) {
            ProductPublicationTitleRespVO titleResp = publicationTitleService.get(title.getId());
            if (isAllBlank(titleResp.getIssn(), titleResp.getCnCode(), titleResp.getPostDistributionCode())) {
                throw exception(PUBLICATION_PRODUCT_IDENTIFIER_REQUIRED);
            }
        }
        ProductPublicationTypeDO type = publicationTypeService.validateExists(title.getTypeId());
        if ("BOOK".equalsIgnoreCase(type.getCode())) {
            boolean missingIsbn = reqVO.getSkus().stream().anyMatch(item -> item.getIsbn() == null || item.getIsbn().isBlank());
            if (missingIsbn) {
                throw exception(PUBLICATION_PRODUCT_ISBN_REQUIRED);
            }
        }
    }

    private ProductSpuDO validatePublicationSpu(Long spuId) {
        ProductSpuDO spu = productSpuService.getSpu(spuId);
        if (spu == null || !ProductDomainTypeEnum.isPublication(spu.getDomainType())) {
            throw exception(PUBLICATION_PRODUCT_NOT_EXISTS);
        }
        return spu;
    }

    private boolean isAllBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private List<ProductPublicationProductRespVO> buildRespList(List<ProductSpuDO> spus) {
        if (spus == null || spus.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> spuIds = CollectionUtils.convertSet(spus, ProductSpuDO::getId);
        Map<Long, List<ProductSkuDO>> skuMap = CollectionUtils.convertMultiMap(productSkuService.getSkuListBySpuId(spuIds), ProductSkuDO::getSpuId);
        List<ProductSpuPublicationDO> spuPublications = productSpuPublicationMapper.selectListByProductSpuIds(spuIds);
        Map<Long, ProductSpuPublicationDO> spuPublicationMap = CollectionUtils.convertMap(
                spuPublications, ProductSpuPublicationDO::getProductSpuId);
        Map<Long, List<Long>> gradeIdsMap = CollectionUtils.convertMultiMap(
                productSpuGradeMapper.selectListByProductSpuIds(spuIds), ProductSpuGradeDO::getProductSpuId, ProductSpuGradeDO::getGradeCatalogId);
        Map<Long, PublicationTitleContext> titleContextMap = buildPublicationTitleContext(
                CollectionUtils.convertSet(spuPublications, ProductSpuPublicationDO::getPublicationTitleId));
        Map<Long, GradeCatalogSimpleRespVO> gradeMap = buildGradeCatalogMap();
        List<ProductSkuDO> allSkus = skuMap.values().stream().flatMap(Collection::stream).toList();
        Map<Long, ProductSkuPublicationDO> skuPublicationMap = buildSkuPublicationMap(allSkus);
        return CollectionUtils.convertList(spus, spu -> buildResp(spu, skuMap.getOrDefault(spu.getId(), Collections.emptyList()),
                spuPublicationMap.get(spu.getId()), gradeIdsMap.getOrDefault(spu.getId(), Collections.emptyList()),
                titleContextMap, gradeMap, skuPublicationMap));
    }

    private ProductPublicationProductRespVO buildResp(ProductSpuDO spu, List<ProductSkuDO> skus,
                                                      ProductSpuPublicationDO spuPublication, List<Long> gradeCatalogIds,
                                                      Map<Long, PublicationTitleContext> titleContextMap,
                                                      Map<Long, GradeCatalogSimpleRespVO> gradeMap,
                                                      Map<Long, ProductSkuPublicationDO> skuPublicationMap) {
        ProductPublicationProductRespVO respVO = BeanUtils.toBean(spu, ProductPublicationProductRespVO.class);
        if (spuPublication != null) {
            PublicationTitleContext titleContext = titleContextMap.get(spuPublication.getPublicationTitleId());
            if (titleContext != null) {
                fillTitleFields(respVO, titleContext);
            }
        }
        respVO.setApplicableGradeCatalogIds(gradeCatalogIds);
        respVO.setApplicableGradeNames(CollectionUtils.convertList(gradeCatalogIds,
                item -> gradeMap.get(item) == null ? null : gradeMap.get(item).getGradeName()));
        respVO.setSkus(CollectionUtils.convertList(skus, sku -> {
            ProductPublicationProductSkuRespVO skuRespVO = BeanUtils.toBean(sku, ProductPublicationProductSkuRespVO.class);
            skuRespVO.setSkuId(sku.getId());
            ProductSkuPublicationDO skuPublication = skuPublicationMap.get(sku.getId());
            if (skuPublication != null) {
                skuRespVO.setVolumeLabel(skuPublication.getVolumeLabel());
                skuRespVO.setEditionLabel(skuPublication.getEditionLabel());
                skuRespVO.setIsbn(skuPublication.getIsbn());
                skuRespVO.setRemark(skuPublication.getRemark());
            }
            return skuRespVO;
        }));
        return respVO;
    }

    private Map<Long, GradeCatalogSimpleRespVO> buildGradeCatalogMap() {
        return CollectionUtils.convertMap(schoolService.getGradeCatalogList(), GradeCatalogSimpleRespVO::getId);
    }

    private Map<Long, ProductSkuPublicationDO> buildSkuPublicationMap(Collection<ProductSkuDO> skus) {
        Set<Long> skuIds = CollectionUtils.convertSet(skus, ProductSkuDO::getId);
        if (CollUtil.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(productSkuPublicationMapper.selectListByProductSkuIds(skuIds),
                ProductSkuPublicationDO::getProductSkuId);
    }

    private Map<Long, PublicationTitleContext> buildPublicationTitleContext(Set<Long> titleIds) {
        if (CollUtil.isEmpty(titleIds)) {
            return Collections.emptyMap();
        }
        Map<Long, ProductPublicationTitleDO> titleMap = publicationTitleService.getTitleMap(titleIds);
        Map<Long, ProductPublicationTitleIdentifierDO> identifierMap = publicationTitleService.getIdentifierMap(titleIds);
        Map<Long, ProductPublicationTypeDO> typeMap = publicationTypeService.getSimpleList().stream()
                .map(item -> ProductPublicationTypeDO.builder().id(item.getId()).code(item.getCode()).name(item.getName()).build())
                .collect(Collectors.toMap(ProductPublicationTypeDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, ProductPublicationPublisherDO> publisherMap = publicationPublisherService.getSimpleList().stream()
                .map(item -> ProductPublicationPublisherDO.builder().id(item.getId()).code(item.getCode()).name(item.getName()).build())
                .collect(Collectors.toMap(ProductPublicationPublisherDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, PublicationTitleContext> result = new HashMap<>(titleMap.size());
        titleMap.forEach((titleId, title) -> result.put(titleId, new PublicationTitleContext(
                title,
                identifierMap.get(titleId),
                typeMap.get(title.getTypeId()),
                publisherMap.get(title.getPublisherId()))));
        return result;
    }

    private void fillTitleFields(ProductPublicationProductRespVO respVO, PublicationTitleContext titleContext) {
        ProductPublicationTitleDO title = titleContext.title();
        respVO.setPublicationTitleId(title.getId());
        respVO.setPublicationTitleName(title.getName());
        respVO.setPublicationTypeId(title.getTypeId());
        if (titleContext.type() != null) {
            respVO.setPublicationTypeCode(titleContext.type().getCode());
            respVO.setPublicationTypeName(titleContext.type().getName());
        }
        respVO.setPublisherId(title.getPublisherId());
        if (titleContext.publisher() != null) {
            respVO.setPublisherName(titleContext.publisher().getName());
        }
        respVO.setIssueCycle(title.getIssueCycle());
        if (titleContext.identifier() != null) {
            respVO.setIssn(titleContext.identifier().getIssn());
            respVO.setCnCode(titleContext.identifier().getCnCode());
            respVO.setPostDistributionCode(titleContext.identifier().getPostDistributionCode());
        }
    }

    private Set<Long> collectSpuIdsByTitleIds(Set<Long> titleIds) {
        if (titleIds == null || titleIds.isEmpty()) {
            return Collections.emptySet();
        }
        return CollectionUtils.convertSet(productSpuPublicationMapper.selectListByPublicationTitleIds(titleIds),
                ProductSpuPublicationDO::getProductSpuId);
    }

    private Set<Long> intersect(Set<Long> current, Set<Long> next) {
        if (current == null) {
            return next;
        }
        if (current.isEmpty() || next.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> result = new HashSet<>(current);
        result.retainAll(next);
        return result;
    }

    private String buildPropertySignature(ProductPublicationProductSkuSaveReqVO skuReqVO) {
        return buildPropertySignature(skuReqVO.getProperties());
    }

    private String buildPropertySignature(List<ProductSkuSaveReqVO.Property> properties) {
        if (properties == null || properties.isEmpty()) {
            return "[]";
        }
        return properties.stream()
                .sorted(Comparator.comparing(ProductSkuSaveReqVO.Property::getPropertyId))
                .map(item -> item.getPropertyId() + ":" + item.getValueId())
                .collect(Collectors.joining("|"));
    }

    private String buildPropertySignature(ProductSkuDO skuDO) {
        if (skuDO.getProperties() == null || skuDO.getProperties().isEmpty()) {
            return "[]";
        }
        return skuDO.getProperties().stream()
                .sorted(Comparator.comparing(ProductSkuDO.Property::getPropertyId))
                .map(item -> item.getPropertyId() + ":" + item.getValueId())
                .collect(Collectors.joining("|"));
    }

    private record PublicationTitleContext(ProductPublicationTitleDO title,
                                           ProductPublicationTitleIdentifierDO identifier,
                                           ProductPublicationTypeDO type,
                                           ProductPublicationPublisherDO publisher) {
    }
}
