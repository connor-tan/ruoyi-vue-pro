package cn.iocoder.yudao.module.subscription.service.support;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
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
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationPublisherDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTitleDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTypeDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuGradeDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.category.ProductCategoryMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationPublisherMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationTitleMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationTypeMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductSkuPublicationMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductSpuGradeMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductSpuPublicationMapper;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.enums.publication.ProductDomainTypeEnum;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportStudentSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportWindowYearSimpleRespVO;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

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

@Service
@Validated
public class SubscriptionSupportServiceImpl implements SubscriptionSupportService {

    @Resource
    private SchoolYearMapper schoolYearMapper;
    @Resource
    private StudentMapper studentMapper;
    @Resource
    private SchoolMapper schoolMapper;
    @Resource
    private SchoolClassMapper schoolClassMapper;
    @Resource
    private SchoolGradeMapper schoolGradeMapper;
    @Resource
    private GradeCatalogMapper gradeCatalogMapper;
    @Resource
    private ProductSpuMapper productSpuMapper;
    @Resource
    private ProductCategoryMapper productCategoryMapper;
    @Resource
    private ProductSpuPublicationMapper productSpuPublicationMapper;
    @Resource
    private ProductPublicationTitleMapper productPublicationTitleMapper;
    @Resource
    private ProductPublicationTypeMapper productPublicationTypeMapper;
    @Resource
    private ProductPublicationPublisherMapper productPublicationPublisherMapper;
    @Resource
    private ProductSpuGradeMapper productSpuGradeMapper;
    @Resource
    private ProductSkuService productSkuService;
    @Resource
    private ProductSkuPublicationMapper productSkuPublicationMapper;

    @Override
    public List<SubscriptionSupportWindowYearSimpleRespVO> getWindowYearSimpleList() {
        List<SchoolYearDO> schoolYears = schoolYearMapper.selectList(new LambdaQueryWrapperX<SchoolYearDO>()
                .orderByDesc(SchoolYearDO::getYearStart)
                .orderByDesc(SchoolYearDO::getYearEnd)
                .orderByAsc(SchoolYearDO::getId));
        return schoolYears.stream()
                .collect(Collectors.toMap(year -> buildWindowYearKey(year.getYearStart(), year.getYearEnd()),
                        this::buildWindowYearSimpleResp, (left, right) -> left, LinkedHashMap::new))
                .values().stream()
                .sorted(Comparator.comparing(SubscriptionSupportWindowYearSimpleRespVO::getYearStart).reversed()
                        .thenComparing(SubscriptionSupportWindowYearSimpleRespVO::getYearEnd, Comparator.reverseOrder()))
                .toList();
    }

    @Override
    public void validateWindowYear(Integer targetYearStart, Integer targetYearEnd) {
        if (targetYearStart == null || targetYearEnd == null) {
            throw exception(ErrorCodeConstants.SUPPORT_WINDOW_YEAR_NOT_EXISTS);
        }
        boolean exists = schoolYearMapper.selectList(new LambdaQueryWrapperX<SchoolYearDO>()
                        .eq(SchoolYearDO::getYearStart, targetYearStart)
                        .eq(SchoolYearDO::getYearEnd, targetYearEnd))
                .stream()
                .findAny()
                .isPresent();
        if (!exists) {
            throw exception(ErrorCodeConstants.SUPPORT_WINDOW_YEAR_NOT_EXISTS);
        }
    }

    @Override
    public List<SubscriptionSupportStudentSimpleRespVO> getStudentSimpleList(String keyword) {
        List<StudentDO> students = studentMapper.selectList(new LambdaQueryWrapperX<StudentDO>()
                .likeIfPresent(StudentDO::getStudentName, StrUtil.trimToNull(keyword))
                .orderByDesc(StudentDO::getId)
                .last("LIMIT 20"));
        if (students.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, SchoolDO> schoolMap = getSchoolMap(students.stream()
                .map(StudentDO::getCurrentSchoolId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        return students.stream().map(student -> {
            SubscriptionSupportStudentSimpleRespVO respVO =
                    BeanUtils.toBean(student, SubscriptionSupportStudentSimpleRespVO.class);
            SchoolDO school = schoolMap.get(student.getCurrentSchoolId());
            respVO.setCurrentSchoolName(school == null ? null : school.getSchoolName());
            return respVO;
        }).toList();
    }

    @Override
    public StudentDO getStudent(Long id) {
        StudentDO student = studentMapper.selectById(id);
        if (student == null) {
            throw exception(ErrorCodeConstants.SUPPORT_STUDENT_NOT_EXISTS);
        }
        return student;
    }

    @Override
    public SchoolDO getSchool(Long id) {
        return id == null ? null : schoolMapper.selectById(id);
    }

    @Override
    public Map<Long, SchoolDO> getSchoolMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(schoolMapper.selectBatchIds(ids), SchoolDO::getId);
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
        if (schoolId == null) {
            return Collections.emptyList();
        }
        return schoolGradeMapper.selectListBySchoolId(schoolId);
    }

    @Override
    public GradeCatalogDO getGradeCatalog(Long id) {
        return id == null ? null : gradeCatalogMapper.selectById(id);
    }

    @Override
    public Map<Long, GradeCatalogDO> getGradeCatalogMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(gradeCatalogMapper.selectBatchIds(ids), GradeCatalogDO::getId);
    }

    @Override
    public void validateGradeCatalogIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Map<Long, GradeCatalogDO> gradeCatalogMap = getGradeCatalogMap(ids);
        boolean invalid = ids.stream().anyMatch(id -> {
            GradeCatalogDO gradeCatalog = gradeCatalogMap.get(id);
            return gradeCatalog == null || !CommonStatusEnum.isEnable(gradeCatalog.getStatus());
        });
        if (invalid) {
            throw exception(ErrorCodeConstants.SUPPORT_GRADE_CATALOG_NOT_EXISTS);
        }
    }

