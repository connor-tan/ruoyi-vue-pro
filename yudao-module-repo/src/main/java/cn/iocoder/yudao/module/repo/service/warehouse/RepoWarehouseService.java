package cn.iocoder.yudao.module.repo.service.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.repo.controller.admin.warehouse.vo.RepoWarehousePageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.warehouse.vo.RepoWarehouseSaveReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.warehouse.RepoWarehouseDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RepoWarehouseService {

    Long createWarehouse(RepoWarehouseSaveReqVO createReqVO);

    void updateWarehouse(RepoWarehouseSaveReqVO updateReqVO);

    void updateWarehouseDefaultStatus(Long id, Boolean defaultStatus);

    void deleteWarehouse(Long id);

    RepoWarehouseDO getWarehouse(Long id);

    PageResult<RepoWarehouseDO> getWarehousePage(RepoWarehousePageReqVO pageReqVO);

    List<RepoWarehouseDO> getWarehouseListByStatus(Integer status);

    Map<Long, RepoWarehouseDO> getWarehouseMap(Collection<Long> warehouseIds);

    RepoWarehouseDO validateWarehouseBindable(Long warehouseId);

}
