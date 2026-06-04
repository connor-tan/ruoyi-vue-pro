package cn.iocoder.yudao.module.repo.service.supplierpublication;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo.RepoPublicationSkuPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo.RepoSupplierPublicationSkuPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo.RepoSupplierPublicationSkuSaveReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.supplierpublication.RepoSupplierPublicationSkuDO;
import cn.iocoder.yudao.module.repo.service.supplierpublication.bo.RepoPublicationSkuBO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RepoSupplierPublicationSkuService {

    Long createSupplierPublicationSku(RepoSupplierPublicationSkuSaveReqVO createReqVO);

    void updateSupplierPublicationSku(RepoSupplierPublicationSkuSaveReqVO updateReqVO);

    void deleteSupplierPublicationSku(Long id);

    RepoSupplierPublicationSkuDO getSupplierPublicationSku(Long id);

    PageResult<RepoSupplierPublicationSkuDO> getSupplierPublicationSkuPage(RepoSupplierPublicationSkuPageReqVO pageReqVO);

    PageResult<RepoPublicationSkuBO> getPublicationSkuPage(RepoPublicationSkuPageReqVO pageReqVO);

    RepoPublicationSkuBO getPublicationSku(Long skuId);

    Map<Long, RepoPublicationSkuBO> getPublicationSkuMap(Collection<Long> skuIds);

    RepoSupplierPublicationSkuDO validateSupplierPublicationSkuEnabled(Long supplierId, Long skuId);

    List<RepoSupplierPublicationSkuDO> getSupplierPublicationSkuListBySupplierId(Long supplierId);

}
