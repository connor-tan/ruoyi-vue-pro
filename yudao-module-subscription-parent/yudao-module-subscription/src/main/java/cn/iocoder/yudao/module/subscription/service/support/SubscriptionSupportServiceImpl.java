package cn.iocoder.yudao.module.subscription.service.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.YearCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolGradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.YearCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.studentclass.StudentClassMapper;
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
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportWindowYearSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportStudentSimpleRespVO;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class SubscriptionSupportServiceImpl implements SubscriptionSupportService {

    @Resource
    private SchoolYearMapper schoolYearMapper;
    @Resource
    private YearCatalogMapper yearCatalogMapper;
    @Resource
    private StudentMapper studentMapper;
    @Resource
    private StudentClassMapper studentClassMapper;
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
        return yearCatalogMapper.selectAllList().stream()
                .map(this::buildWindowYearSimpleResp)
                .sorted(Comparator.comparing(SubscriptionSupportWindowYearSimpleRespVO::getYearStart).reversed()
                        .thenComparing(SubscriptionSupportWindowYearSimpleRespVO::getYearEnd, Comparator.reverseOrder()))
                .toList();
    }

    @Override
    public List<SubscriptionSupportStudentSimpleRespVO> getStudentSimpleList(String keyword, Long schoolId) {
        String trimmedKeyword = StrUtil.trimToNull(keyword);
        if (trimmedKeyword == null) {
            return Collections.emptyList();
        }
        List<StudentDO> exactStudents = schoolId == null
                ? studentMapper.selectSimpleListByExactStudentName(trimmedKeyword)
                : studentMapper.selectSimpleListByExactStudentNameAndSchoolId(trimmedKeyword, schoolId);
        List<StudentDO> students = CollUtil.isNotEmpty(exactStudents)
                ? exactStudents
                : schoolId == null
                ? studentMapper.selectSimpleListByStudentName(trimmedKeyword)
                : studentMapper.selectSimpleListByStudentNameAndSchoolId(trimmedKeyword, schoolId);
        List<StudentClassDO> currentStudentClasses = studentClassMapper.selectCurrentListByStudentIds(
                CollectionUtils.convertSet(students, StudentDO::getId));
        Map<Long, SchoolDO> schoolMap = getSchoolMap(CollectionUtils.convertSet(students, StudentDO::getCurrentSchoolId));
        Map<Long, StudentClassDO> currentStudentClassMap = currentStudentClasses.stream()
                .collect(Collectors.toMap(StudentClassDO::getStudentId, item -> item, (left, right) -> left));
        Map<Long, SchoolClassDO> schoolClassMap = getSchoolClassMap(
                CollectionUtils.convertSet(currentStudentClasses, StudentClassDO::getClassId));
        Map<Long, SchoolGradeDO> schoolGradeMap = getSchoolGradeMap(
                CollectionUtils.convertSet(schoolClassMap.values(), SchoolClassDO::getSchoolGradeId));
        Map<Long, GradeCatalogDO> gradeCatalogMap = getGradeCatalogMap(
                CollectionUtils.convertSet(schoolGradeMap.values(), SchoolGradeDO::getGradeCatalogId));
        return students.stream().map(student -> {
            SubscriptionSupportStudentSimpleRespVO respVO = new SubscriptionSupportStudentSimpleRespVO();
            respVO.setId(student.getId());
            respVO.setStudentName(student.getStudentName());
            respVO.setStudentCode(student.getStudentCode() == null ? null : String.valueOf(student.getStudentCode()));
            StudentClassDO currentStudentClass = currentStudentClassMap.get(student.getId());
            SchoolClassDO schoolClass = currentStudentClass == null ? null : schoolClassMap.get(currentStudentClass.getClassId());
            SchoolGradeDO schoolGrade = schoolClass == null ? null : schoolGradeMap.get(schoolClass.getSchoolGradeId());
            GradeCatalogDO gradeCatalog = schoolGrade == null ? null : gradeCatalogMap.get(schoolGrade.getGradeCatalogId());
            respVO.setGradeName(gradeCatalog == null ? null : gradeCatalog.getGradeName());
            respVO.setCurrentSchoolId(student.getCurrentSchoolId());
            SchoolDO school = schoolMap.get(student.getCurrentSchoolId());
            respVO.setCurrentSchoolName(school == null ? null : school.getSchoolName());
            respVO.setStatus(student.getStatus());
            return respVO;
        }).toList();
    }

    @Override
    public YearCatalogDO validateWindowYear(Long targetYearCatalogId) {
        if (targetYearCatalogId == null) {
            throw exception(ErrorCodeConstants.SUPPORT_WINDOW_YEAR_NOT_EXISTS);
        }
        YearCatalogDO yearCatalog = yearCatalogMapper.selectById(targetYearCatalogId);
        if (yearCatalog == null) {
            throw exception(ErrorCodeConstants.SUPPORT_WINDOW_YEAR_NOT_EXISTS);
        }
        return yearCatalog;
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
    public List<Long> getStudentSchoolIdList() {
        return studentMapper.selectDistinctCurrentSchoolIds();
    }

    @Override
    public List<Long> getStudentSchoolIdListByStatuses(Collection<Integer> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return Collections.emptyList();
        }
        return studentMapper.selectDistinctCurrentSchoolIdsByStatuses(statuses);
    }

    @Override
    public List<StudentDO> getStudentListBySchoolId(Long schoolId) {
        return schoolId == null ? Collections.emptyList() : studentMapper.selectListByCurrentSchoolId(schoolId);
    }

    @Override
    public List<StudentDO> getStudentListByCursor(Long lastId, Integer limit) {
        return studentMapper.selectListByIdGreaterThan(lastId, limit);
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
        List<SchoolDO> schoolDOS = schoolMapper.selectList(new LambdaQueryWrapperX<SchoolDO>().in(SchoolDO::getId, ids));
        return CollectionUtils.convertMap(schoolDOS, SchoolDO::getId);
    }

    @Override
    public SchoolClassDO getSchoolClass(Long id) {
        return id == null ? null : schoolClassMapper.selectById(id);
    }

    @Override
    public Map<Long, SchoolClassDO> getSchoolClassMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SchoolClassDO> schoolClassDOS = schoolClassMapper.selectList(new LambdaQueryWrapperX<SchoolClassDO>()
                .in(SchoolClassDO::getId, ids));
        return CollectionUtils.convertMap(schoolClassDOS, SchoolClassDO::getId);
    }

    @Override
    public SchoolGradeDO getSchoolGrade(Long id) {
        return id == null ? null : schoolGradeMapper.selectById(id);
    }

    @Override
    public Map<Long, SchoolGradeDO> getSchoolGradeMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SchoolGradeDO> schoolGradeDOS = schoolGradeMapper.selectList(new LambdaQueryWrapperX<SchoolGradeDO>()
                .in(SchoolGradeDO::getId, ids));
        return CollectionUtils.convertMap(schoolGradeDOS, SchoolGradeDO::getId);
    }

    @Override
    public List<SchoolGradeDO> getSchoolGradeList(Long schoolId) {
        if (schoolId == null) {
            return Collections.emptyList();
        }
        return schoolGradeMapper.selectListBySchoolId(schoolId);
    }

    @Override
    public List<SchoolGradeDO> getSchoolGradeListBySchoolIds(Collection<Long> schoolIds) {
        if (schoolIds == null || schoolIds.isEmpty()) {
            return Collections.emptyList();
        }
        return schoolGradeMapper.selectListBySchoolIds(schoolIds);
    }

    @Override
    public GradeCatalogDO getGradeCatalog(Long id) {
        return id == null ? null : gradeCatalogMapper.selectById(id);
    }

    @Override
    public List<GradeCatalogDO> getEnabledGradeCatalogList() {
        return gradeCatalogMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public Map<Long, GradeCatalogDO> getGradeCatalogMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<GradeCatalogDO> gradeCatalogDOS = gradeCatalogMapper.selectList(new LambdaQueryWrapperX<GradeCatalogDO>().in(GradeCatalogDO::getId, ids));
        return CollectionUtils.convertMap(gradeCatalogDOS, GradeCatalogDO::getId);
    }

    @Override
    public boolean hasSchoolYear(Long schoolId, Long yearCatalogId) {
        if (schoolId == null || yearCatalogId == null) {
            return false;
        }
        return schoolYearMapper.selectBySchoolIdAndYearCatalogId(schoolId, yearCatalogId) != null;
    }

    @Override
    public long countSchoolYearByYearCatalogId(Long yearCatalogId) {
        return yearCatalogId == null ? 0L : schoolYearMapper.countByYearCatalogId(yearCatalogId);
    }

    @Override
    public Map<Long, Boolean> getSchoolYearCoverageMap(Collection<Long> schoolIds, Long yearCatalogId) {
        if (schoolIds == null || schoolIds.isEmpty() || yearCatalogId == null) {
            return Collections.emptyMap();
        }
        return schoolYearMapper.selectListBySchoolIdsAndYearCatalogIds(schoolIds, Collections.singleton(yearCatalogId))
                .stream()
                .collect(Collectors.toMap(SchoolYearDO::getSchoolId, item -> Boolean.TRUE, (left, right) -> left));
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
    public List<ProductSpuDO> getPublicationSpuList(String productName, Long categoryId, Collection<Long> gradeCatalogIds,
                                                    boolean onlyEnable) {
        Set<Long> matchedSpuIds = null;
        if (CollUtil.isNotEmpty(gradeCatalogIds)) {
            matchedSpuIds = CollectionUtils.convertSet(
                    productSpuGradeMapper.selectListByGradeCatalogIds(gradeCatalogIds),
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
        List<ProductSpuDO> productSpuDOS = productSpuMapper.selectList(new LambdaQueryWrapperX<ProductSpuDO>().in(ProductSpuDO::getId, ids));
        return CollectionUtils.convertMap(productSpuDOS.stream()
                .filter(item -> ProductDomainTypeEnum.isPublication(item.getDomainType()))
                .toList(), ProductSpuDO::getId);
    }

    @Override
    public Map<Long, ProductCategoryDO> getCategoryMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ProductCategoryDO> productCategoryDOS = productCategoryMapper.selectList(new LambdaQueryWrapperX<ProductCategoryDO>().in(ProductCategoryDO::getId, ids));
        return CollectionUtils.convertMap(productCategoryDOS, ProductCategoryDO::getId);
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
        List<ProductPublicationTitleDO> productPublicationTitleDOS = productPublicationTitleMapper.selectList(new LambdaQueryWrapperX<ProductPublicationTitleDO>().in(ProductPublicationTitleDO::getId, ids));
        return CollectionUtils.convertMap(productPublicationTitleDOS, ProductPublicationTitleDO::getId);
    }

    @Override
    public Map<Long, ProductPublicationTypeDO> getPublicationTypeMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ProductPublicationTypeDO> productPublicationTypeDOS = productPublicationTypeMapper.selectList(new LambdaQueryWrapperX<ProductPublicationTypeDO>().in(ProductPublicationTypeDO::getId, ids));
        return CollectionUtils.convertMap(productPublicationTypeDOS, ProductPublicationTypeDO::getId);
    }

    @Override
    public Map<Long, ProductPublicationPublisherDO> getPublicationPublisherMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ProductPublicationPublisherDO> productPublicationPublisherDOS = productPublicationPublisherMapper.selectList(new LambdaQueryWrapperX<ProductPublicationPublisherDO>().in(ProductPublicationPublisherDO::getId, ids));
        return CollectionUtils.convertMap(productPublicationPublisherDOS,
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

    private SubscriptionSupportWindowYearSimpleRespVO buildWindowYearSimpleResp(YearCatalogDO yearCatalog) {
        SubscriptionSupportWindowYearSimpleRespVO respVO = new SubscriptionSupportWindowYearSimpleRespVO();
        respVO.setId(yearCatalog.getId());
        respVO.setYearStart(yearCatalog.getYearStart());
        respVO.setYearEnd(yearCatalog.getYearEnd());
        respVO.setName(buildWindowYearName(yearCatalog.getYearStart(), yearCatalog.getYearEnd()));
        return respVO;
    }

    private String buildWindowYearName(Integer yearStart, Integer yearEnd) {
        return yearStart + "-" + yearEnd + "学年";
    }
}
