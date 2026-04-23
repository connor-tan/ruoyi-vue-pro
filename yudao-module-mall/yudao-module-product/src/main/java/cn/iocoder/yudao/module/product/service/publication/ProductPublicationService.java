package cn.iocoder.yudao.module.product.service.publication;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.*;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.*;
import cn.iocoder.yudao.module.publication.api.enums.PublicationFulfillmentModeEnum;
import cn.iocoder.yudao.module.publication.api.enums.PublicationIdentifierRuleEnum;
import cn.iocoder.yudao.module.publication.api.enums.PublicationTargetPeriodEnum;
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
public class ProductPublicationService {

    @Resource
    private ProductPublicationPublisherService publicationPublisherService;
    @Resource
    private ProductPublicationTypeService publicationTypeService;
    @Resource
    private ProductPublicationSpuExtMapper publicationSpuExtMapper;
    @Resource
    private ProductPublicationSkuExtMapper publicationSkuExtMapper;
    @Resource
    private ProductPublicationSkuGradeRelMapper publicationSkuGradeRelMapper;
    @Resource
    private GradeCatalogMapper gradeCatalogMapper;

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
        ProductPublicationPublisherDO publisher = publicationPublisherService.validateEnabled(ext.getPublisherId());
        ProductPublicationTypeDO type = publicationTypeService.validateEnabled(ext.getPublicationTypeId());
        List<ProductSkuSaveReqVO> skus = reqVO.getSkus();
        if (CollUtil.isEmpty(skus)) {
            throw exception(PUBLICATION_SKU_REQUIRED);
        }
        validateGradeCatalogIds(skus);
        for (ProductSkuSaveReqVO sku : skus) {
            if (sku.getPublicationExt() == null) {
                throw exception(PUBLICATION_SKU_EXT_REQUIRED);
            }
            if (CollUtil.isEmpty(sku.getApplicableGradeCatalogIds())) {
                throw exception(PUBLICATION_SKU_GRADE_REQUIRED);
            }
            sku.getPublicationExt().setTargetPeriod(PublicationTargetPeriodEnum.normalize(sku.getPublicationExt().getTargetPeriod()));
            if (PublicationIdentifierRuleEnum.requiresSkuIsbn(type.getIdentifierRule())
                    && StrUtil.isBlank(sku.getPublicationExt().getIsbn())) {
                throw exception(PUBLICATION_SKU_ISBN_REQUIRED);
            }
        }
        if (PublicationIdentifierRuleEnum.requiresTitleIdentifier(type.getIdentifierRule())
                && StrUtil.isAllBlank(ext.getIssn(), ext.getCnCode(), ext.getPostDistributionCode())) {
            throw exception(PUBLICATION_TITLE_IDENTIFIER_REQUIRED);
        }
        ext.setFulfillmentMode(PublicationFulfillmentModeEnum.normalize(ext.getFulfillmentMode()));
        reqVO.setBrandId(null);
        reqVO.setDeliveryTypes(null);
        reqVO.setDeliveryTemplateId(null);
        reqVO.setGiveIntegral(reqVO.getGiveIntegral() == null ? 0 : reqVO.getGiveIntegral());
        reqVO.setSubCommissionType(Boolean.FALSE);
    }

    @Transactional(rollbackFor = Exception.class)
    public void savePublication(Long spuId, ProductSpuSaveReqVO reqVO, List<ProductSkuDO> savedSkus,
                                Collection<Long> cleanupSkuIds) {
        ProductSpuSaveReqVO.PublicationSpuExtSaveReqVO extReq = reqVO.getPublicationExt();
        ProductPublicationSpuExtDO extDO = BeanUtils.toBean(extReq, ProductPublicationSpuExtDO.class);
        extDO.setSpuId(spuId);
        publicationSpuExtMapper.deleteById(spuId);
        publicationSpuExtMapper.insert(extDO);

        publicationSkuExtMapper.deleteBatch(ProductPublicationSkuExtDO::getSkuId, cleanupSkuIds);
        publicationSkuGradeRelMapper.deleteBatch(ProductPublicationSkuGradeRelDO::getSkuId, cleanupSkuIds);
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
            publicationSkuExtMapper.insert(skuExtDO);

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
    public void clearPublication(Long spuId, Collection<Long> skuIds) {
        publicationSpuExtMapper.deleteById(spuId);
        if (CollUtil.isNotEmpty(skuIds)) {
            publicationSkuExtMapper.deleteBatch(ProductPublicationSkuExtDO::getSkuId, skuIds);
            publicationSkuGradeRelMapper.deleteBatch(ProductPublicationSkuGradeRelDO::getSkuId, skuIds);
        }
    }

    public void fillAdminDetail(ProductSpuRespVO respVO, List<ProductSkuRespVO> skuRespVOList) {
        ProductPublicationSpuExtDO extDO = publicationSpuExtMapper.selectById(respVO.getId());
        if (extDO == null) {
            return;
        }
        ProductPublicationPublisherDO publisher = publicationPublisherService.validateExists(extDO.getPublisherId());
        ProductPublicationTypeDO type = publicationTypeService.validateExists(extDO.getPublicationTypeId());
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
        Map<Long, GradeCatalogDO> gradeCatalogMap = CollUtil.isEmpty(gradeCatalogIds) ? Collections.emptyMap()
                : convertMap(gradeCatalogMapper.selectList(GradeCatalogDO::getId, gradeCatalogIds), GradeCatalogDO::getId);
        skuRespVOList.forEach(sku -> {
            ProductPublicationSkuExtDO skuExtDO = skuExtMap.get(sku.getId());
            if (skuExtDO != null) {
                sku.setPublicationExt(BeanUtils.toBean(skuExtDO, ProductSkuRespVO.PublicationSkuExtRespVO.class));
            }
            List<ProductPublicationSkuGradeRelDO> relList = gradeRelMap.get(sku.getId());
            if (CollUtil.isNotEmpty(relList)) {
                sku.setApplicableGradeCatalogIds(convertList(relList, ProductPublicationSkuGradeRelDO::getGradeCatalogId));
                sku.setApplicableGradeNames(convertList(relList, rel -> {
                    GradeCatalogDO gradeCatalog = gradeCatalogMap.get(rel.getGradeCatalogId());
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
        List<GradeCatalogDO> gradeCatalogList = gradeCatalogMapper.selectList(GradeCatalogDO::getId, gradeCatalogIds);
        if (gradeCatalogList.size() != gradeCatalogIds.size()
                || gradeCatalogList.stream().anyMatch(item -> !CommonStatusEnum.ENABLE.getStatus().equals(item.getStatus()))) {
            throw exception(PUBLICATION_GRADE_CATALOG_NOT_EXISTS);
        }
    }
}
