package cn.iocoder.yudao.module.repo.service.publicationdelivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchCreateReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchGroupCreateReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchGroupCreateRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryBatchRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidateChildReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidateGroupRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidateItemRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidatePageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationdelivery.vo.RepoPublicationDeliveryCandidateRespVO;

import java.util.List;

public interface RepoPublicationDeliveryBatchService {

    PageResult<RepoPublicationDeliveryCandidateRespVO> getCandidatePage(
            RepoPublicationDeliveryCandidatePageReqVO reqVO);

    PageResult<RepoPublicationDeliveryCandidateGroupRespVO> getCandidateGroupPage(
            RepoPublicationDeliveryCandidatePageReqVO reqVO);

    List<RepoPublicationDeliveryCandidateRespVO> getCandidateChildList(
            RepoPublicationDeliveryCandidateChildReqVO reqVO);

    PageResult<RepoPublicationDeliveryCandidateRespVO> getCandidateChildPage(
            RepoPublicationDeliveryCandidateChildReqVO reqVO);

    List<RepoPublicationDeliveryCandidateItemRespVO> getCandidateItemList(
            RepoPublicationDeliveryCandidatePageReqVO reqVO);

    Long createAndDeliver(RepoPublicationDeliveryBatchCreateReqVO reqVO, Long operatorUserId);

    RepoPublicationDeliveryBatchGroupCreateRespVO createGroupAndDeliver(
            RepoPublicationDeliveryBatchGroupCreateReqVO reqVO, Long operatorUserId);

    PageResult<RepoPublicationDeliveryBatchRespVO> getBatchPage(RepoPublicationDeliveryBatchPageReqVO reqVO);

    RepoPublicationDeliveryBatchRespVO getBatch(Long id);

}
