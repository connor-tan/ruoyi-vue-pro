package cn.iocoder.yudao.module.edu.service.publication;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.publicationpublisher.vo.ProductPublicationPublisherPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.publicationpublisher.vo.ProductPublicationPublisherRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.publicationpublisher.vo.ProductPublicationPublisherSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.publicationpublisher.vo.ProductPublicationPublisherSimpleRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.publication.ProductPublicationPublisherDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 刊物出版社 Service 接口
 */
public interface ProductPublicationPublisherService {

    Long create(ProductPublicationPublisherSaveReqVO reqVO);

    void update(ProductPublicationPublisherSaveReqVO reqVO);

    void delete(Long id);

    ProductPublicationPublisherRespVO get(Long id);

    ProductPublicationPublisherDO getDO(Long id);

    Map<Long, ProductPublicationPublisherDO> getDOMap(Collection<Long> ids);

    PageResult<ProductPublicationPublisherRespVO> getPage(ProductPublicationPublisherPageReqVO reqVO);

    List<ProductPublicationPublisherSimpleRespVO> getSimpleList();

    ProductPublicationPublisherDO validateExists(Long id);

    ProductPublicationPublisherDO validateEnabled(Long id);

}
