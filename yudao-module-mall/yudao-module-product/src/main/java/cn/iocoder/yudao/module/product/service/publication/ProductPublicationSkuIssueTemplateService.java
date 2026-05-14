package cn.iocoder.yudao.module.product.service.publication;

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductPublicationSkuIssueTemplateGenerateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductPublicationSkuIssueTemplateRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductPublicationSkuIssueTemplateSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSkuIssueTemplateDO;

import java.util.List;

public interface ProductPublicationSkuIssueTemplateService {

    List<ProductPublicationSkuIssueTemplateRespVO> getTemplateList(Long skuId);

    List<ProductPublicationSkuIssueTemplateDO> getEnabledTemplateList(Long skuId);

    Long saveTemplate(ProductPublicationSkuIssueTemplateSaveReqVO reqVO);

    int generateTemplates(ProductPublicationSkuIssueTemplateGenerateReqVO reqVO);

    void deleteTemplate(Long id);

}
