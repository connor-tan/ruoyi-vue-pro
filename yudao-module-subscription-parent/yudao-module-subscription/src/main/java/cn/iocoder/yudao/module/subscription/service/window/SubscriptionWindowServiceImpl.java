package cn.iocoder.yudao.module.subscription.service.window;

import com.baomidou.dynamic.datasource.annotation.Master;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuGradeDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowEnablePrecheckRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowSaveReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowUpdateStatusReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowTemplateDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuGradeDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuRuleDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSkuMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuGradeMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuRuleMapper;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeCalcRuleEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeResolveModeEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleScopeTypeEnum;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSkuPeriodUtils;
import cn.iocoder.yudao.module.subscription.service.windowtemplate.SubscriptionWindowTemplateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class SubscriptionWindowServiceImpl implements SubscriptionWindowService {

    @Resource
    private SubscriptionWindowMapper subscriptionWindowMapper;
    @Resource
    private SubscriptionSupportService subscriptionSupportService;
    @Resource
    private SubscriptionWindowTemplateService subscriptionWindowTemplateService;
    @Resource
    private SubscriptionWindowSpuMapper subscriptionWindowSpuMapper;
    @Resource
    private SubscriptionWindowSpuRuleMapper subscriptionWindowSpuRuleMapper;
    @Resource
    private SubscriptionWindowSkuMapper subscriptionWindowSkuMapper;
    @Resource
    private SubscriptionWindowSpuGradeMapper subscriptionWindowSpuGradeMapper;

    @Override
    @Master
    public Long createWindow(SubscriptionWindowSaveReqVO createReqVO) {
        validateWindowTime(createReqVO.getStartTime(), createReqVO.getEndTime());
        validateTargetYear(createReqVO.getTargetYearStart(), createReqVO.getTargetYearEnd());
        validateEnableConflict(null, createReqVO.getStatus(), createReqVO.getStartTime(), createReqVO.getEndTime());
        if (CommonStatusEnum.isEnable(createReqVO.getStatus())) {
            throw exception(ErrorCodeConstants.WINDOW_ENABLE_PRECHECK_FAILED, "新建窗口需先保存为停用状态，配置刊物和 SKU 后再启用");
        }
        SubscriptionWindowTemplateDO template = subscriptionWindowTemplateService.getEnabledWindowTemplate(createReqVO.getTemplateId());
        SubscriptionWindowDO subscriptionWindow = BeanUtils.toBean(createReqVO, SubscriptionWindowDO.class);
        applyTemplateSnapshot(subscriptionWindow, template);
        subscriptionWindowMapper.insert(subscriptionWindow);
        return subscriptionWindow.getId();
    }

    @Override
    @Master
    public void updateWindow(SubscriptionWindowSaveReqVO updateReqVO) {
        SubscriptionWindowDO oldWindow = validateWindowExists(updateReqVO.getId());
        validateWindowTime(updateReqVO.getStartTime(), updateReqVO.getEndTime());
        validateTargetYear(updateReqVO.getTargetYearStart(), updateReqVO.getTargetYearEnd());
        validateEnableConflict(oldWindow.getId(), updateReqVO.getStatus(), updateReqVO.getStartTime(), updateReqVO.getEndTime());
        if (!CommonStatusEnum.isEnable(oldWindow.getStatus()) && CommonStatusEnum.isEnable(updateReqVO.getStatus())) {
            validateEnablePrecheck(oldWindow.getId(), updateReqVO.getStatus(), false);
        }
        Long nextTemplateId = updateReqVO.getTemplateId() != null ? updateReqVO.getTemplateId() : oldWindow.getTemplateId();
        boolean templateChanged = !Objects.equals(oldWindow.getTemplateId(), nextTemplateId);
        if (templateChanged && hasWindowConfig(oldWindow.getId())) {
            throw exception(ErrorCodeConstants.WINDOW_TEMPLATE_SWITCH_LOCKED);
        }
        SubscriptionWindowDO updateObj = BeanUtils.toBean(updateReqVO, SubscriptionWindowDO.class);
        updateObj.setTemplateId(nextTemplateId);
        if (templateChanged) {
            SubscriptionWindowTemplateDO template = subscriptionWindowTemplateService.getEnabledWindowTemplate(nextTemplateId);
            applyTemplateSnapshot(updateObj, template);
        } else {
            updateObj.setTemplateNameSnapshot(oldWindow.getTemplateNameSnapshot());
            updateObj.setTargetPeriod(oldWindow.getTargetPeriod());
            updateObj.setGradeCalcRule(normalizeGradeCalcRule(oldWindow.getGradeCalcRule()));
            updateObj.setGradeResolveMode(normalizeGradeResolveMode(oldWindow.getGradeResolveMode()));
        }
        subscriptionWindowMapper.updateById(updateObj);
    }

    @Override
    public SubscriptionWindowRespVO getWindow(Long id) {
        SubscriptionWindowDO window = subscriptionWindowMapper.selectById(id);
        return window == null ? null : buildWindowResp(window);
    }

    @Override
    public PageResult<SubscriptionWindowRespVO> getWindowPage(SubscriptionWindowPageReqVO pageReqVO) {
        PageResult<SubscriptionWindowDO> pageResult = subscriptionWindowMapper.selectPage(pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }
        return new PageResult<>(pageResult.getList().stream().map(this::buildWindowResp).toList(), pageResult.getTotal());
    }

    @Override
    public List<SubscriptionWindowSimpleRespVO> getWindowSimpleList() {
        return subscriptionWindowMapper.selectAllList().stream().map(window -> {
            SubscriptionWindowSimpleRespVO respVO = BeanUtils.toBean(window, SubscriptionWindowSimpleRespVO.class);
            respVO.setGradeCalcRule(normalizeGradeCalcRule(respVO.getGradeCalcRule()));
            respVO.setGradeResolveMode(normalizeGradeResolveMode(respVO.getGradeResolveMode()));
            respVO.setTargetYearName(buildTargetYearName(window.getTargetYearStart(), window.getTargetYearEnd()));
            return respVO;
        }).toList();
    }

    @Override
    public SubscriptionWindowEnablePrecheckRespVO precheckEnableWindow(Long id) {
        SubscriptionWindowDO window = validateWindowExists(id);
        return buildEnablePrecheck(window.getId());
    }

    @Override
    @Master
    public void updateWindowStatus(SubscriptionWindowUpdateStatusReqVO reqVO) {
        SubscriptionWindowDO window = validateWindowExists(reqVO.getId());
        validateEnableConflict(window.getId(), reqVO.getStatus(), window.getStartTime(), window.getEndTime());
        validateEnablePrecheck(window.getId(), reqVO.getStatus(), Boolean.TRUE.equals(reqVO.getConfirmWarnings()));
        SubscriptionWindowDO updateObj = new SubscriptionWindowDO();
        updateObj.setId(reqVO.getId());
        updateObj.setStatus(reqVO.getStatus());
        subscriptionWindowMapper.updateById(updateObj);
    }

    @Override
    public SubscriptionWindowDO getWindowDO(Long id) {
        return validateWindowExists(id);
    }

    @Override
    public SubscriptionWindowDO getCurrentOpenWindow() {
        return subscriptionWindowMapper.selectCurrentEnabledWindow(LocalDateTime.now());
    }

    private SubscriptionWindowDO validateWindowExists(Long id) {
        SubscriptionWindowDO window = subscriptionWindowMapper.selectById(id);
        if (window == null) {
            throw exception(ErrorCodeConstants.WINDOW_NOT_EXISTS);
        }
        return window;
    }

    private void validateWindowTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw exception(ErrorCodeConstants.WINDOW_TIME_INVALID);
        }
    }

    private void validateTargetYear(Integer targetYearStart, Integer targetYearEnd) {
        subscriptionSupportService.validateWindowYear(targetYearStart, targetYearEnd);
    }

    private void validateEnableConflict(Long currentId, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        if (!CommonStatusEnum.isEnable(status)) {
            return;
        }
        if (subscriptionWindowMapper.countEnabledOverlapWindowExceptId(currentId, startTime, endTime) > 0) {
            throw exception(ErrorCodeConstants.WINDOW_ENABLE_CONFLICT);
        }
    }

    private void validateEnablePrecheck(Long windowId, Integer status, boolean confirmWarnings) {
        if (!CommonStatusEnum.isEnable(status)) {
            return;
        }
        SubscriptionWindowEnablePrecheckRespVO precheck = buildEnablePrecheck(windowId);
        if (!precheck.getBlockers().isEmpty()) {
            throw exception(ErrorCodeConstants.WINDOW_ENABLE_PRECHECK_FAILED, String.join("；", precheck.getBlockers()));
        }
        if (!confirmWarnings && !precheck.getWarnings().isEmpty()) {
            throw exception(ErrorCodeConstants.WINDOW_ENABLE_PRECHECK_WARNING, String.join("；", precheck.getWarnings()));
        }
    }

    private SubscriptionWindowEnablePrecheckRespVO buildEnablePrecheck(Long windowId) {
        SubscriptionWindowDO window = validateWindowExists(windowId);
        List<String> blockers = new ArrayList<>();
        Set<String> warnings = new LinkedHashSet<>();
        List<SubscriptionWindowSpuDO> windowSpus = subscriptionWindowSpuMapper.selectListByWindowId(windowId);
        if (windowSpus.isEmpty()) {
            blockers.add("订刊窗口未配置刊物");
            return buildEnablePrecheckResp(blockers, warnings);
        }
        List<Long> windowSpuIds = CollectionUtils.convertList(windowSpus, SubscriptionWindowSpuDO::getId);
        Set<Long> productSpuIds = CollectionUtils.convertSet(windowSpus, SubscriptionWindowSpuDO::getProductSpuId);
        Map<Long, ProductSpuDO> productSpuMap = subscriptionSupportService.getPublicationSpuMap(productSpuIds);
        Map<Long, List<ProductSpuGradeDO>> productSpuGradeMap = subscriptionSupportService.getPublicationSpuGradeMap(productSpuIds);
        Map<Long, List<SubscriptionWindowSpuGradeDO>> gradeMap = CollectionUtils.convertMultiMap(
                subscriptionWindowSpuGradeMapper.selectListByWindowSpuIds(windowSpuIds),
                SubscriptionWindowSpuGradeDO::getWindowSpuId);
        Map<Long, List<SubscriptionWindowSpuRuleDO>> ruleMap = CollectionUtils.convertMultiMap(
                subscriptionWindowSpuRuleMapper.selectListByWindowSpuIds(windowSpuIds),
                SubscriptionWindowSpuRuleDO::getWindowSpuId);
        Map<Long, List<SubscriptionWindowSkuDO>> skuMap = CollectionUtils.convertMultiMap(
                subscriptionWindowSkuMapper.selectListByWindowSpuIds(windowSpuIds),
                SubscriptionWindowSkuDO::getWindowSpuId);
        Map<Long, ProductSkuPublicationDO> skuPublicationMap = subscriptionSupportService.getSkuPublicationMap(
                skuMap.values().stream()
                        .flatMap(List::stream)
                        .map(SubscriptionWindowSkuDO::getProductSkuId)
                        .collect(Collectors.toSet()));

        for (SubscriptionWindowSpuDO windowSpu : windowSpus) {
            String productName = buildProductName(windowSpu, productSpuMap);
            ProductSpuDO productSpu = productSpuMap.get(windowSpu.getProductSpuId());
            if (productSpu == null || !ProductSpuStatusEnum.isEnable(productSpu.getStatus())) {
                blockers.add("刊物「" + productName + "」商品不存在或未启用");
                continue;
            }
            long enabledMatchingSkuCount = skuMap.getOrDefault(windowSpu.getId(), Collections.emptyList()).stream()
                    .filter(sku -> CommonStatusEnum.isEnable(sku.getStatus()))
                    .filter(sku -> SubscriptionSkuPeriodUtils.isMatched(
                            skuPublicationMap.get(sku.getProductSkuId()), window.getTargetPeriod()))
                    .count();
            long enabledMismatchedSkuCount = skuMap.getOrDefault(windowSpu.getId(), Collections.emptyList()).stream()
                    .filter(sku -> CommonStatusEnum.isEnable(sku.getStatus()))
                    .filter(sku -> !SubscriptionSkuPeriodUtils.isMatched(
                            skuPublicationMap.get(sku.getProductSkuId()), window.getTargetPeriod()))
                    .count();
            if (enabledMatchingSkuCount == 0) {
                blockers.add("刊物「" + productName + "」没有启用且匹配目标周期的 SKU");
            }
            if (enabledMismatchedSkuCount > 0) {
                warnings.add("刊物「" + productName + "」存在已启用但与窗口周期不匹配的 SKU，不会进入 app 可见结果");
            }
            List<SubscriptionWindowSpuRuleDO> includeRules = ruleMap.getOrDefault(windowSpu.getId(), Collections.emptyList()).stream()
                    .filter(rule -> Objects.equals(rule.getEffectType(), SubscriptionRuleEffectTypeEnum.INCLUDE.getType()))
                    .toList();
            if (gradeMap.getOrDefault(windowSpu.getId(), Collections.emptyList()).isEmpty() && !includeRules.isEmpty()) {
                warnings.add("刊物「" + productName + "」未配置基础可见年级，仅依赖特殊允许规则");
            }
            Set<Long> supportedGradeIds = productSpuGradeMap.getOrDefault(windowSpu.getProductSpuId(), Collections.emptyList())
                    .stream()
                    .map(ProductSpuGradeDO::getGradeCatalogId)
                    .collect(Collectors.toSet());
            includeRules.stream()
                    .map(rule -> buildIncludeRuleApplicabilityWarning(productName, rule, supportedGradeIds))
                    .filter(Objects::nonNull)
                    .forEach(warnings::add);
        }
        return buildEnablePrecheckResp(blockers, warnings);
    }

    private SubscriptionWindowEnablePrecheckRespVO buildEnablePrecheckResp(Collection<String> blockers,
                                                                           Collection<String> warnings) {
        SubscriptionWindowEnablePrecheckRespVO respVO = new SubscriptionWindowEnablePrecheckRespVO();
        respVO.setBlockers(new ArrayList<>(blockers));
        respVO.setWarnings(new ArrayList<>(warnings));
        respVO.setPass(blockers.isEmpty());
        return respVO;
    }

    private String buildIncludeRuleApplicabilityWarning(String productName, SubscriptionWindowSpuRuleDO rule,
                                                        Set<Long> supportedGradeIds) {
        if (supportedGradeIds.isEmpty()) {
            return "刊物「" + productName + "」存在特殊允许规则，但商品未配置适用年级";
        }
        if (Objects.equals(rule.getScopeType(), SubscriptionRuleScopeTypeEnum.GRADE.getType())
                || Objects.equals(rule.getScopeType(), SubscriptionRuleScopeTypeEnum.SCHOOL_GRADE.getType())) {
            return supportedGradeIds.contains(rule.getGradeCatalogId()) ? null
                    : "刊物「" + productName + "」存在突破商品适用年级的特殊允许规则";
        }
        if (Objects.equals(rule.getScopeType(), SubscriptionRuleScopeTypeEnum.ALL.getType())
                || Objects.equals(rule.getScopeType(), SubscriptionRuleScopeTypeEnum.SCHOOL.getType())) {
            return "刊物「" + productName + "」存在未限定年级的特殊允许规则，可能突破商品适用年级";
        }
        return null;
    }

    private String buildProductName(SubscriptionWindowSpuDO windowSpu, Map<Long, ProductSpuDO> productSpuMap) {
        ProductSpuDO productSpu = productSpuMap.get(windowSpu.getProductSpuId());
        return productSpu == null ? "SPU#" + windowSpu.getProductSpuId() : productSpu.getName();
    }

    private SubscriptionWindowRespVO buildWindowResp(SubscriptionWindowDO window) {
        SubscriptionWindowRespVO respVO = BeanUtils.toBean(window, SubscriptionWindowRespVO.class);
        respVO.setGradeCalcRule(normalizeGradeCalcRule(respVO.getGradeCalcRule()));
        respVO.setGradeResolveMode(normalizeGradeResolveMode(respVO.getGradeResolveMode()));
        respVO.setTargetYearName(buildTargetYearName(window.getTargetYearStart(), window.getTargetYearEnd()));
        respVO.setTemplateLocked(hasWindowConfig(window.getId()));
        return respVO;
    }

    private void applyTemplateSnapshot(SubscriptionWindowDO window, SubscriptionWindowTemplateDO template) {
        window.setTemplateId(template.getId());
        window.setTemplateNameSnapshot(template.getName());
        window.setTargetPeriod(template.getTargetPeriod());
        window.setGradeCalcRule(normalizeGradeCalcRule(template.getGradeCalcRule()));
        window.setGradeResolveMode(normalizeGradeResolveMode(template.getGradeResolveMode()));
    }

    private boolean hasWindowConfig(Long windowId) {
        return subscriptionWindowSpuMapper.countByWindowId(windowId) > 0
                || subscriptionWindowSpuRuleMapper.countByWindowId(windowId) > 0
                || subscriptionWindowSkuMapper.countByWindowId(windowId) > 0;
    }

    private String normalizeGradeCalcRule(String gradeCalcRule) {
        if (Objects.equals(gradeCalcRule, SubscriptionGradeCalcRuleEnum.PROMOTED_GRADE.getRule())) {
            return SubscriptionGradeCalcRuleEnum.PROMOTED_GRADE.getRule();
        }
        return SubscriptionGradeCalcRuleEnum.CURRENT_GRADE.getRule();
    }

    private String buildTargetYearName(Integer targetYearStart, Integer targetYearEnd) {
        if (targetYearStart == null || targetYearEnd == null) {
            return null;
        }
        return targetYearStart + "-" + targetYearEnd + "学年";
    }

    private String normalizeGradeResolveMode(String gradeResolveMode) {
        if (Objects.equals(gradeResolveMode, SubscriptionGradeResolveModeEnum.TARGET_CLASS_FIRST.getMode())) {
            return SubscriptionGradeResolveModeEnum.TARGET_CLASS_FIRST.getMode();
        }
        return SubscriptionGradeResolveModeEnum.CURRENT_CHAIN.getMode();
    }
}
