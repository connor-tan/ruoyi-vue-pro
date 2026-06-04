package cn.iocoder.yudao.module.repo.dal.mysql.supplier;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.repo.controller.admin.supplier.vo.RepoSupplierPageReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.supplier.RepoSupplierDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RepoSupplierMapper extends BaseMapperX<RepoSupplierDO> {

    default PageResult<RepoSupplierDO> selectPage(RepoSupplierPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RepoSupplierDO>()
                .likeIfPresent(RepoSupplierDO::getName, reqVO.getName())
                .likeIfPresent(RepoSupplierDO::getCode, reqVO.getCode())
                .eqIfPresent(RepoSupplierDO::getStatus, reqVO.getStatus())
                .orderByAsc(RepoSupplierDO::getSort)
                .orderByDesc(RepoSupplierDO::getId));
    }

    default RepoSupplierDO selectByName(String name) {
        return selectOne(RepoSupplierDO::getName, name);
    }

    default RepoSupplierDO selectByCode(String code) {
        return selectOne(RepoSupplierDO::getCode, code);
    }

    default List<RepoSupplierDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<RepoSupplierDO>()
                .eqIfPresent(RepoSupplierDO::getStatus, status)
                .orderByAsc(RepoSupplierDO::getSort)
                .orderByDesc(RepoSupplierDO::getId));
    }

}
