package cn.iocoder.yudao.module.product.service.publication;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.api.gradecatalog.EduGradeCatalogApi;
import cn.iocoder.yudao.module.edu.api.gradecatalog.dto.EduGradeCatalogRespDTO;
import cn.iocoder.yudao.module.edu.api.publication.EduPublicationPublisherApi;
import cn.iocoder.yudao.module.edu.api.publication.EduPublicationTypeApi;
import cn.iocoder.yudao.module.edu.api.publication.dto.EduPublicationPublisherRespDTO;
import cn.iocoder.yudao.module.edu.api.publication.dto.EduPublicationTypeRespDTO;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationQueryReqDTO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSkuExtDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSkuGradeRelDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSpuExtDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSkuExtMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSkuGradeRelMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSpuExtMapper;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.service.category.ProductCategoryService;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.publication.api.enums.PublicationFulfillmentModeEnum;
import cn.iocoder.yudao.module.publication.api.enums.PublicationIdentifierRuleEnum;
import cn.iocoder.yudao.module.publication.api.enums.PublicationTargetPeriodEnum;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

@Service
public class ProductPublicationServiceImpl implements ProductPublicationService {

    private static final String DICT_TYPE_PUBLICATION_VOLUME = "edu_publication_volume";
    private static final String DICT_TYPE_PUBLICATION_EDITION = "edu_publication_edition";

    @Resource
    private ProductSpuMapper productSpuMapper;
    @Resource
    private ProductSkuMapper productSkuMapper;
    @Resource
    private ProductCategoryService productCategoryService;
    @Resource
    private EduPublicationPublisherApi publicationPublisherApi;
    @Resource
    private EduPublicationTypeApi publicationTypeApi;
    @Resource
    private ProductPublicationSpuExtMapper publicationSpuExtMapper;
    @Resource
    private ProductPublicationSkuExtMapper publicationSkuExtMapper;
    @Resource
    private ProductPublicationSkuGradeRelMapper publicationSkuGradeRelMapper;
    @Resource
    private EduGradeCatalogApi gradeCatalogApi;
    @Resource
    private DictDataApi dictDataApi;

    @Override
    public ProductPublicationRespDTO getPublication(Long spuId) {
        if (spuId == null) {
            return null;
        }
        List<ProductPublicationRespDTO> publications = getPublicationList(Collections.singleton(spuId));
        return CollUtil.isEmpty(publications) ? null : publications.get(0);
    }

    @Override
    public List<ProductPublicationRespDTO> getPublicationList(Collection<Long> spuIds) {
        if (CollUtil.isEmpty(spuIds)) {
            return Collections.emptyList();
        }
        List<ProductSpuDO> spus = productSpuMapper.selectByIds(spuIds);
        List<ProductSpuDO> publicationSpus = convertList(spus, item -> {
            ProductCategoryDO category = productCategoryService.getCategory(item.getCategoryId());
            return category != null && BizSceneEnum.isPublication(category.getBizScene()) ? item : null;
        }).stream().filter(Objects::nonNull).toList();
        if (CollUtil.isEmpty(publicationSpus)) {
            return Collections.emptyList();
        }

        Map<Long, ProductCategoryDO> categoryMap = convertMap(
                convertList(publicationSpus, spu -> productCategoryService.getCategory(spu.getCategoryId())),
                ProductCategoryDO::getId);
        Map<Long, ProductPublicationSpuExtDO> spuExtMap = convertMap(
                publicationSpuExtMapper.selectByIds(convertSet(publicationSpus, ProductSpuDO::getId)),
                ProductPublicationSpuExtDO::getSpuId);
        Set<Long> publisherIds = convertSet(spuExtMap.values(), ProductPublicationSpuExtDO::getPublisherId);
        Set<Long> publicationTypeIds = convertSet(spuExtMap.values(), ProductPublicationSpuExtDO::getPublicationTypeId);
        Map<Long, EduPublicationPublisherRespDTO> publisherMap = publicationPublisherApi.getPublicationPublisherMap(publisherIds);
        Map<Long, EduPublicationTypeRespDTO> publicationTypeMap = publicationTypeApi.getPublicationTypeMap(publicationTypeIds);

        List<ProductSkuDO> skuList = productSkuMapper.selectListBySpuId(convertSet(publicationSpus, ProductSpuDO::getId));
        Map<Long, List<ProductSkuDO>> skuMap = convertMultiMap(skuList, ProductSkuDO::getSpuId);
        Set<Long> skuIds = convertSet(skuList, ProductSkuDO::getId);
        Map<Long, ProductPublicationSkuExtDO> skuExtMap = convertMap(
                publicationSkuExtMapper.selectListBySkuIds(skuIds), ProductPublicationSkuExtDO::getSkuId);
        Map<Long, List<ProductPublicationSkuGradeRelDO>> skuGradeMap = convertMultiMap(
                publicationSkuGradeRelMapper.selectListBySkuIds(skuIds), ProductPublicationSkuGradeRelDO::getSkuId);
        Set<Long> gradeCatalogIds = new LinkedHashSet<>();
        skuGradeMap.values().forEach(items -> items.forEach(item -> gradeCatalogIds.add(item.getGradeCatalogId())));
        Map<Long, EduGradeCatalogRespDTO> gradeCatalogMap = gradeCatalogApi.getGradeCatalogMap(gradeCatalogIds);

        return convertList(publicationSpus, spu -> buildPublicationResp(spu, categoryMap, spuExtMap, publisherMap,
                publicationTypeMap, skuMap, skuExtMap, skuGradeMap, gradeCatalogMap));
    }

