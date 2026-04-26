package cn.iocoder.yudao.module.infra.dal.mysql.codegen;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface CodegenColumnMapper extends BaseMapperX<CodegenColumnDO> {

    default List<CodegenColumnDO> selectListByTableId(Long tableId) {
        return selectList(new LambdaQueryWrapperX<CodegenColumnDO>()
                .eq(CodegenColumnDO::getTableId, tableId)
                .orderByAsc(CodegenColumnDO::getOrdinalPosition));
    }

    default int deleteListByTableId(Long tableId) {
        return delete(CodegenColumnDO::getTableId, tableId);
    }

    default int deleteListByTableId(Collection<Long> tableIds) {
        return delete(new LambdaQueryWrapperX<CodegenColumnDO>()
               .in(CodegenColumnDO::getTableId, tableIds));
    }

}
