package cn.iocoder.yudao.module.subscription.service.app;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTitleDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.subscription.controller.app.publication.vo.AppSubscriptionPublicationPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.app.publication.vo.AppSubscriptionPublicationRespVO;
import cn.iocoder.yudao.module.subscription.controller.app.window.vo.AppSubscriptionCurrentWindowRespVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeCalcRuleEnum;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityService;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionVisibleSpuBO;
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
        respVO.setTargetYearStart(window.getTargetYearStart());
        respVO.setTargetYearEnd(window.getTargetYearEnd());
        respVO.setTargetYearName(window.getTargetYearStart() + "-" + window.getTargetYearEnd() + "学年");
        respVO.setTemplateNameSnapshot(window.getTemplateNameSnapshot());
        respVO.setTargetPeriod(window.getTargetPeriod());
        respVO.setGradeCalcRule(normalizeGradeCalcRule(window.getGradeCalcRule()));
        respVO.setStartTime(window.getStartTime());
        respVO.setEndTime(window.getEndTime());
        return respVO;
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
            throw exception(ErrorCodeConstants.PREVIEW_STUDENT_BLOCKED, visibilityResult.getBlockedReasonDesc());
        }
        List<AppSubscriptionPublicationRespVO> publications = buildPublicationRespList(visibilityResult.getVisibleSpus());
        List<AppSubscriptionPublicationRespVO> filtered = publications.stream()
                .filter(publication -> matchCategory(publication, reqVO.getCategoryId()))
                .filter(publication -> matchKeyword(publication, reqVO.getKeyword()))
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
            throw exception(ErrorCodeConstants.PREVIEW_STUDENT_BLOCKED, visibilityResult.getBlockedReasonDesc());
        }
        SubscriptionVisibleSpuBO visibleSpu = visibilityResult.getVisibleSpus().stream()
                .filter(item -> Objects.equals(item.getWindowSpu().getProductSpuId(), productSpuId))
                .findFirst()
                .orElseThrow(() -> exception(ErrorCodeConstants.APP_PUBLICATION_NOT_VISIBLE));
        return buildPublicationRespList(Collections.singletonList(visibleSpu)).stream()
                .findFirst()
                .orElseThrow(() -> exception(ErrorCodeConstants.APP_PUBLICATION_NOT_VISIBLE));
    }

    private String normalizeGradeCalcRule(String gradeCalcRule) {
        return Objects.equals(gradeCalcRule, SubscriptionGradeCalcRuleEnum.PROMOTED_GRADE.getRule())
                ? SubscriptionGradeCalcRuleEnum.PROMOTED_GRADE.getRule()
                : SubscriptionGradeCalcRuleEnum.CURRENT_GRADE.getRule();
    }

    private void validateStudentBelongToParent(Long loginUserId, Long studentId) {
        cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO student = subscriptionSupportService.getStudent(studentId);
        if (!Objects.equals(student.getBelongTo(), loginUserId)) {
            throw exception(ErrorCodeConstants.APP_STUDENT_NOT_BELONG_TO_PARENT);
        }
    }

    private List<AppSubscriptionPublicationRespVO> buildPublicationRespList(List<SubscriptionVisibleSpuBO> visibleSpus) {
        if (CollUtil.isEmpty(visibleSpus)) {
            return Collections.emptyList();
        }
        List<Long> productSpuIds = visibleSpus.stream()
                .map(item -> item.getWindowSpu().getProductSpuId())
                .distinct()
                .toList();
        List<Long> productSkuIds = visibleSpus.stream()
                .flatMap(item -> item.getWindowSkus().stream())
                .map(SubscriptionWindowSkuDO::getProductSkuId)
                .distinct()
                .toList();
        Map<Long, ProductSpuDO> productSpuMap = subscriptionSupportService.getPublicationSpuMap(productSpuIds);
        Map<Long, ProductCategoryDO> categoryMap = subscriptionSupportService.getCategoryMap(productSpuMap.values().stream()
                .map(ProductSpuDO::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        Map<Long, ProductSpuPublicationDO> spuPublicationMap = subscriptionSupportService.getSpuPublicationMap(productSpuIds);
        Map<Long, ProductPublicationTitleDO> titleMap = subscriptionSupportService.getPublicationTitleMap(spuPublicationMap.values().stream()
                .map(ProductSpuPublicationDO::getPublicationTitleId)
                .collect(Collectors.toSet()));
        Map<Long, List<ProductSkuDO>> skuMap = subscriptionSupportService.getSkuMapBySpuIds(productSpuIds);
        Map<Long, ProductSkuPublicationDO> skuPublicationMap = subscriptionSupportService.getSkuPublicationMap(productSkuIds);
        return visibleSpus.stream()
                .map(visibleSpu -> buildPublicationResp(visibleSpu, productSpuMap, categoryMap, spuPublicationMap, titleMap,
                        skuMap, skuPublicationMap))
                .filter(Objects::nonNull)
                .toList();
    }

    private AppSubscriptionPublicationRespVO buildPublicationResp(SubscriptionVisibleSpuBO visibleSpu,
                                                                  Map<Long, ProductSpuDO> productSpuMap,
                                                                  Map<Long, ProductCategoryDO> categoryMap,
                                                                  Map<Long, ProductSpuPublicationDO> spuPublicationMap,
                                                                  Map<Long, ProductPublicationTitleDO> titleMap,
                                                                  Map<Long, List<ProductSkuDO>> skuMap,
                                                                  Map<Long, ProductSkuPublicationDO> skuPublicationMap) {
        SubscriptionWindowSpuDO windowSpu = visibleSpu.getWindowSpu();
        ProductSpuDO productSpu = productSpuMap.get(windowSpu.getProductSpuId());
        if (productSpu == null) {
            return null;
        }
        AppSubscriptionPublicationRespVO respVO = new AppSubscriptionPublicationRespVO();
        respVO.setProductSpuId(productSpu.getId());
        respVO.setProductName(productSpu.getName());
        respVO.setCategoryId(productSpu.getCategoryId());
        ProductCategoryDO category = categoryMap.get(productSpu.getCategoryId());
        respVO.setCategoryName(category == null ? null : category.getName());
        respVO.setPicUrl(productSpu.getPicUrl());
        respVO.setPrice(productSpu.getPrice());
        respVO.setKeyword(productSpu.getKeyword());
        respVO.setIntroduction(productSpu.getIntroduction());
        respVO.setDescription(productSpu.getDescription());
        ProductSpuPublicationDO spuPublication = spuPublicationMap.get(productSpu.getId());
        ProductPublicationTitleDO title = spuPublication == null ? null : titleMap.get(spuPublication.getPublicationTitleId());
        respVO.setPublicationTitleName(title == null ? null : title.getName());
        respVO.setRecommendFlag(Boolean.TRUE.equals(windowSpu.getRecommendFlag()));
        Map<Long, ProductSkuDO> skuIdMap = CollectionUtils.convertMap(skuMap.getOrDefault(productSpu.getId(), Collections.emptyList()),
                ProductSkuDO::getId);
        respVO.setSkus(visibleSpu.getWindowSkus().stream()
                .map(windowSku -> buildSkuResp(windowSku, skuIdMap.get(windowSku.getProductSkuId()),
                        skuPublicationMap.get(windowSku.getProductSkuId())))
                .filter(Objects::nonNull)
                .toList());
        return respVO;
    }

    private AppSubscriptionPublicationRespVO.Sku buildSkuResp(SubscriptionWindowSkuDO windowSku,
                                                              ProductSkuDO productSku,
                                                              ProductSkuPublicationDO skuPublication) {
        if (productSku == null) {
            return null;
        }
        AppSubscriptionPublicationRespVO.Sku respVO = new AppSubscriptionPublicationRespVO.Sku();
        respVO.setProductSkuId(productSku.getId());
        respVO.setVolumeLabel(skuPublication == null ? null : skuPublication.getVolumeLabel());
        respVO.setEditionLabel(skuPublication == null ? null : skuPublication.getEditionLabel());
        respVO.setIsbn(skuPublication == null ? null : skuPublication.getIsbn());
        respVO.setPrice(productSku.getPrice());
        respVO.setMaxQuantityPerStudent(windowSku.getMaxQuantityPerStudent());
        return respVO;
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

    private PageResult<AppSubscriptionPublicationRespVO> paginate(List<AppSubscriptionPublicationRespVO> records, Integer pageNo,
                                                                  Integer pageSize) {
        if (CollUtil.isEmpty(records)) {
            return PageResult.empty();
        }
        long total = records.size();
        int safePageNo = pageNo == null || pageNo <= 0 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize <= 0 ? records.size() : pageSize;
        int start = Math.max((safePageNo - 1) * safePageSize, 0);
        if (start >= records.size()) {
            return PageResult.empty(total);
        }
        int end = Math.min(start + safePageSize, records.size());
        return new PageResult<>(records.subList(start, end), total);
    }
}
