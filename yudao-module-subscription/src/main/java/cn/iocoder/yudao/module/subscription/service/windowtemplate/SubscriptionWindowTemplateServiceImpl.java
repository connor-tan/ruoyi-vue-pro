package cn.iocoder.yudao.module.subscription.service.windowtemplate;

import com.baomidou.dynamic.datasource.annotation.Master;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplatePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplateRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplateSaveReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplateSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo.SubscriptionWindowTemplateUpdateStatusReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowTemplateDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowTemplateMapper;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class SubscriptionWindowTemplateServiceImpl implements SubscriptionWindowTemplateService {

    @Resource
    private SubscriptionWindowTemplateMapper subscriptionWindowTemplateMapper;
    @Resource
    private SubscriptionWindowMapper subscriptionWindowMapper;

    @Override
    @Master
    public Long createWindowTemplate(SubscriptionWindowTemplateSaveReqVO createReqVO) {
        validateTemplateCodeUnique(createReqVO.getCode(), null);
        validateTemplateNameUnique(createReqVO.getName(), null);
        SubscriptionWindowTemplateDO template = BeanUtils.toBean(createReqVO, SubscriptionWindowTemplateDO.class);
        template.setBuiltIn(Boolean.FALSE);
        subscriptionWindowTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    @Master
    public void updateWindowTemplate(SubscriptionWindowTemplateSaveReqVO updateReqVO) {
        SubscriptionWindowTemplateDO oldTemplate = validateWindowTemplateExists(updateReqVO.getId());
        validateTemplateCodeUnique(updateReqVO.getCode(), oldTemplate.getId());
        validateTemplateNameUnique(updateReqVO.getName(), oldTemplate.getId());
        if (Boolean.TRUE.equals(oldTemplate.getBuiltIn())
                && (!Objects.equals(oldTemplate.getTargetPeriod(), updateReqVO.getTargetPeriod())
                || !Objects.equals(oldTemplate.getGradeCalcRule(), updateReqVO.getGradeCalcRule()))) {
            throw exception(ErrorCodeConstants.WINDOW_TEMPLATE_BUILT_IN_RULE_IMMUTABLE);
        }
        SubscriptionWindowTemplateDO updateObj = BeanUtils.toBean(updateReqVO, SubscriptionWindowTemplateDO.class);
        updateObj.setBuiltIn(oldTemplate.getBuiltIn());
        subscriptionWindowTemplateMapper.updateById(updateObj);
    }

    @Override
    @Master
    public void updateWindowTemplateStatus(SubscriptionWindowTemplateUpdateStatusReqVO reqVO) {
        validateWindowTemplateExists(reqVO.getId());
        SubscriptionWindowTemplateDO updateObj = new SubscriptionWindowTemplateDO();
        updateObj.setId(reqVO.getId());
        updateObj.setStatus(reqVO.getStatus());
        subscriptionWindowTemplateMapper.updateById(updateObj);
    }

    @Override
    @Master
    public void deleteWindowTemplate(Long id) {
        SubscriptionWindowTemplateDO template = validateWindowTemplateExists(id);
        if (Boolean.TRUE.equals(template.getBuiltIn())) {
            throw exception(ErrorCodeConstants.WINDOW_TEMPLATE_DELETE_FORBIDDEN);
        }
        if (subscriptionWindowMapper.countByTemplateId(id) > 0) {
            throw exception(ErrorCodeConstants.WINDOW_TEMPLATE_USED);
        }
        subscriptionWindowTemplateMapper.deleteById(id);
    }

    @Override
    public SubscriptionWindowTemplateRespVO getWindowTemplate(Long id) {
        SubscriptionWindowTemplateDO template = subscriptionWindowTemplateMapper.selectById(id);
        return template == null ? null : BeanUtils.toBean(template, SubscriptionWindowTemplateRespVO.class);
    }

    @Override
    public SubscriptionWindowTemplateDO getWindowTemplateDO(Long id) {
        return validateWindowTemplateExists(id);
    }

    @Override
    public SubscriptionWindowTemplateDO getEnabledWindowTemplate(Long id) {
        SubscriptionWindowTemplateDO template = validateWindowTemplateExists(id);
        if (!CommonStatusEnum.isEnable(template.getStatus())) {
            throw exception(ErrorCodeConstants.WINDOW_TEMPLATE_DISABLED);
        }
        return template;
    }

    @Override
    public PageResult<SubscriptionWindowTemplateRespVO> getWindowTemplatePage(SubscriptionWindowTemplatePageReqVO pageReqVO) {
        PageResult<SubscriptionWindowTemplateDO> pageResult = subscriptionWindowTemplateMapper.selectPage(pageReqVO);
        return new PageResult<>(BeanUtils.toBean(pageResult.getList(), SubscriptionWindowTemplateRespVO.class),
                pageResult.getTotal());
    }

    @Override
    public List<SubscriptionWindowTemplateSimpleRespVO> getWindowTemplateSimpleList() {
        return BeanUtils.toBean(subscriptionWindowTemplateMapper.selectEnabledList(), SubscriptionWindowTemplateSimpleRespVO.class);
    }

    private SubscriptionWindowTemplateDO validateWindowTemplateExists(Long id) {
        SubscriptionWindowTemplateDO template = subscriptionWindowTemplateMapper.selectById(id);
        if (template == null) {
            throw exception(ErrorCodeConstants.WINDOW_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    private void validateTemplateNameUnique(String name, Long excludeId) {
        if (subscriptionWindowTemplateMapper.countByName(name, excludeId) > 0) {
            throw exception(ErrorCodeConstants.WINDOW_TEMPLATE_NAME_DUPLICATE);
        }
    }

    private void validateTemplateCodeUnique(String code, Long excludeId) {
        if (subscriptionWindowTemplateMapper.countByCode(code, excludeId) > 0) {
            throw exception(ErrorCodeConstants.WINDOW_TEMPLATE_CODE_DUPLICATE);
        }
    }
}
