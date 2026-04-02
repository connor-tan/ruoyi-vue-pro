package cn.iocoder.yudao.module.subscription.service.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolGradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentMapper;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.category.ProductCategoryMapper;
import cn.iocoder.yudao.module.product.dal.mysql.property.ProductPropertyMapper;
import cn.iocoder.yudao.module.product.dal.mysql.property.ProductPropertyValueMapper;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportCategorySimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportGradeCatalogSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportProductSpuSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportPropertySimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportPropertyValueSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportSchoolSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportSchoolYearSimpleRespVO;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionConfigKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_NOT_EXISTS;

@Service
@Validated
public class SubscriptionSupportServiceImpl implements SubscriptionSupportService {

    @Resource
    private ProductSpuMapper productSpuMapper;
    @Resource
    private ProductCategoryMapper productCategoryMapper;
    @Resource
    private ProductPropertyMapper productPropertyMapper;
    @Resource
    private ProductPropertyValueMapper productPropertyValueMapper;
    @Resource
    private ProductSkuMapper productSkuMapper;
    @Resource
    private GradeCatalogMapper gradeCatalogMapper;
    @Resource
    private SchoolMapper schoolMapper;
    @Resource
    private SchoolYearMapper schoolYearMapper;
    @Resource
    private StudentMapper studentMapper;
    @Resource
    private SchoolClassMapper schoolClassMapper;
    @Resource
    private SchoolGradeMapper schoolGradeMapper;
    @Resource
    private ConfigApi configApi;

