package cn.iocoder.yudao.module.subscription.service.windowpublicationrule;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule.vo.SubscriptionWindowPublicationRulePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule.vo.SubscriptionWindowPublicationRuleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule.vo.SubscriptionWindowPublicationRuleSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowPublicationRuleDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowPublicationRuleMapper;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleScopeTypeEnum;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import cn.iocoder.yudao.module.subscription.service.windowpublication.SubscriptionWindowPublicationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class SubscriptionWindowPublicationRuleServiceImpl implements SubscriptionWindowPublicationRuleService {

    @Resource
    private SubscriptionWindowPublicationRuleMapper subscriptionWindowPublicationRuleMapper;
    @Resource
    private SubscriptionWindowPublicationService subscriptionWindowPublicationService;
    @Resource
    private SubscriptionSupportService subscriptionSupportService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWindowPublicationRule(SubscriptionWindowPublicationRuleSaveReqVO createReqVO) {
        validateRuleData(createReqVO);
        SubscriptionWindowPublicationRuleDO rule = BeanUtils.toBean(createReqVO, SubscriptionWindowPublicationRuleDO.class);
        subscriptionWindowPublicationRuleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWindowPublicationRule(SubscriptionWindowPublicationRuleSaveReqVO updateReqVO) {
        SubscriptionWindowPublicationRuleDO oldRule = validateWindowPublicationRuleExists(updateReqVO.getId());
        validateRuleData(updateReqVO);
        SubscriptionWindowPublicationRuleDO updateObj = BeanUtils.toBean(updateReqVO, SubscriptionWindowPublicationRuleDO.class);
        updateObj.setId(oldRule.getId());
        subscriptionWindowPublicationRuleMapper.updateById(updateObj);
    }

    @Override
    public List<SubscriptionWindowPublicationRuleRespVO> getWindowPublicationRuleList(SubscriptionWindowPublicationRulePageReqVO pageReqVO) {
        List<SubscriptionWindowPublicationRuleDO> rules = subscriptionWindowPublicationRuleMapper.selectPageList(pageReqVO);
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, SchoolDO> schoolMap = subscriptionSupportService.getSchoolMap(rules.stream()
                .map(SubscriptionWindowPublicationRuleDO::getSchoolId).filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<Long, GradeCatalogDO> gradeCatalogMap = subscriptionSupportService.getGradeCatalogMap(rules.stream()
                .map(SubscriptionWindowPublicationRuleDO::getGradeCatalogId).filter(Objects::nonNull).collect(Collectors.toSet()));
        return rules.stream().map(rule -> buildRuleResp(rule, schoolMap, gradeCatalogMap)).toList();
    }

    @Override
    public List<SubscriptionWindowPublicationRuleDO> getWindowPublicationRuleDOList(Collection<Long> windowPublicationIds) {
        return subscriptionWindowPublicationRuleMapper.selectListByWindowPublicationIds(windowPublicationIds);
    }

    private SubscriptionWindowPublicationRuleDO validateWindowPublicationRuleExists(Long id) {
        SubscriptionWindowPublicationRuleDO rule = subscriptionWindowPublicationRuleMapper.selectById(id);
        if (rule == null) {
            throw exception(ErrorCodeConstants.WINDOW_PUBLICATION_RULE_NOT_EXISTS);
        }
        return rule;
    }

    private void validateRuleData(SubscriptionWindowPublicationRuleSaveReqVO reqVO) {
        subscriptionWindowPublicationService.getWindowPublicationDO(reqVO.getWindowPublicationId());
        switch (SubscriptionRuleScopeTypeEnum.valueOf(reqVO.getScopeType())) {
            case ALL -> {
                if (reqVO.getSchoolId() != null || reqVO.getGradeCatalogId() != null) {
                    throw exception(ErrorCodeConstants.WINDOW_PUBLICATION_RULE_SCOPE_INVALID);
                }
            }
            case SCHOOL -> {
                if (reqVO.getSchoolId() == null || reqVO.getGradeCatalogId() != null) {
                    throw exception(ErrorCodeConstants.WINDOW_PUBLICATION_RULE_SCOPE_INVALID);
                }
                if (subscriptionSupportService.getSchool(reqVO.getSchoolId()) == null) {
                    throw exception(ErrorCodeConstants.SUPPORT_SCHOOL_NOT_EXISTS);
                }
            }
            case GRADE -> {
                if (reqVO.getSchoolId() != null || reqVO.getGradeCatalogId() == null) {
                    throw exception(ErrorCodeConstants.WINDOW_PUBLICATION_RULE_SCOPE_INVALID);
                }
                subscriptionSupportService.validateGradeCatalogIds(Collections.singleton(reqVO.getGradeCatalogId()));
            }
            case SCHOOL_GRADE -> {
                if (reqVO.getSchoolId() == null || reqVO.getGradeCatalogId() == null) {
                    throw exception(ErrorCodeConstants.WINDOW_PUBLICATION_RULE_SCOPE_INVALID);
                }
                if (subscriptionSupportService.getSchool(reqVO.getSchoolId()) == null) {
                    throw exception(ErrorCodeConstants.SUPPORT_SCHOOL_NOT_EXISTS);
                }
                subscriptionSupportService.validateGradeCatalogIds(Collections.singleton(reqVO.getGradeCatalogId()));
            }
            default -> throw exception(ErrorCodeConstants.WINDOW_PUBLICATION_RULE_SCOPE_INVALID);
        }
    }

    private SubscriptionWindowPublicationRuleRespVO buildRuleResp(SubscriptionWindowPublicationRuleDO rule,
                                                                  Map<Long, SchoolDO> schoolMap,
                                                                  Map<Long, GradeCatalogDO> gradeCatalogMap) {
        SubscriptionWindowPublicationRuleRespVO respVO = BeanUtils.toBean(rule, SubscriptionWindowPublicationRuleRespVO.class);
        SchoolDO school = schoolMap.get(rule.getSchoolId());
        if (school != null) {
            respVO.setSchoolName(school.getSchoolName());
        }
        GradeCatalogDO gradeCatalog = gradeCatalogMap.get(rule.getGradeCatalogId());
        if (gradeCatalog != null) {
            respVO.setGradeName(gradeCatalog.getGradeName());
            respVO.setGradeAliasName(gradeCatalog.getAliasName());
        }
        return respVO;
    }
}