    @Override
    public List<ProductPublicationRespDTO> getPublicationList(ProductPublicationQueryReqDTO reqDTO) {
        List<Long> spuIds = publicationSpuExtMapper.selectSpuIdsByQuery(reqDTO);
        if (CollUtil.isEmpty(spuIds)) {
            return Collections.emptyList();
        }
        return getPublicationList(spuIds);
    }

    private ProductPublicationRespDTO buildPublicationResp(
            ProductSpuDO spu,
            Map<Long, ProductCategoryDO> categoryMap,
            Map<Long, ProductPublicationSpuExtDO> spuExtMap,
            Map<Long, EduPublicationPublisherRespDTO> publisherMap,
            Map<Long, EduPublicationTypeRespDTO> publicationTypeMap,
            Map<Long, List<ProductSkuDO>> skuMap,
            Map<Long, ProductPublicationSkuExtDO> skuExtMap,
            Map<Long, List<ProductPublicationSkuGradeRelDO>> skuGradeMap,
            Map<Long, EduGradeCatalogRespDTO> gradeCatalogMap) {
        ProductPublicationRespDTO dto = BeanUtils.toBean(spu, ProductPublicationRespDTO.class);
        ProductCategoryDO category = categoryMap.get(spu.getCategoryId());
        dto.setBizScene(category == null ? null : category.getBizScene());
        dto.setCategoryName(category == null ? null : category.getName());

        ProductPublicationSpuExtDO spuExt = spuExtMap.get(spu.getId());
        if (spuExt != null) {
            ProductPublicationRespDTO.PublicationSpuExtDTO spuExtDTO =
                    BeanUtils.toBean(spuExt, ProductPublicationRespDTO.PublicationSpuExtDTO.class);
            EduPublicationPublisherRespDTO publisher = publisherMap.get(spuExt.getPublisherId());
            EduPublicationTypeRespDTO publicationType = publicationTypeMap.get(spuExt.getPublicationTypeId());
            spuExtDTO.setPublisherName(publisher == null ? null : publisher.getName());
            spuExtDTO.setPublicationTypeName(publicationType == null ? null : publicationType.getName());
            spuExtDTO.setPublicationTypeIdentifierRule(publicationType == null ? null : publicationType.getIdentifierRule());
            dto.setPublicationExt(spuExtDTO);
        }

        dto.setSkus(convertList(skuMap.get(spu.getId()), sku -> buildPublicationSkuResp(sku, skuExtMap, skuGradeMap,
                gradeCatalogMap)));
        return dto;
    }

