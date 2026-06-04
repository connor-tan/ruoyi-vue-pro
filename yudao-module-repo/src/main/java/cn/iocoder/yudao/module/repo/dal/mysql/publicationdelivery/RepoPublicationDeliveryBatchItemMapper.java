package cn.iocoder.yudao.module.repo.dal.mysql.publicationdelivery;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationdelivery.RepoPublicationDeliveryBatchItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RepoPublicationDeliveryBatchItemMapper extends BaseMapperX<RepoPublicationDeliveryBatchItemDO> {

    default List<RepoPublicationDeliveryBatchItemDO> selectListByBatchId(Long batchId) {
        return selectList(RepoPublicationDeliveryBatchItemDO::getBatchId, batchId);
    }

}
