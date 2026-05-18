package cn.iocoder.yudao.module.subscription.service.rule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.api.gradecatalog.EduGradeCatalogApi;
import cn.iocoder.yudao.module.edu.api.publication.EduPublicationPublisherApi;
import cn.iocoder.yudao.module.edu.api.publication.EduPublicationTypeApi;
import cn.iocoder.yudao.module.edu.api.publication.dto.EduPublicationPublisherRespDTO;
import cn.iocoder.yudao.module.edu.api.publication.dto.EduPublicationTypeRespDTO;
import cn.iocoder.yudao.module.edu.api.school.EduSchoolApi;
import cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo.SubscriptionOfferSkuRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionRuleConditionValueRespVO;
import cn.iocoder.yudao.module.subscription.enums.SubscriptionRuleFactorEnum;
import cn.iocoder.yudao.module.subscription.service.offersku.SubscriptionOfferSkuService;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.RULE_CONDITION_VALUE_INVALID;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.RULE_FACTOR_INVALID;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.RULE_OFFER_SKU_NOT_MATCHED;
import static cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants.RULE_OFFER_SKU_SCOPE_INVALID;

@Service
@Validated
public class SubscriptionRuleConditionValueServiceImpl implements SubscriptionRuleConditionValueService {

    private static final String DICT_TYPE_EDU_CYCLE = "edu_cycle";
    @Resource
    private EduSchoolApi schoolApi;
    @Resource
    private EduGradeCatalogApi gradeCatalogApi;
    @Resource
    private EduPublicationPublisherApi publicationPublisherApi;
    @Resource
    private EduPublicationTypeApi publicationTypeApi;
    @Resource
    private DictDataApi dictDataApi;
    @Resource
    private SubscriptionOfferSkuService offerSkuService;

    @Override
    public List<SubscriptionRuleConditionValueRespVO> getConditionValueList(String factor, Long windowId, Long offerId) {
        SubscriptionRuleFactorEnum factorEnum = SubscriptionRuleFactorEnum.getByCode(factor);
        if (factorEnum == null) {
            throw exception(RULE_FACTOR_INVALID);
        }
        return switch (factorEnum) {
            case STUDENT_SCHOOL -> schoolApi.getSchoolSimpleList().stream()
                    .map(item -> option(item.getId(), item.getSchoolName()))
                    .toList();
            case STUDENT_GRADE -> gradeCatalogApi.getEnabledGradeCatalogList().stream()
                    .map(item -> option(item.getId(), item.getGradeName()))
                    .toList();
            case OFFER_SKU -> buildOfferSkuOptions(offerId);
            case SKU_PUBLISHER -> publicationPublisherApi.getEnabledPublicationPublisherList().stream()
                    .map(item -> option(item.getId(), item.getName()))
                    .toList();
            case SKU_PUBLICATION_TYPE -> publicationTypeApi.getEnabledPublicationTypeList().stream()
                    .map(item -> option(item.getId(), item.getName()))
                    .toList();
            case SKU_ISSUE_CYCLE -> buildDictOptions(DICT_TYPE_EDU_CYCLE);
        };
    }

    @Override
    public String validateAndGetValueName(String factor, String value, Long windowId, Long offerId) {
        if (StrUtil.isBlank(value)) {
            throw exception(RULE_CONDITION_VALUE_INVALID);
        }
        SubscriptionRuleFactorEnum factorEnum = SubscriptionRuleFactorEnum.getByCode(factor);
        if (factorEnum == null) {
            throw exception(RULE_FACTOR_INVALID);
        }
        return switch (factorEnum) {
            case STUDENT_SCHOOL, STUDENT_GRADE, SKU_ISSUE_CYCLE -> findLabel(getConditionValueList(factor, windowId, offerId), value);
            case OFFER_SKU -> findOfferSkuLabel(offerId, value);
            case SKU_PUBLISHER -> validatePublisher(value);
            case SKU_PUBLICATION_TYPE -> validatePublicationType(value);
        };
    }

    private List<SubscriptionRuleConditionValueRespVO> buildOfferSkuOptions(Long offerId) {
        if (offerId == null) {
            throw exception(RULE_OFFER_SKU_SCOPE_INVALID);
        }
        List<SubscriptionOfferSkuRespVO> offerSkus = offerSkuService.getOfferSkuList(offerId);
        if (CollUtil.isEmpty(offerSkus)) {
            return Collections.emptyList();
        }
        return offerSkus.stream()
                .filter(item -> CommonStatusEnum.isEnable(item.getStatus()))
                .map(item -> new SubscriptionRuleConditionValueRespVO(String.valueOf(item.getId()), buildOfferSkuLabel(item)))
                .toList();
    }

    private String buildOfferSkuLabel(SubscriptionOfferSkuRespVO item) {
        List<String> meta = Collections.singletonList(
                        CollUtil.isEmpty(item.getApplicableGradeNames()) ? null : String.join("、", item.getApplicableGradeNames()))
                .stream()
                .filter(StrUtil::isNotBlank)
                .toList();
        return StrUtil.format("{}（{}）", StrUtil.blankToDefault(item.getProductSkuName(), "SKU#" + item.getProductSkuId()),
                CollUtil.isEmpty(meta) ? "-" : String.join(" / ", meta));
    }

    private String validatePublisher(String value) {
        Long id = parseLongValue(value);
        EduPublicationPublisherRespDTO publisher = publicationPublisherApi.getPublicationPublisher(id);
        if (publisher == null || !CommonStatusEnum.isEnable(publisher.getStatus())) {
            throw exception(RULE_CONDITION_VALUE_INVALID);
        }
        return publisher.getName();
    }

    private String findOfferSkuLabel(Long offerId, String value) {
        return buildOfferSkuOptions(offerId).stream()
                .filter(item -> Objects.equals(item.getValue(), value))
                .findFirst()
                .map(SubscriptionRuleConditionValueRespVO::getLabel)
                .orElseThrow(() -> exception(RULE_OFFER_SKU_NOT_MATCHED));
    }

    private String validatePublicationType(String value) {
        Long id = parseLongValue(value);
        EduPublicationTypeRespDTO publicationType = publicationTypeApi.getPublicationType(id);
        if (publicationType == null || !CommonStatusEnum.isEnable(publicationType.getStatus())) {
            throw exception(RULE_CONDITION_VALUE_INVALID);
        }
        return publicationType.getName();
    }

    private Long parseLongValue(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw exception(RULE_CONDITION_VALUE_INVALID);
        }
    }

    private String findLabel(Collection<SubscriptionRuleConditionValueRespVO> options, String value) {
        return options.stream()
                .filter(item -> Objects.equals(item.getValue(), value))
                .findFirst()
                .map(SubscriptionRuleConditionValueRespVO::getLabel)
                .orElseThrow(() -> exception(RULE_CONDITION_VALUE_INVALID));
    }

    private List<SubscriptionRuleConditionValueRespVO> buildDictOptions(String dictType) {
        return dictDataApi.getDictDataList(dictType).stream()
                .filter(item -> CommonStatusEnum.isEnable(item.getStatus()))
                .map(item -> new SubscriptionRuleConditionValueRespVO(item.getValue(), item.getLabel()))
                .toList();
    }

    private SubscriptionRuleConditionValueRespVO option(Long value, String label) {
        return new SubscriptionRuleConditionValueRespVO(String.valueOf(value), label);
    }
}