    private ProductPublicationRespDTO.PublicationSkuDTO buildPublicationSkuResp(
            ProductSkuDO sku,
            Map<Long, ProductPublicationSkuExtDO> skuExtMap,
            Map<Long, List<ProductPublicationSkuGradeRelDO>> skuGradeMap,
            Map<Long, EduGradeCatalogRespDTO> gradeCatalogMap) {
        ProductPublicationRespDTO.PublicationSkuDTO skuDTO =
                BeanUtils.toBean(sku, ProductPublicationRespDTO.PublicationSkuDTO.class);
        ProductPublicationSkuExtDO skuExt = skuExtMap.get(sku.getId());
        if (skuExt != null) {
            skuDTO.setPublicationExt(BeanUtils.toBean(skuExt, ProductPublicationRespDTO.PublicationSkuExtDTO.class));
        }
        List<ProductPublicationSkuGradeRelDO> relList = skuGradeMap.get(sku.getId());
        if (CollUtil.isNotEmpty(relList)) {
            skuDTO.setApplicableGradeCatalogIds(convertList(relList, ProductPublicationSkuGradeRelDO::getGradeCatalogId));
            skuDTO.setApplicableGradeNames(convertList(relList, rel -> {
                EduGradeCatalogRespDTO gradeCatalog = gradeCatalogMap.get(rel.getGradeCatalogId());
                return gradeCatalog == null ? null : gradeCatalog.getGradeName();
            }));
        }
        return skuDTO;
    }

    @Override
    public void validatePublicationSaveReq(ProductSpuSaveReqVO reqVO) {
        if (reqVO.getPublicationExt() == null) {
            throw exception(PUBLICATION_EXT_REQUIRED);
        }
        ProductSpuSaveReqVO.PublicationSpuExtSaveReqVO ext = reqVO.getPublicationExt();
        if (ext.getPublisherId() == null) {
            throw exception(PUBLICATION_PUBLISHER_REQUIRED);
        }
        if (ext.getPublicationTypeId() == null) {
            throw exception(PUBLICATION_TYPE_REQUIRED);
        }
        if (StrUtil.isBlank(ext.getIssueCycle())) {
            throw exception(PUBLICATION_ISSUE_CYCLE_REQUIRED);
        }
        EduPublicationPublisherRespDTO publisher = getEnabledPublicationPublisher(ext.getPublisherId());
        EduPublicationTypeRespDTO type = getEnabledPublicationType(ext.getPublicationTypeId());
        List<ProductSkuSaveReqVO> skus = reqVO.getSkus();
        if (CollUtil.isEmpty(skus)) {
            throw exception(PUBLICATION_SKU_REQUIRED);
        }
        validateGradeCatalogIds(skus);
        Set<String> volumeLabels = new LinkedHashSet<>();
        Set<String> editionLabels = new LinkedHashSet<>();
        for (ProductSkuSaveReqVO sku : skus) {
            if (sku.getPublicationExt() == null) {
                throw exception(PUBLICATION_SKU_EXT_REQUIRED);
            }
            if (CollUtil.isEmpty(sku.getApplicableGradeCatalogIds())) {
                throw exception(PUBLICATION_SKU_GRADE_REQUIRED);
            }
            sku.getPublicationExt().setTargetPeriod(PublicationTargetPeriodEnum.normalize(sku.getPublicationExt().getTargetPeriod()));
            if (StrUtil.isNotBlank(sku.getPublicationExt().getVolumeLabel())) {
                volumeLabels.add(sku.getPublicationExt().getVolumeLabel());
            }
            if (StrUtil.isNotBlank(sku.getPublicationExt().getEditionLabel())) {
                editionLabels.add(sku.getPublicationExt().getEditionLabel());
            }
            if (PublicationIdentifierRuleEnum.requiresSkuIsbn(type.getIdentifierRule())
                    && StrUtil.isBlank(sku.getPublicationExt().getIsbn())) {
                throw exception(PUBLICATION_SKU_ISBN_REQUIRED);
            }
        }
        validatePublicationSkuDictValues(volumeLabels, editionLabels);
        if (PublicationIdentifierRuleEnum.requiresTitleIdentifier(type.getIdentifierRule())
                && StrUtil.isAllBlank(ext.getIssn(), ext.getCnCode(), ext.getPostDistributionCode())) {
            throw exception(PUBLICATION_TITLE_IDENTIFIER_REQUIRED);
        }
        ext.setFulfillmentMode(PublicationFulfillmentModeEnum.normalize(ext.getFulfillmentMode()));
        reqVO.setBrandId(null);
        reqVO.setGiveIntegral(reqVO.getGiveIntegral() == null ? 0 : reqVO.getGiveIntegral());
        reqVO.setSubCommissionType(Boolean.FALSE);
    }

