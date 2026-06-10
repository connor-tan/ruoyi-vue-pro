package cn.iocoder.yudao.module.product.service.spu;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuUpdateStatusReqVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuCategoryRelDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuCategoryRelMapper;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.enums.sku.ProductSkuStatusEnum;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.product.mq.producer.spu.ProductSpuProducer;
import cn.iocoder.yudao.module.product.service.brand.ProductBrandService;
import cn.iocoder.yudao.module.product.service.category.ProductCategoryService;
import cn.iocoder.yudao.module.product.service.publication.ProductPublicationService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import cn.iocoder.yudao.module.product.service.spu.scene.ProductSceneHandler;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.trade.api.order.TradeSubscriptionOrderApi;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 商品 SPU Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductSpuServiceImpl implements ProductSpuService {

    @Resource
    private ProductSpuMapper productSpuMapper;
    @Resource
    private ProductSpuCategoryRelMapper productSpuCategoryRelMapper;

    @Resource
    @Lazy // 循环依赖，避免报错
    private ProductSkuService productSkuService;
    @Resource
    private ProductBrandService brandService;
    @Resource
    private ProductCategoryService categoryService;
    @Resource
    private ProductPublicationService productPublicationService;
    @Resource
    private TradeSubscriptionOrderApi tradeSubscriptionOrderApi;
    @Resource
    private List<ProductSceneHandler> productSceneHandlers;
    @Resource
    private ProductSpuProducer productSpuProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSpu(ProductSpuSaveReqVO createReqVO) {
        List<ProductCategoryDO> categories = categoryService.validateLeafCategoryList(
                createReqVO.getBizScene(), createReqVO.getCategoryIds());
        createReqVO.setCategoryIds(convertList(categories, ProductCategoryDO::getId));
        ProductSceneHandler sceneHandler = getSceneHandler(createReqVO.getBizScene());
        sceneHandler.validateForSave(createReqVO);

        ProductSpuDO spu = BeanUtils.toBean(createReqVO, ProductSpuDO.class);
        // 初始化 SPU 中 SKU 相关属性
        initSpuFromSkus(spu, createReqVO.getSkus());
        // 插入 SPU
        productSpuMapper.insert(spu);
        saveSpuCategoryRels(spu.getId(), createReqVO.getCategoryIds());
        // 插入 SKU
        List<ProductSkuDO> savedSkus = productSkuService.createSkuList(spu.getId(), createReqVO.getSkus());
        sceneHandler.afterSave(spu.getId(), createReqVO, savedSkus, convertSet(savedSkus, ProductSkuDO::getId));
        // 返回
        return spu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpu(ProductSpuSaveReqVO updateReqVO) {
        // 校验 SPU 是否存在
        ProductSpuDO spu = validateSpuExists(updateReqVO.getId());
        List<ProductSkuDO> oldSkus = productSkuService.getSkuListBySpuId(spu.getId());
        validatePublicationUpdateOrderReference(spu, updateReqVO, oldSkus);
        List<ProductCategoryDO> categories = categoryService.validateLeafCategoryList(
                updateReqVO.getBizScene(), updateReqVO.getCategoryIds());
        updateReqVO.setCategoryIds(convertList(categories, ProductCategoryDO::getId));
        ProductSceneHandler sceneHandler = getSceneHandler(updateReqVO.getBizScene());
        sceneHandler.validateForSave(updateReqVO);

        // 更新 SPU
        ProductSpuDO updateObj = BeanUtils.toBean(updateReqVO, ProductSpuDO.class).setStatus(spu.getStatus());
        initSpuFromSkus(updateObj, updateReqVO.getSkus());
        productSpuMapper.updateById(updateObj);
        saveSpuCategoryRels(updateObj.getId(), updateReqVO.getCategoryIds());
        // 批量更新 SKU
        List<ProductSkuDO> savedSkus = productSkuService.updateSkuList(updateObj.getId(), updateReqVO.getSkus());
        Set<Long> cleanupSkuIds = new LinkedHashSet<>(convertSet(oldSkus, ProductSkuDO::getId));
        cleanupSkuIds.addAll(convertSet(savedSkus, ProductSkuDO::getId));
        sceneHandler.afterSave(updateObj.getId(), updateReqVO, savedSkus, cleanupSkuIds);
    }

    /**
     * 基于 SKU 的信息，初始化 SPU 的信息
     * 主要是计数相关的字段，例如说市场价、最大最小价、库存等等
     *
     * @param spu  商品 SPU
     * @param skus 商品 SKU 数组
     */
    private void initSpuFromSkus(ProductSpuDO spu, List<ProductSkuSaveReqVO> skus) {
        List<ProductSkuSaveReqVO> enabledSkus = skus == null ? Collections.emptyList() : skus.stream()
                .filter(item -> ProductSkuStatusEnum.isEnable(
                        ObjectUtil.defaultIfNull(item.getStatus(), ProductSkuStatusEnum.ENABLE.getStatus())))
                .collect(Collectors.toList());
        List<ProductSkuSaveReqVO> aggregateSkus = CollUtil.isNotEmpty(enabledSkus) ? enabledSkus : Collections.emptyList();
        // sku 单价最低的商品的价格
        spu.setPrice(CollUtil.isEmpty(aggregateSkus) ? 0 : getMinValue(aggregateSkus, ProductSkuSaveReqVO::getPrice));
        // sku 单价最低的商品的市场价格
        spu.setMarketPrice(CollUtil.isEmpty(aggregateSkus) ? 0 : getMinValue(aggregateSkus, ProductSkuSaveReqVO::getMarketPrice));
        // sku 单价最低的商品的成本价格
        spu.setCostPrice(CollUtil.isEmpty(aggregateSkus) ? 0 : getMinValue(aggregateSkus, ProductSkuSaveReqVO::getCostPrice));
        // skus 库存总数
        spu.setStock(CollUtil.isEmpty(aggregateSkus) ? 0 : getSumValue(aggregateSkus, ProductSkuSaveReqVO::getStock, Math::addExact));
        // 若是 spu 已有状态则不处理
        if (spu.getStatus() == null) {
            spu.setStatus(ProductSpuStatusEnum.ENABLE.getStatus()); // 默认状态为上架
            spu.setSalesCount(0); // 默认商品销量
            spu.setBrowseCount(0); // 默认商品浏览量
        }
    }

    private void saveSpuCategoryRels(Long spuId, List<Long> categoryIds) {
        productSpuCategoryRelMapper.deleteBySpuId(spuId);
        if (CollUtil.isEmpty(categoryIds)) {
            return;
        }
        List<ProductSpuCategoryRelDO> rels = new ArrayList<>(categoryIds.size());
        for (int i = 0; i < categoryIds.size(); i++) {
            rels.add(new ProductSpuCategoryRelDO()
                    .setSpuId(spuId)
                    .setCategoryId(categoryIds.get(i))
                    .setSort(i));
        }
        productSpuCategoryRelMapper.insertBatch(rels);
    }

    private ProductSceneHandler getSceneHandler(String bizScene) {
        return productSceneHandlers.stream()
                .filter(handler -> Objects.equals(handler.getBizScene(), bizScene))
                .findFirst()
                .orElseThrow(() -> exception(CATEGORY_BIZ_SCENE_INVALID));
    }

    @Override
    public List<ProductSpuDO> validateSpuList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        // 获得商品信息
        List<ProductSpuDO> list = productSpuMapper.selectByIds(ids);
        Map<Long, ProductSpuDO> spuMap = CollectionUtils.convertMap(list, ProductSpuDO::getId);
        // 校验
        ids.forEach(id -> {
            ProductSpuDO spu = spuMap.get(id);
            if (spu == null) {
                throw exception(SPU_NOT_EXISTS);
            }
            if (!ProductSpuStatusEnum.isEnable(spu.getStatus())) {
                throw exception(SPU_NOT_ENABLE, spu.getName());
            }
        });
        return list;
    }

    @Override
    public void updateBrowseCount(Long id, int incrCount) {
        productSpuMapper.updateBrowseCount(id , incrCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpu(Long id) {
        // 校验存在
        ProductSpuDO spuDO = validateSpuExists(id);
        // 判断 SPU 状态是否为回收站
        if (ObjectUtil.notEqual(spuDO.getStatus(), ProductSpuStatusEnum.RECYCLE.getStatus())) {
            throw exception(SPU_NOT_RECYCLE);
        }
        validatePublicationProductNotReferenced(spuDO);
        // TODO 芋艿：【可选】参与活动中的商品，不允许删除？？？

        // 删除 SPU
        List<ProductSkuDO> skuList = productSkuService.getSkuListBySpuId(id);
        productPublicationService.clearPublication(id, convertSet(skuList, ProductSkuDO::getId));
        productSpuMapper.deleteById(id);
        productSpuCategoryRelMapper.deleteBySpuId(id);
        // 删除关联的 SKU
        productSkuService.deleteSkuBySpuId(id);
        productSpuProducer.sendProductSpuDeleteMessage(id);
    }

    private ProductSpuDO validateSpuExists(Long id) {
        ProductSpuDO spuDO = productSpuMapper.selectById(id);
        if (spuDO == null) {
            throw exception(SPU_NOT_EXISTS);
        }
        return spuDO;
    }

    @Override
    public ProductSpuDO getSpu(Long id) {
        return productSpuMapper.selectById(id);
    }

    @Override
    public ProductSpuDO getSpu(Long id, boolean includeDeleted) {
        if (includeDeleted) {
            return productSpuMapper.selectByIdIncludeDeleted(id);
        }
        return getSpu(id);
    }

    @Override
    public List<ProductSpuDO> getSpuList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        Map<Long, ProductSpuDO> spuMap = convertMap(productSpuMapper.selectByIds(ids), ProductSpuDO::getId);
        // 需要按照 ids 顺序返回。例如说：店铺装修选择了 [3, 1, 2] 三个商品，返回结果还是 [3, 1, 2]  这样的顺序
        return convertList(ids, spuMap::get);
    }

    @Override
    public List<ProductSpuDO> getSpuListByStatus(Integer status) {
        return productSpuMapper.selectList(ProductSpuDO::getStatus, status);
    }

    @Override
    public Map<Long, List<ProductCategoryDO>> getCategoryListMapBySpuIds(Collection<Long> spuIds) {
        if (CollUtil.isEmpty(spuIds)) {
            return Collections.emptyMap();
        }
        List<ProductSpuCategoryRelDO> rels = productSpuCategoryRelMapper.selectListBySpuIds(spuIds);
        if (CollUtil.isEmpty(rels)) {
            return Collections.emptyMap();
        }
        Map<Long, ProductCategoryDO> categoryMap = convertMap(
                categoryService.getCategoryList(convertSet(rels, ProductSpuCategoryRelDO::getCategoryId)),
                ProductCategoryDO::getId);
        Map<Long, List<ProductSpuCategoryRelDO>> relMap = convertMultiMap(rels, ProductSpuCategoryRelDO::getSpuId);
        Map<Long, List<ProductCategoryDO>> result = new LinkedHashMap<>(relMap.size());
        relMap.forEach((spuId, spuRels) -> result.put(spuId, convertList(spuRels, rel -> categoryMap.get(rel.getCategoryId()))));
        return result;
    }

    @Override
    public PageResult<ProductSpuDO> getSpuPage(ProductSpuPageReqVO pageReqVO) {
        return productSpuMapper.selectPage(pageReqVO,
                categoryService.getSelfAndDescendantCategoryIds(pageReqVO.getCategoryIds()));
    }

    @Override
    public PageResult<ProductSpuDO> getSpuPage(AppProductSpuPageReqVO pageReqVO) {
        // 分页查询
        return productSpuMapper.selectPage(pageReqVO,
                categoryService.getSelfAndDescendantCategoryIds(pageReqVO.getCategoryIds()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpuStock(Map<Long, Integer> stockIncrCounts) {
        stockIncrCounts.forEach((id, incCount) -> productSpuMapper.updateStock(id, incCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpuSalesCount(Map<Long, Integer> salesCountIncrCounts) {
        salesCountIncrCounts.forEach((id, incCount) -> productSpuMapper.updateSalesCount(id, incCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpuStatus(ProductSpuUpdateStatusReqVO updateReqVO) {
        // 校验存在
        ProductSpuDO spuDO = validateSpuExists(updateReqVO.getId());
        if (ObjectUtil.equal(updateReqVO.getStatus(), ProductSpuStatusEnum.RECYCLE.getStatus())) {
            validatePublicationProductNotReferenced(spuDO);
        }
        // TODO 芋艿：【可选】参与活动中的商品，不允许下架？？？

        // 更新状态
        ProductSpuDO productSpuDO = spuDO.setStatus(updateReqVO.getStatus());
        productSpuMapper.updateById(productSpuDO);
    }

    private void validatePublicationProductNotReferenced(ProductSpuDO spu) {
        if (!BizSceneEnum.isPublication(spu.getBizScene())) {
            return;
        }
        if (tradeSubscriptionOrderApi.hasPublicationOrderReferenceByProductSpuId(spu.getId())) {
            throw exception(PUBLICATION_PRODUCT_ORDER_REFERENCED);
        }
    }

    private void validatePublicationUpdateOrderReference(ProductSpuDO spu, ProductSpuSaveReqVO updateReqVO,
                                                         List<ProductSkuDO> oldSkus) {
        if (!BizSceneEnum.isPublication(spu.getBizScene())) {
            return;
        }
        Set<Long> oldSkuIds = CollUtil.isEmpty(oldSkus) ? Collections.emptySet()
                : new LinkedHashSet<>(convertSet(oldSkus, ProductSkuDO::getId));
        if (!BizSceneEnum.isPublication(updateReqVO.getBizScene())
                && (tradeSubscriptionOrderApi.hasPublicationOrderReferenceByProductSpuId(spu.getId())
                || hasPublicationSkuOrderReference(oldSkuIds))) {
            throw exception(PUBLICATION_PRODUCT_ORDER_REFERENCED);
        }
        if (CollUtil.isEmpty(oldSkuIds)) {
            return;
        }
        Set<Long> updateSkuIds = updateReqVO.getSkus() == null ? Collections.emptySet() : updateReqVO.getSkus().stream()
                .map(ProductSkuSaveReqVO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        oldSkuIds.removeAll(updateSkuIds);
        if (CollUtil.isEmpty(oldSkuIds)) {
            return;
        }
        Set<Long> referencedSkuIds = tradeSubscriptionOrderApi.getPublicationOrderReferencedProductSkuIds(oldSkuIds);
        if (CollUtil.isNotEmpty(referencedSkuIds)) {
            throw exception(PUBLICATION_SKU_ORDER_REFERENCED);
        }
    }

    private boolean hasPublicationSkuOrderReference(Set<Long> skuIds) {
        return CollUtil.isNotEmpty(skuIds)
                && CollUtil.isNotEmpty(tradeSubscriptionOrderApi.getPublicationOrderReferencedProductSkuIds(skuIds));
    }

    @Override
    public Map<Integer, Long> getTabsCount(String bizScene) {
        if (bizScene != null) {
            categoryService.validateBizScene(bizScene);
        }
        Map<Integer, Long> counts = Maps.newLinkedHashMapWithExpectedSize(5);
        // 查询销售中的商品数量
        counts.put(ProductSpuPageReqVO.FOR_SALE,
                productSpuMapper.selectCountByTab(ProductSpuPageReqVO.FOR_SALE, bizScene));
        // 查询仓库中的商品数量
        counts.put(ProductSpuPageReqVO.IN_WAREHOUSE,
                productSpuMapper.selectCountByTab(ProductSpuPageReqVO.IN_WAREHOUSE, bizScene));
        boolean publication = BizSceneEnum.isPublication(bizScene);
        // 查询售空的商品数量
        counts.put(ProductSpuPageReqVO.SOLD_OUT, publication ? 0L :
                productSpuMapper.selectCountByTab(ProductSpuPageReqVO.SOLD_OUT, bizScene));
        // 查询触发警戒库存的商品数量
        counts.put(ProductSpuPageReqVO.ALERT_STOCK, publication ? 0L :
                productSpuMapper.selectCountByTab(ProductSpuPageReqVO.ALERT_STOCK, bizScene));
        // 查询回收站中的商品数量
        counts.put(ProductSpuPageReqVO.RECYCLE_BIN,
                productSpuMapper.selectCountByTab(ProductSpuPageReqVO.RECYCLE_BIN, bizScene));
        return counts;
    }

    @Override
    public Long getSpuCountByCategoryId(Long categoryId) {
        return productSpuCategoryRelMapper.selectCountByCategoryId(categoryId);
    }

    @Override
    public Long getSpuCountByCategoryIds(Collection<Long> categoryIds) {
        return productSpuCategoryRelMapper.selectCountByCategoryIds(categoryIds);
    }

}
