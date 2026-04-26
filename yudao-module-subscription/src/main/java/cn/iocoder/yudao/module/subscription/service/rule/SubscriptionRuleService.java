package cn.iocoder.yudao.module.subscription.service.rule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.publication.ProductPublicationApi;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.subscription.controller.admin.rule.vo.SubscriptionRuleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.rule.vo.SubscriptionRulePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.rule.vo.SubscriptionRuleSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionRuleConditionDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionRuleDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionRuleConditionMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionRuleMapper;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleFactorEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleScopeEnum;
import cn.iocoder.yudao.module.subscription.service.offer.SubscriptionOfferService;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum.isExclude;
import static cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleEffectTypeEnum.isInclude;
import static cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleFactorEnum.isValid;

@Service
@Validated
public class SubscriptionRuleService {

    @Resource
    private SubscriptionRuleMapper ruleMapper;
    @Resource
    private SubscriptionRuleConditionMapper conditionMapper;
    @Resource
    private SubscriptionWindowService windowService;
    @Resource
    private SubscriptionOfferService offerService;
    @Resource
    private ProductPublicationApi productPublicationApi;
    @Resource
    private SubscriptionRuleConditionValueService conditionValueService;

    private static final String OPERATOR_EQ = "EQ";

    @Transactional(rollbackFor = Exception.class)
    public Long createRule(SubscriptionRuleSaveReqVO reqVO) {
        validateRuleSave(reqVO);
        SubscriptionRuleDO rule = BeanUtils.toBean(reqVO, SubscriptionRuleDO.class);
        ruleMapper.insert(rule);
        saveConditions(rule.getId(), reqVO);
        return rule.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRule(SubscriptionRuleSaveReqVO reqVO) {
        validateRuleExists(reqVO.getId());
        validateRuleSave(reqVO);
        ruleMapper.updateById(BeanUtils.toBean(reqVO, SubscriptionRuleDO.class));
        conditionMapper.deleteByRuleId(reqVO.getId());
        saveConditions(reqVO.getId(), reqVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        validateRuleExists(id);
        conditionMapper.deleteByRuleId(id);
        ruleMapper.deleteById(id);
    }

    public SubscriptionRuleDO getRule(Long id) {
        return id == null ? null : ruleMapper.selectById(id);
    }

    public SubscriptionRuleDO validateRuleExists(Long id) {
        SubscriptionRuleDO rule = getRule(id);
        if (rule == null) {
            throw exception(RULE_NOT_EXISTS);
        }
        return rule;
    }

    public PageResult<SubscriptionRuleRespVO> getRulePage(SubscriptionRulePageReqVO reqVO) {
        validateRulePage(reqVO);
        PageResult<SubscriptionRuleDO> pageResult = ruleMapper.selectPage(reqVO);
        return new PageResult<>(buildRuleRespList(pageResult.getList()), pageResult.getTotal());
    }

    public SubscriptionRuleRespVO getRuleResp(Long id) {
        return buildRuleRespList(Collections.singletonList(validateRuleExists(id))).get(0);
    }

    public List<SubscriptionRuleDO> getRuleListByWindowId(Long windowId) {
        return ruleMapper.selectListByWindowId(windowId);
    }

    public Map<Long, List<SubscriptionRuleConditionDO>> getConditionMap(List<SubscriptionRuleDO> rules) {
        return convertMultiMap(conditionMapper.selectListByRuleIds(convertSet(rules, SubscriptionRuleDO::getId)),
                SubscriptionRuleConditionDO::getRuleId);
    }

    private void validateRuleSave(SubscriptionRuleSaveReqVO reqVO) {
        windowService.validateWindowExists(reqVO.getWindowId());
        if (reqVO.getOfferId() != null) {
            SubscriptionWindowOfferDO offer = offerService.validateOfferExists(reqVO.getOfferId());
            if (!Objects.equals(offer.getWindowId(), reqVO.getWindowId())) {
                throw exception(RULE_OFFER_WINDOW_NOT_MATCHED);
            }
        }
        if (!isInclude(reqVO.getEffectType()) && !isExclude(reqVO.getEffectType())) {
            throw exception(RULE_EFFECT_INVALID);
        }
        if (CollUtil.isEmpty(reqVO.getConditions())) {
            throw exception(RULE_CONDITION_REQUIRED);
        }
        reqVO.getConditions().forEach(condition -> {
            if (!isValid(condition.getFactor())) {
                throw exception(RULE_FACTOR_INVALID);
            }
            if (StrUtil.isBlank(condition.getOperator())) {
                condition.setOperator(OPERATOR_EQ);
            }
            if (!OPERATOR_EQ.equals(condition.getOperator())) {
                throw exception(RULE_OPERATOR_INVALID);
            }
            conditionValueService.validateAndGetValueName(condition.getFactor(), condition.getValue(),
                    reqVO.getWindowId(), reqVO.getOfferId());
        });
    }

    private void validateRulePage(SubscriptionRulePageReqVO reqVO) {
        windowService.validateWindowExists(reqVO.getWindowId());
        if (!SubscriptionRuleScopeEnum.isValid(reqVO.getScope())) {
            throw exception(RULE_SCOPE_INVALID);
        }
        if (SubscriptionRuleScopeEnum.isOffer(reqVO.getScope())) {
            SubscriptionWindowOfferDO offer = offerService.validateOfferExists(reqVO.getOfferId());
            if (!Objects.equals(offer.getWindowId(), reqVO.getWindowId())) {
                throw exception(RULE_OFFER_WINDOW_NOT_MATCHED);
            }
        }
    }

    private void saveConditions(Long ruleId, SubscriptionRuleSaveReqVO reqVO) {
        List<SubscriptionRuleConditionDO> conditions = convertList(reqVO.getConditions(), item ->
                BeanUtils.toBean(item, SubscriptionRuleConditionDO.class)
                        .setRuleId(ruleId)
                        .setOperator(OPERATOR_EQ)
                        .setValueName(conditionValueService.validateAndGetValueName(item.getFactor(), item.getValue(),
                                reqVO.getWindowId(), reqVO.getOfferId())));
        conditionMapper.insertBatch(conditions);
    }

    private List<SubscriptionRuleRespVO> buildRuleRespList(List<SubscriptionRuleDO> rules) {
        if (CollUtil.isEmpty(rules)) {
            return Collections.emptyList();
        }
        Map<Long, List<SubscriptionRuleConditionDO>> conditionMap = getConditionMap(rules);
        Map<Long, SubscriptionWindowOfferDO> offerMap = convertMap(
                offerService == null ? Collections.emptyList()
                        : rules.stream().map(SubscriptionRuleDO::getOfferId).filter(Objects::nonNull)
                        .map(offerService::getOffer).filter(Objects::nonNull).toList(),
                SubscriptionWindowOfferDO::getId);
        Map<Long, ProductPublicationRespDTO> publicationMap = productPublicationApi.getPublicationList(
                        convertSet(offerMap.values(), SubscriptionWindowOfferDO::getProductSpuId))
                .stream().collect(java.util.stream.Collectors.toMap(ProductPublicationRespDTO::getId, item -> item));
        return rules.stream().map(rule -> {
            SubscriptionRuleRespVO respVO = BeanUtils.toBean(rule, SubscriptionRuleRespVO.class);
            SubscriptionWindowOfferDO offer = offerMap.get(rule.getOfferId());
            ProductPublicationRespDTO publication = offer == null ? null : publicationMap.get(offer.getProductSpuId());
            respVO.setOfferProductName(publication == null ? null : publication.getName());
            respVO.setConditions(convertList(conditionMap.get(rule.getId()), condition ->
                    BeanUtils.toBean(condition, SubscriptionRuleRespVO.Condition.class)));
            return respVO;
        }).toList();
    }
}