    private void validatePublicationSkuDictValues(Set<String> volumeLabels, Set<String> editionLabels) {
        if (CollUtil.isNotEmpty(volumeLabels)) {
            dictDataApi.validateDictDataList(DICT_TYPE_PUBLICATION_VOLUME, volumeLabels);
        }
        if (CollUtil.isNotEmpty(editionLabels)) {
            dictDataApi.validateDictDataList(DICT_TYPE_PUBLICATION_EDITION, editionLabels);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void savePublication(Long spuId, ProductSpuSaveReqVO reqVO, List<ProductSkuDO> savedSkus,
                                Collection<Long> cleanupSkuIds) {
        ProductSpuSaveReqVO.PublicationSpuExtSaveReqVO extReq = reqVO.getPublicationExt();
        ProductPublicationSpuExtDO extDO = BeanUtils.toBean(extReq, ProductPublicationSpuExtDO.class);
        extDO.setSpuId(spuId);
        publicationSpuExtMapper.upsert(extDO);

        if (CollUtil.isNotEmpty(cleanupSkuIds)) {
            publicationSkuExtMapper.deleteBySkuIdsPhysically(cleanupSkuIds);
            publicationSkuGradeRelMapper.deleteBySkuIdsPhysically(cleanupSkuIds);
        }
        Map<Long, ProductSkuSaveReqVO> reqSkuMap = convertMap(reqVO.getSkus(), ProductSkuSaveReqVO::getId);
        for (int i = 0; i < savedSkus.size(); i++) {
            ProductSkuDO savedSku = savedSkus.get(i);
            ProductSkuSaveReqVO reqSku = reqSkuMap.get(savedSku.getId());
            if (reqSku == null && i < reqVO.getSkus().size()) {
                reqSku = reqVO.getSkus().get(i);
            }
            if (reqSku == null || reqSku.getPublicationExt() == null) {
                continue;
            }
            ProductPublicationSkuExtDO skuExtDO = BeanUtils.toBean(reqSku.getPublicationExt(), ProductPublicationSkuExtDO.class);
            skuExtDO.setSkuId(savedSku.getId());
            publicationSkuExtMapper.upsert(skuExtDO);

            List<ProductPublicationSkuGradeRelDO> gradeRelList = convertList(reqSku.getApplicableGradeCatalogIds(),
                    gradeCatalogId -> ProductPublicationSkuGradeRelDO.builder()
                            .skuId(savedSku.getId())
                            .gradeCatalogId(gradeCatalogId)
                            .build());
            if (CollUtil.isNotEmpty(gradeRelList)) {
                publicationSkuGradeRelMapper.insertBatch(gradeRelList);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void clearPublication(Long spuId, Collection<Long> skuIds) {
        publicationSpuExtMapper.deleteBySpuIdPhysically(spuId);
        if (CollUtil.isNotEmpty(skuIds)) {
            publicationSkuExtMapper.deleteBySkuIdsPhysically(skuIds);
            publicationSkuGradeRelMapper.deleteBySkuIdsPhysically(skuIds);
        }
    }

    @Override
    public void fillAdminDetail(ProductSpuRespVO respVO, List<ProductSkuRespVO> skuRespVOList) {
        ProductPublicationSpuExtDO extDO = publicationSpuExtMapper.selectById(respVO.getId());
        if (extDO == null) {
            return;
        }
        EduPublicationPublisherRespDTO publisher = getRequiredPublicationPublisher(extDO.getPublisherId());
        EduPublicationTypeRespDTO type = getRequiredPublicationType(extDO.getPublicationTypeId());
        ProductSpuRespVO.PublicationSpuExtRespVO extRespVO = BeanUtils.toBean(extDO, ProductSpuRespVO.PublicationSpuExtRespVO.class);
        extRespVO.setPublisherName(publisher.getName());
        extRespVO.setPublicationTypeName(type.getName());
        extRespVO.setPublicationTypeIdentifierRule(type.getIdentifierRule());
        respVO.setPublicationExt(extRespVO);

        if (CollUtil.isEmpty(skuRespVOList)) {
            return;
        }
        Set<Long> skuIds = convertSet(skuRespVOList, ProductSkuRespVO::getId);
        Map<Long, ProductPublicationSkuExtDO> skuExtMap = convertMap(
                publicationSkuExtMapper.selectListBySkuIds(skuIds), ProductPublicationSkuExtDO::getSkuId);
        Map<Long, List<ProductPublicationSkuGradeRelDO>> gradeRelMap = convertMultiMap(
                publicationSkuGradeRelMapper.selectListBySkuIds(skuIds), ProductPublicationSkuGradeRelDO::getSkuId);
        Set<Long> gradeCatalogIds = new LinkedHashSet<>();
        gradeRelMap.values().forEach(relList -> relList.forEach(rel -> gradeCatalogIds.add(rel.getGradeCatalogId())));
        Map<Long, EduGradeCatalogRespDTO> gradeCatalogMap = gradeCatalogApi.getGradeCatalogMap(gradeCatalogIds);
        skuRespVOList.forEach(sku -> {
            ProductPublicationSkuExtDO skuExtDO = skuExtMap.get(sku.getId());
            if (skuExtDO != null) {
                sku.setPublicationExt(BeanUtils.toBean(skuExtDO, ProductSkuRespVO.PublicationSkuExtRespVO.class));
            }
            List<ProductPublicationSkuGradeRelDO> relList = gradeRelMap.get(sku.getId());
            if (CollUtil.isNotEmpty(relList)) {
                sku.setApplicableGradeCatalogIds(convertList(relList, ProductPublicationSkuGradeRelDO::getGradeCatalogId));
                sku.setApplicableGradeNames(convertList(relList, rel -> {
                    EduGradeCatalogRespDTO gradeCatalog = gradeCatalogMap.get(rel.getGradeCatalogId());
                    return gradeCatalog == null ? null : gradeCatalog.getGradeName();
                }));
            }
        });
    }

    private void validateGradeCatalogIds(List<ProductSkuSaveReqVO> skus) {
        Set<Long> gradeCatalogIds = skus.stream()
                .filter(item -> CollUtil.isNotEmpty(item.getApplicableGradeCatalogIds()))
                .flatMap(item -> item.getApplicableGradeCatalogIds().stream())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        if (CollUtil.isEmpty(gradeCatalogIds)) {
            throw exception(PUBLICATION_SKU_GRADE_REQUIRED);
        }
        Map<Long, EduGradeCatalogRespDTO> gradeCatalogMap = gradeCatalogApi.getGradeCatalogMap(gradeCatalogIds);
        if (gradeCatalogMap.size() != gradeCatalogIds.size()
                || gradeCatalogMap.values().stream().anyMatch(item -> !CommonStatusEnum.ENABLE.getStatus().equals(item.getStatus()))) {
            throw exception(PUBLICATION_GRADE_CATALOG_NOT_EXISTS);
        }
    }

    private EduPublicationPublisherRespDTO getRequiredPublicationPublisher(Long publisherId) {
        EduPublicationPublisherRespDTO publisher = publicationPublisherApi.getPublicationPublisher(publisherId);
        if (publisher == null) {
            throw exception(PUBLICATION_PUBLISHER_NOT_EXISTS);
        }
        return publisher;
    }

    private EduPublicationPublisherRespDTO getEnabledPublicationPublisher(Long publisherId) {
        EduPublicationPublisherRespDTO publisher = getRequiredPublicationPublisher(publisherId);
        if (!CommonStatusEnum.ENABLE.getStatus().equals(publisher.getStatus())) {
            throw exception(PUBLICATION_PUBLISHER_NOT_EXISTS);
        }
        return publisher;
    }

    private EduPublicationTypeRespDTO getRequiredPublicationType(Long publicationTypeId) {
        EduPublicationTypeRespDTO publicationType = publicationTypeApi.getPublicationType(publicationTypeId);
        if (publicationType == null) {
            throw exception(PUBLICATION_TYPE_NOT_EXISTS);
        }
        return publicationType;
    }

    private EduPublicationTypeRespDTO getEnabledPublicationType(Long publicationTypeId) {
        EduPublicationTypeRespDTO publicationType = getRequiredPublicationType(publicationTypeId);
        if (!CommonStatusEnum.ENABLE.getStatus().equals(publicationType.getStatus())) {
            throw exception(PUBLICATION_TYPE_NOT_EXISTS);
        }
        return publicationType;
    }
}
