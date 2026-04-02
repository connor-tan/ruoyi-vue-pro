package cn.iocoder.yudao.module.subscription.service.windowpublication;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublication.vo.SubscriptionWindowPublicationPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublication.vo.SubscriptionWindowPublicationRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublication.vo.SubscriptionWindowPublicationSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowPublicationDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowPublicationGradeDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowPublicationGradeMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowPublicationMapper;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class SubscriptionWindowPublicationServiceImpl implements SubscriptionWindowPublicationService {

    @Resource
    private SubscriptionWindowPublicationMapper subscriptionWindowPublicationMapper;
    @Resource
    private SubscriptionWindowPublicationGradeMapper subscriptionWindowPublicationGradeMapper;
    @Resource
    private SubscriptionWindowMapper subscriptionWindowMapper;
    @Resource
    private SubscriptionSupportService subscriptionSupportService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWindowPublication(SubscriptionWindowPublicationSaveReqVO createReqVO) {
        validateWindowPublicationData(null, createReqVO);
        SubscriptionWindowPublicationDO windowPublication = BeanUtils.toBean(createReqVO, SubscriptionWindowPublicationDO.class);
        subscriptionWindowPublicationMapper.insert(windowPublication);
        replaceWindowPublicationGrades(windowPublication.getId(), createReqVO.getGradeCatalogIds());
        return windowPublication.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWindowPublication(SubscriptionWindowPublicationSaveReqVO updateReqVO) {
        SubscriptionWindowPublicationDO oldWindowPublication = validateWindowPublicationExists(updateReqVO.getId());
        validateWindowPublicationData(oldWindowPublication, updateReqVO);
        SubscriptionWindowPublicationDO updateObj = BeanUtils.toBean(updateReqVO, SubscriptionWindowPublicationDO.class);
        subscriptionWindowPublicationMapper.updateById(updateObj);
        replaceWindowPublicationGrades(updateReqVO.getId(), updateReqVO.getGradeCatalogIds());
    }

    @Override
    public PageResult<SubscriptionWindowPublicationRespVO> getWindowPublicationPage(SubscriptionWindowPublicationPageReqVO pageReqVO) {
        List<ProductSpuDO> productSpuList = subscriptionSupportService.getProductSpuList(pageReqVO.getProductName(), false);
        Set<Long> productSpuIds = productSpuList.stream().map(ProductSpuDO::getId).collect(Collectors.toSet());
        if (pageReqVO.getProductName() != null && productSpuIds.isEmpty()) {
            return PageResult.empty();
        }
        PageResult<SubscriptionWindowPublicationDO> pageResult =
                subscriptionWindowPublicationMapper.selectPage(pageReqVO, productSpuIds);
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }
        List<Long> productIds = pageResult.getList().stream().map(SubscriptionWindowPublicationDO::getProductSpuId).distinct().toList();
        List<Long> windowIds = pageResult.getList().stream().map(SubscriptionWindowPublicationDO::getWindowId).distinct().toList();
        List<Long> windowPublicationIds = pageResult.getList().stream().map(SubscriptionWindowPublicationDO::getId).toList();
        Map<Long, ProductSpuDO> productSpuMap = subscriptionSupportService.getProductSpuMap(productIds);
        Map<Long, ProductCategoryDO> categoryMap = subscriptionSupportService.getCategoryMap(productSpuMap.values().stream()
                .map(ProductSpuDO::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<Long, SubscriptionWindowDO> windowMap = CollectionUtils.convertMap(subscriptionWindowMapper.selectBatchIds(windowIds),
                SubscriptionWindowDO::getId);
        Map<Long, List<SubscriptionWindowPublicationGradeDO>> gradeMap = getGradeDOMap(windowPublicationIds);
        Map<Long, GradeCatalogDO> gradeCatalogMap = subscriptionSupportService.getGradeCatalogMap(gradeMap.values().stream()
                .flatMap(List::stream).map(SubscriptionWindowPublicationGradeDO::getGradeCatalogId).collect(Collectors.toSet()));
        return new PageResult<>(pageResult.getList().stream()
                .map(windowPublication -> buildWindowPublicationResp(windowPublication, windowMap.get(windowPublication.getWindowId()),
                        productSpuMap.get(windowPublication.getProductSpuId()), categoryMap,
                        gradeMap.getOrDefault(windowPublication.getId(), Collections.emptyList()), gradeCatalogMap))
                .toList(), pageResult.getTotal());
    }

    @Override
    public SubscriptionWindowPublicationDO getWindowPublicationDO(Long id) {
        return validateWindowPublicationExists(id);
    }

    @Override
    public List<SubscriptionWindowPublicationDO> getWindowPublicationDOListByWindowId(Long windowId) {
        return subscriptionWindowPublicationMapper.selectListByWindowId(windowId);
    }

    @Override
    public Map<Long, List<SubscriptionWindowPublicationGradeDO>> getGradeDOMap(Collection<Long> windowPublicationIds) {
        return CollectionUtils.convertMultiMap(subscriptionWindowPublicationGradeMapper.selectListByWindowPublicationIds(windowPublicationIds),
                SubscriptionWindowPublicationGradeDO::getWindowPublicationId);
    }

    private SubscriptionWindowPublicationDO validateWindowPublicationExists(Long id) {
        SubscriptionWindowPublicationDO windowPublication = subscriptionWindowPublicationMapper.selectById(id);
        if (windowPublication == null) {
            throw exception(ErrorCodeConstants.WINDOW_PUBLICATION_NOT_EXISTS);
        }
        return windowPublication;
    }

    private void validateWindowPublicationData(SubscriptionWindowPublicationDO oldWindowPublication,
                                               SubscriptionWindowPublicationSaveReqVO reqVO) {
        if (subscriptionWindowMapper.selectById(reqVO.getWindowId()) == null) {
            throw exception(ErrorCodeConstants.WINDOW_NOT_EXISTS);
        }
        ProductSpuDO productSpu = subscriptionSupportService.validateProductSpu(reqVO.getProductSpuId(), true);
        subscriptionSupportService.validatePublicationTypeCategory(productSpu.getCategoryId());
        subscriptionSupportService.validateSingleSpecProduct(productSpu);
        subscriptionSupportService.validateGradeCatalogIds(reqVO.getGradeCatalogIds());
        validateWindowPublicationDuplicate(oldWindowPublication, reqVO.getWindowId(), reqVO.getProductSpuId());
    }

    private void validateWindowPublicationDuplicate(SubscriptionWindowPublicationDO oldWindowPublication, Long windowId,
                                                    Long productSpuId) {
        SubscriptionWindowPublicationDO exists = subscriptionWindowPublicationMapper.selectByWindowIdAndProductSpuId(windowId, productSpuId);
        if (exists == null) {
            return;
        }
        if (oldWindowPublication == null || !Objects.equals(exists.getId(), oldWindowPublication.getId())) {
            throw exception(ErrorCodeConstants.WINDOW_PUBLICATION_DUPLICATE);
        }
    }

    private void replaceWindowPublicationGrades(Long windowPublicationId, List<Long> gradeCatalogIds) {
        if (CollUtil.isEmpty(gradeCatalogIds)) {
            throw exception(ErrorCodeConstants.WINDOW_PUBLICATION_GRADE_EMPTY);
        }
        subscriptionWindowPublicationGradeMapper.deleteByWindowPublicationId(windowPublicationId);
        gradeCatalogIds.stream().distinct().map(gradeCatalogId -> SubscriptionWindowPublicationGradeDO.builder()
                .windowPublicationId(windowPublicationId)
                .gradeCatalogId(gradeCatalogId)
                .build()).forEach(subscriptionWindowPublicationGradeMapper::insert);
    }

    private SubscriptionWindowPublicationRespVO buildWindowPublicationResp(SubscriptionWindowPublicationDO windowPublication,
                                                                           SubscriptionWindowDO window,
                                                                           ProductSpuDO productSpu,
                                                                           Map<Long, ProductCategoryDO> categoryMap,
                                                                           List<SubscriptionWindowPublicationGradeDO> grades,
                                                                           Map<Long, GradeCatalogDO> gradeCatalogMap) {
        SubscriptionWindowPublicationRespVO respVO = BeanUtils.toBean(windowPublication, SubscriptionWindowPublicationRespVO.class);
        respVO.setRecommendFlag(Boolean.TRUE.equals(windowPublication.getRecommendFlag()));
        respVO.setMaxQuantityPerStudent(windowPublication.getMaxQuantityPerStudent() != null
                ? windowPublication.getMaxQuantityPerStudent() : 1);
        if (window != null) {
            respVO.setWindowName(window.getName());
        }
        if (productSpu != null) {
            respVO.setProductName(productSpu.getName());
            respVO.setCategoryId(productSpu.getCategoryId());
            ProductCategoryDO category = categoryMap.get(productSpu.getCategoryId());
            respVO.setCategoryName(category != null ? category.getName() : null);
            respVO.setPrice(productSpu.getPrice());
            respVO.setPicUrl(productSpu.getPicUrl());
        }
        List<Long> gradeCatalogIds = grades.stream().map(SubscriptionWindowPublicationGradeDO::getGradeCatalogId)
                .distinct().toList();
        respVO.setGradeCatalogIds(gradeCatalogIds);
        respVO.setGradeNames(gradeCatalogIds.stream().map(gradeCatalogMap::get).filter(Objects::nonNull)
                .map(grade -> grade.getGradeNo() + "/" + grade.getGradeName()
                        + (grade.getAliasName() == null ? "" : "（" + grade.getAliasName() + "）"))
                .collect(Collectors.joining("、")));
        return respVO;
    }
}
