package cn.iocoder.yudao.module.repo.dal.mysql.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.repo.controller.admin.warehouse.vo.RepoWarehousePageReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.warehouse.RepoWarehouseDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RepoWarehouseMapper extends BaseMapperX<RepoWarehouseDO> {

    default PageResult<RepoWarehouseDO> selectPage(RepoWarehousePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RepoWarehouseDO>()
                .likeIfPresent(RepoWarehouseDO::getName, reqVO.getName())
                .eqIfPresent(RepoWarehouseDO::getStatus, reqVO.getStatus())
                .orderByAsc(RepoWarehouseDO::getSort)
                .orderByDesc(RepoWarehouseDO::getId));
    }

    default RepoWarehouseDO selectByName(String name) {
        return selectOne(RepoWarehouseDO::getName, name);
    }

    default RepoWarehouseDO selectByDefaultStatus() {
        return selectOne(RepoWarehouseDO::getDefaultStatus, true);
    }

    default List<RepoWarehouseDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<RepoWarehouseDO>()
                .eqIfPresent(RepoWarehouseDO::getStatus, status)
                .orderByAsc(RepoWarehouseDO::getSort)
                .orderByDesc(RepoWarehouseDO::getId));
    }

    long countBoundSchoolByWarehouseId(@Param("warehouseId") Long warehouseId);

}
