package cn.iocoder.yudao.module.product.service.publication;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductPublicationSkuIssueTemplateGenerateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductPublicationSkuIssueTemplateRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductPublicationSkuIssueTemplateSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSkuIssueTemplateDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSpuExtDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSkuIssueTemplateMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSpuExtMapper;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import cn.iocoder.yudao.module.publication.api.enums.PublicationIssueModeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ProductPublicationSkuIssueTemplateServiceImpl implements ProductPublicationSkuIssueTemplateService {

    @Resource
    private ProductSkuMapper productSkuMapper;
    @Resource
    private ProductPublicationSpuExtMapper publicationSpuExtMapper;
    @Resource
    private ProductPublicationSkuIssueTemplateMapper issueTemplateMapper;

    @Override
    public List<ProductPublicationSkuIssueTemplateRespVO> getTemplateList(Long skuId) {
        validateProductSkuExists(skuId);
        return BeanUtils.toBean(issueTemplateMapper.selectListBySkuId(skuId),
                ProductPublicationSkuIssueTemplateRespVO.class);
    }

    @Override
    public List<ProductPublicationSkuIssueTemplateDO> getEnabledTemplateList(Long skuId) {
        return issueTemplateMapper.selectEnabledListBySkuId(skuId, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveTemplate(ProductPublicationSkuIssueTemplateSaveReqVO reqVO) {
        validateSkuPeriodical(reqVO.getSkuId());
        ProductPublicationSkuIssueTemplateDO oldTemplate =
                reqVO.getId() == null ? null : validateTemplateExists(reqVO.getId());
        if (oldTemplate != null && !oldTemplate.getSkuId().equals(reqVO.getSkuId())) {
            throw exception(PUBLICATION_SKU_ISSUE_TEMPLATE_NOT_EXISTS);
        }
        if (issueTemplateMapper.selectBySkuIdAndIssueNoAndIdNot(reqVO.getSkuId(), reqVO.getIssueNo(),
                reqVO.getId()) != null) {
            throw exception(PUBLICATION_SKU_ISSUE_TEMPLATE_DUPLICATE);
        }
        ProductPublicationSkuIssueTemplateDO template =
                BeanUtils.toBean(reqVO, ProductPublicationSkuIssueTemplateDO.class);
        if (template.getSort() == null) {
            template.setSort(template.getIssueNo());
        }
        if (template.getStatus() == null) {
            template.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (template.getId() == null) {
            issueTemplateMapper.insert(template);
            validateEnabledPeriodicalSkuHasEnabledTemplate(reqVO.getSkuId());
            return template.getId();
        }
        issueTemplateMapper.updateById(template);
        validateEnabledPeriodicalSkuHasEnabledTemplate(reqVO.getSkuId());
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateTemplates(ProductPublicationSkuIssueTemplateGenerateReqVO reqVO) {
        validateSkuPeriodical(reqVO.getSkuId());
        int publishInterval = reqVO.getPublishIntervalDays() == null ? 30 : reqVO.getPublishIntervalDays();
        int deliveryInterval = reqVO.getDeliveryIntervalDays() == null ? publishInterval : reqVO.getDeliveryIntervalDays();
        String prefix = StrUtil.blankToDefault(reqVO.getIssueNamePrefix(), "第");
        List<ProductPublicationSkuIssueTemplateDO> templates = new ArrayList<>(reqVO.getIssueCount());
        for (int i = 0; i < reqVO.getIssueCount(); i++) {
            int issueNo = reqVO.getStartIssueNo() + i;
            if (issueTemplateMapper.selectBySkuIdAndIssueNoAndIdNot(reqVO.getSkuId(), issueNo, null) != null) {
                throw exception(PUBLICATION_SKU_ISSUE_TEMPLATE_DUPLICATE);
            }
            Integer publishOffsetDays = reqVO.getFirstPublishOffsetDays() == null ? null
                    : reqVO.getFirstPublishOffsetDays() + i * publishInterval;
            Integer deliveryOffsetDays = reqVO.getFirstDeliveryOffsetDays() == null ? null
                    : reqVO.getFirstDeliveryOffsetDays() + i * deliveryInterval;
            templates.add(new ProductPublicationSkuIssueTemplateDO()
                    .setSkuId(reqVO.getSkuId())
                    .setIssueNo(issueNo)
                    .setIssueName(formatIssueName(prefix, issueNo))
                    .setPublishOffsetDays(publishOffsetDays)
                    .setDeliveryOffsetDays(deliveryOffsetDays)
                    .setSort(issueNo)
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        }
        issueTemplateMapper.insertBatch(templates);
        validateEnabledPeriodicalSkuHasEnabledTemplate(reqVO.getSkuId());
        return templates.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long id) {
        ProductPublicationSkuIssueTemplateDO template = validateTemplateExists(id);
        issueTemplateMapper.deleteById(id);
        validateEnabledPeriodicalSkuHasEnabledTemplate(template.getSkuId());
    }

    private ProductPublicationSkuIssueTemplateDO validateTemplateExists(Long id) {
        ProductPublicationSkuIssueTemplateDO template = issueTemplateMapper.selectById(id);
        if (template == null) {
            throw exception(PUBLICATION_SKU_ISSUE_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    private ProductSkuDO validateProductSkuExists(Long skuId) {
        ProductSkuDO sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw exception(SKU_NOT_EXISTS);
        }
        return sku;
    }

    private void validateSkuPeriodical(Long skuId) {
        ProductSkuDO sku = validateProductSkuExists(skuId);
        ProductPublicationSpuExtDO spuExt = publicationSpuExtMapper.selectById(sku.getSpuId());
        if (spuExt == null || !PublicationIssueModeEnum.isPeriodical(spuExt.getIssueMode())) {
            throw exception(PUBLICATION_SKU_ISSUE_TEMPLATE_PERIODICAL_REQUIRED);
        }
    }

    private void validateEnabledPeriodicalSkuHasEnabledTemplate(Long skuId) {
        ProductSkuDO sku = productSkuMapper.selectById(skuId);
        if (sku == null || !CommonStatusEnum.isEnable(sku.getStatus())) {
            return;
        }
        ProductPublicationSpuExtDO spuExt = publicationSpuExtMapper.selectById(sku.getSpuId());
        if (spuExt == null || !PublicationIssueModeEnum.isPeriodical(spuExt.getIssueMode())) {
            return;
        }
        if (CollUtil.isEmpty(issueTemplateMapper.selectEnabledListBySkuId(skuId,
                CommonStatusEnum.ENABLE.getStatus()))) {
            throw exception(PUBLICATION_SKU_ISSUE_TEMPLATE_REQUIRED);
        }
    }

    private String formatIssueName(String prefix, int issueNo) {
        if ("第".equals(prefix)) {
            return "第" + issueNo + "期";
        }
        return prefix + issueNo;
    }

}
