package cn.iocoder.yudao.module.edu.service.publication;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.publicationtype.vo.ProductPublicationTypePageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.publicationtype.vo.ProductPublicationTypeRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.publicationtype.vo.ProductPublicationTypeSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.publicationtype.vo.ProductPublicationTypeSimpleRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.publication.ProductPublicationTypeDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 刊物类型 Service 接口
 */
public interface ProductPublicationTypeService {

    Long create(ProductPublicationTypeSaveReqVO reqVO);

    void update(ProductPublicationTypeSaveReqVO reqVO);

    void delete(Long id);

    ProductPublicationTypeRespVO get(Long id);

    ProductPublicationTypeDO getDO(Long id);

    Map<Long, ProductPublicationTypeDO> getDOMap(Collection<Long> ids);

    PageResult<ProductPublicationTypeRespVO> getPage(ProductPublicationTypePageReqVO reqVO);

    List<ProductPublicationTypeSimpleRespVO> getSimpleList();

    ProductPublicationTypeDO validateExists(Long id);

    ProductPublicationTypeDO validateEnabled(Long id);

}
