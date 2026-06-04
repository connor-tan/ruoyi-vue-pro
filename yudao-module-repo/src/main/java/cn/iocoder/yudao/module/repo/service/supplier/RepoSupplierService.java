package cn.iocoder.yudao.module.repo.service.supplier;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.repo.controller.admin.supplier.vo.RepoSupplierPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.supplier.vo.RepoSupplierSaveReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.supplier.RepoSupplierDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RepoSupplierService {

    Long createSupplier(RepoSupplierSaveReqVO createReqVO);

    void updateSupplier(RepoSupplierSaveReqVO updateReqVO);

    void deleteSupplier(Long id);

    RepoSupplierDO getSupplier(Long id);

    PageResult<RepoSupplierDO> getSupplierPage(RepoSupplierPageReqVO pageReqVO);

    List<RepoSupplierDO> getSupplierListByStatus(Integer status);

    Map<Long, RepoSupplierDO> getSupplierMap(Collection<Long> supplierIds);

    RepoSupplierDO validateSupplierEnabled(Long supplierId);

}
