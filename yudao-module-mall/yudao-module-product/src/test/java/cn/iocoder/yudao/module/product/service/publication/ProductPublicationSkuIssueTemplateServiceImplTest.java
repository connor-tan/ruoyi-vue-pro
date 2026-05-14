package cn.iocoder.yudao.module.product.service.publication;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductPublicationSkuIssueTemplateSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSkuIssueTemplateDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSpuExtDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSkuIssueTemplateMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSpuExtMapper;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import cn.iocoder.yudao.module.publication.api.enums.PublicationIssueModeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PUBLICATION_SKU_ISSUE_TEMPLATE_PERIODICAL_REQUIRED;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PUBLICATION_SKU_ISSUE_TEMPLATE_REQUIRED;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPublicationSkuIssueTemplateServiceImplTest {

    private static final long SKU_ID = 10L;
    private static final long SPU_ID = 20L;
    private static final long TEMPLATE_ID = 30L;

    @Mock
    private ProductSkuMapper productSkuMapper;
    @Mock
    private ProductPublicationSpuExtMapper publicationSpuExtMapper;
    @Mock
    private ProductPublicationSkuIssueTemplateMapper issueTemplateMapper;
    @InjectMocks
    private ProductPublicationSkuIssueTemplateServiceImpl issueTemplateService;

    @Test
    void saveTemplate_shouldRejectDisablingLastEnabledTemplateForEnabledPeriodicalSku() {
        mockEnabledPeriodicalSku();
        when(issueTemplateMapper.selectById(TEMPLATE_ID)).thenReturn(template(CommonStatusEnum.ENABLE.getStatus()));
        when(issueTemplateMapper.selectEnabledListBySkuId(SKU_ID, CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(List.of());
        ProductPublicationSkuIssueTemplateSaveReqVO reqVO = templateReq();
        reqVO.setStatus(CommonStatusEnum.DISABLE.getStatus());

        assertServiceException(() -> issueTemplateService.saveTemplate(reqVO),
                PUBLICATION_SKU_ISSUE_TEMPLATE_REQUIRED);

        verify(issueTemplateMapper).updateById(any(ProductPublicationSkuIssueTemplateDO.class));
    }

    @Test
    void deleteTemplate_shouldRejectDeletingLastEnabledTemplateForEnabledPeriodicalSku() {
        when(issueTemplateMapper.selectById(TEMPLATE_ID)).thenReturn(template(CommonStatusEnum.ENABLE.getStatus()));
        mockEnabledPeriodicalSku();
        when(issueTemplateMapper.selectEnabledListBySkuId(SKU_ID, CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(List.of());

        assertServiceException(() -> issueTemplateService.deleteTemplate(TEMPLATE_ID),
                PUBLICATION_SKU_ISSUE_TEMPLATE_REQUIRED);

        verify(issueTemplateMapper).deleteById(TEMPLATE_ID);
    }

    @Test
    void deleteTemplate_shouldAllowEmptyTemplateForDisabledPeriodicalSku() {
        when(issueTemplateMapper.selectById(TEMPLATE_ID)).thenReturn(template(CommonStatusEnum.ENABLE.getStatus()));
        when(productSkuMapper.selectById(SKU_ID)).thenReturn(sku(CommonStatusEnum.DISABLE.getStatus()));

        issueTemplateService.deleteTemplate(TEMPLATE_ID);

        verify(issueTemplateMapper).deleteById(TEMPLATE_ID);
        verify(issueTemplateMapper, never()).selectEnabledListBySkuId(anyLong(), anyInt());
    }

    @Test
    void saveTemplate_shouldRejectSinglePublicationSku() {
        when(productSkuMapper.selectById(SKU_ID)).thenReturn(sku(CommonStatusEnum.ENABLE.getStatus()));
        when(publicationSpuExtMapper.selectById(SPU_ID)).thenReturn(spuExt(PublicationIssueModeEnum.SINGLE.getCode()));

        assertServiceException(() -> issueTemplateService.saveTemplate(templateReq()),
                PUBLICATION_SKU_ISSUE_TEMPLATE_PERIODICAL_REQUIRED);

        verify(issueTemplateMapper, never()).insert(any(ProductPublicationSkuIssueTemplateDO.class));
        verify(issueTemplateMapper, never()).updateById(any(ProductPublicationSkuIssueTemplateDO.class));
    }

    private void mockEnabledPeriodicalSku() {
        when(productSkuMapper.selectById(SKU_ID)).thenReturn(sku(CommonStatusEnum.ENABLE.getStatus()));
        when(publicationSpuExtMapper.selectById(SPU_ID))
                .thenReturn(spuExt(PublicationIssueModeEnum.PERIODICAL.getCode()));
    }

    private ProductPublicationSkuIssueTemplateSaveReqVO templateReq() {
        ProductPublicationSkuIssueTemplateSaveReqVO reqVO = new ProductPublicationSkuIssueTemplateSaveReqVO();
        reqVO.setId(TEMPLATE_ID);
        reqVO.setSkuId(SKU_ID);
        reqVO.setIssueNo(1);
        reqVO.setIssueName("第1期");
        reqVO.setSort(1);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

    private ProductPublicationSkuIssueTemplateDO template(Integer status) {
        return ProductPublicationSkuIssueTemplateDO.builder()
                .id(TEMPLATE_ID)
                .skuId(SKU_ID)
                .issueNo(1)
                .issueName("第1期")
                .sort(1)
                .status(status)
                .build();
    }

    private ProductSkuDO sku(Integer status) {
        return ProductSkuDO.builder()
                .id(SKU_ID)
                .spuId(SPU_ID)
                .status(status)
                .build();
    }

    private ProductPublicationSpuExtDO spuExt(String issueMode) {
        return ProductPublicationSpuExtDO.builder()
                .spuId(SPU_ID)
                .issueMode(issueMode)
                .build();
    }

}
