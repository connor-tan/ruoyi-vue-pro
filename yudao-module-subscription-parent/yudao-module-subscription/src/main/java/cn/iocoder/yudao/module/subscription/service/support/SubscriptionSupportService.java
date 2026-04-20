package cn.iocoder.yudao.module.subscription.service.support;

import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationPublisherDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTitleDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTypeDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuGradeDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportStudentSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportWindowYearSimpleRespVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SubscriptionSupportService {

    List<SubscriptionSupportWindowYearSimpleRespVO> getWindowYearSimpleList();

    List<SubscriptionSupportStudentSimpleRespVO> getStudentSimpleList(String keyword, Long schoolId);

    void validateWindowYear(Integer targetYearStart, Integer targetYearEnd);

    StudentDO getStudent(Long id);

    List<Long> getStudentSchoolIdList();

    List<StudentDO> getStudentListBySchoolId(Long schoolId);

    List<StudentDO> getStudentListByCursor(Long lastId, Integer limit);

    SchoolDO getSchool(Long id);

    Map<Long, SchoolDO> getSchoolMap(Collection<Long> ids);

    SchoolClassDO getSchoolClass(Long id);

    Map<Long, SchoolClassDO> getSchoolClassMap(Collection<Long> ids);

    SchoolGradeDO getSchoolGrade(Long id);

    Map<Long, SchoolGradeDO> getSchoolGradeMap(Collection<Long> ids);

    List<SchoolGradeDO> getSchoolGradeList(Long schoolId);

    List<SchoolGradeDO> getSchoolGradeListBySchoolIds(Collection<Long> schoolIds);

    GradeCatalogDO getGradeCatalog(Long id);

    List<GradeCatalogDO> getEnabledGradeCatalogList();

    Map<Long, GradeCatalogDO> getGradeCatalogMap(Collection<Long> ids);

    void validateGradeCatalogIds(Collection<Long> ids);

    ProductSpuDO getPublicationSpu(Long id, boolean onlyEnable);

    List<ProductSpuDO> getPublicationSpuList(String productName, Long categoryId, Collection<Long> gradeCatalogIds, boolean onlyEnable);

    Map<Long, ProductSpuDO> getPublicationSpuMap(Collection<Long> ids);

    Map<Long, ProductCategoryDO> getCategoryMap(Collection<Long> ids);

    Map<Long, ProductSpuPublicationDO> getSpuPublicationMap(Collection<Long> productSpuIds);

    Map<Long, ProductPublicationTitleDO> getPublicationTitleMap(Collection<Long> ids);

    Map<Long, ProductPublicationTypeDO> getPublicationTypeMap(Collection<Long> ids);

    Map<Long, ProductPublicationPublisherDO> getPublicationPublisherMap(Collection<Long> ids);

    Map<Long, List<ProductSpuGradeDO>> getPublicationSpuGradeMap(Collection<Long> productSpuIds);

    List<ProductSkuDO> getSkuListBySpuId(Long spuId);

    Map<Long, List<ProductSkuDO>> getSkuMapBySpuIds(Collection<Long> spuIds);

    Map<Long, ProductSkuPublicationDO> getSkuPublicationMap(Collection<Long> skuIds);
}
