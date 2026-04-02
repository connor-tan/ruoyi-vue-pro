package cn.iocoder.yudao.module.subscription.service.support;

import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportCategorySimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportGradeCatalogSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportProductSpuSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportPropertySimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportPropertyValueSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportSchoolSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportSchoolYearSimpleRespVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 订刊模块的基础引用数据服务。
 */
public interface SubscriptionSupportService {

    List<SubscriptionSupportProductSpuSimpleRespVO> getProductSpuSimpleList(String name);

    List<SubscriptionSupportCategorySimpleRespVO> getCategorySimpleList();

    List<SubscriptionSupportCategorySimpleRespVO> getPublicationTypeCategorySimpleList();

    List<SubscriptionSupportPropertySimpleRespVO> getPropertySimpleList();

    List<SubscriptionSupportPropertyValueSimpleRespVO> getPropertyValueSimpleList(Long propertyId);

    List<SubscriptionSupportGradeCatalogSimpleRespVO> getGradeCatalogSimpleList();

    List<SubscriptionSupportSchoolSimpleRespVO> getSchoolSimpleList();

    List<SubscriptionSupportSchoolYearSimpleRespVO> getSchoolYearSimpleList(Long schoolId);

    ProductSpuDO getProductSpu(Long id);

    ProductSpuDO validateProductSpu(Long id, boolean requireEnabled);

    void validateSingleSpecProduct(ProductSpuDO productSpu);

    List<ProductSpuDO> getProductSpuList(String name, boolean onlyEnabled);

    Map<Long, ProductSpuDO> getProductSpuMap(Collection<Long> ids);

    Map<Long, ProductSkuDO> getSingleSpecSkuMap(Collection<Long> productSpuIds);

    ProductCategoryDO validateCategory(Long id);

    ProductCategoryDO validatePublicationTypeCategory(Long id);

    Map<Long, ProductCategoryDO> getCategoryMap(Collection<Long> ids);

    List<ProductCategoryDO> getPublicationTypeCategoryList();

    Map<Long, ProductCategoryDO> getPublicationTypeCategoryMap();

    Map<Long, ProductPropertyDO> getPropertyMap(Collection<Long> ids);

    Map<Long, ProductPropertyValueDO> getPropertyValueMap(Collection<Long> ids);

    GradeCatalogDO getGradeCatalog(Long id);

    void validateGradeCatalogIds(Collection<Long> ids);

    Map<Long, GradeCatalogDO> getGradeCatalogMap(Collection<Long> ids);

    SchoolDO getSchool(Long id);

    Map<Long, SchoolDO> getSchoolMap(Collection<Long> ids);

    SchoolYearDO getSchoolYear(Long id);

    Map<Long, SchoolYearDO> getSchoolYearMap(Collection<Long> ids);

    StudentDO getStudent(Long id);

    SchoolClassDO getSchoolClass(Long id);

    SchoolGradeDO getSchoolGrade(Long id);

    List<SchoolGradeDO> getSchoolGradeList(Long schoolId);
}
