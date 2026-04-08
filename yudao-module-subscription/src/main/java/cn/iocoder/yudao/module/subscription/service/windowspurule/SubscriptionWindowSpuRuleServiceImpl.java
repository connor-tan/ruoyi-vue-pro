package cn.iocoder.yudao.module.subscription.service.windowspurule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo.SubscriptionWindowSpuRulePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo.SubscriptionWindowSpuRuleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo.SubscriptionWindowSpuRuleSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuRuleDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuRuleMapper;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleScopeTypeEnum;
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

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class SubscriptionWindowSpuRuleServiceImpl implements SubscriptionWindowSpuRuleService {

    @Resource
    private SubscriptionWindowSpuRuleMapper subscriptionWindowSpuRuleMapper;
    @Resource
    private SubscriptionWindowSpuMapper subscriptionWindowSpuMapper;
    @Resource
    private SubscriptionSupportService subscriptionSupportService;

    @Override
    public PageResult<SubscriptionWindowSpuRuleRespVO> getWindowSpuRulePage(SubscriptionWindowSpuRulePageReqVO reqVO) {
        validateWindowSpuExists(reqVO.getWindowSpuId());
        PageResult<SubscriptionWindowSpuRuleDO> pageResult = subscriptionWindowSpuRuleMapper.selectPage(reqVO);
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }
        return new PageResult<>(buildRespList(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWindowSpuRule(SubscriptionWindowSpuRuleSaveReqVO reqVO) {
        validateWindowSpuExists(reqVO.getWindowSpuId());
        validateRuleData(reqVO);
        validateRuleDuplicate(null, reqVO);
        SubscriptionWindowSpuRuleDO rule = buildRuleDO(reqVO);
        subscriptionWindowSpuRuleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWindowSpuRule(SubscriptionWindowSpuRuleSaveReqVO reqVO) {
        SubscriptionWindowSpuRuleDO oldRule = validateRuleExists(reqVO.getId());
        validateWindowSpuExists(reqVO.getWindowSpuId());
        validateRuleData(reqVO);
        validateRuleDuplicate(oldRule, reqVO);
        SubscriptionWindowSpuRuleDO updateObj = buildRuleDO(reqVO);
        updateObj.setId(reqVO.getId());
        subscriptionWindowSpuRuleMapper.updateById(updateObj);
    }

    @Override
    public void deleteWindowSpuRule(Long id) {
        validateRuleExists(id);
        subscriptionWindowSpuRuleMapper.deleteById(id);
    }

    @Override
    public List<SubscriptionWindowSpuRuleDO> getWindowSpuRuleDOList(Collection<Long> windowSpuIds) {
        return subscriptionWindowSpuRuleMapper.selectListByWindowSpuIds(windowSpuIds);
    }

    private void validateWindowSpuExists(Long windowSpuId) {
        if (subscriptionWindowSpuMapper.selectById(windowSpuId) == null) {
            throw exception(ErrorCodeConstants.WINDOW_SPU_NOT_EXISTS);
        }
    }

    private SubscriptionWindowSpuRuleDO validateRuleExists(Long id) {
        SubscriptionWindowSpuRuleDO rule = subscriptionWindowSpuRuleMapper.selectById(id);
        if (rule == null) {
            throw exception(ErrorCodeConstants.WINDOW_SPU_RULE_NOT_EXISTS);
        }
        return rule;
    }

    private void validateRuleData(SubscriptionWindowSpuRuleSaveReqVO reqVO) {
        if (Objects.equals(reqVO.getScopeType(), SubscriptionRuleScopeTypeEnum.ALL.getType())) {
            reqVO.setSchoolId(null);
            reqVO.setGradeCatalogId(null);
            return;
        }
        if (Objects.equals(reqVO.getScopeType(), SubscriptionRuleScopeTypeEnum.SCHOOL.getType())) {
            validateSchool(reqVO.getSchoolId());
            reqVO.setGradeCatalogId(null);
            return;
        }
        if (Objects.equals(reqVO.getScopeType(), SubscriptionRuleScopeTypeEnum.GRADE.getType())) {
            validateGrade(reqVO.getGradeCatalogId());
            reqVO.setSchoolId(null);
            return;
        }
        if (Objects.equals(reqVO.getScopeType(), SubscriptionRuleScopeTypeEnum.SCHOOL_GRADE.getType())) {
            validateSchool(reqVO.getSchoolId());
            validateGrade(reqVO.getGradeCatalogId());
            return;
        }
        throw exception(ErrorCodeConstants.WINDOW_SPU_RULE_SCOPE_INVALID);
    }

    private void validateSchool(Long schoolId) {
        if (schoolId == null || subscriptionSupportService.getSchool(schoolId) == null) {
            throw exception(ErrorCodeConstants.SUPPORT_SCHOOL_NOT_EXISTS);
        }
    }

    private void validateGrade(Long gradeCatalogId) {
        if (gradeCatalogId == null) {
            throw exception(ErrorCodeConstants.WINDOW_SPU_RULE_SCOPE_INVALID);
        }
        subscriptionSupportService.validateGradeCatalogIds(Collections.singleton(gradeCatalogId));
    }

    private void validateRuleDuplicate(SubscriptionWindowSpuRuleDO oldRule, SubscriptionWindowSpuRuleSaveReqVO reqVO) {
        SubscriptionWindowSpuRuleDO duplicate = subscriptionWindowSpuRuleMapper.selectOne(new LambdaQueryWrapperX<SubscriptionWindowSpuRuleDO>()
                .eq(SubscriptionWindowSpuRuleDO::getWindowSpuId, reqVO.getWindowSpuId())
                .eq(SubscriptionWindowSpuRuleDO::getEffectType, reqVO.getEffectType())
                .eq(SubscriptionWindowSpuRuleDO::getScopeType, reqVO.getScopeType())
                .eqIfPresent(SubscriptionWindowSpuRuleDO::getSchoolId, reqVO.getSchoolId())
                .eqIfPresent(SubscriptionWindowSpuRuleDO::getGradeCatalogId, reqVO.getGradeCatalogId()));
        if (duplicate == null) {
            return;
        }
        if (oldRule == null || !Objects.equals(oldRule.getId(), duplicate.getId())) {
            throw exception(ErrorCodeConstants.WINDOW_SPU_RULE_SCOPE_INVALID);
        }
    }

    private List<SubscriptionWindowSpuRuleRespVO> buildRespList(List<SubscriptionWindowSpuRuleDO> rules) {
        Map<Long, SchoolDO> schoolMap = subscriptionSupportService.getSchoolMap(rules.stream()
                .map(SubscriptionWindowSpuRuleDO::getSchoolId)
                .filter(Objects::nonNull)
                .toList());
        Map<Long, GradeCatalogDO> gradeCatalogMap = subscriptionSupportService.getGradeCatalogMap(rules.stream()
                .map(SubscriptionWindowSpuRuleDO::getGradeCatalogId)
                .filter(Objects::nonNull)
                .toList());
        return rules.stream().map(rule -> {
            SubscriptionWindowSpuRuleRespVO respVO = new SubscriptionWindowSpuRuleRespVO();
            respVO.setId(rule.getId());
            respVO.setWindowSpuId(rule.getWindowSpuId());
            respVO.setEffectType(rule.getEffectType());
            respVO.setScopeType(rule.getScopeType());
            respVO.setSchoolId(rule.getSchoolId());
            SchoolDO school = schoolMap.get(rule.getSchoolId());
            respVO.setSchoolName(school == null ? null : school.getSchoolName());
            respVO.setGradeCatalogId(rule.getGradeCatalogId());
            GradeCatalogDO gradeCatalog = gradeCatalogMap.get(rule.getGradeCatalogId());
            if (gradeCatalog != null) {
                respVO.setGradeName(gradeCatalog.getGradeName());
                respVO.setGradeAliasName(gradeCatalog.getAliasName());
            }
            respVO.setSort(rule.getSort());
            respVO.setRemark(rule.getRemark());
            respVO.setCreateTime(rule.getCreateTime());
            return respVO;
        }).toList();
    }

    private SubscriptionWindowSpuRuleDO buildRuleDO(SubscriptionWindowSpuRuleSaveReqVO reqVO) {
        return SubscriptionWindowSpuRuleDO.builder()
                .windowSpuId(reqVO.getWindowSpuId())
                .effectType(reqVO.getEffectType())
                .scopeType(reqVO.getScopeType())
                .schoolId(reqVO.getSchoolId())
                .gradeCatalogId(reqVO.getGradeCatalogId())
                .sort(reqVO.getSort())
                .remark(reqVO.getRemark())
                .build();
    }
}
