package cn.iocoder.yudao.module.repo.service.warehouse;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.repo.controller.admin.warehouse.vo.RepoWarehousePageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.warehouse.vo.RepoWarehouseSaveReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.warehouse.RepoWarehouseDO;
import cn.iocoder.yudao.module.repo.dal.mysql.warehouse.RepoWarehouseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.WAREHOUSE_DISABLED;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.WAREHOUSE_IN_USE_BY_SCHOOL;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.WAREHOUSE_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.WAREHOUSE_NOT_EXISTS;

@Service
@Validated
public class RepoWarehouseServiceImpl implements RepoWarehouseService {

    @Resource
    private RepoWarehouseMapper warehouseMapper;

    @Override
    public Long createWarehouse(RepoWarehouseSaveReqVO createReqVO) {
        validateWarehouseNameUnique(null, createReqVO.getName());
        RepoWarehouseDO warehouse = BeanUtils.toBean(createReqVO, RepoWarehouseDO.class);
        if (warehouse.getDefaultStatus() == null) {
            warehouse.setDefaultStatus(false);
        }
        warehouseMapper.insert(warehouse);
        return warehouse.getId();
    }

    @Override
    public void updateWarehouse(RepoWarehouseSaveReqVO updateReqVO) {
        validateWarehouseExists(updateReqVO.getId());
        validateWarehouseNameUnique(updateReqVO.getId(), updateReqVO.getName());
        if (CommonStatusEnum.isDisable(updateReqVO.getStatus())) {
            validateWarehouseUnused(updateReqVO.getId());
        }
        RepoWarehouseDO updateObj = BeanUtils.toBean(updateReqVO, RepoWarehouseDO.class);
        if (updateObj.getDefaultStatus() == null) {
            updateObj.setDefaultStatus(false);
        }
        warehouseMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWarehouseDefaultStatus(Long id, Boolean defaultStatus) {
        validateWarehouseExists(id);
        if (Boolean.TRUE.equals(defaultStatus)) {
            RepoWarehouseDO oldDefault = warehouseMapper.selectByDefaultStatus();
            if (oldDefault != null && !Objects.equals(oldDefault.getId(), id)) {
                warehouseMapper.updateById(new RepoWarehouseDO().setId(oldDefault.getId()).setDefaultStatus(false));
            }
        }
        warehouseMapper.updateById(new RepoWarehouseDO().setId(id).setDefaultStatus(Boolean.TRUE.equals(defaultStatus)));
    }

    @Override
    public void deleteWarehouse(Long id) {
        validateWarehouseExists(id);
        validateWarehouseUnused(id);
        warehouseMapper.deleteById(id);
    }

    @Override
    public RepoWarehouseDO getWarehouse(Long id) {
        return warehouseMapper.selectById(id);
    }

    @Override
    public PageResult<RepoWarehouseDO> getWarehousePage(RepoWarehousePageReqVO pageReqVO) {
        return warehouseMapper.selectPage(pageReqVO);
    }

    @Override
    public List<RepoWarehouseDO> getWarehouseListByStatus(Integer status) {
        return warehouseMapper.selectListByStatus(status);
    }

    @Override
    public Map<Long, RepoWarehouseDO> getWarehouseMap(Collection<Long> warehouseIds) {
        if (CollUtil.isEmpty(warehouseIds)) {
            return Collections.emptyMap();
        }
        List<Long> filteredWarehouseIds = warehouseIds.stream().filter(Objects::nonNull).distinct().toList();
        if (CollUtil.isEmpty(filteredWarehouseIds)) {
            return Collections.emptyMap();
        }
        return convertMap(warehouseMapper.selectByIds(filteredWarehouseIds), RepoWarehouseDO::getId);
    }

    @Override
    public RepoWarehouseDO validateWarehouseBindable(Long warehouseId) {
        RepoWarehouseDO warehouse = validateWarehouseExists(warehouseId);
        if (CommonStatusEnum.isDisable(warehouse.getStatus())) {
            throw exception(WAREHOUSE_DISABLED);
        }
        return warehouse;
    }

    private RepoWarehouseDO validateWarehouseExists(Long id) {
        RepoWarehouseDO warehouse = warehouseMapper.selectById(id);
        if (warehouse == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }
        return warehouse;
    }

    private void validateWarehouseNameUnique(Long id, String name) {
        RepoWarehouseDO existed = warehouseMapper.selectByName(name);
        if (existed == null || Objects.equals(existed.getId(), id)) {
            return;
        }
        throw exception(WAREHOUSE_NAME_DUPLICATE);
    }

    private void validateWarehouseUnused(Long warehouseId) {
        if (warehouseMapper.countBoundSchoolByWarehouseId(warehouseId) > 0) {
            throw exception(WAREHOUSE_IN_USE_BY_SCHOOL);
        }
    }

}