    @Override
    public ProductSpuDO getPublicationSpu(Long id, boolean onlyEnable) {
        ProductSpuDO productSpu = productSpuMapper.selectById(id);
        if (productSpu == null || !ProductDomainTypeEnum.isPublication(productSpu.getDomainType())) {
            return null;
        }
        if (onlyEnable && !ProductSpuStatusEnum.isEnable(productSpu.getStatus())) {
            return null;
        }
        return productSpu;
    }

    @Override
    public List<ProductSpuDO> getPublicationSpuList(String productName, Long categoryId, Long gradeCatalogId,
                                                    boolean onlyEnable) {
        Set<Long> matchedSpuIds = null;
        if (gradeCatalogId != null) {
            matchedSpuIds = CollectionUtils.convertSet(
                    productSpuGradeMapper.selectListByGradeCatalogIds(Collections.singleton(gradeCatalogId)),
                    ProductSpuGradeDO::getProductSpuId);
            if (matchedSpuIds.isEmpty()) {
                return Collections.emptyList();
            }
        }
        LambdaQueryWrapperX<ProductSpuDO> queryWrapper = new LambdaQueryWrapperX<ProductSpuDO>()
                .likeIfPresent(ProductSpuDO::getName, StrUtil.trimToNull(productName))
                .eqIfPresent(ProductSpuDO::getCategoryId, categoryId)
                .eq(ProductSpuDO::getDomainType, ProductDomainTypeEnum.PUBLICATION.getCode())
                .inIfPresent(ProductSpuDO::getId, matchedSpuIds)
                .orderByDesc(ProductSpuDO::getSort)
                .orderByDesc(ProductSpuDO::getId);
        if (onlyEnable) {
            queryWrapper.eq(ProductSpuDO::getStatus, ProductSpuStatusEnum.ENABLE.getStatus());
        }
        return productSpuMapper.selectList(queryWrapper);
    }

    @Override
    public Map<Long, ProductSpuDO> getPublicationSpuMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(productSpuMapper.selectBatchIds(ids).stream()
                .filter(item -> ProductDomainTypeEnum.isPublication(item.getDomainType()))
                .toList(), ProductSpuDO::getId);
    }

    @Override
    public Map<Long, ProductCategoryDO> getCategoryMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(productCategoryMapper.selectBatchIds(ids), ProductCategoryDO::getId);
    }

    @Override
    public Map<Long, ProductSpuPublicationDO> getSpuPublicationMap(Collection<Long> productSpuIds) {
        if (productSpuIds == null || productSpuIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(productSpuPublicationMapper.selectListByProductSpuIds(productSpuIds),
                ProductSpuPublicationDO::getProductSpuId);
    }

    @Override
    public Map<Long, ProductPublicationTitleDO> getPublicationTitleMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(productPublicationTitleMapper.selectBatchIds(ids), ProductPublicationTitleDO::getId);
    }

    @Override
    public Map<Long, ProductPublicationTypeDO> getPublicationTypeMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(productPublicationTypeMapper.selectBatchIds(ids), ProductPublicationTypeDO::getId);
    }

    @Override
    public Map<Long, ProductPublicationPublisherDO> getPublicationPublisherMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(productPublicationPublisherMapper.selectBatchIds(ids),
                ProductPublicationPublisherDO::getId);
    }

    @Override
    public Map<Long, List<ProductSpuGradeDO>> getPublicationSpuGradeMap(Collection<Long> productSpuIds) {
        if (productSpuIds == null || productSpuIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMultiMap(productSpuGradeMapper.selectListByProductSpuIds(productSpuIds),
                ProductSpuGradeDO::getProductSpuId);
    }

    @Override
    public List<ProductSkuDO> getSkuListBySpuId(Long spuId) {
        return spuId == null ? Collections.emptyList() : productSkuService.getSkuListBySpuId(spuId);
    }

    @Override
    public Map<Long, List<ProductSkuDO>> getSkuMapBySpuIds(Collection<Long> spuIds) {
        if (spuIds == null || spuIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMultiMap(productSkuService.getSkuListBySpuId(spuIds), ProductSkuDO::getSpuId);
    }

    @Override
    public Map<Long, ProductSkuPublicationDO> getSkuPublicationMap(Collection<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(productSkuPublicationMapper.selectListByProductSkuIds(skuIds),
                ProductSkuPublicationDO::getProductSkuId);
    }

    private SubscriptionSupportWindowYearSimpleRespVO buildWindowYearSimpleResp(SchoolYearDO schoolYear) {
        SubscriptionSupportWindowYearSimpleRespVO respVO = new SubscriptionSupportWindowYearSimpleRespVO();
        respVO.setYearStart(schoolYear.getYearStart());
        respVO.setYearEnd(schoolYear.getYearEnd());
        respVO.setName(buildWindowYearName(schoolYear.getYearStart(), schoolYear.getYearEnd()));
        return respVO;
    }

    private String buildWindowYearKey(Integer yearStart, Integer yearEnd) {
        return yearStart + "-" + yearEnd;
    }

    private String buildWindowYearName(Integer yearStart, Integer yearEnd) {
        return yearStart + "-" + yearEnd + "学年";
    }
}
