package cn.iocoder.yudao.module.subscription.service.offerskuissue;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.publication.ProductPublicationApi;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.PublicationIssueModeEnum;
import cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo.SubscriptionOfferSkuIssueApplyDefaultTemplateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo.SubscriptionOfferSkuIssueGenerateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo.SubscriptionOfferSkuIssueRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offerskuissue.vo.SubscriptionOfferSkuIssueSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionOfferSkuIssueDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionOfferSkuIssueMapper;
import cn.iocoder.yudao.module.subscription.service.offer.SubscriptionOfferService;
import cn.iocoder.yudao.module.subscription.service.offersku.SubscriptionOfferSkuService;
import cn.iocoder.yudao.module.subscription.service.window.SubscriptionWindowService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_SKU_ISSUE_DUPLICATE;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_SKU_ISSUE_NOT_EXISTS;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_SKU_ISSUE_PERIODICAL_REQUIRED;

@Service
@Validated
public class SubscriptionOfferSkuIssueServiceImpl implements SubscriptionOfferSkuIssueService {

    @Resource
    private SubscriptionOfferSkuIssueMapper offerSkuIssueMapper;
    @Resource
    private SubscriptionOfferSkuService offerSkuService;
    @Resource
    private SubscriptionOfferService offerService;
    @Resource
    private SubscriptionWindowService windowService;
    @Resource
    private ProductPublicationApi productPublicationApi;
    @Resource
    private SubscriptionOfferSkuIssueDefaultTemplateService defaultTemplateService;

    @Override
    public List<SubscriptionOfferSkuIssueRespVO> getIssueList(Long offerSkuId) {
        offerSkuService.validateOfferSkuExists(offerSkuId);
        return BeanUtils.toBean(offerSkuIssueMapper.selectListByOfferSkuId(offerSkuId),
                SubscriptionOfferSkuIssueRespVO.class);
    }

    @Override
    public List<SubscriptionOfferSkuIssueDO> getEnabledIssueList(Long offerSkuId) {
        return offerSkuIssueMapper.selectEnabledListByOfferSkuId(offerSkuId, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveIssue(SubscriptionOfferSkuIssueSaveReqVO reqVO) {
        SubscriptionWindowOfferSkuDO offerSku = offerSkuService.validateOfferSkuExists(reqVO.getOfferSkuId());
        validateOfferSkuPeriodical(offerSku);
        SubscriptionOfferSkuIssueDO oldIssue = reqVO.getId() == null ? null : validateIssueExists(reqVO.getId());
        if (oldIssue != null && !oldIssue.getOfferSkuId().equals(reqVO.getOfferSkuId())) {
            throw exception(OFFER_SKU_ISSUE_NOT_EXISTS);
        }
        if (offerSkuIssueMapper.selectByOfferSkuIdAndIssueNoAndIdNot(reqVO.getOfferSkuId(), reqVO.getIssueNo(),
                reqVO.getId()) != null) {
            throw exception(OFFER_SKU_ISSUE_DUPLICATE);
        }
        SubscriptionOfferSkuIssueDO issue = BeanUtils.toBean(reqVO, SubscriptionOfferSkuIssueDO.class)
                .setOfferId(offerSku.getOfferId());
        if (issue.getSort() == null) {
            issue.setSort(issue.getIssueNo());
        }
        if (issue.getStatus() == null) {
            issue.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (issue.getId() == null) {
            offerSkuIssueMapper.insert(issue);
            return issue.getId();
        }
        offerSkuIssueMapper.updateById(issue);
        return issue.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateIssues(SubscriptionOfferSkuIssueGenerateReqVO reqVO) {
        SubscriptionWindowOfferSkuDO offerSku = offerSkuService.validateOfferSkuExists(reqVO.getOfferSkuId());
        validateOfferSkuPeriodical(offerSku);
        int publishInterval = reqVO.getPublishIntervalDays() == null ? 7 : reqVO.getPublishIntervalDays();
        int deliveryInterval = reqVO.getDeliveryIntervalDays() == null ? publishInterval : reqVO.getDeliveryIntervalDays();
        String prefix = StrUtil.blankToDefault(reqVO.getIssueNamePrefix(), "第");
        List<SubscriptionOfferSkuIssueDO> issues = new ArrayList<>(reqVO.getIssueCount());
        for (int i = 0; i < reqVO.getIssueCount(); i++) {
            int issueNo = reqVO.getStartIssueNo() + i;
            if (offerSkuIssueMapper.selectByOfferSkuIdAndIssueNoAndIdNot(reqVO.getOfferSkuId(), issueNo, null) != null) {
                throw exception(OFFER_SKU_ISSUE_DUPLICATE);
            }
            LocalDate plannedPublishDate = reqVO.getFirstPublishDate() == null ? null
                    : reqVO.getFirstPublishDate().plusDays((long) i * publishInterval);
            LocalDate plannedDeliveryDate = reqVO.getFirstDeliveryDate() == null ? null
                    : reqVO.getFirstDeliveryDate().plusDays((long) i * deliveryInterval);
            issues.add(new SubscriptionOfferSkuIssueDO()
                    .setOfferId(offerSku.getOfferId())
                    .setOfferSkuId(reqVO.getOfferSkuId())
                    .setIssueNo(issueNo)
                    .setIssueName(formatIssueName(prefix, issueNo))
                    .setPlannedPublishDate(plannedPublishDate)
                    .setPlannedDeliveryDate(plannedDeliveryDate)
                    .setSort(issueNo)
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        }
        offerSkuIssueMapper.insertBatch(issues);
        return issues.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int applyDefaultTemplate(SubscriptionOfferSkuIssueApplyDefaultTemplateReqVO reqVO) {
        SubscriptionWindowOfferSkuDO offerSku = offerSkuService.validateOfferSkuExists(reqVO.getOfferSkuId());
        validateOfferSkuPeriodical(offerSku);
        SubscriptionWindowOfferDO offer = offerService.validateOfferExists(offerSku.getOfferId());
        SubscriptionWindowDO window = windowService.validateWindowExists(offer.getWindowId());
        ProductPublicationRespDTO publication = productPublicationApi.getPublication(offer.getProductSpuId());
        return defaultTemplateService.applyDefaultTemplate(window, offerSku, publication,
                Boolean.TRUE.equals(reqVO.getOverwrite()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIssue(Long id) {
        validateIssueExists(id);
        offerSkuIssueMapper.deleteById(id);
    }

    private SubscriptionOfferSkuIssueDO validateIssueExists(Long id) {
        SubscriptionOfferSkuIssueDO issue = offerSkuIssueMapper.selectById(id);
        if (issue == null) {
            throw exception(OFFER_SKU_ISSUE_NOT_EXISTS);
        }
        return issue;
    }

    private void validateOfferSkuPeriodical(SubscriptionWindowOfferSkuDO offerSku) {
        SubscriptionWindowOfferDO offer = offerService.validateOfferExists(offerSku.getOfferId());
        ProductPublicationRespDTO publication = productPublicationApi.getPublication(offer.getProductSpuId());
        ProductPublicationRespDTO.PublicationSpuExtDTO publicationExt =
                publication == null ? null : publication.getPublicationExt();
        if (publicationExt == null || !PublicationIssueModeEnum.isPeriodical(publicationExt.getIssueMode())) {
            throw exception(OFFER_SKU_ISSUE_PERIODICAL_REQUIRED);
        }
    }

    private String formatIssueName(String prefix, int issueNo) {
        if ("第".equals(prefix)) {
            return "第" + issueNo + "期";
        }
        return prefix + issueNo;
    }

}
