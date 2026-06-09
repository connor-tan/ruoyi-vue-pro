package cn.iocoder.yudao.module.repo.dal.mysql.publicationdelivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchPageReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationdelivery.RepoPublicationDeliveryBatchDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RepoPublicationDeliveryBatchMapper extends BaseMapperX<RepoPublicationDeliveryBatchDO> {

    default PageResult<RepoPublicationDeliveryBatchDO> selectPage(RepoPublicationDeliveryBatchPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RepoPublicationDeliveryBatchDO>()
                .likeIfPresent(RepoPublicationDeliveryBatchDO::getBatchNo, reqVO.getBatchNo())
                .eqIfPresent(RepoPublicationDeliveryBatchDO::getDeliveryType, reqVO.getDeliveryType())
                .eqIfPresent(RepoPublicationDeliveryBatchDO::getSchoolId, reqVO.getSchoolId())
                .eqIfPresent(RepoPublicationDeliveryBatchDO::getStationId, reqVO.getStationId())
                .eqIfPresent(RepoPublicationDeliveryBatchDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(RepoPublicationDeliveryBatchDO::getWindowId, reqVO.getWindowId())
                .eqIfPresent(RepoPublicationDeliveryBatchDO::getOfferId, reqVO.getOfferId())
                .eqIfPresent(RepoPublicationDeliveryBatchDO::getOfferSkuId, reqVO.getOfferSkuId())
                .eqIfPresent(RepoPublicationDeliveryBatchDO::getSkuId, reqVO.getSkuId())
                .eqIfPresent(RepoPublicationDeliveryBatchDO::getIssueId, reqVO.getIssueId())
                .eqIfPresent(RepoPublicationDeliveryBatchDO::getIssueNo, reqVO.getIssueNo())
                .eqIfPresent(RepoPublicationDeliveryBatchDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(RepoPublicationDeliveryBatchDO::getDeliveryTime, reqVO.getDeliveryTime())
                .orderByDesc(RepoPublicationDeliveryBatchDO::getId));
    }

}
