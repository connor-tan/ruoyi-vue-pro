package cn.iocoder.yudao.module.subscription.api.order;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuRuleDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSkuMapper;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSkuPeriodUtils;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityService;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionGradeResolveRespBO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionSpuVisibilityDecisionBO;
import cn.iocoder.yudao.module.subscription.service.visibility.bo.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import cn.iocoder.yudao.module.subscription.service.windowspu.SubscriptionWindowSpuService;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Service
@Validated
public class SubscriptionOrderEligibilityApiImpl implements SubscriptionOrderEligibilityApi {

    @Resource
    private SubscriptionWindowService subscriptionWindowService;
    @Resource
    private SubscriptionWindowSpuService subscriptionWindowSpuService;
    @Resource
    private SubscriptionWindowSkuMapper subscriptionWindowSkuMapper;
    @Resource
    private SubscriptionVisibilityService subscriptionVisibilityService;
    @Resource
    private SubscriptionSupportService subscriptionSupportService;

    @Override
    public List<SubscriptionOrderEligibilityRespDTO> validateOrderItems(SubscriptionOrderEligibilityReqDTO reqDTO) {
        if (reqDTO == null || CollUtil.isEmpty(reqDTO.getItems())) {
            return Collections.emptyList();
        }
        SubscriptionWindowDO currentWindow = subscriptionWindowService.getCurrentOpenWindow();
        if (currentWindow == null) {
            throw exception(ErrorCodeConstants.WINDOW_CURRENT_NOT_EXISTS);
        }
        Map<Long, SubscriptionWindowSkuDO> windowSkuMap = getWindowSkuMap(reqDTO.getItems());
        Map<Long, ProductSkuPublicationDO> skuPublicationMap = subscriptionSupportService.getSkuPublicationMap(
                convertSet(windowSkuMap.values(), SubscriptionWindowSkuDO::getProductSkuId));
        Map<Long, SubscriptionWindowSpuDO> windowSpuMap = getCurrentWindowSpuMap(currentWindow);
        Map<Long, StudentDO> studentMap = getStudentMap(reqDTO.getItems());
        Map<Long, SubscriptionVisibilityResultBO> visibilityMap = studentMap.keySet().stream()
                .collect(Collectors.toMap(studentId -> studentId,
                        studentId -> subscriptionVisibilityService.calculate(studentId, currentWindow.getId())));

        List<SubscriptionOrderEligibilityRespDTO> result = new java.util.ArrayList<>(reqDTO.getItems().size());
        for (int i = 0; i < reqDTO.getItems().size(); i++) {
            SubscriptionOrderEligibilityReqDTO.Item item = reqDTO.getItems().get(i);
            result.add(validateOrderItem(reqDTO.getUserId(), item, i, currentWindow, windowSkuMap, windowSpuMap,
                    skuPublicationMap, studentMap, visibilityMap));
        }
        return result;
    }

