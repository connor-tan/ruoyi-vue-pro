package cn.iocoder.yudao.module.product.api.publication;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationPublisherDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSkuExtDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSkuGradeRelDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSpuExtDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTypeDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationPublisherMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSkuExtMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSkuGradeRelMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSpuExtMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationTypeMapper;
import cn.iocoder.yudao.module.product.service.category.ProductCategoryService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Service
@Validated
public class ProductPublicationApiImpl implements ProductPublicationApi {

    @Resource
    private ProductSpuService productSpuService;
    @Resource
    private ProductSkuService productSkuService;
    @Resource
    private ProductCategoryService productCategoryService;
    @Resource
    private ProductPublicationSpuExtMapper publicationSpuExtMapper;
    @Resource
    private ProductPublicationSkuExtMapper publicationSkuExtMapper;
    @Resource
    private ProductPublicationSkuGradeRelMapper publicationSkuGradeRelMapper;
    @Resource
    private ProductPublicationPublisherMapper publicationPublisherMapper;
    @Resource
    private ProductPublicationTypeMapper publicationTypeMapper;
    @Resource
    private GradeCatalogMapper gradeCatalogMapper;

    @Override
    public ProductPublicationRespDTO getPublication(Long spuId) {
        if (spuId == null) {
            return null;
        }
        List<ProductPublicationRespDTO> list = getPublicationList(Collections.singleton(spuId));
        return CollUtil.isEmpty(list) ? null : list.get(0);
    }

    @Override
    public List<ProductPublicationRespDTO> getPublicationList(Collection<Long> spuIds) {
        if (CollUtil.isEmpty(spuIds)) {
            return Collections.emptyList();
        }
        List<ProductSpuDO> spus = productSpuService.getSpuList(spuIds);
        List<ProductSpuDO> publicationSpus = convertList(spus, item -> {
            ProductCategoryDO category = productCategoryService.getCategory(item.getCategoryId());
            if (category == null || !BizSceneEnum.isPublication(category.getBizScene())) {
                return null;
            }
            return item;
        }).stream().filter(java.util.Objects::nonNull).toList();
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
        Map<Long, ProductPublicationPublisherDO> publisherMap = convertMap(
                publicationPublisherMapper.selectByIds(publisherIds), ProductPublicationPublisherDO::getId);
        Map<Long, ProductPublicationTypeDO> publicationTypeMap = convertMap(
                publicationTypeMapper.selectByIds(publicationTypeIds), ProductPublicationTypeDO::getId);

        List<ProductSkuDO> skuList = productSkuService.getSkuListBySpuId(convertSet(publicationSpus, ProductSpuDO::getId));
        Map<Long, List<ProductSkuDO>> skuMap = convertMultiMap(skuList, ProductSkuDO::getSpuId);
        Set<Long> skuIds = convertSet(skuList, ProductSkuDO::getId);
        Map<Long, ProductPublicationSkuExtDO> skuExtMap = convertMap(
                publicationSkuExtMapper.selectListBySkuIds(skuIds), ProductPublicationSkuExtDO::getSkuId);
        Map<Long, List<ProductPublicationSkuGradeRelDO>> skuGradeMap = convertMultiMap(
                publicationSkuGradeRelMapper.selectListBySkuIds(skuIds), ProductPublicationSkuGradeRelDO::getSkuId);
        Set<Long> gradeCatalogIds = new LinkedHashSet<>();
        skuGradeMap.values().forEach(items -> items.forEach(item -> gradeCatalogIds.add(item.getGradeCatalogId())));
        Map<Long, GradeCatalogDO> gradeCatalogMap = CollUtil.isEmpty(gradeCatalogIds) ? Collections.emptyMap()
                : convertMap(gradeCatalogMapper.selectByIds(gradeCatalogIds), GradeCatalogDO::getId);

        return convertList(publicationSpus, spu -> {
            ProductPublicationRespDTO dto = BeanUtils.toBean(spu, ProductPublicationRespDTO.class);
            ProductCategoryDO category = categoryMap.get(spu.getCategoryId());
            dto.setBizScene(category == null ? null : category.getBizScene());

            ProductPublicationSpuExtDO spuExt = spuExtMap.get(spu.getId());
            if (spuExt != null) {
                ProductPublicationRespDTO.PublicationSpuExtDTO spuExtDTO =
                        BeanUtils.toBean(spuExt, ProductPublicationRespDTO.PublicationSpuExtDTO.class);
                ProductPublicationPublisherDO publisher = publisherMap.get(spuExt.getPublisherId());
                ProductPublicationTypeDO publicationType = publicationTypeMap.get(spuExt.getPublicationTypeId());
                spuExtDTO.setPublisherName(publisher == null ? null : publisher.getName());
                spuExtDTO.setPublicationTypeName(publicationType == null ? null : publicationType.getName());
                spuExtDTO.setPublicationTypeIdentifierRule(publicationType == null ? null : publicationType.getIdentifierRule());
                dto.setPublicationExt(spuExtDTO);
            }

            dto.setSkus(convertList(skuMap.get(spu.getId()), sku -> {
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
                        GradeCatalogDO gradeCatalog = gradeCatalogMap.get(rel.getGradeCatalogId());
                        return gradeCatalog == null ? null : gradeCatalog.getGradeName();
                    }));
                }
                return skuDTO;
            }));
            return dto;
        });
    }

}
