package cn.iocoder.yudao.module.repo.dal.mysql.publicationreceipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptPageReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt.RepoPublicationReceiptDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RepoPublicationReceiptMapper extends BaseMapperX<RepoPublicationReceiptDO> {

    default PageResult<RepoPublicationReceiptDO> selectPage(RepoPublicationReceiptPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RepoPublicationReceiptDO>()
                .likeIfPresent(RepoPublicationReceiptDO::getReceiptNo, reqVO.getReceiptNo())
                .eqIfPresent(RepoPublicationReceiptDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(RepoPublicationReceiptDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(RepoPublicationReceiptDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(RepoPublicationReceiptDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(RepoPublicationReceiptDO::getId));
    }

}
