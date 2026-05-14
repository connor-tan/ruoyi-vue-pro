package cn.iocoder.yudao.module.product.service.publication;

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSkuIssueTemplateDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSpuExtDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSkuGradeRelDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSkuExtMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSkuGradeRelMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSkuIssueTemplateMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationSpuExtMapper;
import cn.iocoder.yudao.module.publication.api.enums.PublicationIssueModeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductPublicationServiceImplTest {

    @Mock
    private ProductPublicationSpuExtMapper publicationSpuExtMapper;
    @Mock
    private ProductPublicationSkuExtMapper publicationSkuExtMapper;
    @Mock
    private ProductPublicationSkuGradeRelMapper publicationSkuGradeRelMapper;
    @Mock
    private ProductPublicationSkuIssueTemplateMapper publicationSkuIssueTemplateMapper;
    @InjectMocks
    private ProductPublicationServiceImpl publicationService;

    @Test
    void savePublication_shouldRewriteGradeRelationsForSavedSkus() {
        ProductSpuSaveReqVO reqVO = new ProductSpuSaveReqVO();
        ProductSpuSaveReqVO.PublicationSpuExtSaveReqVO spuExt = new ProductSpuSaveReqVO.PublicationSpuExtSaveReqVO();
        spuExt.setPublisherId(1L);
        spuExt.setPublicationTypeId(2L);
        reqVO.setPublicationExt(spuExt);
        ProductSkuSaveReqVO skuReq = new ProductSkuSaveReqVO();
        skuReq.setId(10L);
        ProductSkuSaveReqVO.PublicationSkuExtSaveReqVO skuExt = new ProductSkuSaveReqVO.PublicationSkuExtSaveReqVO();
        skuExt.setVolumeLabel("上册");
        skuReq.setPublicationExt(skuExt);
        skuReq.setApplicableGradeCatalogIds(List.of(100L, 101L));
        reqVO.setSkus(List.of(skuReq));
        ProductSkuDO savedSku = ProductSkuDO.builder().id(10L).build();

        publicationService.savePublication(1L, reqVO, List.of(savedSku), List.of());

        ArgumentCaptor<ProductPublicationSpuExtDO> spuExtCaptor = ArgumentCaptor.forClass(ProductPublicationSpuExtDO.class);
        verify(publicationSpuExtMapper).upsert(spuExtCaptor.capture());
        assertEquals(PublicationIssueModeEnum.SINGLE.getCode(), spuExtCaptor.getValue().getIssueMode());
        assertEquals("", spuExtCaptor.getValue().getIssueCycle());
        ArgumentCaptor<Collection<Long>> deletedSkuIdsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(publicationSkuGradeRelMapper).deleteBySkuIdsPhysically(deletedSkuIdsCaptor.capture());
        assertEquals(List.of(10L), List.copyOf(deletedSkuIdsCaptor.getValue()));
        ArgumentCaptor<List<ProductPublicationSkuGradeRelDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(publicationSkuGradeRelMapper).insertBatch(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertEquals(10L, captor.getValue().get(0).getSkuId());
        assertEquals(100L, captor.getValue().get(0).getGradeCatalogId());
        assertEquals(101L, captor.getValue().get(1).getGradeCatalogId());
    }

    @Test
    void savePublication_shouldPersistPeriodicalSkuIssueTemplates() {
        ProductSpuSaveReqVO reqVO = new ProductSpuSaveReqVO();
        ProductSpuSaveReqVO.PublicationSpuExtSaveReqVO spuExt = new ProductSpuSaveReqVO.PublicationSpuExtSaveReqVO();
        spuExt.setPublisherId(1L);
        spuExt.setPublicationTypeId(2L);
        spuExt.setIssueMode(PublicationIssueModeEnum.PERIODICAL.getCode());
        spuExt.setIssueCycle("MONTHLY");
        reqVO.setPublicationExt(spuExt);
        ProductSkuSaveReqVO skuReq = new ProductSkuSaveReqVO();
        skuReq.setId(10L);
        ProductSkuSaveReqVO.PublicationSkuExtSaveReqVO skuExt = new ProductSkuSaveReqVO.PublicationSkuExtSaveReqVO();
        skuExt.setVolumeLabel("上册");
        skuReq.setPublicationExt(skuExt);
        skuReq.setApplicableGradeCatalogIds(List.of(100L));
        ProductSkuSaveReqVO.PublicationSkuIssueTemplateSaveReqVO template =
                new ProductSkuSaveReqVO.PublicationSkuIssueTemplateSaveReqVO();
        template.setIssueNo(1);
        template.setIssueName("第1期");
        template.setPublishOffsetDays(0);
        template.setDeliveryOffsetDays(7);
        template.setSort(1);
        template.setStatus(0);
        skuReq.setIssueTemplates(List.of(template));
        reqVO.setSkus(List.of(skuReq));
        ProductSkuDO savedSku = ProductSkuDO.builder().id(10L).build();

        publicationService.savePublication(1L, reqVO, List.of(savedSku), List.of());

        ArgumentCaptor<List<ProductPublicationSkuIssueTemplateDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(publicationSkuIssueTemplateMapper).insertBatch(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(10L, captor.getValue().get(0).getSkuId());
        assertEquals(1, captor.getValue().get(0).getIssueNo());
        assertEquals("第1期", captor.getValue().get(0).getIssueName());
        assertEquals(7, captor.getValue().get(0).getDeliveryOffsetDays());
    }
}
