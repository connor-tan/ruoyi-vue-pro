package cn.iocoder.yudao.module.subscription.service.window;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowSaveReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowSimpleRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowUpdateStatusReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowMapper;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeCalcRuleEnum;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class SubscriptionWindowServiceImpl implements SubscriptionWindowService {

    @Resource
    private SubscriptionWindowMapper subscriptionWindowMapper;
    @Resource
    private SubscriptionSupportService subscriptionSupportService;

    @Override
    public Long createWindow(SubscriptionWindowSaveReqVO createReqVO) {
        validateWindowTime(createReqVO.getStartTime(), createReqVO.getEndTime());
        validateTargetYear(createReqVO.getTargetYearStart(), createReqVO.getTargetYearEnd());
        validateEnableConflict(null, createReqVO.getStatus());
        SubscriptionWindowDO subscriptionWindow = BeanUtils.toBean(createReqVO, SubscriptionWindowDO.class);
        subscriptionWindow.setGradeCalcRule(normalizeGradeCalcRule(createReqVO.getGradeCalcRule()));
        subscriptionWindowMapper.insert(subscriptionWindow);
        return subscriptionWindow.getId();
    }

    @Override
    public void updateWindow(SubscriptionWindowSaveReqVO updateReqVO) {
        SubscriptionWindowDO oldWindow = validateWindowExists(updateReqVO.getId());
        validateWindowTime(updateReqVO.getStartTime(), updateReqVO.getEndTime());
        validateTargetYear(updateReqVO.getTargetYearStart(), updateReqVO.getTargetYearEnd());
        validateEnableConflict(oldWindow.getId(), updateReqVO.getStatus());
        SubscriptionWindowDO updateObj = BeanUtils.toBean(updateReqVO, SubscriptionWindowDO.class);
        updateObj.setGradeCalcRule(normalizeGradeCalcRule(updateReqVO.getGradeCalcRule()));
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
            respVO.setTargetYearName(buildTargetYearName(window.getTargetYearStart(), window.getTargetYearEnd()));
            return respVO;
        }).toList();
    }

    @Override
    public void updateWindowStatus(SubscriptionWindowUpdateStatusReqVO reqVO) {
        SubscriptionWindowDO window = validateWindowExists(reqVO.getId());
        validateEnableConflict(window.getId(), reqVO.getStatus());
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

    private void validateEnableConflict(Long currentId, Integer status) {
        if (!CommonStatusEnum.isEnable(status)) {
            return;
        }
        if (subscriptionWindowMapper.countEnabledWindowExceptId(currentId) > 0) {
            throw exception(ErrorCodeConstants.WINDOW_ENABLE_CONFLICT);
        }
    }

    private SubscriptionWindowRespVO buildWindowResp(SubscriptionWindowDO window) {
        SubscriptionWindowRespVO respVO = BeanUtils.toBean(window, SubscriptionWindowRespVO.class);
        respVO.setGradeCalcRule(normalizeGradeCalcRule(respVO.getGradeCalcRule()));
        respVO.setTargetYearName(buildTargetYearName(window.getTargetYearStart(), window.getTargetYearEnd()));
        return respVO;
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
}
