package cn.iocoder.yudao.module.product.service.publication;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.controller.admin.publicationtitle.vo.ProductPublicationTitleSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationPublisherDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTitleDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTypeDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationTitleIdentifierMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationTitleMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductSpuPublicationMapper;
import cn.iocoder.yudao.module.product.enums.publication.ProductPublicationTypeIdentifierRuleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PUBLICATION_TITLE_IDENTIFIER_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductPublicationTitleServiceTest {

    private ProductPublicationTitleService service;
    private ProductPublicationTitleMapper publicationTitleMapper;
    private ProductPublicationTitleIdentifierMapper publicationTitleIdentifierMapper;
    private ProductSpuPublicationMapper productSpuPublicationMapper;
    private ProductPublicationTypeService publicationTypeService;
    private ProductPublicationPublisherService publicationPublisherService;

    @BeforeEach
    void setUp() {
        service = new ProductPublicationTitleService();
        publicationTitleMapper = mock(ProductPublicationTitleMapper.class);
        publicationTitleIdentifierMapper = mock(ProductPublicationTitleIdentifierMapper.class);
        productSpuPublicationMapper = mock(ProductSpuPublicationMapper.class);
        publicationTypeService = mock(ProductPublicationTypeService.class);
        publicationPublisherService = mock(ProductPublicationPublisherService.class);
        ReflectionTestUtils.setField(service, "publicationTitleMapper", publicationTitleMapper);
        ReflectionTestUtils.setField(service, "publicationTitleIdentifierMapper", publicationTitleIdentifierMapper);
        ReflectionTestUtils.setField(service, "productSpuPublicationMapper", productSpuPublicationMapper);
        ReflectionTestUtils.setField(service, "publicationTypeService", publicationTypeService);
        ReflectionTestUtils.setField(service, "publicationPublisherService", publicationPublisherService);
    }

    @Test
    void createShouldRejectEnabledTitleWhenIdentifierRuleRequiresTitleIdentifier() {
        ProductPublicationTitleSaveReqVO reqVO = titleReq(CommonStatusEnum.ENABLE.getStatus());
        when(publicationTypeService.validateExists(2L)).thenReturn(type(
                ProductPublicationTypeIdentifierRuleEnum.TITLE_PERIODICAL_IDENTIFIER_REQUIRED.getRule()));
        when(publicationPublisherService.validateExists(1L)).thenReturn(ProductPublicationPublisherDO.builder().id(1L).build());

        assertServiceException(() -> service.create(reqVO), PUBLICATION_TITLE_IDENTIFIER_REQUIRED);
    }

    @Test
    void createShouldAllowDisabledTitleWithoutIdentifier() {
        ProductPublicationTitleSaveReqVO reqVO = titleReq(CommonStatusEnum.DISABLE.getStatus());
        when(publicationTypeService.validateExists(2L)).thenReturn(type(
                ProductPublicationTypeIdentifierRuleEnum.TITLE_PERIODICAL_IDENTIFIER_REQUIRED.getRule()));
        when(publicationPublisherService.validateExists(1L)).thenReturn(ProductPublicationPublisherDO.builder().id(1L).build());
        when(publicationTitleMapper.selectByCode(reqVO.getCode())).thenReturn(null);
        when(publicationTitleMapper.selectByName(reqVO.getName())).thenReturn(null);
        doAnswer(invocation -> {
            ProductPublicationTitleDO title = invocation.getArgument(0);
            title.setId(10L);
            return 1;
        }).when(publicationTitleMapper).insert(any(ProductPublicationTitleDO.class));

        Long id = service.create(reqVO);

        assertEquals(10L, id);
        ArgumentCaptor<ProductPublicationTitleDO> captor = ArgumentCaptor.forClass(ProductPublicationTitleDO.class);
        verify(publicationTitleMapper).insert(captor.capture());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), captor.getValue().getStatus());
    }

    @Test
    void updateShouldRejectEnablingTitleWithoutIdentifier() {
        ProductPublicationTitleSaveReqVO reqVO = titleReq(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setId(10L);
        when(publicationTitleMapper.selectById(10L)).thenReturn(ProductPublicationTitleDO.builder().id(10L).build());
        when(publicationTypeService.validateExists(2L)).thenReturn(type(
                ProductPublicationTypeIdentifierRuleEnum.TITLE_PERIODICAL_IDENTIFIER_REQUIRED.getRule()));
        when(publicationPublisherService.validateExists(1L)).thenReturn(ProductPublicationPublisherDO.builder().id(1L).build());
        when(publicationTitleMapper.selectByCode(reqVO.getCode())).thenReturn(null);
        when(publicationTitleMapper.selectByName(reqVO.getName())).thenReturn(null);

        assertServiceException(() -> service.update(reqVO), PUBLICATION_TITLE_IDENTIFIER_REQUIRED);
    }

    @Test
    void createShouldAllowEnabledTitleWhenAnyIdentifierExists() {
        ProductPublicationTitleSaveReqVO reqVO = titleReq(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCnCode("CN-TEST");
        when(publicationTypeService.validateExists(2L)).thenReturn(type(
                ProductPublicationTypeIdentifierRuleEnum.TITLE_PERIODICAL_IDENTIFIER_REQUIRED.getRule()));
        when(publicationPublisherService.validateExists(1L)).thenReturn(ProductPublicationPublisherDO.builder().id(1L).build());
        when(publicationTitleMapper.selectByCode(reqVO.getCode())).thenReturn(null);
        when(publicationTitleMapper.selectByName(reqVO.getName())).thenReturn(null);
        doAnswer(invocation -> {
            ProductPublicationTitleDO title = invocation.getArgument(0);
            title.setId(11L);
            return 1;
        }).when(publicationTitleMapper).insert(any(ProductPublicationTitleDO.class));

        Long id = service.create(reqVO);

        assertEquals(11L, id);
    }

    private ProductPublicationTitleSaveReqVO titleReq(Integer status) {
        ProductPublicationTitleSaveReqVO reqVO = new ProductPublicationTitleSaveReqVO();
        reqVO.setCode("TEST_TITLE");
        reqVO.setName("测试刊物主档");
        reqVO.setTypeId(2L);
        reqVO.setPublisherId(1L);
        reqVO.setIssueCycle("月刊");
        reqVO.setStatus(status);
        return reqVO;
    }

    private ProductPublicationTypeDO type(String identifierRule) {
        return ProductPublicationTypeDO.builder()
                .id(2L)
                .code("MAGAZINE")
                .name("杂志")
                .identifierRule(identifierRule)
                .build();
    }
}
