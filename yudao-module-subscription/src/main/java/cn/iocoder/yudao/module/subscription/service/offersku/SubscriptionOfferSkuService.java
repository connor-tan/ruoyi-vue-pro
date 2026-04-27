package cn.iocoder.yudao.module.subscription.service.offersku;

import cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo.SubscriptionOfferSkuBatchUpdateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo.SubscriptionOfferSkuRespVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offersku.vo.SubscriptionOfferSkuSaveReqVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;

import java.util.List;

/**
 * 订刊窗口 SKU Service 接口
 */
public interface SubscriptionOfferSkuService {

    List<SubscriptionOfferSkuRespVO> getOfferSkuList(Long offerId);

    int syncMatchedOfferSkus(Long offerId);

    void batchUpdate(SubscriptionOfferSkuBatchUpdateReqVO reqVO);

    Long saveOfferSku(SubscriptionOfferSkuSaveReqVO reqVO);

    void deleteOfferSku(Long id);

    SubscriptionWindowOfferSkuDO getOfferSku(Long id);

    SubscriptionWindowOfferSkuDO validateOfferSkuExists(Long id);

}
