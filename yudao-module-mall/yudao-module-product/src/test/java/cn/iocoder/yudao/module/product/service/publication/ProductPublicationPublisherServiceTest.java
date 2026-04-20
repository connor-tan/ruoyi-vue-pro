package cn.iocoder.yudao.module.product.service.publication;

import cn.iocoder.yudao.module.product.controller.admin.publicationpublisher.vo.ProductPublicationPublisherSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationPublisherDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationPublisherMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationTitleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductPublicationPublisherServiceTest {

    private ProductPublicationPublisherService service;
    private ProductPublicationPublisherMapper publicationPublisherMapper;
    private ProductPublicationTitleMapper publicationTitleMapper;

    @BeforeEach
    void setUp() {
        service = new ProductPublicationPublisherService();
        publicationPublisherMapper = mock(ProductPublicationPublisherMapper.class);
        publicationTitleMapper = mock(ProductPublicationTitleMapper.class);
        ReflectionTestUtils.setField(service, "publicationPublisherMapper", publicationPublisherMapper);
        ReflectionTestUtils.setField(service, "publicationTitleMapper", publicationTitleMapper);
    }

    @Test
    void createShouldGenerateReadableCodeFromPublisherName() {
        ProductPublicationPublisherSaveReqVO reqVO = publisherReq("人民教育出版社");
        when(publicationPublisherMapper.selectByName(reqVO.getName())).thenReturn(null);
        when(publicationPublisherMapper.selectByCodeIncludeDeleted("PUB_RMJYCBS")).thenReturn(null);
        doAnswer(invocation -> {
            ProductPublicationPublisherDO publisher = invocation.getArgument(0);
            publisher.setId(1L);
            return 1;
        }).when(publicationPublisherMapper).insert(any(ProductPublicationPublisherDO.class));

        Long id = service.create(reqVO);

        assertEquals(1L, id);
        ArgumentCaptor<ProductPublicationPublisherDO> captor = ArgumentCaptor.forClass(ProductPublicationPublisherDO.class);
        verify(publicationPublisherMapper).insert(captor.capture());
        assertEquals("PUB_RMJYCBS", captor.getValue().getCode());
    }

    @Test
    void createShouldAppendSequenceWhenCodeConflicts() {
        ProductPublicationPublisherSaveReqVO reqVO = publisherReq("人民教育出版社");
        when(publicationPublisherMapper.selectByName(reqVO.getName())).thenReturn(null);
        when(publicationPublisherMapper.selectByCodeIncludeDeleted("PUB_RMJYCBS"))
                .thenReturn(ProductPublicationPublisherDO.builder().id(1L).code("PUB_RMJYCBS").build());
        when(publicationPublisherMapper.selectByCodeIncludeDeleted("PUB_RMJYCBS_02")).thenReturn(null);
        doAnswer(invocation -> {
            ProductPublicationPublisherDO publisher = invocation.getArgument(0);
            publisher.setId(2L);
            return 1;
        }).when(publicationPublisherMapper).insert(any(ProductPublicationPublisherDO.class));

        Long id = service.create(reqVO);

        assertEquals(2L, id);
        ArgumentCaptor<ProductPublicationPublisherDO> captor = ArgumentCaptor.forClass(ProductPublicationPublisherDO.class);
        verify(publicationPublisherMapper).insert(captor.capture());
        assertEquals("PUB_RMJYCBS_02", captor.getValue().getCode());
    }

    @Test
    void updateShouldKeepExistingCodeWhenNameChanges() {
        ProductPublicationPublisherSaveReqVO reqVO = publisherReq("新出版社");
        reqVO.setId(1L);
        when(publicationPublisherMapper.selectById(1L))
                .thenReturn(ProductPublicationPublisherDO.builder().id(1L).code("PUB_OLD").name("旧出版社").build());
        when(publicationPublisherMapper.selectByName(reqVO.getName())).thenReturn(null);

        service.update(reqVO);

        ArgumentCaptor<ProductPublicationPublisherDO> captor = ArgumentCaptor.forClass(ProductPublicationPublisherDO.class);
        verify(publicationPublisherMapper).updateById(captor.capture());
        assertEquals("PUB_OLD", captor.getValue().getCode());
    }

    private ProductPublicationPublisherSaveReqVO publisherReq(String name) {
        ProductPublicationPublisherSaveReqVO reqVO = new ProductPublicationPublisherSaveReqVO();
        reqVO.setName(name);
        reqVO.setSort(10);
        reqVO.setStatus(0);
        reqVO.setRemark("");
        return reqVO;
    }
}
