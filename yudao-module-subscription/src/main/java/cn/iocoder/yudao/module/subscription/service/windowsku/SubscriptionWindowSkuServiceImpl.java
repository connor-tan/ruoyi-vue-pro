package cn.iocoder.yudao.module.subscription.service.windowsku;

import com.baomidou.dynamic.datasource.annotation.Master;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowsku.vo.SubscriptionWindowSkuBatchUpdateReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.windowsku.vo.SubscriptionWindowSkuRespVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSkuDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.window.SubscriptionWindowSpuDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSkuMapper;
import cn.iocoder.yudao.module.subscription.dal.mysql.window.SubscriptionWindowSpuMapper;
import cn.iocoder.yudao.module.subscription.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.subscription.service.support.SubscriptionSupportService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class SubscriptionWindowSkuServiceImpl implements SubscriptionWindowSkuService {

    @Resource
    private SubscriptionWindowSkuMapper subscriptionWindowSkuMapper;
    @Resource
    private SubscriptionWindowSpuMapper subscriptionWindowSpuMapper;
    @Resource
    private SubscriptionSupportService subscriptionSupportService;

    @Override
    @Master
    @Transactional(rollbackFor = Exception.class)
    public List<SubscriptionWindowSkuRespVO> getWindowSkuListByWindowSpuId(Long windowSpuId) {
        SubscriptionWindowSpuDO windowSpu = validateWindowSpuExists(windowSpuId);
        syncMissingWindowSkus(windowSpu);
        List<SubscriptionWindowSkuDO> windowSkus = subscriptionWindowSkuMapper.selectListByWindowSpuId(windowSpuId);
        if (windowSkus.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, ProductSkuDO> productSkuMap = CollectionUtils.convertMap(
                subscriptionSupportService.getSkuListBySpuId(windowSpu.getProductSpuId()), ProductSkuDO::getId);
        Map<Long, ProductSkuPublicationDO> skuPublicationMap = subscriptionSupportService.getSkuPublicationMap(
                CollectionUtils.convertSet(windowSkus, SubscriptionWindowSkuDO::getProductSkuId));
        return windowSkus.stream()
                .map(windowSku -> buildResp(windowSku, productSkuMap.get(windowSku.getProductSkuId()),
                        skuPublicationMap.get(windowSku.getProductSkuId())))
                .toList();
    }

    @Override
    @Master
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateWindowSku(SubscriptionWindowSkuBatchUpdateReqVO reqVO) {
        SubscriptionWindowSpuDO windowSpu = validateWindowSpuExists(reqVO.getWindowSpuId());
        syncMissingWindowSkus(windowSpu);
        Map<Long, SubscriptionWindowSkuDO> windowSkuMap = CollectionUtils.convertMap(
                subscriptionWindowSkuMapper.selectListByWindowSpuId(reqVO.getWindowSpuId()),
                SubscriptionWindowSkuDO::getId);
        for (SubscriptionWindowSkuBatchUpdateReqVO.Item item : reqVO.getItems()) {
            SubscriptionWindowSkuDO windowSku = windowSkuMap.get(item.getId());
            if (windowSku == null) {
                throw exception(ErrorCodeConstants.WINDOW_SKU_NOT_EXISTS);
            }
            windowSku.setStatus(item.getStatus());
            windowSku.setSort(item.getSort());
            windowSku.setMaxQuantityPerStudent(item.getMaxQuantityPerStudent());
            windowSku.setRemark(item.getRemark());
            subscriptionWindowSkuMapper.updateById(windowSku);
        }
    }

    @Override
    public List<SubscriptionWindowSkuDO> getWindowSkuDOList(Collection<Long> windowSpuIds) {
        return subscriptionWindowSkuMapper.selectListByWindowSpuIds(windowSpuIds);
    }

    private SubscriptionWindowSpuDO validateWindowSpuExists(Long windowSpuId) {
        SubscriptionWindowSpuDO windowSpu = subscriptionWindowSpuMapper.selectById(windowSpuId);
        if (windowSpu == null) {
            throw exception(ErrorCodeConstants.WINDOW_SPU_NOT_EXISTS);
        }
        return windowSpu;
    }

    private void syncMissingWindowSkus(SubscriptionWindowSpuDO windowSpu) {
        List<SubscriptionWindowSkuDO> existingRows = subscriptionWindowSkuMapper.selectListByWindowSpuId(windowSpu.getId());
        Map<Long, SubscriptionWindowSkuDO> existingMap = CollectionUtils.convertMap(existingRows, SubscriptionWindowSkuDO::getProductSkuId);
        List<ProductSkuDO> productSkus = subscriptionSupportService.getSkuListBySpuId(windowSpu.getProductSpuId());
        productSkus.sort(Comparator.comparing(ProductSkuDO::getId));
        int nextSort = existingRows.stream()
                .map(SubscriptionWindowSkuDO::getSort)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        for (ProductSkuDO productSku : productSkus) {
            if (existingMap.containsKey(productSku.getId())) {
                continue;
            }
            subscriptionWindowSkuMapper.insert(SubscriptionWindowSkuDO.builder()
                    .windowSpuId(windowSpu.getId())
                    .productSkuId(productSku.getId())
                    .status(CommonStatusEnum.ENABLE.getStatus())
                    .sort(nextSort++)
                    .maxQuantityPerStudent(1)
                    .remark(null)
                    .build());
        }
    }

    private SubscriptionWindowSkuRespVO buildResp(SubscriptionWindowSkuDO windowSku,
                                                  ProductSkuDO productSku,
                                                  ProductSkuPublicationDO skuPublication) {
        SubscriptionWindowSkuRespVO respVO = new SubscriptionWindowSkuRespVO();
        respVO.setId(windowSku.getId());
        respVO.setWindowSpuId(windowSku.getWindowSpuId());
        respVO.setProductSkuId(windowSku.getProductSkuId());
        respVO.setStatus(windowSku.getStatus());
        respVO.setSort(windowSku.getSort());
        respVO.setMaxQuantityPerStudent(windowSku.getMaxQuantityPerStudent());
        respVO.setRemark(windowSku.getRemark());
        if (productSku != null) {
            respVO.setPrice(productSku.getPrice());
            respVO.setStock(productSku.getStock());
        }
        if (skuPublication != null) {
            respVO.setVolumeLabel(skuPublication.getVolumeLabel());
            respVO.setEditionLabel(skuPublication.getEditionLabel());
            respVO.setIsbn(skuPublication.getIsbn());
        }
        return respVO;
    }
}
