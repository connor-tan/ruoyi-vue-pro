package cn.iocoder.yudao.module.subscription.service.app;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.subscription.controller.app.publication.vo.AppSubscriptionPublicationAttrRespVO;
import cn.iocoder.yudao.module.subscription.controller.app.publication.vo.AppSubscriptionPublicationPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.app.publication.vo.AppSubscriptionPublicationProfileRespVO;
import cn.iocoder.yudao.module.subscription.controller.app.publication.vo.AppSubscriptionPublicationRespVO;
import cn.iocoder.yudao.module.subscription.controller.app.window.vo.AppSubscriptionCurrentWindowRespVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowPublicationDO;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeCalcRuleEnum;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityService;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionGradeResolveRespBO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class SubscriptionAppQueryServiceImpl implements SubscriptionAppQueryService {

    @Resource
    private SubscriptionWindowService subscriptionWindowService;
    @Resource
    private SubscriptionVisibilityService subscriptionVisibilityService;
    @Resource
    private SubscriptionSupportService subscriptionSupportService;
    @Override
    public AppSubscriptionCurrentWindowRespVO getCurrentWindow() {
        SubscriptionWindowDO window = subscriptionWindowService.getCurrentOpenWindow();
        AppSubscriptionCurrentWindowRespVO respVO = new AppSubscriptionCurrentWindowRespVO();
        if (window == null) {
            respVO.setOpened(Boolean.FALSE);
            return respVO;
        }
        respVO.setOpened(Boolean.TRUE);
        respVO.setId(window.getId());
        respVO.setName(window.getName());
        respVO.setTargetSchoolYearId(window.getTargetSchoolYearId());
        respVO.setTargetSemester(window.getTargetSemester());
        respVO.setGradeCalcRule(normalizeGradeCalcRule(window.getGradeCalcRule()));
        respVO.setStartTime(window.getStartTime());
        respVO.setEndTime(window.getEndTime());
        cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO schoolYear =
                subscriptionSupportService.getSchoolYear(window.getTargetSchoolYearId());
        if (schoolYear != null) {
            respVO.setTargetSchoolYearName(schoolYear.getYearStart() + "-" + schoolYear.getYearEnd() + "学年");
        }
        return respVO;
    }

    private String normalizeGradeCalcRule(String gradeCalcRule) {
        return Objects.equals(gradeCalcRule, SubscriptionGradeCalcRuleEnum.PROMOTED_GRADE.getRule())
                ? SubscriptionGradeCalcRuleEnum.PROMOTED_GRADE.getRule()
                : SubscriptionGradeCalcRuleEnum.CURRENT_GRADE.getRule();
    }

    @Override
    public PageResult<AppSubscriptionPublicationRespVO> getPublicationPage(Long loginUserId,
                                                                           AppSubscriptionPublicationPageReqVO reqVO) {
        SubscriptionWindowDO currentWindow = subscriptionWindowService.getCurrentOpenWindow();
        if (currentWindow == null) {
            return PageResult.empty();
        }
        validateStudentBelongToParent(loginUserId, reqVO.getStudentId());
        SubscriptionVisibilityResultBO visibilityResult = subscriptionVisibilityService.calculate(reqVO.getStudentId(), currentWindow.getId());
        if (visibilityResult.getBlockedReasonDesc() != null) {
            throw exception(ErrorCodeConstants.APP_SUBSCRIPTION_STUDENT_BLOCKED, visibilityResult.getBlockedReasonDesc());
        }
        List<AppSubscriptionPublicationRespVO> publications = buildPublicationRespList(currentWindow,
                visibilityResult.getGradeResolve(), visibilityResult.getVisibleWindowPublications());
        List<AppSubscriptionPublicationRespVO> filtered = publications.stream()
                .filter(publication -> matchCategory(publication, reqVO.getCategoryId()))
                .filter(publication -> matchKeyword(publication, reqVO.getKeyword()))
                .filter(publication -> matchPropertyValues(publication, reqVO.getPropertyValueIds()))
                .toList();
        return paginate(filtered, reqVO.getPageNo(), reqVO.getPageSize());
    }

    @Override
    public AppSubscriptionPublicationRespVO getPublication(Long loginUserId, Long studentId, Long productSpuId) {
        SubscriptionWindowDO currentWindow = subscriptionWindowService.getCurrentOpenWindow();
        if (currentWindow == null) {
            throw exception(ErrorCodeConstants.WINDOW_CURRENT_NOT_EXISTS);
        }
        validateStudentBelongToParent(loginUserId, studentId);
        SubscriptionVisibilityResultBO visibilityResult = subscriptionVisibilityService.calculate(studentId, currentWindow.getId());
        if (visibilityResult.getBlockedReasonDesc() != null) {
            throw exception(ErrorCodeConstants.APP_SUBSCRIPTION_STUDENT_BLOCKED, visibilityResult.getBlockedReasonDesc());
        }
        SubscriptionWindowPublicationDO windowPublication = visibilityResult.getVisibleWindowPublications().stream()
                .filter(item -> Objects.equals(item.getProductSpuId(), productSpuId))
                .findFirst()
                .orElseThrow(() -> exception(ErrorCodeConstants.APP_PUBLICATION_NOT_VISIBLE));
        return buildPublicationRespList(currentWindow, visibilityResult.getGradeResolve(), Collections.singletonList(windowPublication))
                .stream()
                .findFirst()
                .orElseThrow(() -> exception(ErrorCodeConstants.APP_PUBLICATION_NOT_VISIBLE));
    }

    private void validateStudentBelongToParent(Long loginUserId, Long studentId) {
        cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO student = subscriptionSupportService.getStudent(studentId);
        if (!Objects.equals(student.getBelongTo(), loginUserId)) {
            throw exception(ErrorCodeConstants.APP_STUDENT_NOT_BELONG_TO_PARENT);
        }
    }

    private List<AppSubscriptionPublicationRespVO> buildPublicationRespList(SubscriptionWindowDO window,
                                                                            SubscriptionGradeResolveRespBO gradeResolve,
                                                                            List<SubscriptionWindowPublicationDO> windowPublications) {
        if (CollUtil.isEmpty(windowPublications)) {
            return Collections.emptyList();
        }
        List<Long> productSpuIds = windowPublications.stream()
                .map(SubscriptionWindowPublicationDO::getProductSpuId)
                .distinct()
                .toList();
        Map<Long, ProductSpuDO> productSpuMap = subscriptionSupportService.getProductSpuMap(productSpuIds);
        Map<Long, ProductSkuDO> singleSpecSkuMap = subscriptionSupportService.getSingleSpecSkuMap(productSpuIds);
        Map<Long, ProductCategoryDO> categoryMap = subscriptionSupportService.getCategoryMap(productSpuMap.values().stream()
                .map(ProductSpuDO::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        return windowPublications.stream()
                .map(windowPublication -> buildPublicationResp(window, gradeResolve, windowPublication,
                        productSpuMap, singleSpecSkuMap, categoryMap))
                .filter(Objects::nonNull)
                .toList();
    }

    private AppSubscriptionPublicationRespVO buildPublicationResp(SubscriptionWindowDO window,
                                                                  SubscriptionGradeResolveRespBO gradeResolve,
                                                                  SubscriptionWindowPublicationDO windowPublication,
                                                                  Map<Long, ProductSpuDO> productSpuMap,
                                                                  Map<Long, ProductSkuDO> singleSpecSkuMap,
                                                                  Map<Long, ProductCategoryDO> categoryMap) {
        ProductSpuDO productSpu = productSpuMap.get(windowPublication.getProductSpuId());
        ProductSkuDO sku = singleSpecSkuMap.get(windowPublication.getProductSpuId());
        if (productSpu == null || sku == null) {
            return null;
        }
        AppSubscriptionPublicationRespVO respVO = new AppSubscriptionPublicationRespVO();
        respVO.setProductSpuId(productSpu.getId());
        respVO.setProductName(productSpu.getName());
        respVO.setCategoryId(productSpu.getCategoryId());
        ProductCategoryDO category = categoryMap.get(productSpu.getCategoryId());
        respVO.setCategoryName(category != null ? category.getName() : null);
        respVO.setPicUrl(productSpu.getPicUrl());
        respVO.setPrice(productSpu.getPrice());
        respVO.setKeyword(productSpu.getKeyword());
        respVO.setIntroduction(productSpu.getIntroduction());
        respVO.setDescription(productSpu.getDescription());
        respVO.setWindowId(window.getId());
        respVO.setTargetSemester(window.getTargetSemester());
        respVO.setEffectiveGradeCatalogId(gradeResolve.getEffectiveGradeCatalogId());
        respVO.setEffectiveGradeNo(gradeResolve.getEffectiveGradeNo());
        respVO.setEffectiveGradeName(gradeResolve.getEffectiveGradeName());
        respVO.setEffectiveGradeAliasName(gradeResolve.getEffectiveGradeAliasName());
        respVO.setPublicationProfile(buildProfileResp(windowPublication, category));
        respVO.setAttrs(buildAttrResp(sku));
        return respVO;
    }

    private AppSubscriptionPublicationProfileRespVO buildProfileResp(SubscriptionWindowPublicationDO windowPublication,
                                                                     ProductCategoryDO category) {
        AppSubscriptionPublicationProfileRespVO respVO = new AppSubscriptionPublicationProfileRespVO();
        respVO.setTypeCategoryId(category != null ? category.getId() : null);
        respVO.setTypeCategoryName(category != null ? category.getName() : null);
        respVO.setSupportsGift(category != null && Boolean.TRUE.equals(category.getSupportsGift()));
        respVO.setRecommendFlag(Boolean.TRUE.equals(windowPublication.getRecommendFlag()));
        respVO.setMaxQuantityPerStudent(windowPublication.getMaxQuantityPerStudent() != null
                ? windowPublication.getMaxQuantityPerStudent() : 1);
        return respVO;
    }

    private List<AppSubscriptionPublicationAttrRespVO> buildAttrResp(ProductSkuDO sku) {
        if (sku == null || CollUtil.isEmpty(sku.getProperties())) {
            return Collections.emptyList();
        }
        return sku.getProperties().stream().map(property -> {
            AppSubscriptionPublicationAttrRespVO respVO = new AppSubscriptionPublicationAttrRespVO();
            respVO.setPropertyId(property.getPropertyId());
            respVO.setPropertyName(property.getPropertyName());
            respVO.setPropertyValueId(property.getValueId());
            respVO.setPropertyValueName(property.getValueName());
            return respVO;
        }).toList();
    }

    private boolean matchCategory(AppSubscriptionPublicationRespVO publication, Long categoryId) {
        return categoryId == null || Objects.equals(publication.getCategoryId(), categoryId);
    }

    private boolean matchKeyword(AppSubscriptionPublicationRespVO publication, String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return true;
        }
        return StrUtil.containsIgnoreCase(publication.getProductName(), keyword)
                || StrUtil.containsIgnoreCase(publication.getKeyword(), keyword)
                || StrUtil.containsIgnoreCase(publication.getIntroduction(), keyword);
    }

    private boolean matchPropertyValues(AppSubscriptionPublicationRespVO publication, Set<Long> propertyValueIds) {
        if (CollUtil.isEmpty(propertyValueIds)) {
            return true;
        }
        Set<Long> publicationPropertyValueIds = publication.getAttrs().stream()
                .map(AppSubscriptionPublicationAttrRespVO::getPropertyValueId)
                .collect(Collectors.toSet());
        return publicationPropertyValueIds.containsAll(propertyValueIds);
    }

    private PageResult<AppSubscriptionPublicationRespVO> paginate(List<AppSubscriptionPublicationRespVO> records, Integer pageNo,
                                                                  Integer pageSize) {
        if (CollUtil.isEmpty(records)) {
            return PageResult.empty();
        }
        long total = records.size();
        if (pageSize == null || pageSize <= 0) {
            return new PageResult<>(records, total);
        }
        int start = Math.max((pageNo - 1) * pageSize, 0);
        if (start >= records.size()) {
            return PageResult.empty(total);
        }
        int end = Math.min(start + pageSize, records.size());
        return new PageResult<>(records.subList(start, end), total);
    }
}