    @Override
    public List<SubscriptionSupportProductSpuSimpleRespVO> getProductSpuSimpleList(String name) {
        List<ProductSpuDO> productSpuList = getProductSpuList(name, true);
        Map<Long, ProductCategoryDO> categoryMap = getCategoryMap(productSpuList.stream()
                .map(ProductSpuDO::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        return productSpuList.stream().map(productSpu -> {
            SubscriptionSupportProductSpuSimpleRespVO respVO =
                    BeanUtils.toBean(productSpu, SubscriptionSupportProductSpuSimpleRespVO.class);
            ProductCategoryDO category = categoryMap.get(productSpu.getCategoryId());
            respVO.setCategoryName(category != null ? category.getName() : null);
            respVO.setSupportsGift(category != null ? category.getSupportsGift() : Boolean.FALSE);
            return respVO;
        }).toList();
    }

    @Override
    public List<SubscriptionSupportCategorySimpleRespVO> getCategorySimpleList() {
        List<ProductCategoryDO> categories = productCategoryMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        categories.sort(Comparator.comparing(ProductCategoryDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ProductCategoryDO::getId));
        return BeanUtils.toBean(categories, SubscriptionSupportCategorySimpleRespVO.class);
    }

    @Override
    public List<SubscriptionSupportCategorySimpleRespVO> getPublicationTypeCategorySimpleList() {
        return BeanUtils.toBean(getPublicationTypeCategoryList(), SubscriptionSupportCategorySimpleRespVO.class);
    }

    @Override
    public List<SubscriptionSupportPropertySimpleRespVO> getPropertySimpleList() {
        List<ProductPropertyDO> properties = productPropertyMapper.selectList();
        properties.sort(Comparator.comparing(ProductPropertyDO::getId));
        return BeanUtils.toBean(properties, SubscriptionSupportPropertySimpleRespVO.class);
    }

    @Override
    public List<SubscriptionSupportPropertyValueSimpleRespVO> getPropertyValueSimpleList(Long propertyId) {
        List<ProductPropertyValueDO> values;
        if (propertyId != null) {
            values = productPropertyValueMapper.selectListByPropertyId(Collections.singletonList(propertyId));
        } else {
            List<Long> propertyIds = productPropertyMapper.selectList().stream()
                    .map(ProductPropertyDO::getId)
                    .toList();
            values = propertyIds.isEmpty() ? Collections.emptyList()
                    : productPropertyValueMapper.selectListByPropertyId(propertyIds);
        }
        values.sort(Comparator.comparing(ProductPropertyValueDO::getPropertyId)
                .thenComparing(ProductPropertyValueDO::getId));
        return BeanUtils.toBean(values, SubscriptionSupportPropertyValueSimpleRespVO.class);
    }

    @Override
    public List<SubscriptionSupportGradeCatalogSimpleRespVO> getGradeCatalogSimpleList() {
        return BeanUtils.toBean(gradeCatalogMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()),
                SubscriptionSupportGradeCatalogSimpleRespVO.class);
    }

    @Override
    public List<SubscriptionSupportSchoolSimpleRespVO> getSchoolSimpleList() {
        List<SchoolDO> schools = schoolMapper.selectList();
        schools.sort(Comparator.comparing(SchoolDO::getSchoolName, Comparator.nullsLast(String::compareTo))
                .thenComparing(SchoolDO::getId));
        return BeanUtils.toBean(schools, SubscriptionSupportSchoolSimpleRespVO.class);
    }

    @Override
    public List<SubscriptionSupportSchoolYearSimpleRespVO> getSchoolYearSimpleList(Long schoolId) {
        List<SchoolYearDO> schoolYears;
        if (schoolId != null) {
            schoolYears = schoolYearMapper.selectListBySchoolId(schoolId);
        } else {
            schoolYears = schoolYearMapper.selectList(new LambdaQueryWrapperX<SchoolYearDO>()
                    .orderByDesc(SchoolYearDO::getYearStart)
                    .orderByDesc(SchoolYearDO::getYearEnd)
                    .orderByAsc(SchoolYearDO::getId));
            schoolYears = new ArrayList<>(schoolYears.stream()
                    .collect(Collectors.toMap(year -> year.getYearStart() + "-" + year.getYearEnd(),
                            year -> year, (left, right) -> left.getId() <= right.getId() ? left : right,
                            LinkedHashMap::new))
                    .values());
        }
        schoolYears.sort(Comparator.comparing(SchoolYearDO::getYearStart).reversed()
                .thenComparing(SchoolYearDO::getYearEnd, Comparator.reverseOrder())
                .thenComparing(SchoolYearDO::getId));
        return schoolYears.stream().map(this::buildSchoolYearSimpleResp).toList();
    }

    @Override
    public ProductSpuDO getProductSpu(Long id) {
        return id == null ? null : productSpuMapper.selectById(id);
    }

    @Override
    public ProductSpuDO validateProductSpu(Long id, boolean requireEnabled) {
        ProductSpuDO productSpu = getProductSpu(id);
        if (productSpu == null) {
            throw exception(ErrorCodeConstants.PUBLICATION_PRODUCT_NOT_EXISTS);
        }
        if (Objects.equals(productSpu.getStatus(), ProductSpuStatusEnum.RECYCLE.getStatus())
                || (requireEnabled && !ProductSpuStatusEnum.isEnable(productSpu.getStatus()))) {
            throw exception(ErrorCodeConstants.PUBLICATION_PRODUCT_DISABLED);
        }
        return productSpu;
    }

    @Override
    public void validateSingleSpecProduct(ProductSpuDO productSpu) {
        if (Boolean.TRUE.equals(productSpu.getSpecType())) {
            throw exception(ErrorCodeConstants.PUBLICATION_PRODUCT_SPEC_INVALID);
        }
        List<ProductSkuDO> skuList = productSkuMapper.selectListBySpuId(productSpu.getId());
        if (CollUtil.size(skuList) != 1) {
            throw exception(ErrorCodeConstants.PUBLICATION_PRODUCT_SPEC_INVALID);
        }
    }

    @Override
    public List<ProductSpuDO> getProductSpuList(String name, boolean onlyEnabled) {
        List<Long> publicationTypeCategoryIds = getPublicationTypeCategoryList().stream()
                .map(ProductCategoryDO::getId)
                .toList();
        if (publicationTypeCategoryIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapperX<ProductSpuDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.likeIfPresent(ProductSpuDO::getName, StrUtil.blankToDefault(name, null))
                .in(ProductSpuDO::getCategoryId, publicationTypeCategoryIds)
                .eq(ProductSpuDO::getSpecType, Boolean.FALSE)
                .ne(ProductSpuDO::getStatus, ProductSpuStatusEnum.RECYCLE.getStatus())
                .orderByDesc(ProductSpuDO::getSort)
                .orderByDesc(ProductSpuDO::getId);
        if (onlyEnabled) {
            wrapper.eq(ProductSpuDO::getStatus, ProductSpuStatusEnum.ENABLE.getStatus());
        }
        return productSpuMapper.selectList(wrapper);
    }

    @Override
    public Map<Long, ProductSpuDO> getProductSpuMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(productSpuMapper.selectBatchIds(ids), ProductSpuDO::getId);
    }

    @Override
    public Map<Long, ProductSkuDO> getSingleSpecSkuMap(Collection<Long> productSpuIds) {
        if (CollUtil.isEmpty(productSpuIds)) {
            return Collections.emptyMap();
        }
        return productSkuMapper.selectListBySpuId(productSpuIds).stream()
                .collect(Collectors.groupingBy(ProductSkuDO::getSpuId))
                .entrySet().stream()
                .filter(entry -> CollUtil.size(entry.getValue()) == 1)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0)));
    }

    @Override
    public ProductCategoryDO validateCategory(Long id) {
        ProductCategoryDO category = id == null ? null : productCategoryMapper.selectById(id);
        if (category == null) {
            throw exception(ErrorCodeConstants.PUBLICATION_CATEGORY_NOT_EXISTS);
        }
        return category;
    }

    @Override
    public ProductCategoryDO validatePublicationTypeCategory(Long id) {
        ProductCategoryDO category = getPublicationTypeCategoryMap().get(id);
        if (category == null) {
            throw exception(ErrorCodeConstants.PUBLICATION_TYPE_CATEGORY_INVALID);
        }
        return category;
    }

    @Override
    public Map<Long, ProductCategoryDO> getCategoryMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(productCategoryMapper.selectBatchIds(ids), ProductCategoryDO::getId);
    }

    @Override
    public List<ProductCategoryDO> getPublicationTypeCategoryList() {
        List<ProductCategoryDO> categories = productCategoryMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        Long rootCategoryId = getPublicationTypeRootCategoryId();
        Map<Long, List<ProductCategoryDO>> parentCategoryMap = CollectionUtils.convertMultiMap(categories,
                ProductCategoryDO::getParentId);
        List<ProductCategoryDO> result = new ArrayList<>();
        collectPublicationTypeChildren(rootCategoryId, parentCategoryMap, result);
        result.sort(Comparator
                .comparing(ProductCategoryDO::getParentId, Comparator.nullsLast(Long::compareTo))
                .thenComparing(ProductCategoryDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ProductCategoryDO::getId));
        return result;
    }

    @Override
    public Map<Long, ProductCategoryDO> getPublicationTypeCategoryMap() {
        return CollectionUtils.convertMap(getPublicationTypeCategoryList(), ProductCategoryDO::getId);
    }

    @Override
    public Map<Long, ProductPropertyDO> getPropertyMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(productPropertyMapper.selectBatchIds(ids), ProductPropertyDO::getId);
    }

    @Override
    public Map<Long, ProductPropertyValueDO> getPropertyValueMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(productPropertyValueMapper.selectBatchIds(ids), ProductPropertyValueDO::getId);
    }

    @Override
    public GradeCatalogDO getGradeCatalog(Long id) {
        return id == null ? null : gradeCatalogMapper.selectById(id);
    }

    @Override
    public void validateGradeCatalogIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw exception(ErrorCodeConstants.SUPPORT_GRADE_CATALOG_NOT_EXISTS);
        }
        Map<Long, GradeCatalogDO> gradeCatalogMap = getGradeCatalogMap(ids);
        for (Long id : ids) {
            GradeCatalogDO gradeCatalog = gradeCatalogMap.get(id);
            if (gradeCatalog == null || !CommonStatusEnum.isEnable(gradeCatalog.getStatus())) {
                throw exception(ErrorCodeConstants.SUPPORT_GRADE_CATALOG_NOT_EXISTS);
            }
        }
    }

    @Override
    public Map<Long, GradeCatalogDO> getGradeCatalogMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(gradeCatalogMapper.selectBatchIds(ids), GradeCatalogDO::getId);
    }

    @Override
    public SchoolDO getSchool(Long id) {
        return id == null ? null : schoolMapper.selectById(id);
    }

    @Override
    public Map<Long, SchoolDO> getSchoolMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(schoolMapper.selectBatchIds(ids), SchoolDO::getId);
    }

    @Override
    public SchoolYearDO getSchoolYear(Long id) {
        return id == null ? null : schoolYearMapper.selectById(id);
    }

    @Override
    public Map<Long, SchoolYearDO> getSchoolYearMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(schoolYearMapper.selectBatchIds(ids), SchoolYearDO::getId);
    }

    @Override
    public StudentDO getStudent(Long id) {
        StudentDO student = id == null ? null : studentMapper.selectById(id);
        if (student == null) {
            throw exception(STUDENT_NOT_EXISTS);
        }
        return student;
    }

    @Override
    public SchoolClassDO getSchoolClass(Long id) {
        return id == null ? null : schoolClassMapper.selectById(id);
    }

    @Override
    public SchoolGradeDO getSchoolGrade(Long id) {
        return id == null ? null : schoolGradeMapper.selectById(id);
    }

    @Override
    public List<SchoolGradeDO> getSchoolGradeList(Long schoolId) {
        return schoolGradeMapper.selectListBySchoolId(schoolId);
    }

    private Long getPublicationTypeRootCategoryId() {
        String configValue = configApi.getConfigValueByKey(SubscriptionConfigKeyConstants.PUBLICATION_TYPE_ROOT_CATEGORY_ID);
        if (StrUtil.isBlank(configValue) || !StrUtil.isNumeric(configValue)) {
            throw exception(ErrorCodeConstants.PUBLICATION_TYPE_ROOT_CATEGORY_NOT_CONFIGURED);
        }
        Long rootCategoryId = Long.valueOf(configValue);
        ProductCategoryDO rootCategory = productCategoryMapper.selectById(rootCategoryId);
        if (rootCategory == null || !CommonStatusEnum.isEnable(rootCategory.getStatus())) {
            throw exception(ErrorCodeConstants.PUBLICATION_TYPE_ROOT_CATEGORY_INVALID);
        }
        return rootCategoryId;
    }

    private void collectPublicationTypeChildren(Long parentId,
                                                Map<Long, List<ProductCategoryDO>> parentCategoryMap,
                                                List<ProductCategoryDO> result) {
        List<ProductCategoryDO> children = parentCategoryMap.getOrDefault(parentId, Collections.emptyList());
        children.stream()
                .sorted(Comparator.comparing(ProductCategoryDO::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ProductCategoryDO::getId))
                .forEach(child -> {
                    result.add(child);
                    collectPublicationTypeChildren(child.getId(), parentCategoryMap, result);
                });
    }

    private SubscriptionSupportSchoolYearSimpleRespVO buildSchoolYearSimpleResp(SchoolYearDO schoolYear) {
        SubscriptionSupportSchoolYearSimpleRespVO respVO = new SubscriptionSupportSchoolYearSimpleRespVO();
        respVO.setId(schoolYear.getId());
        respVO.setName(schoolYear.getYearStart() + "-" + schoolYear.getYearEnd() + "学年");
        return respVO;
    }
}