    private Map<Long, SubscriptionWindowSkuDO> getWindowSkuMap(List<SubscriptionOrderEligibilityReqDTO.Item> items) {
        Set<Long> windowSkuIds = convertSet(items, SubscriptionOrderEligibilityReqDTO.Item::getWindowSkuId,
                item -> item.getWindowSkuId() != null);
        if (windowSkuIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return convertMap(subscriptionWindowSkuMapper.selectList(SubscriptionWindowSkuDO::getId, windowSkuIds),
                SubscriptionWindowSkuDO::getId);
    }

    private Map<Long, SubscriptionWindowSpuDO> getCurrentWindowSpuMap(SubscriptionWindowDO currentWindow) {
        return convertMap(subscriptionWindowSpuService.getWindowSpuDOListByWindowId(currentWindow.getId()),
                SubscriptionWindowSpuDO::getId);
    }

    private Map<Long, StudentDO> getStudentMap(List<SubscriptionOrderEligibilityReqDTO.Item> items) {
        Set<Long> studentIds = convertSet(items, SubscriptionOrderEligibilityReqDTO.Item::getStudentId,
                item -> item.getStudentId() != null);
        if (studentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return studentIds.stream()
                .map(subscriptionSupportService::getStudent)
                .collect(Collectors.toMap(StudentDO::getId, item -> item));
    }

    private SubscriptionOrderEligibilityRespDTO validateOrderItem(Long userId,
                                                                 SubscriptionOrderEligibilityReqDTO.Item item,
                                                                 int fallbackIndex,
                                                                 SubscriptionWindowDO currentWindow,
                                                                 Map<Long, SubscriptionWindowSkuDO> windowSkuMap,
                                                                 Map<Long, SubscriptionWindowSpuDO> windowSpuMap,
                                                                 Map<Long, ProductSkuPublicationDO> skuPublicationMap,
                                                                 Map<Long, StudentDO> studentMap,
                                                                 Map<Long, SubscriptionVisibilityResultBO> visibilityMap) {
        if (item.getCount() == null || item.getCount() <= 0) {
            throw exception(ErrorCodeConstants.ORDER_ITEM_COUNT_INVALID);
        }
        StudentDO student = studentMap.get(item.getStudentId());
        if (student == null || !Objects.equals(student.getBelongTo(), userId)) {
            throw exception(ErrorCodeConstants.APP_STUDENT_NOT_BELONG_TO_PARENT);
        }
        SubscriptionWindowSkuDO windowSku = windowSkuMap.get(item.getWindowSkuId());
        if (windowSku == null) {
            throw exception(ErrorCodeConstants.WINDOW_SKU_NOT_EXISTS);
        }
        if (!CommonStatusEnum.isEnable(windowSku.getStatus())) {
            throw exception(ErrorCodeConstants.ORDER_WINDOW_SKU_NOT_AVAILABLE);
        }
        if (!Objects.equals(windowSku.getProductSkuId(), item.getSkuId())) {
            throw exception(ErrorCodeConstants.ORDER_WINDOW_SKU_PRODUCT_SKU_MISMATCH);
        }
        SubscriptionWindowSpuDO windowSpu = windowSpuMap.get(windowSku.getWindowSpuId());
        if (windowSpu == null || !Objects.equals(windowSpu.getWindowId(), currentWindow.getId())) {
            throw exception(ErrorCodeConstants.ORDER_WINDOW_SKU_NOT_AVAILABLE);
        }
        if (!SubscriptionSkuPeriodUtils.isMatched(skuPublicationMap.get(windowSku.getProductSkuId()),
                currentWindow.getTargetPeriod())) {
            throw exception(ErrorCodeConstants.ORDER_WINDOW_SKU_TARGET_PERIOD_NOT_MATCHED);
        }

        SubscriptionVisibilityResultBO visibilityResult = visibilityMap.get(item.getStudentId());
        if (visibilityResult == null || visibilityResult.getBlockedReason() != null) {
            throw exception(ErrorCodeConstants.PREVIEW_STUDENT_BLOCKED,
                    visibilityResult == null ? "" : visibilityResult.getBlockedReasonDesc());
        }
        SubscriptionSpuVisibilityDecisionBO decision = visibilityResult.getDecisions().stream()
                .filter(candidate -> Objects.equals(candidate.getWindowSpu().getId(), windowSpu.getId()))
                .findFirst()
                .orElseThrow(() -> exception(ErrorCodeConstants.APP_PUBLICATION_NOT_VISIBLE));
        if (!Boolean.TRUE.equals(decision.getVisible())
                || decision.getEnabledSkus().stream().noneMatch(candidate -> Objects.equals(candidate.getId(), windowSku.getId()))) {
            throw exception(ErrorCodeConstants.APP_PUBLICATION_NOT_VISIBLE);
        }
        return buildResp(item, fallbackIndex, currentWindow, windowSku, windowSpu, visibilityResult.getGradeResolve(), decision);
    }

    private SubscriptionOrderEligibilityRespDTO buildResp(SubscriptionOrderEligibilityReqDTO.Item item,
                                                         int fallbackIndex,
                                                         SubscriptionWindowDO currentWindow,
                                                         SubscriptionWindowSkuDO windowSku,
                                                         SubscriptionWindowSpuDO windowSpu,
                                                         SubscriptionGradeResolveRespBO gradeResolve,
                                                         SubscriptionSpuVisibilityDecisionBO decision) {
        SubscriptionOrderEligibilityRespDTO respDTO = new SubscriptionOrderEligibilityRespDTO();
        respDTO.setRequestIndex(item.getRequestIndex() == null ? fallbackIndex : item.getRequestIndex());
        respDTO.setStudentId(gradeResolve.getStudentId());
        respDTO.setStudentName(gradeResolve.getStudentName());
        respDTO.setSchoolId(gradeResolve.getSchoolId());
        respDTO.setSchoolName(gradeResolve.getSchoolName());
        respDTO.setGradeCatalogId(gradeResolve.getEffectiveGradeCatalogId());
        respDTO.setGradeNo(gradeResolve.getEffectiveGradeNo());
        respDTO.setGradeName(gradeResolve.getEffectiveGradeName());
        respDTO.setWindowId(currentWindow.getId());
        respDTO.setWindowNameSnapshot(currentWindow.getName());
        respDTO.setTargetYearStart(currentWindow.getTargetYearStart());
        respDTO.setTargetYearEnd(currentWindow.getTargetYearEnd());
        respDTO.setTargetPeriod(currentWindow.getTargetPeriod());
        respDTO.setWindowSpuId(windowSpu.getId());
        respDTO.setWindowSkuId(windowSku.getId());
        respDTO.setProductSpuId(windowSpu.getProductSpuId());
        respDTO.setProductSkuId(windowSku.getProductSkuId());
        respDTO.setVisibilityReason(decision.getReason());
        respDTO.setVisibilityReasonDesc(decision.getReasonDesc());
        SubscriptionWindowSpuRuleDO matchedRule = decision.getMatchedRule();
        if (matchedRule != null) {
            respDTO.setMatchedRuleId(matchedRule.getId());
            respDTO.setMatchedRuleEffectType(matchedRule.getEffectType());
            respDTO.setMatchedRuleScopeType(matchedRule.getScopeType());
        }
        respDTO.setGradeApplicabilityOverride(Boolean.TRUE.equals(decision.getGradeApplicabilityOverride()));
        respDTO.setMaxQuantityPerStudent(windowSku.getMaxQuantityPerStudent());
        return respDTO;
    }
}
