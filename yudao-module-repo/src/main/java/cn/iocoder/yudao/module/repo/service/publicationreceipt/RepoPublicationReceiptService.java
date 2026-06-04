package cn.iocoder.yudao.module.repo.service.publicationreceipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptCloseReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptCreateReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptDemandPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptDemandRespVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptReceiveReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt.RepoPublicationReceiptDO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt.RepoPublicationReceiptItemDO;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptBalanceBO;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptBalanceKey;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptDeliveryAllocateReqBO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RepoPublicationReceiptService {

    PageResult<RepoPublicationReceiptDemandRespVO> getDemandPage(RepoPublicationReceiptDemandPageReqVO pageReqVO);

    Long createReceipt(RepoPublicationReceiptCreateReqVO createReqVO);

    void submitReceipt(Long id);

    void receiveReceipt(RepoPublicationReceiptReceiveReqVO reqVO, Long operatorUserId);

    void closeReceipt(RepoPublicationReceiptCloseReqVO reqVO);

    RepoPublicationReceiptDO getReceipt(Long id);

    List<RepoPublicationReceiptItemDO> getReceiptItemList(Long receiptId);

    PageResult<RepoPublicationReceiptDO> getReceiptPage(RepoPublicationReceiptPageReqVO pageReqVO);

    Map<RepoPublicationReceiptBalanceKey, RepoPublicationReceiptBalanceBO> getBalanceMap(
            Collection<RepoPublicationReceiptBalanceKey> keys);

    void allocateDeliveryBatch(RepoPublicationReceiptDeliveryAllocateReqBO reqBO);

}
