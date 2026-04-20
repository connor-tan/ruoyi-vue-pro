package cn.iocoder.yudao.module.product.service.publication;

import cn.iocoder.yudao.module.product.controller.admin.publicationproduct.vo.ProductPublicationProductSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.publicationproduct.vo.ProductPublicationProductSkuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.publicationtitle.vo.ProductPublicationTitleRespVO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTitleDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTypeDO;
import cn.iocoder.yudao.module.product.enums.publication.ProductPublicationTypeIdentifierRuleEnum;
import cn.iocoder.yudao.module.product.service.category.ProductCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PUBLICATION_PRODUCT_IDENTIFIER_REQUIRED;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PUBLICATION_PRODUCT_ISBN_REQUIRED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductPublicationProductServiceTest {

    private ProductPublicationProductService service;
    private ProductPublicationTitleService publicationTitleService;
    private ProductPublicationTypeService publicationTypeService;

    @BeforeEach
    void setUp() {
        service = new ProductPublicationProductService();
        ReflectionTestUtils.setField(service, "categoryService", mock(ProductCategoryService.class));
        publicationTitleService = mock(ProductPublicationTitleService.class);
        publicationTypeService = mock(ProductPublicationTypeService.class);
        ReflectionTestUtils.setField(service, "publicationTitleService", publicationTitleService);
        ReflectionTestUtils.setField(service, "publicationTypeService", publicationTypeService);
    }

    @Test
    void createShouldRejectTitleIdentifierRuleWhenTitleIdentifiersAreMissing() {
        ProductPublicationProductSaveReqVO reqVO = productReq("");
        mockTitleAndType(ProductPublicationTypeIdentifierRuleEnum.TITLE_PERIODICAL_IDENTIFIER_REQUIRED.getRule());
        when(publicationTitleService.get(10L)).thenReturn(new ProductPublicationTitleRespVO());

        assertServiceException(() -> service.create(reqVO), PUBLICATION_PRODUCT_IDENTIFIER_REQUIRED);
    }

    @Test
    void createShouldRejectSkuIsbnRuleWhenSkuIsbnIsMissing() {
        ProductPublicationProductSaveReqVO reqVO = productReq("");
        mockTitleAndType(ProductPublicationTypeIdentifierRuleEnum.SKU_ISBN_REQUIRED.getRule());

        assertServiceException(() -> service.create(reqVO), PUBLICATION_PRODUCT_ISBN_REQUIRED);
    }

    private void mockTitleAndType(String identifierRule) {
        ProductPublicationTitleDO title = ProductPublicationTitleDO.builder()
                .id(10L)
                .typeId(2L)
                .build();
        when(publicationTitleService.validateExists(10L)).thenReturn(title);
        when(publicationTypeService.validateExists(2L)).thenReturn(ProductPublicationTypeDO.builder()
                .id(2L)
                .code("MAGAZINE")
                .name("杂志")
                .identifierRule(identifierRule)
                .build());
    }

    private ProductPublicationProductSaveReqVO productReq(String isbn) {
        ProductPublicationProductSaveReqVO reqVO = new ProductPublicationProductSaveReqVO();
        reqVO.setCategoryId(90002L);
        reqVO.setPublicationTitleId(10L);
        reqVO.setApplicableGradeCatalogIds(List.of(1L));
        ProductPublicationProductSkuSaveReqVO sku = new ProductPublicationProductSkuSaveReqVO();
        sku.setIsbn(isbn);
        reqVO.setSkus(List.of(sku));
        return reqVO;
    }
}
