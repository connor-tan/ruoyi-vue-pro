package cn.iocoder.yudao.module.subscription.service.offerskuissue;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.PublicationIssueModeEnum;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionOfferSkuIssueDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionOfferSkuIssueMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_SKU_ISSUE_APPLY_EXISTS;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.OFFER_SKU_ISSUE_DEFAULT_TEMPLATE_REQUIRED;

@Service
public class SubscriptionOfferSkuIssueDefaultTemplateService {

    @Resource
    private SubscriptionOfferSkuIssueMapper offerSkuIssueMapper;

    public void copyDefaultIssuesForNewOfferSkus(SubscriptionWindowDO window, ProductPublicationRespDTO publication,
                                                 Collection<SubscriptionWindowOfferSkuDO> offerSkus) {
        if (window == null || publication == null || CollUtil.isEmpty(offerSkus)
                || !isPeriodical(publication)) {
            return;
        }
        for (SubscriptionWindowOfferSkuDO offerSku : offerSkus) {
            ProductPublicationRespDTO.PublicationSkuDTO productSku = findProductSku(publication, offerSku.getProductSkuId());
            if (productSku == null || offerSku.getId() == null
                    || CollUtil.isNotEmpty(offerSkuIssueMapper.selectListByOfferSkuId(offerSku.getId()))) {
                continue;
            }
            List<ProductPublicationRespDTO.PublicationSkuIssueTemplateDTO> templates = getEnabledTemplates(productSku);
            if (CollUtil.isEmpty(templates)) {
                continue;
            }
            offerSkuIssueMapper.insertBatch(buildIssueList(window, offerSku, templates));
        }
    }

    public int applyDefaultTemplate(SubscriptionWindowDO window, SubscriptionWindowOfferSkuDO offerSku,
                                    ProductPublicationRespDTO publication, boolean overwrite) {
        ProductPublicationRespDTO.PublicationSkuDTO productSku = findProductSku(publication, offerSku.getProductSkuId());
        List<ProductPublicationRespDTO.PublicationSkuIssueTemplateDTO> templates = getEnabledTemplates(productSku);
        if (CollUtil.isEmpty(templates)) {
            throw exception(OFFER_SKU_ISSUE_DEFAULT_TEMPLATE_REQUIRED);
        }
        if (CollUtil.isNotEmpty(offerSkuIssueMapper.selectListByOfferSkuId(offerSku.getId()))) {
            if (!overwrite) {
                throw exception(OFFER_SKU_ISSUE_APPLY_EXISTS);
            }
            offerSkuIssueMapper.deleteByOfferSkuId(offerSku.getId());
        }
        List<SubscriptionOfferSkuIssueDO> issues = buildIssueList(window, offerSku, templates);
        offerSkuIssueMapper.insertBatch(issues);
        return issues.size();
    }

    private boolean isPeriodical(ProductPublicationRespDTO publication) {
        ProductPublicationRespDTO.PublicationSpuExtDTO ext = publication.getPublicationExt();
        return ext != null && PublicationIssueModeEnum.isPeriodical(ext.getIssueMode());
    }

    private ProductPublicationRespDTO.PublicationSkuDTO findProductSku(ProductPublicationRespDTO publication,
                                                                       Long productSkuId) {
        if (publication == null || CollUtil.isEmpty(publication.getSkus())) {
            return null;
        }
        return publication.getSkus().stream()
                .filter(sku -> Objects.equals(sku.getId(), productSkuId))
                .findFirst()
                .orElse(null);
    }

    private List<ProductPublicationRespDTO.PublicationSkuIssueTemplateDTO> getEnabledTemplates(
            ProductPublicationRespDTO.PublicationSkuDTO productSku) {
        if (productSku == null || CollUtil.isEmpty(productSku.getIssueTemplates())) {
            return List.of();
        }
        return productSku.getIssueTemplates().stream()
                .filter(template -> CommonStatusEnum.isEnable(template.getStatus()))
                .toList();
    }

    private List<SubscriptionOfferSkuIssueDO> buildIssueList(SubscriptionWindowDO window,
                                                             SubscriptionWindowOfferSkuDO offerSku,
                                                             List<ProductPublicationRespDTO.PublicationSkuIssueTemplateDTO> templates) {
        LocalDate windowStartDate = window.getStartTime() == null ? null : window.getStartTime().toLocalDate();
        return templates.stream()
                .map(template -> new SubscriptionOfferSkuIssueDO()
                        .setOfferId(offerSku.getOfferId())
                        .setOfferSkuId(offerSku.getId())
                        .setIssueNo(template.getIssueNo())
                        .setIssueName(template.getIssueName())
                        .setPlannedPublishDate(toPlannedDate(windowStartDate, template.getPublishOffsetDays()))
                        .setPlannedDeliveryDate(toPlannedDate(windowStartDate, template.getDeliveryOffsetDays()))
                        .setSort(template.getSort() == null ? template.getIssueNo() : template.getSort())
                        .setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setRemark(template.getRemark()))
                .toList();
    }

    private LocalDate toPlannedDate(LocalDate windowStartDate, Integer offsetDays) {
        if (windowStartDate == null || offsetDays == null) {
            return null;
        }
        return windowStartDate.plusDays(offsetDays);
    }

}
