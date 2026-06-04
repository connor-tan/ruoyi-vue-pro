package cn.iocoder.yudao.module.repo.dal.mysql.publicationreceipt;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptDemandPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.publicationreceipt.vo.RepoPublicationReceiptDemandRespVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt.RepoPublicationReceiptItemDO;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptBalanceBO;
import cn.iocoder.yudao.module.repo.service.publicationreceipt.bo.RepoPublicationReceiptBalanceKey;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface RepoPublicationReceiptItemMapper extends BaseMapperX<RepoPublicationReceiptItemDO> {

    default List<RepoPublicationReceiptItemDO> selectListByReceiptId(Long receiptId) {
        return selectList(new LambdaQueryWrapperX<RepoPublicationReceiptItemDO>()
                .eq(RepoPublicationReceiptItemDO::getReceiptId, receiptId)
                .orderByAsc(RepoPublicationReceiptItemDO::getIssueNo)
                .orderByAsc(RepoPublicationReceiptItemDO::getId));
    }

    IPage<RepoPublicationReceiptDemandRespVO> selectDemandPage(IPage<?> page,
            @Param("reqVO") RepoPublicationReceiptDemandPageReqVO reqVO);

    RepoPublicationReceiptDemandRespVO selectDemandByKey(
            @Param("warehouseId") Long warehouseId,
            @Param("windowId") Long windowId,
            @Param("offerId") Long offerId,
            @Param("offerSkuId") Long offerSkuId,
            @Param("skuId") Long skuId,
            @Param("issueId") Long issueId,
            @Param("issueNo") Integer issueNo);

    List<RepoPublicationReceiptBalanceBO> selectBalanceList(@Param("keys") Collection<RepoPublicationReceiptBalanceKey> keys);

    List<RepoPublicationReceiptItemDO> selectAvailableListForUpdate(@Param("key") RepoPublicationReceiptBalanceKey key);

}
