package cn.iocoder.yudao.module.subscription.service.window;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.api.yearcatalog.EduYearCatalogApi;
import cn.iocoder.yudao.module.edu.api.yearcatalog.dto.EduYearCatalogRespDTO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowPageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowSaveReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.window.vo.SubscriptionWindowUpdateStatusReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionRuleDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionRuleConditionMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionRuleMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferGradeRelMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionWindowOfferSkuMapper;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeCalcRuleEnum;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionGradeResolveModeEnum;
import cn.iocoder.yudao.module.subscription.service.offersku.SubscriptionOfferSkuAvailabilityValidator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.*;

@Service
@Validated
public class SubscriptionWindowServiceImpl implements SubscriptionWindowService {

    private static final String WINDOW_MUTATION_LOCK_NAME = "xiaokanhui:subscription:window:enabled";
    private static final int WINDOW_MUTATION_LOCK_TIMEOUT_SECONDS = 10;

    @Resource
    private SubscriptionWindowMapper windowMapper;
    @Resource
    private EduYearCatalogApi yearCatalogApi;
    @Resource
    private SubscriptionWindowOfferMapper offerMapper;
    @Resource
    private SubscriptionWindowOfferSkuMapper offerSkuMapper;
    @Resource
    private SubscriptionWindowOfferGradeRelMapper offerGradeRelMapper;
    @Resource
    private SubscriptionRuleMapper ruleMapper;
    @Resource
    private SubscriptionRuleConditionMapper ruleConditionMapper;
    @Resource
    private SubscriptionOfferSkuAvailabilityValidator offerSkuAvailabilityValidator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWindow(SubscriptionWindowSaveReqVO reqVO) {
        acquireWindowMutationLock();
        SubscriptionWindowDO window = buildWindow(reqVO);
        if (CommonStatusEnum.isEnable(window.getStatus())) {
            validateNoOtherEnabledWindowOverlap(window);
            validateEnabledOfferSkusIfWindowEnabled(window);
        }
        windowMapper.insert(window);
        return window.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWindow(SubscriptionWindowSaveReqVO reqVO) {
        acquireWindowMutationLock();
        validateWindowExists(reqVO.getId());
        SubscriptionWindowDO window = buildWindow(reqVO);
        if (CommonStatusEnum.isEnable(window.getStatus())) {
            validateNoOtherEnabledWindowOverlap(window);
            validateEnabledOfferSkusIfWindowEnabled(window);
        }
        windowMapper.updateById(window);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWindowStatus(SubscriptionWindowUpdateStatusReqVO reqVO) {
        updateWindowStatus(reqVO.getId(), reqVO.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWindowStatus(Long id, Integer status) {
        acquireWindowMutationLock();
        SubscriptionWindowDO window = validateWindowExists(id);
        if (CommonStatusEnum.isEnable(status)) {
            window.setStatus(status);
            validateNoOtherEnabledWindowOverlap(window);
            validateEnabledOfferSkusIfWindowEnabled(window);
        }
        windowMapper.updateById(new SubscriptionWindowDO().setId(id).setStatus(status));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteWindow(Long id) {
        validateWindowExists(id);
        List<SubscriptionWindowOfferDO> offers = offerMapper.selectListByWindowId(id);
        List<SubscriptionRuleDO> rules = ruleMapper.selectListByWindowId(id);
        ruleConditionMapper.deleteByRuleIds(convertSet(rules, SubscriptionRuleDO::getId));
        ruleMapper.deleteByWindowId(id);
        offerSkuMapper.deleteByOfferIds(convertSet(offers, SubscriptionWindowOfferDO::getId));
        offerGradeRelMapper.deleteByOfferIds(convertSet(offers, SubscriptionWindowOfferDO::getId));
        offerMapper.deleteByWindowId(id);
        windowMapper.deleteById(id);
    }

    @Override
    public SubscriptionWindowDO getWindow(Long id) {
        return id == null ? null : windowMapper.selectById(id);
    }

    @Override
    public SubscriptionWindowDO validateWindowExists(Long id) {
        SubscriptionWindowDO window = getWindow(id);
        if (window == null) {
            throw exception(WINDOW_NOT_EXISTS);
        }
        return window;
    }

    @Override
    public PageResult<SubscriptionWindowDO> getWindowPage(SubscriptionWindowPageReqVO reqVO) {
        return windowMapper.selectPage(reqVO);
    }

    @Override
    public SubscriptionWindowRespVO getWindowResp(Long id) {
        return buildWindowResp(validateWindowExists(id));
    }

    @Override
    public PageResult<SubscriptionWindowRespVO> getWindowPageResp(SubscriptionWindowPageReqVO reqVO) {
        return BeanUtils.toBean(getWindowPage(reqVO), SubscriptionWindowRespVO.class, this::fillWindowResp);
    }

    @Override
    public List<SubscriptionWindowRespVO> getWindowSimpleList(Integer status) {
        return BeanUtils.toBean(windowMapper.selectListByStatus(status), SubscriptionWindowRespVO.class,
                this::fillWindowResp);
    }

    @Override
    public SubscriptionWindowRespVO getCurrentOpenWindowResp() {
        return buildWindowResp(getCurrentOpenWindow());
    }

    @Override
    public SubscriptionWindowDO getCurrentOpenWindow() {
        List<SubscriptionWindowDO> windows = windowMapper.selectOpenList(LocalDateTime.now(),
                CommonStatusEnum.ENABLE.getStatus());
        return windows.isEmpty() ? null : windows.get(0);
    }

    @Override
    public boolean isOpen(SubscriptionWindowDO window) {
        if (window == null || !CommonStatusEnum.isEnable(window.getStatus())) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(window.getStartTime()) && now.isBefore(window.getEndTime());
    }

    private void validateWindowTime(SubscriptionWindowSaveReqVO reqVO) {
        if (reqVO.getStartTime() == null || reqVO.getEndTime() == null
                || !reqVO.getEndTime().isAfter(reqVO.getStartTime())) {
            throw exception(WINDOW_TIME_INVALID);
        }
    }

    private SubscriptionWindowDO buildWindow(SubscriptionWindowSaveReqVO reqVO) {
        validateWindowTime(reqVO);
        EduYearCatalogRespDTO yearCatalog = yearCatalogApi.getYearCatalog(reqVO.getTargetYearCatalogId());
        return BeanUtils.toBean(reqVO, SubscriptionWindowDO.class)
                .setTargetYearCatalogId(yearCatalog.getId())
                .setTargetYearNameSnapshot(yearCatalog.getName())
                .setTargetYearStart(yearCatalog.getYearStart())
                .setTargetYearEnd(yearCatalog.getYearEnd())
                .setGradeCalcRule(SubscriptionGradeCalcRuleEnum.defaultRule())
                .setGradeResolveMode(SubscriptionGradeResolveModeEnum.defaultMode());
    }

    private SubscriptionWindowRespVO buildWindowResp(SubscriptionWindowDO window) {
        if (window == null) {
            return null;
        }
        return fillWindowResp(BeanUtils.toBean(window, SubscriptionWindowRespVO.class));
    }

    private SubscriptionWindowRespVO fillWindowResp(SubscriptionWindowRespVO respVO) {
        if (respVO != null) {
            respVO.setGradePolicyName(SubscriptionGradeCalcRuleEnum.AUTO_TARGET_YEAR_GRADE.getName());
        }
        return respVO;
    }

    private void validateNoOtherEnabledWindowOverlap(SubscriptionWindowDO candidate) {
        List<SubscriptionWindowDO> windows = windowMapper.selectEnabledOverlapList(candidate.getId(),
                candidate.getStartTime(), candidate.getEndTime(),
                CommonStatusEnum.ENABLE.getStatus());
        if (!windows.isEmpty()) {
            throw exception(WINDOW_TIME_OVERLAP);
        }
    }

    private void acquireWindowMutationLock() {
        Integer result = windowMapper.getWindowMutationLock(WINDOW_MUTATION_LOCK_NAME,
                WINDOW_MUTATION_LOCK_TIMEOUT_SECONDS);
        if (!Objects.equals(result, 1)) {
            throw exception(WINDOW_TIME_OVERLAP);
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            windowMapper.releaseWindowMutationLock(WINDOW_MUTATION_LOCK_NAME);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                windowMapper.releaseWindowMutationLock(WINDOW_MUTATION_LOCK_NAME);
            }
        });
    }

    private void validateEnabledOfferSkusIfWindowEnabled(SubscriptionWindowDO window) {
        if (!CommonStatusEnum.isEnable(window.getStatus())) {
            return;
        }
        if (window.getId() == null) {
            throw exception(OFFER_SKU_EFFECTIVE_REQUIRED);
        }
        List<SubscriptionWindowOfferDO> offers = offerMapper.selectListByWindowId(window.getId());
        boolean hasEnabledOffer = false;
        for (SubscriptionWindowOfferDO offer : offers) {
            if (!CommonStatusEnum.isEnable(offer.getStatus())) {
                continue;
            }
            hasEnabledOffer = true;
            offerSkuAvailabilityValidator.validateEnabledOfferHasEffectiveSku(offer.getId());
        }
        if (!hasEnabledOffer) {
            throw exception(OFFER_SKU_EFFECTIVE_REQUIRED);
        }
    }
}
